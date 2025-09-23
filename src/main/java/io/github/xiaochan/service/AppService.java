package io.github.xiaochan.service;

import cn.hutool.core.date.DateUtil;
import io.github.xiaochan.constant.StorePlatformEnum;
import io.github.xiaochan.http.MessageHttp;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.NotifyConfig;
import io.github.xiaochan.model.StoreInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppService {


    @Resource
    private RedissonClient redissonClient;
    @Resource
    private XiaoChanService xiaoChanService;
    @Resource
    private NotifyService notifyService;


    private static final int DEFAULT_MAX_SIZE = 100;


    /**
     * 指定门店活动定时任务
     */
    @Scheduled(cron = "0 10 * * * ? ")
    public void specifyStoreScheduled(){
        //获取所有配置信息
        List<NotifyConfig> notifyConfigList = notifyService.getNotifyConfigList();
        for (NotifyConfig notifyConfig : notifyConfigList) {
            try {
                if (notifyConfig.getType() != 1 || !checkNotifyAvailable(notifyConfig)) {
                    continue;
                }
                specifyStoreActivityRemind(notifyConfig);
            }catch (Exception e){
                log.error("发生异常 {}", notifyConfig, e);
            }

        }
    }

    /**
     * 配置是否有效
     */
    private boolean checkNotifyAvailable(NotifyConfig notifyConfig){
        if(notifyConfig.getStatus() != 1)
            return false;
        Date now = new Date();
        if (notifyConfig.getLastNotifyTime() != null) {
            //如果上次通知时间在今天内，则不进行通知
            if (DateUtil.isSameDay(now, notifyConfig.getLastNotifyTime())) {
                return false;
            }
        }
        Date createTime = notifyConfig.getCreateTime();
        Integer expireDay = notifyConfig.getExpireDay();
        if (expireDay != null) {
            //如果超过了有效天数，则不进行通知
            Date expireDate = DateUtil.offsetDay(createTime, expireDay);
            if (now.after(expireDate)) {
                notifyConfig.setRemark("超过有效期天数");
                notifyService.updateNotifyConfig(notifyConfig);
                return false;
            }
        }
        return true;
    }

    /**
     * 指定门店活动提醒
     */
    private void specifyStoreActivityRemind(NotifyConfig notifyConfig){
        List<StoreInfo> availableStores = getAvailableStores(notifyConfig);
        if(availableStores.isEmpty())
            return;
        notifyConfig.setNotifyCount(notifyConfig.getNotifyCount() + 1);
        Date now = new Date();
        notifyConfig.setLastNotifyTime(now);
        if (notifyConfig.getOnlyOne()) {
            notifyConfig.setStatus(2);
        }
        notifyConfig.setRemark("任务完成" + DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"));
        notifyService.updateNotifyConfig(notifyConfig);
        //通知
        sendMessage(availableStores, notifyConfig.getLocation());
    }

    /**
     * 获取符合规则的活动信息
     */
    private List<StoreInfo> getAvailableStores(NotifyConfig notifyConfig) {
        //通过搜索来获取门店活动信息
        List<StoreInfo> storeInfos = xiaoChanService.searchList(notifyConfig.getStoreInfo().getName(),
                notifyConfig.getLocation().getCityCode(), notifyConfig.getLocation().getLongitude(),
                notifyConfig.getLocation().getLatitude());
        return storeInfos
                .stream()
                //同一个门店
                .filter(storeInfo -> notifyConfig.getStoreInfo().getStoreId().equals(storeInfo.getStoreId()))
                .filter(storeInfo -> storeInfo.getLeftNumber() > 0)
                //返现金额必须大于等于之前的返现金额
                .filter(storeInfo -> storeInfo.getRebatePrice().compareTo(notifyConfig.getStoreInfo().getRebatePrice()) >= 0)
                //价格必须小于等于之前的价格
                .filter(storeInfo -> storeInfo.getPrice().compareTo(notifyConfig.getStoreInfo().getPrice()) <= 0)
                .toList();
    }

    public void run(){
        List<Location> locations = Collections.emptyList();
        for (Location location : locations) {
            run(location);
            log.info("执行完成 {}",location);
        }
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


    private boolean check(StoreInfo storeInfo, Location location) {
        Integer leftNumber = storeInfo.getLeftNumber();
        if (leftNumber < 1) {
            return false;
        }
        //金额判断
/*        BigDecimal price = storeInfo.getPrice();
        if (location.getPrice() != null && price.compareTo(location.getPrice()) < 0) {
            return false;
        }
        //反现金额大于指定值
        BigDecimal rebatePrice = storeInfo.getRebatePrice();
        if (location.getRebatePrice() != null &&
                rebatePrice.compareTo(location.getRebatePrice()) < 0) {
            return false;
        }
        if (location.getDifPrice() != null &&
                price.subtract(rebatePrice).compareTo(location.getDifPrice()) > 0) {
            //金额差大于指定值
            return false;
        }*/
        return false;

    }

}
