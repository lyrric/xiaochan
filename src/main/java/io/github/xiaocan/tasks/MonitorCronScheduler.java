package io.github.xiaocan.tasks;

import io.github.xiaocan.model.entity.MonitorConfigEntity;
import io.github.xiaocan.model.enums.MonitorConfigStatusEnums;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import io.github.xiaocan.service.MonitoryConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 监控配置 cron 动态调度器
 */
@Slf4j
@Component
public class MonitorCronScheduler {

    @Resource
    private TaskScheduler taskScheduler;
    @Resource
    private MonitoryConfigService monitoryConfigService;
    @Resource
    private StoreTask storeTask;
    @Resource
    private MinimumPayService minimumPayService;

    private final Map<Integer, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        refreshAll();
    }

    /**
     * 全量刷新：启动时加载所有启用且配置了 cron 的配置
     */
    public void refreshAll() {
        log.info("开始初始化 cron 动态调度任务");
        monitoryConfigService.listAllWithCron(MonitorConfigStatusEnums.ENABLE)
                .forEach(this::schedule);
        log.info("cron 动态调度任务初始化完成，共 {} 个", scheduledFutureMap.size());
    }

    /**
     * 刷新单个配置的调度任务
     */
    public void refresh(Integer configId) {
        if (configId == null) {
            return;
        }
        cancel(configId);
        MonitorConfigEntity config = monitoryConfigService.getById(configId);
        if (config != null) {
            schedule(config);
        }
    }

    /**
     * 根据配置注册 cron 任务
     */
    public void schedule(MonitorConfigEntity config) {
        if (config == null || config.getId() == null) {
            return;
        }
        String cron = config.getCron();
        if (!StringUtils.hasText(cron)) {
            return;
        }
        if (config.getStatus() != MonitorConfigStatusEnums.ENABLE) {
            return;
        }
        // 避免重复调度
        cancel(config.getId());
        try {
            CronTrigger trigger = new CronTrigger(cron.trim());
            ScheduledFuture<?> future = taskScheduler.schedule(() -> execute(config), trigger);
            if (future != null) {
                scheduledFutureMap.put(config.getId(), future);
                log.info("已注册 cron 调度任务 configId: {}, type: {}, cron: {}", config.getId(), config.getType(), cron);
            }
        } catch (Exception e) {
            log.error("注册 cron 调度任务失败 configId: {}, cron: {}", config.getId(), cron, e);
        }
    }

    /**
     * 取消单个配置的调度任务
     */
    public void cancel(Integer configId) {
        ScheduledFuture<?> future = scheduledFutureMap.remove(configId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
            log.info("已取消 cron 调度任务 configId: {}", configId);
        }
    }

    /**
     * 执行单个配置
     */
    private void execute(MonitorConfigEntity config) {
        // 重新查询最新配置，确保状态/cron 是最新的
        MonitorConfigEntity latest = monitoryConfigService.getById(config.getId());
        if (latest == null
                || latest.getStatus() != MonitorConfigStatusEnums.ENABLE
                || !StringUtils.hasText(latest.getCron())) {
            log.info("configId: {} 当前状态不允许执行，取消调度", config.getId());
            cancel(config.getId());
            return;
        }
        try {
            if (latest.getType() == MonitorTypeEnums.MINIMUM_PAY) {
                minimumPayService.execute(latest, true);
            } else {
                storeTask.execute(latest, true);
            }
        } catch (Exception e) {
            log.error("cron 动态调度任务执行异常 configId: {}", latest.getId(), e);
        }
    }
}
