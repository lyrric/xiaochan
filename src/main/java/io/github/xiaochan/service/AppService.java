package io.github.xiaochan.service;

import cn.hutool.core.date.DateUtil;
import io.github.xiaochan.constant.StorePlatformEnum;
import io.github.xiaochan.http.MessageHttp;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.NotifyConfig;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.rules.MaxDiffPriceRule;
import io.github.xiaochan.rules.SpecifyStoreRule;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppService {

    @Resource
    private XiaoChanService xiaoChanService;
    @Resource
    private NotifyService notifyService;
    @Resource
    private SpecifyStoreRule specifyStoreRule;
    @Resource
    private MaxDiffPriceRule maxDiffPriceRule;


    private static final int DEFAULT_MAX_SIZE = 150;


    /**
     * 指定门店活动定时任务
     */
    @Scheduled(cron = "0 10 * * * ? ")
    public void specifyStoreScheduled(){
        if (isSkip()) {
            log.info("当前时间段位于00:00-08:00，不进行定时任务");
            return;
        }
        try {
            //获取所有配置信息
            List<NotifyConfig> notifyConfigList = notifyService.getNotifyConfigList();
            for (NotifyConfig notifyConfig : notifyConfigList) {
                if (!specifyStoreRule.notifyAvailable(notifyConfig)) {
                    continue;
                }
                specifyStoreActivityRemind(notifyConfig);
            }
        }catch (Exception e){
            log.error("发生异常", e);
        }
    }

    /**
     * 自定义规则提醒
     */
    @Scheduled(cron = "0 30 * * * ? ")
    public void customerScheduled(){
        if (isSkip()) {
            log.info("当前时间段位于00:00-08:00，不进行定时任务");
            return;
        }
        try {
            //获取所有配置信息
            List<NotifyConfig> notifyConfigList = notifyService.getNotifyConfigList();
            for (NotifyConfig notifyConfig : notifyConfigList) {
                if (!maxDiffPriceRule.notifyAvailable(notifyConfig)) {
                    continue;
                }
                customerActivityRemind(notifyConfig);
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
    private void customerActivityRemind(NotifyConfig notifyConfig) {
        Location location = notifyConfig.getLocation();
        List<StoreInfo> list = xiaoChanService.getList(location.getCityCode(), location.getLongitude(), location.getLatitude(), DEFAULT_MAX_SIZE);
        List<StoreInfo> availableStores = maxDiffPriceRule.filter(notifyConfig, list);
        if (availableStores.isEmpty()) {
            log.info("没有满足条件的门店活动");
            return;
        }
        notifyConfig.setNotifyCount(notifyConfig.getNotifyCount() + 1);
        Date now = new Date();
        notifyConfig.setLastNotifyTime(now);
        notifyService.updateNotifyConfig(notifyConfig);
        //通知
        sendMessage(availableStores, notifyConfig.getLocation());
    }


    /**
     * 指定门店活动提醒
     */
    private void specifyStoreActivityRemind(NotifyConfig notifyConfig){
        //通过搜索来获取门店活动信息
        List<StoreInfo> storeInfos = xiaoChanService.searchList(notifyConfig.getStoreExtNotifyConfig().getStoreInfo().getName(),
                notifyConfig.getLocation().getCityCode(), notifyConfig.getLocation().getLongitude(),
                notifyConfig.getLocation().getLatitude());
        List<StoreInfo> availableStores = specifyStoreRule.filter(notifyConfig, storeInfos);
        if(availableStores.isEmpty())
            return;
        notifyConfig.setNotifyCount(notifyConfig.getNotifyCount() + 1);
        Date now = new Date();
        notifyConfig.setLastNotifyTime(now);
        if (notifyConfig.getStoreExtNotifyConfig().getOnlyOne()) {
            notifyConfig.setStatus(2);
        }
        notifyConfig.setRemark("任务完成" + DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"));
        notifyService.updateNotifyConfig(notifyConfig);
        //通知
        sendMessage(availableStores, notifyConfig.getLocation());
    }

    public void run(Location location){
        try {
            List<StoreInfo> storeInfos = xiaoChanService.getList(location.getCityCode(), location.getLongitude(), location.getLatitude(), DEFAULT_MAX_SIZE);
            log.info("当前位置:{}，门店数量:{}", location.getName(), storeInfos.size());
            //发送通知
            sendMessage(storeInfos, location);
            log.info("当前位置:{}，结束", location.getName());
        }catch (Exception e){
            log.error("发生异常", e);
        }

    }


    private void sendMessage(List<StoreInfo> storeInfos, Location location) {
        String body = storeInfos.stream()
                .map(storeInfo -> buildMessage(storeInfo, location))
                .collect(Collectors.joining("<br/><br/>"));
        String header = "有新的返现活动啦，共" + storeInfos.size() + "个";
        try {
            log.info("发送消息:{}", body);
            MessageHttp.sendMessage(location.getSpt(), body, header);
        }catch (Exception e){
            log.error("发送消息失败", e);
        }
    }

    private String buildMessage(StoreInfo storeInfo, Location location) {
        return "地点：" + location.getName() + "<br/>" +
                "平台：" + StorePlatformEnum.getByType(storeInfo.getType()).name + "<br/>" +
                "店铺：" + storeInfo.getName() + "<br/>" +
                "时间范围：" + storeInfo.getStartTime() + "-" + storeInfo.getEndTime() + "<br/>" +
                "距离：" + storeInfo.getDistance() + "米" + "<br/>" +
                "库存：" + storeInfo.getLeftNumber() + "<br/>" +
                "规则：满" + storeInfo.getPrice() + "返" + storeInfo.getRebatePrice() + "<br/>" +
                "是否需要评价:" + (storeInfo.getRebateCondition() == null ? "未知" : (storeInfo.getRebateCondition() != 99 ? "是" : "否")) + "\r\n";
    }

}
