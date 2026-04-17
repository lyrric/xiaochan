package io.github.xiaochan.tasks;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import io.github.xiaochan.constant.StorePlatformEnum;
import io.github.xiaochan.http.MessageHttp;
import io.github.xiaochan.model.MinimumPayExtNotifyConfig;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.model.dto.TaskExecHistoryEntity;
import io.github.xiaochan.model.entity.LocationEntity;
import io.github.xiaochan.model.entity.NotifyConfigEntity;
import io.github.xiaochan.model.entity.UserEntity;
import io.github.xiaochan.model.enums.NotifyConfigStatusEnums;
import io.github.xiaochan.model.enums.NotifyTypeEnums;
import io.github.xiaochan.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MinimumPayService {

    @Resource
    private XiaoChanService xiaoChanService;
    @Resource
    private NotifyConfigService notifyConfigService;
    @Resource
    private TaskExecHistoryService taskExecHistoryService;
    @Resource
    private LocationService locationService;
    @Resource
    private StorePushedHistoryService storePushedHistoryService;
    @Resource
    private UserService userService;


    private static final int DEFAULT_MAX_SIZE = 150;


    /**
     * 最小实付
     */
    @Scheduled(cron = "0 30 * * * ? ")
    public void start(){
        if (isSkip()) {
            log.info("当前时间段位于00:00-08:00，不进行定时任务");
            return;
        }
        try {
            //获取所有配置信息
            log.info("开始执行 最小实付活动 定时任务");
            List<NotifyConfigEntity> notifyConfigList = notifyConfigService.list(NotifyTypeEnums.MINIMUM_PAY, NotifyConfigStatusEnums.ENABLE);
            for (NotifyConfigEntity notifyConfig : notifyConfigList) {
                minimumActivityRemind(notifyConfig);
            }
        }catch (Exception e){
            log.error("发生异常 ", e);
        }
    }

    private boolean isSkip() {
        Date now = new Date();
        int hour = DateUtil.hour(now, true);
        return hour >= 0 && hour <= 8;
    }

    private void minimumActivityRemind(NotifyConfigEntity notifyConfig) {
        TaskExecHistoryEntity execHistory = new TaskExecHistoryEntity();
        execHistory.setUserId(notifyConfig.getUserId());
        execHistory.setNotifyConfigId(notifyConfig.getId());
        execHistory.setStartTime(LocalDateTime.now());
        execHistory.setNotifyType(NotifyTypeEnums.MINIMUM_PAY);
        execHistory.setSuccess(true);
        try {
            MinimumPayExtNotifyConfig extNotifyConfig = JSON.parseObject(notifyConfig.getExtConfig(), MinimumPayExtNotifyConfig.class);
            log.info("开始执行最小实付活动提醒 {} 最小实付 {}", notifyConfig.getId(), extNotifyConfig.getMinimumPay());
            //获取地址
            Optional<LocationEntity> optionalLocation = locationService.getOptById(notifyConfig.getLocationId());
            if (optionalLocation.isEmpty()) {
                log.error("位置信息不存在 {} {}", notifyConfig.getId(), notifyConfig.getLocationId());
                notifyConfigService.updateConfig(notifyConfig.getId(), NotifyConfigStatusEnums.DISABLE, "位置信息不存在");
                execHistory.setSuccess(false);
                execHistory.setRemark("执行失败：位置信息不存在");
                return;
            }
            LocationEntity location = optionalLocation.get();
            List<StoreInfo> storeInfos = xiaoChanService.getList(location.getCityCode(), location.getLongitude(), location.getLatitude(), DEFAULT_MAX_SIZE);
            List<StoreInfo> availableStores = storeInfos
                    .stream()
                    .filter(storeInfo -> storeInfo.getLeftNumber() > 0)
                    .filter(storeInfo -> storeInfo.getPrice().subtract(storeInfo.getRebatePrice()).compareTo(extNotifyConfig.getMinimumPay()) <= 0)
                    .filter(storeInfo -> storePushedHistoryService.findByNotifyIdAndStoreIdAll(notifyConfig.getId(), storeInfo.getStoreId()) == null)
                    .toList();
            if(availableStores.isEmpty()){
                log.info("configId: {} 没有满足条件的门店活动", notifyConfig.getId());
                execHistory.setRemark("没有满足条件的门店活动");
                execHistory.setNotifyStoreCount(0);
                return;
            }
            execHistory.setNotifyStoreCount(availableStores.size());
            log.info("configId: {} 找到{}个满足条件的门店活动", notifyConfig.getId(), availableStores.size());
            //通知
            sendMessage(availableStores, location);
        }catch (Exception e){
            log.error("最小实付活动提醒时发生异常 {}", notifyConfig.getId(), e);
            execHistory.setSuccess(false);
            execHistory.setRemark("执行失败:" + e.getMessage());
        }finally {
            execHistory.setEndTime(LocalDateTime.now());
            taskExecHistoryService.save(execHistory);
        }
    }


    private void sendMessage(List<StoreInfo> storeInfos, LocationEntity locationEntity) {
        String body = storeInfos.stream()
                .map(storeInfo -> buildMessage(storeInfo, locationEntity))
                .collect(Collectors.joining("<br/><br/>"));
        UserEntity userEntity = userService.getById(locationEntity.getUserId());
        try {
            log.info("发送消息:{}", body);
            MessageHttp.sendMessage(userEntity.getSpt(), body, "有新的返现活动啦");
        }catch (Exception e){
            log.error("发送消息失败", e);
        }
    }

    private String buildMessage(StoreInfo storeInfo, LocationEntity locationEntity) {
        return "地点：" + locationEntity.getName() + "<br/>" +
                "平台：" + StorePlatformEnum.getByType(storeInfo.getType()).name + "<br/>" +
                "店铺：" + storeInfo.getName() + "<br/>" +
                "时间范围：" + storeInfo.getStartTime() + "-" + storeInfo.getEndTime() + "<br/>" +
                "距离：" + storeInfo.getDistance() + "米" + "<br/>" +
                "库存：" + storeInfo.getLeftNumber() + "<br/>" +
                "规则：满" + storeInfo.getPrice() + "返" + storeInfo.getRebatePrice() + "<br/>" +
                "是否需要评价:" + (storeInfo.getRebateCondition() == null ? "未知" : (storeInfo.getRebateCondition() != 99 ? "是" : "否")) + "\r\n";
    }

}
