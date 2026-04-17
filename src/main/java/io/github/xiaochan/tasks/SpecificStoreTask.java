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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
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
public class SpecificStoreTask extends BaseTask {


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
                runSingle(notifyConfig);
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
    @Override
    protected List<StoreInfo> doRunSingle(NotifyConfigEntity notifyConfig, TaskExecHistoryEntity execHistory, LocationEntity location) {
        execHistory.setNotifyType(NotifyTypeEnums.STORE_ACTIVITY);
        StoreExtNotifyConfig storeExtNotifyConfig = JSON.parseObject(notifyConfig.getExtConfig(), StoreExtNotifyConfig.class);
        //通过搜索来获取门店活动信息
        List<StoreInfo> storeInfos = xiaoChanService.searchList(storeExtNotifyConfig.getStoreInfo().getName(),
                location.getCityCode(), location.getLongitude(),
                location.getLatitude());
        return storeInfos
                .stream()
                //同一个门店
                .filter(storeInfo -> storeExtNotifyConfig.getStoreInfo().getStoreId().equals(storeInfo.getStoreId()))
                .filter(storeInfo -> storeInfo.getLeftNumber() > 0)
                //返现金额必须大于等于之前的返现金额
                .filter(storeInfo -> storeInfo.getRebatePrice().compareTo(storeExtNotifyConfig.getStoreInfo().getRebatePrice()) >= 0)
                //价格必须小于等于之前的价格
                .filter(storeInfo -> storeInfo.getPrice().compareTo(storeExtNotifyConfig.getStoreInfo().getPrice()) <= 0)
                .toList();
    }
    @Override
    protected void afterSuccess(NotifyConfigEntity notifyConfig) {
        notifyConfigService.updateConfig(notifyConfig.getId(), NotifyConfigStatusEnums.DISABLE, "任务完成，完成时间" + DateUtil.format(new Date(), "MM-dd HH:mm:ss"));
    }
}
