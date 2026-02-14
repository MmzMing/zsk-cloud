package com.zsk.common.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.UUID;

/**
 * 追踪上下文 - 链路追踪
 *
 * @author wuhuaming
 */
public class TraceContext {

    private static final TransmittableThreadLocal<String> TRACE_ID = new TransmittableThreadLocal<>();

    /**
     * 获取当前追踪ID
     */
    public static String getTraceId() {
        String traceId = TRACE_ID.get();
        if (traceId == null) {
            traceId = generateTraceId();
            setTraceId(traceId);
        }
        return traceId;
    }

    /**
     * 设置当前追踪ID
     */
    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    /**
     * 生成新的追踪ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 清除追踪ID
     */
    public static void clear() {
        TRACE_ID.remove();
    }
}
