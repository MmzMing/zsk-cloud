package com.zsk.common.xxljob.service;

import com.zsk.common.xxljob.config.XxlJobProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * XXL-Job Admin API服务
 * 用于与XXL-Job控制台进行交互，实现执行器和任务的自动注册
 *
 * @author wuhuaming
 * @version 1.0
 * @date 2026-02-15
 */
@Slf4j
@RequiredArgsConstructor
public class XxlJobService {

    /**
     * 配置属性
     */
    private final XxlJobProperties properties;

    /**
     * HTTP客户端
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 登录后的Cookie
     */
    private String cookie;

    /**
     * 登录XXL-Job控制台
     *
     * @return 是否登录成功
     */
    public boolean login() {
        String url = properties.getAdmin().getAddresses() + "/login";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userName", properties.getAdmin().getUsername());
        params.add("password", properties.getAdmin().getPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
                if (cookies != null && !cookies.isEmpty()) {
                    this.cookie = String.join(";", cookies);
                    log.info("XXL-Job登录成功");
                    return true;
                }
            }
            log.error("XXL-Job登录失败，状态码: {}", response.getStatusCode());
            return false;
        } catch (Exception e) {
            log.error("XXL-Job登录异常", e);
            return false;
        }
    }

    /**
     * 获取执行器列表
     *
     * @return 执行器列表
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getExecutorList() {
        String url = properties.getAdmin().getAddresses() + "/jobgroup/list";
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return (List<Map<String, Object>>) body.get("data");
            }
        } catch (Exception e) {
            log.error("获取执行器列表失败", e);
        }
        return List.of();
    }

    /**
     * 根据AppName获取执行器ID
     *
     * @param appname 执行器AppName
     * @return 执行器ID，不存在返回null
     */
    public Integer getExecutorIdByAppname(String appname) {
        List<Map<String, Object>> executors = getExecutorList();
        for (Map<String, Object> executor : executors) {
            if (appname.equals(executor.get("appname"))) {
                return (Integer) executor.get("id");
            }
        }
        return null;
    }

    /**
     * 注册执行器
     *
     * @param appname 执行器AppName
     * @param title   执行器名称
     * @param address 执行器地址
     * @return 执行器ID
     */
    public Integer registerExecutor(String appname, String title, String address) {
        String url = properties.getAdmin().getAddresses() + "/jobgroup/save";
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("appname", appname);
        params.add("title", title != null ? title : appname);
        params.add("addressType", "0");
        params.add("addressList", address != null ? address : "");
        params.add("registryList", "");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.get("code") != null && (Integer) body.get("code") == 200) {
                    log.info("执行器注册成功: {}", appname);
                    return getExecutorIdByAppname(appname);
                }
            }
            log.error("执行器注册失败: {}", appname);
        } catch (Exception e) {
            log.error("执行器注册异常: {}", appname, e);
        }
        return null;
    }

    /**
     * 获取任务列表
     *
     * @param executorId 执行器ID
     * @return 任务列表
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getJobList(Integer executorId) {
        String url = properties.getAdmin().getAddresses() + "/jobinfo/list?jobGroup=" + executorId;
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return (List<Map<String, Object>>) body.get("data");
            }
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
        }
        return List.of();
    }

    /**
     * 根据执行器ID和任务名称获取任务ID
     *
     * @param executorId 执行器ID
     * @param jobHandler 任务Handler名称
     * @return 任务ID，不存在返回null
     */
    public Integer getJobIdByName(Integer executorId, String jobHandler) {
        List<Map<String, Object>> jobs = getJobList(executorId);
        for (Map<String, Object> job : jobs) {
            if (jobHandler.equals(job.get("executorHandler"))) {
                return (Integer) job.get("id");
            }
        }
        return null;
    }

    /**
     * 注册任务
     *
     * @param executorId 执行器ID
     * @param jobInfo    任务信息
     * @return 任务ID
     */
    public Integer registerJob(Integer executorId, Map<String, String> jobInfo) {
        String url = properties.getAdmin().getAddresses() + "/jobinfo/add";
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", String.valueOf(executorId));
        params.add("jobDesc", jobInfo.getOrDefault("jobDesc", ""));
        params.add("scheduleConf", jobInfo.getOrDefault("cron", ""));
        params.add("scheduleType", "CRON");
        params.add("glueType", "BEAN");
        params.add("executorHandler", jobInfo.getOrDefault("executorHandler", ""));
        params.add("executorParam", jobInfo.getOrDefault("executorParam", ""));
        params.add("executorRouteStrategy", jobInfo.getOrDefault("routeStrategy", "FIRST"));
        params.add("executorBlockStrategy", jobInfo.getOrDefault("blockStrategy", "SERIALIZATION_EXECUTION"));
        params.add("misfireStrategy", "DO_NOTHING");
        params.add("executorTimeout", jobInfo.getOrDefault("timeout", "0"));
        params.add("executorFailRetryCount", jobInfo.getOrDefault("failRetryCount", "0"));
        params.add("author", jobInfo.getOrDefault("author", "admin"));
        params.add("alarmEmail", jobInfo.getOrDefault("alarmEmail", ""));
        params.add("glueRemark", "");
        params.add("glueSource", "");
        params.add("childJobId", jobInfo.getOrDefault("childJobId", ""));
        params.add("executorShardingParam", jobInfo.getOrDefault("shardingParam", ""));
        params.add("status", jobInfo.getOrDefault("status", "0"));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.get("code") != null && (Integer) body.get("code") == 200) {
                    log.info("任务注册成功: {}", jobInfo.get("executorHandler"));
                    return getJobIdByName(executorId, jobInfo.get("executorHandler"));
                }
            }
            log.error("任务注册失败: {}", jobInfo.get("executorHandler"));
        } catch (Exception e) {
            log.error("任务注册异常: {}", jobInfo.get("executorHandler"), e);
        }
        return null;
    }

    /**
     * 更新任务
     *
     * @param jobId   任务ID
     * @param jobInfo 任务信息
     * @return 是否成功
     */
    public boolean updateJob(Integer jobId, Map<String, String> jobInfo) {
        String url = properties.getAdmin().getAddresses() + "/jobinfo/update";
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", String.valueOf(jobId));
        params.add("jobGroup", jobInfo.get("jobGroup"));
        params.add("jobDesc", jobInfo.getOrDefault("jobDesc", ""));
        params.add("scheduleConf", jobInfo.getOrDefault("cron", ""));
        params.add("scheduleType", "CRON");
        params.add("glueType", "BEAN");
        params.add("executorHandler", jobInfo.getOrDefault("executorHandler", ""));
        params.add("executorParam", jobInfo.getOrDefault("executorParam", ""));
        params.add("executorRouteStrategy", jobInfo.getOrDefault("routeStrategy", "FIRST"));
        params.add("executorBlockStrategy", jobInfo.getOrDefault("blockStrategy", "SERIALIZATION_EXECUTION"));
        params.add("misfireStrategy", "DO_NOTHING");
        params.add("executorTimeout", jobInfo.getOrDefault("timeout", "0"));
        params.add("executorFailRetryCount", jobInfo.getOrDefault("failRetryCount", "0"));
        params.add("author", jobInfo.getOrDefault("author", "admin"));
        params.add("alarmEmail", jobInfo.getOrDefault("alarmEmail", ""));
        params.add("glueRemark", "");
        params.add("glueSource", "");
        params.add("childJobId", jobInfo.getOrDefault("childJobId", ""));
        params.add("executorShardingParam", jobInfo.getOrDefault("shardingParam", ""));
        params.add("status", jobInfo.getOrDefault("status", "0"));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.get("code") != null && (Integer) body.get("code") == 200) {
                    log.info("任务更新成功: {}", jobInfo.get("executorHandler"));
                    return true;
                }
            }
            log.error("任务更新失败: {}", jobInfo.get("executorHandler"));
        } catch (Exception e) {
            log.error("任务更新异常: {}", jobInfo.get("executorHandler"), e);
        }
        return false;
    }

    /**
     * 创建请求头
     *
     * @return HTTP请求头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (cookie != null) {
            headers.add(HttpHeaders.COOKIE, cookie);
        }
        return headers;
    }
}
