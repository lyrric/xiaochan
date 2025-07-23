package io.github.xiaochan.service;

import io.github.xiaochan.config.LocationConfig;
import io.github.xiaochan.constant.RedisConstant;
import io.github.xiaochan.http.MessageHttp;
import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.StoreInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppService {


    @Resource
    private LocationConfig locationConfig;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private XiaoChanService xiaoChanService;

    @Scheduled(cron = "0 0 9,11,13,15,17,19 * * ? ")
    @EventListener(ApplicationReadyEvent.class)
    public void run(){
        List<Location> locations = locationConfig.getLocations();
        for (Location location : locations) {
            run(location);
        }
    }

    public void run(Location location){
        try {
            List<StoreInfo> storeInfos = xiaoChanService.getList(location);
            log.info("当前位置:{}，门店数量:{}", location.getName(), storeInfos.size());
            //发送通知
            sendMessage(storeInfos, location);
            log.info("当前位置:{}，结束", location.getName());
        }catch (Exception e){
            log.error("发生异常", e);
        }

    }


    private void sendMessage(List<StoreInfo> storeInfos, Location location) {
        Set<StoreInfo> collect = storeInfos.stream()
                .filter(storeInfo -> check(storeInfo, location))
                .filter(storeInfo -> storeInfo.getDistance() != null && storeInfo.getDistance() < 3000)
                .filter(storeInfo -> {
                    RBucket<String> bucket = redissonClient.getBucket(RedisConstant.PROMOTION_ID + storeInfo.getName() + storeInfo.getType());
                    if (bucket.isExists()) {
                        return false;
                    }
                    bucket.set(String.valueOf(System.currentTimeMillis()), Duration.ofHours(12));
                    return true;
                })
                .collect(Collectors.toSet());
        if (collect.isEmpty()) {
            return;
        }
        String body = collect.stream()
                .map(storeInfo -> buildMessage(storeInfo, location))
                .collect(Collectors.joining("<br/><br/>"));
        String header = "有新的返现活动啦，共" + collect.size() + "个";
        try {
            log.info("发送消息:{}", body);
            MessageHttp.sendMessage(location.getSpt(), body, header);
        }catch (Exception e){
            log.error("发送消息失败", e);
        }
    }

    private String buildMessage(StoreInfo storeInfo, Location location) {
        StringBuilder sb = new StringBuilder();
        sb.append("地点：").append(location.getName()).append("<br/>");
        sb.append("平台：").append(storeInfo.getType() == 1 ? "美团" : "饿了么").append("<br/>")
                .append("店铺：").append(storeInfo.getName()).append("<br/>")
                .append("时间范围：").append(storeInfo.getStartTime()).append("-").append(storeInfo.getEndTime()).append("<br/>")
                .append("距离：").append(storeInfo.getDistance()).append("米").append("<br/>")
                .append("库存：").append(storeInfo.getLeftNumber()).append("<br/>")
                .append("规则：满").append(storeInfo.getPrice()).append("返").append(storeInfo.getRebatePrice()).append("<br/>")
                .append("是否需要评价:").append(storeInfo.getRebateCondition() == null ? "未知" : (storeInfo.getRebateCondition() != 99 ? "是" : "否")).append("\r\n");
        return sb.toString();
    }

    private boolean check(StoreInfo storeInfo, Location location) {
        Integer leftNumber = storeInfo.getLeftNumber();
        if (leftNumber < 1) {
            return false;
        }
        //金额判断
        BigDecimal price = storeInfo.getPrice();
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
        }
        return true;

    }

}
