package io.github.xiaocan.tasks;

import io.github.xiaocan.constant.StorePlatformEnum;
import io.github.xiaocan.http.MessageHttp;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.entity.TaskExecHistoryEntity;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.entity.MonitorConfigEntity;
import io.github.xiaocan.model.entity.StorePushedHistoryEntity;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.enums.MonitorConfigStatusEnums;
import io.github.xiaocan.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

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
    private UserService userService;


    void runSingle(MonitorConfigEntity notifyConfig) {
        TaskExecHistoryEntity execHistory = new TaskExecHistoryEntity();
        execHistory.setUserId(notifyConfig.getUserId());
        execHistory.setNotifyType(notifyConfig.getType());
        execHistory.setNotifyConfigId(notifyConfig.getId());
        execHistory.setStartTime(LocalDateTime.now());
        execHistory.setSuccess(true);
        log.info("开始执行type is {}, config id is {}", notifyConfig.getType().getDescription(), notifyConfig.getId());
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
            savePushedHistory(notifyConfig, availableStores);
            afterSuccess(notifyConfig, availableStores);
            //通知
            sendMessage(notifyConfig, availableStores, location);
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

    private void savePushedHistory(MonitorConfigEntity notifyConfig, List<StoreInfo> storeInfos){
        List<StorePushedHistoryEntity> entities = storeInfos.stream().map(storeInfo -> {
            StorePushedHistoryEntity entity = new StorePushedHistoryEntity();
            BeanUtils.copyProperties(storeInfo, entity);
            entity.setId(null);
            entity.setUserId(notifyConfig.getUserId());
            entity.setNotifyConfigId(notifyConfig.getId());
            entity.setNotifyType(notifyConfig.getType());
            return entity;
        }).toList();
        storePushedHistoryService.saveBatch(entities);
    }


    /**
     * 默认 body 模板
     */
    private static final String DEFAULT_BODY_TEMPLATE =
            "地点：${地点}<br/>" +
            "平台：${平台}<br/>" +
            "店铺：${店铺}<br/>" +
            "时间范围：${开始时间}-${结束时间}<br/>" +
            "距离：${距离}米<br/>" +
            "库存：${库存}<br/>" +
            "规则：满${满}返${返}<br/>" +
            "是否需要评价：${评价条件}";

    /**
     * 不同类型监控的默认 summary 模板
     */
    private static final String DEFAULT_SUMMARY_STORE_ACTIVITY = "${地点}: 指定门店有${数量}个新返现活动";
    private static final String DEFAULT_SUMMARY_STORE_KEYWORD = "${地点}: 关键字匹配到${数量}个新返现活动";
    private static final String DEFAULT_SUMMARY_MINIMUM_PAY = "${地点}: 最小实付匹配到${数量}个新返现活动";


    public void sendMessage(MonitorConfigEntity notifyConfig, List<StoreInfo> storeInfos, LocationEntity locationEntity) {
        String body = storeInfos.stream()
                .map(storeInfo -> buildMessage(storeInfo, locationEntity))
                .collect(Collectors.joining("<br/><br/>"));
        UserEntity userEntity = userService.getById(locationEntity.getUserId());
        try {
            log.info("发送消息:{}", body);
            String summary = buildSummary(notifyConfig, storeInfos, locationEntity);
            MessageHttp.sendMessage(userEntity.getSpt(), body, summary);
        }catch (Exception e){
            log.error("发送消息失败", e);
        }
    }

    private String buildSummary(MonitorConfigEntity notifyConfig, List<StoreInfo> storeInfos, LocationEntity locationEntity) {
        String summaryTemplate = switch (notifyConfig.getType()) {
            case STORE_ACTIVITY -> DEFAULT_SUMMARY_STORE_ACTIVITY;
            case STORE_KEYWORD -> DEFAULT_SUMMARY_STORE_KEYWORD;
            case MINIMUM_PAY -> DEFAULT_SUMMARY_MINIMUM_PAY;
        };
        return summaryTemplate
                .replace("${地点}", locationEntity.getName())
                .replace("${数量}", String.valueOf(storeInfos.size()))
                .replace("${类型}", notifyConfig.getType().getDescription());
    }

    private String buildMessage(StoreInfo storeInfo, LocationEntity locationEntity) {
        String rebateConditionText = storeInfo.getRebateCondition() == null ? "未知"
                : (storeInfo.getRebateCondition() != 99 ? "是" : "否");
        return BaseTask.DEFAULT_BODY_TEMPLATE
                .replace("${地点}", locationEntity.getName())
                .replace("${平台}", StorePlatformEnum.getByType(storeInfo.getType()).name)
                .replace("${店铺}", storeInfo.getName())
                .replace("${开始时间}", storeInfo.getStartTime())
                .replace("${结束时间}", storeInfo.getEndTime())
                .replace("${距离}", String.valueOf(storeInfo.getDistance()))
                .replace("${库存}", String.valueOf(storeInfo.getLeftNumber()))
                .replace("${满}", storeInfo.getPrice().toPlainString())
                .replace("${返}", storeInfo.getRebatePrice().toPlainString())
                .replace("${评价条件}", rebateConditionText);
    }
}
