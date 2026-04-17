package io.github.xiaochan.tasks;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import io.github.xiaochan.constant.StorePlatformEnum;
import io.github.xiaochan.http.MessageHttp;
import io.github.xiaochan.model.StoreExtNotifyConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wangxiaodong
 * @date 2026/4/17
 */
@Component
@Slf4j
public class SpecificStoreTask {


    @Resource
    private XiaoChanService xiaoChanService;
    @Resource
    private NotifyConfigService notifyConfigService;
    @Resource
    private LocationService locationService;
    @Resource
    private TaskExecHistoryService taskExecHistoryService;
    @Resource
    private UserService userService;


    /**
     * 指定门店活动定时任务
     */
    @Scheduled(cron = "0 30 * * * ? ")
    public void start(){
        if (isSkip()) {
            return;
        }
        try {
            //获取所有配置信息
            List<NotifyConfigEntity> notifyConfigList = notifyConfigService.list(NotifyTypeEnums.STORE_ACTIVITY, NotifyConfigStatusEnums.ENABLE);
            log.info("开始执行 指定门店活动定时任务 获取到{}个配置信息", notifyConfigList.size());
            for (NotifyConfigEntity notifyConfig : notifyConfigList) {
                specifyStoreActivityRemind(notifyConfig);
            }
        }catch (Exception e){
            log.error("执行指定门店活动定时任务时发生异常", e);
        }
    }


    private boolean isSkip() {
        Date now = new Date();
        int hour = DateUtil.hour(now, true);
        return hour >= 0 && hour <= 8;
    }

    /**
     * 指定门店活动提醒
     */
    private void specifyStoreActivityRemind(NotifyConfigEntity notifyConfig){
        TaskExecHistoryEntity execHistory = new TaskExecHistoryEntity();
        execHistory.setUserId(notifyConfig.getUserId());
        execHistory.setNotifyConfigId(notifyConfig.getId());
        execHistory.setStartTime(LocalDateTime.now());
        execHistory.setNotifyType(NotifyTypeEnums.STORE_ACTIVITY);
        execHistory.setSuccess(true);
        try {
            log.info("开始执行指定门店活动提醒 {}", notifyConfig.getId());
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
            StoreExtNotifyConfig storeExtNotifyConfig = JSON.parseObject(notifyConfig.getExtConfig(), StoreExtNotifyConfig.class);
            //通过搜索来获取门店活动信息
            List<StoreInfo> storeInfos = xiaoChanService.searchList(storeExtNotifyConfig.getStoreInfo().getName(),
                    location.getCityCode(), location.getLongitude(),
                    location.getLatitude());
            List<StoreInfo> availableStores = storeInfos
                    .stream()
                    //同一个门店
                    .filter(storeInfo -> storeExtNotifyConfig.getStoreInfo().getStoreId().equals(storeInfo.getStoreId()))
                    .filter(storeInfo -> storeInfo.getLeftNumber() > 0)
                    //返现金额必须大于等于之前的返现金额
                    .filter(storeInfo -> storeInfo.getRebatePrice().compareTo(storeExtNotifyConfig.getStoreInfo().getRebatePrice()) >= 0)
                    //价格必须小于等于之前的价格
                    .filter(storeInfo -> storeInfo.getPrice().compareTo(storeExtNotifyConfig.getStoreInfo().getPrice()) <= 0)
                    .toList();
            execHistory.setNotifyStoreCount(availableStores.size());
            if(availableStores.isEmpty()){
                log.info("configId: {} 没有满足条件的门店活动", notifyConfig.getId());
                return;
            }
            log.info("configId: {} 找到{}个满足条件的门店活动", notifyConfig.getId(), availableStores.size());
            notifyConfigService.updateConfig(notifyConfig.getId(), NotifyConfigStatusEnums.DISABLE, "任务完成，完成时间" + DateUtil.format(new Date(), "MM-dd HH:mm:ss"));
            //通知
            sendMessage(availableStores, location);
        }catch (Exception e){
            log.error("执行指定门店活动提醒时发生异常 {}", notifyConfig.getId(), e);
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
