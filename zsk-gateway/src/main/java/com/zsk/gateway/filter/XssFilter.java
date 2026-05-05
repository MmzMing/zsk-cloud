package com.zsk.gateway.filter;

import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.core.utils.XssUtil;
import com.zsk.gateway.config.properties.XssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * XSS 跨站脚本攻击防护过滤器
 *
 * @author wuhuaming
 * @version 2.0
 * @date 2026-02-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XssFilter implements GlobalFilter, Ordered {

    private final XssProperties xssProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 执行过滤逻辑
     *
     * @param exchange 服务网络交换器
     * @param chain    过滤器链
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!xssProperties.getEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();

        if (matches(url, xssProperties.getExcludeUrls())) {
            return chain.filter(exchange);
        }

        ServerHttpRequest cleanedRequest = cleanQueryParams(request);
        boolean queryChanged = cleanedRequest != request;

        if (Boolean.TRUE.equals(xssProperties.getBodyEnabled()) && isBodyContentType(cleanedRequest)) {
            return cleanRequestBody(exchange, chain, cleanedRequest);
        }

        if (queryChanged) {
            return chain.filter(exchange.mutate().request(cleanedRequest).build());
        }

        return chain.filter(exchange);
    }

    /**
     * 清洗 URL 查询参数，返回清洗后的请求（若未变化则返回原始请求）
     */
    private ServerHttpRequest cleanQueryParams(ServerHttpRequest request) {
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        if (queryParams.isEmpty()) {
            return request;
        }

        try {
            MultiValueMap<String, String> cleanedParams = new org.springframework.util.LinkedMultiValueMap<>();
            boolean hasChange = false;

            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                String key = entry.getKey();
                String cleanKey = XssUtil.clean(key);
                if (!key.equals(cleanKey)) {
                    hasChange = true;
                }

                List<String> values = entry.getValue();
                List<String> cleanValues = new ArrayList<>();
                if (values != null) {
                    for (String value : values) {
                        String cleanValue = XssUtil.clean(value);
                        if (!value.equals(cleanValue)) {
                            hasChange = true;
                        }
                        cleanValues.add(cleanValue);
                    }
                }
                cleanedParams.put(cleanKey, cleanValues);
            }

            if (!hasChange) {
                return request;
            }

            URI newUri = UriComponentsBuilder.fromUri(request.getURI())
                    .replaceQueryParams(cleanedParams)
                    .build(true)
                    .toUri();

            return request.mutate().uri(newUri).build();

        } catch (Exception e) {
            log.error("XSS query filter error: {}", e.getMessage());
            return request;
        }
    }

    /**
     * 清洗请求体
     * <p>
     * 注意：必须在 filter 层先消费并处理 body，再构造 decorator 并覆盖 getHeaders()，
     * 否则 ServerHttpRequestDecorator.getHeaders() 返回原始只读 Headers，
     * Content-Length 无法修改，导致下游读取时字节数不匹配而抛出 I/O error。
     * </p>
     */
    private Mono<Void> cleanRequestBody(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest cleanedRequest) {
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        long maxBytes = xssProperties.getBodyMaxBytes() != null ? xssProperties.getBodyMaxBytes() : 1024 * 1024L;

        return DataBufferUtils.join(cleanedRequest.getBody())
                .defaultIfEmpty(bufferFactory.allocateBuffer(0))
                .flatMap(dataBuffer -> {
                    try {
                        if (dataBuffer.readableByteCount() > maxBytes) {
                            log.warn("XSS filter: request body exceeds max size {} bytes, skipping", maxBytes);
                            return chain.filter(exchange.mutate().request(buildDecorator(cleanedRequest, dataBuffer, dataBuffer.readableByteCount())).build());
                        }

                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        String originalBody = new String(bytes, StandardCharsets.UTF_8);
                        if (StringUtils.isEmpty(originalBody)) {
                            return chain.filter(exchange.mutate().request(buildDecorator(cleanedRequest, bufferFactory.allocateBuffer(0), 0)).build());
                        }

                        String cleanedBody = XssUtil.cleanJson(originalBody);
                        byte[] cleanedBytes = cleanedBody.getBytes(StandardCharsets.UTF_8);
                        DataBuffer newBuffer = bufferFactory.wrap(cleanedBytes);

                        return chain.filter(exchange.mutate().request(buildDecorator(cleanedRequest, newBuffer, cleanedBytes.length)).build());
                    } catch (Exception e) {
                        log.error("XSS body filter error: {}", e.getMessage());
                        DataBufferUtils.release(dataBuffer);
                        return chain.filter(exchange);
                    }
                });
    }

    /**
     * 构造带正确 Content-Length 的请求装饰器
     * <p>
     * 覆盖 getHeaders() 返回可变副本以确保 Content-Length 与实际 body 一致。
     * </p>
     */
    private ServerHttpRequestDecorator buildDecorator(ServerHttpRequest request, DataBuffer body, long contentLength) {
        HttpHeaders mutableHeaders = new HttpHeaders();
        mutableHeaders.putAll(request.getHeaders());
        mutableHeaders.setContentLength(contentLength);

        return new ServerHttpRequestDecorator(request) {
            @Override
            public HttpHeaders getHeaders() {
                return mutableHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.just(body);
            }
        };
    }

    /**
     * 判断请求 Content-Type 是否需要 Body 过滤
     */
    private boolean isBodyContentType(ServerHttpRequest request) {
        MediaType contentType = request.getHeaders().getContentType();
        if (contentType == null) {
            return false;
        }

        String mimeType = contentType.getType() + "/" + contentType.getSubtype();
        List<String> bodyContentTypes = xssProperties.getBodyContentTypes();
        if (bodyContentTypes == null || bodyContentTypes.isEmpty()) {
            return false;
        }

        for (String pattern : bodyContentTypes) {
            if (mimeType.equalsIgnoreCase(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找字符串是否匹配白名单
     */
    private boolean matches(String url, List<String> patternList) {
        if (StringUtils.isEmpty(url) || patternList == null || patternList.isEmpty()) {
            return false;
        }
        for (String pattern : patternList) {
            if (antPathMatcher.match(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
