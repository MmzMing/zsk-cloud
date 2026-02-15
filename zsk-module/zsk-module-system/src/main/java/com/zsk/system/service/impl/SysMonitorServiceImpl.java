package com.zsk.system.service.impl;

import cn.hutool.core.net.NetUtil;
import com.zsk.system.domain.SysMonitorData;
import com.zsk.system.service.ISysMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统监控 服务层实现
 *
 * @author wuhuaming
 * @date 2026-02-15
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMonitorServiceImpl implements ISysMonitorService {

    private final MongoTemplate mongoTemplate;

    /** 系统信息实例 */
    private final SystemInfo systemInfo = new SystemInfo();

    /** 上一次CPU时间戳 */
    private long[] prevTicks;

    /**
     * 获取服务器实时监控数据
     *
     * @return 监控数据
     */
    @Override
    public SysMonitorData getRealTimeData() {
        return collectMonitorData();
    }

    /**
     * 获取监控概览数据
     *
     * @return 概览数据
     */
    @Override
    public SysMonitorData getOverview() {
        SysMonitorData data = collectMonitorData();
        data.setId(null);
        return data;
    }

    /**
     * 获取监控趋势数据
     *
     * @param metric 指标类型
     * @param range 时间范围
     * @return 趋势数据列表
     */
    @Override
    public List<SysMonitorData> getTrendData(String metric, String range) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = calculateStartTime(range);

        Query query = new Query();
        query.addCriteria(Criteria.where("collectTime").gte(startTime).lte(endTime));
        query.with(Sort.by(Sort.Direction.ASC, "collectTime"));

        return mongoTemplate.find(query, SysMonitorData.class);
    }

    /**
     * 采集并保存监控数据
     */
    @Override
    public void collectAndSave() {
        try {
            SysMonitorData data = collectMonitorData();
            mongoTemplate.save(data);
            log.debug("监控数据采集成功: {}", data.getCollectTime());
        } catch (Exception e) {
            log.error("监控数据采集失败: {}", e.getMessage());
        }
    }

    /**
     * 定时采集监控数据（每分钟执行）
     */
    @Scheduled(fixedRate = 60000)
    public void scheduledCollect() {
        collectAndSave();
    }

    /**
     * 清理过期监控数据
     *
     * @param days 保留天数
     */
    @Override
    public void cleanExpiredData(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        Query query = new Query(Criteria.where("collectTime").lt(expireTime));
        mongoTemplate.remove(query, SysMonitorData.class);
        log.info("已清理{}天前的监控数据", days);
    }

    /**
     * 采集监控数据
     *
     * @return 监控数据
     */
    private SysMonitorData collectMonitorData() {
        SysMonitorData data = new SysMonitorData();
        data.setCollectTime(LocalDateTime.now());

        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();

        /** CPU使用率 */
        CentralProcessor processor = hardware.getProcessor();
        double cpuUsage = processor.getSystemLoadAverage(1)[0];
        if (cpuUsage < 0) {
            cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        }
        prevTicks = processor.getSystemCpuLoadTicks();
        data.setCpuUsage(Math.max(0, Math.min(100, cpuUsage)));

        /** 内存使用率 */
        GlobalMemory memory = hardware.getMemory();
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        double memUsage = (double) (totalMemory - availableMemory) / totalMemory * 100;
        data.setMemUsage(memUsage);

        /** 磁盘使用率 */
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        long totalDisk = 0;
        long usedDisk = 0;
        for (OSFileStore store : fileStores) {
            totalDisk += store.getTotalSpace();
            usedDisk += store.getTotalSpace() - store.getUsableSpace();
        }
        double diskUsage = totalDisk > 0 ? (double) usedDisk / totalDisk * 100 : 0;
        data.setDiskUsage(diskUsage);

        /** 网络使用率（简化处理，设为CPU和内存的平均值） */
        data.setNetUsage((cpuUsage + memUsage) / 2);

        /** JVM内存使用率 */
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        double jvmHeapUsage = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        data.setJvmHeapUsage(jvmHeapUsage);

        double jvmNonHeapUsage = nonHeapUsage.getMax() > 0
                ? (double) nonHeapUsage.getUsed() / nonHeapUsage.getMax() * 100
                : 0;
        data.setJvmNonHeapUsage(jvmNonHeapUsage);

        /** JVM线程数 */
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        data.setJvmThreadCount(threadMXBean.getThreadCount());

        /** 服务器信息 */
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            data.setHostName(localHost.getHostName());
            data.setHostIp(localHost.getHostAddress());
        } catch (Exception e) {
            data.setHostName("unknown");
            data.setHostIp("unknown");
        }

        data.setOsName(os.toString());
        data.setOsVersion(os.getVersionInfo().toString());

        return data;
    }

    /**
     * 根据时间范围计算开始时间
     *
     * @param range 时间范围
     * @return 开始时间
     */
    private LocalDateTime calculateStartTime(String range) {
        return switch (range) {
            case "1h" -> LocalDateTime.now().minus(1, ChronoUnit.HOURS);
            case "24h" -> LocalDateTime.now().minus(24, ChronoUnit.HOURS);
            case "7d" -> LocalDateTime.now().minus(7, ChronoUnit.DAYS);
            default -> LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        };
    }
}
