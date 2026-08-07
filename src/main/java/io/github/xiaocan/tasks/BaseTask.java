package io.github.xiaocan.tasks;

import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.entity.TaskExecHistoryEntity;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.entity.MonitorConfigEntity;
import io.github.xiaocan.model.entity.StorePushedHistoryEntity;
import io.github.xiaocan.model.enums.MonitorConfigStatusEnums;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import io.github.xiaocan.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wangxiaodong
 * @date 2026/4/17
 */
@Slf4j
@Component
public class BaseTask {

    @Resource
    private MonitoryConfigService monitoryConfigService;
    @Resource
    private TaskExecHistoryService taskExecHistoryService;
    @Resource
    private LocationService locationService;
    @Resource
    private StorePushedHistoryService storePushedHistoryService;
    @Resource
    protected MessageService messageService;


    void runSingle(MonitorConfigEntity notifyConfig) {
        runSingle(notifyConfig, false);
    }

    void runSingle(MonitorConfigEntity notifyConfig, boolean ignoreTimeWindow) {
        TaskExecHistoryEntity execHistory = new TaskExecHistoryEntity();
        execHistory.setUserId(notifyConfig.getUserId());
        execHistory.setNotifyType(notifyConfig.getType());
        execHistory.setNotifyConfigId(notifyConfig.getId());
        execHistory.setStartTime(LocalDateTime.now());
        execHistory.setSuccess(true);
        log.info("开始执行type is {}, config id is {}", notifyConfig.getType().getDescription(), notifyConfig.getId());

        if (!ignoreTimeWindow && !StringUtils.hasText(notifyConfig.getCron())) {
            if (notifyConfig.getStartHour() == null || notifyConfig.getEndHour() == null || !StringUtils.hasText(notifyConfig.getWeeks())) {
                log.info("configId: {} 未配置 cron 且时间字段不完整，跳过执行", notifyConfig.getId());
                return;
            }
            int currentHour = LocalDateTime.now().getHour();
            if (currentHour < notifyConfig.getStartHour() || currentHour >= notifyConfig.getEndHour()) {
                log.info("当前时间{}不在运行时间范围{}-{}内，跳过执行 config id is {}", currentHour, notifyConfig.getStartHour(), notifyConfig.getEndHour(), notifyConfig.getId());
                return;
            }
            // 判断星期
            int currentDayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
            Set<Integer> weekSet = Arrays.stream(notifyConfig.getWeeks().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
            if (!weekSet.contains(currentDayOfWeek)) {
                log.info("当前星期{}不在运行星期{}内，跳过执行 config id is {}", currentDayOfWeek, notifyConfig.getWeeks(), notifyConfig.getId());
                return;
            }
        }

        try {
            //获取地址
            Optional<LocationEntity> optionalLocation = locationService.getOptById(notifyConfig.getLocationId());
            if (optionalLocation.isEmpty()) {
                log.error("位置信息不存在 {} {}", notifyConfig.getId(), notifyConfig.getLocationId());
                monitoryConfigService.updateConfig(notifyConfig.getId(), MonitorConfigStatusEnums.DISABLE, "位置信息不存在");
                execHistory.setSuccess(false);
                execHistory.setRemark("执行失败：位置信息不存在");
                return;
            }
            LocationEntity location = optionalLocation.get();
            List<StoreInfo> storeInfos = fetchStoreInfos(notifyConfig, execHistory, location);
            List<StoreInfo> availableStores = filterStoreInfos(notifyConfig, storeInfos);
            if(availableStores.isEmpty()){
                log.info("configId: {} 没有满足条件的门店活动", notifyConfig.getId());
                execHistory.setRemark("没有满足条件的门店活动");
                execHistory.setNotifyStoreCount(0);
                return;
            }
            execHistory.setNotifyStoreCount(availableStores.size());
            log.info("configId: {} 找到{}个满足条件的门店活动", notifyConfig.getId(), availableStores.size());
            handleAvailableStores(notifyConfig, availableStores, location);
        }catch (Exception e){
            log.error("执行异常 type {} config id is {}", notifyConfig.getType(), notifyConfig.getId(), e);
            execHistory.setSuccess(false);
            execHistory.setRemark("执行异常："+e.getMessage());
        }finally {
            execHistory.setEndTime(LocalDateTime.now());
            taskExecHistoryService.save(execHistory);
        }

    }

    protected List<StoreInfo> fetchStoreInfos(MonitorConfigEntity notifyConfig,
                                              TaskExecHistoryEntity execHistory,
                                              LocationEntity locationEntity){
        throw new UnsupportedOperationException("不支持的调用");
    }

    protected List<StoreInfo> filterStoreInfos(MonitorConfigEntity notifyConfig,
                                               List<StoreInfo> storeInfos){
        throw new UnsupportedOperationException("不支持的调用");
    }

    /**
     * 执行成功后的操作
     * @param notifyConfig
     */
    protected void afterSuccess(MonitorConfigEntity notifyConfig, List<StoreInfo> availableStores){
        //默认为空
    }

    protected String savePushedHistory(MonitorConfigEntity notifyConfig, List<StoreInfo> storeInfos){
        List<StorePushedHistoryEntity> entities = storeInfos.stream().map(storeInfo -> {
            StorePushedHistoryEntity entity = new StorePushedHistoryEntity();
            BeanUtils.copyProperties(storeInfo, entity);
            entity.setId(null);
            entity.setUserId(notifyConfig.getUserId());
            entity.setNotifyConfigId(notifyConfig.getId());
            entity.setLocationId(notifyConfig.getLocationId());
            entity.setNotifyType(notifyConfig.getType());
            return entity;
        }).toList();
        return storePushedHistoryService.saveBatchAndReturnBatchId(entities);
    }

    /**
     * 找到满足条件的门店后，保存历史、后续处理及发送通知
     */
    protected void handleAvailableStores(MonitorConfigEntity notifyConfig, List<StoreInfo> availableStores,
                                         LocationEntity location) {
        String batchId = savePushedHistory(notifyConfig, availableStores);
        afterSuccess(notifyConfig, availableStores);
        messageService.queueMessage(notifyConfig, availableStores, location, batchId);
    }
}
