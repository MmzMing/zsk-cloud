package com.zsk.gateway.filter;

import com.zsk.common.core.utils.StringUtils;
import com.zsk.common.core.utils.XssUtil;
import com.zsk.gateway.config.properties.XssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * XSS 跨站脚本攻击防护过滤器
 *
 * @author zsk
 * @date 2024-02-13
 * @version 1.0
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
     * @param chain 过滤器链
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!xssProperties.getEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().getPath();

        // 排除路径
        if (matches(url, xssProperties.getExcludeUrls())) {
            return chain.filter(exchange);
        }

        // 获取原始查询参数
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        if (queryParams.isEmpty()) {
            return chain.filter(exchange);
        }

        try {
            // 清洗查询参数
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
                return chain.filter(exchange);
            }

            // 重新构建 URI
            URI newUri = UriComponentsBuilder.fromUri(request.getURI())
                    .replaceQueryParams(cleanedParams)
                    .build(true)
                    .toUri();

            ServerHttpRequest newRequest = request.mutate().uri(newUri).build();
            return chain.filter(exchange.mutate().request(newRequest).build());

        } catch (Exception e) {
            log.error("XSS过滤异常: {}", e.getMessage());
            return chain.filter(exchange);
        }
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
