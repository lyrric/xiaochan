package io.github.xiaocan.tasks;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import io.github.xiaocan.model.StoreExtNotifyConfig;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
    private MonitoryConfigService monitoryConfigService;
    @Resource
    private StorePushedHistoryService storePushedHistoryService;


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
            List<MonitorConfigEntity> notifyConfigList = monitoryConfigService.list(MonitorTypeEnums.STORE_ACTIVITY, MonitorConfigStatusEnums.ENABLE);
            log.info("开始执行 指定门店活动定时任务 获取到{}个配置信息", notifyConfigList.size());
            for (MonitorConfigEntity notifyConfig : notifyConfigList) {
                if(!checkRepeat(notifyConfig)){
                    runSingle(notifyConfig);
                }


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
    private boolean checkRepeat(MonitorConfigEntity notifyConfig) {
        //检查今天是否通知过了
        return storePushedHistoryService.lambdaQuery()
                .eq(StorePushedHistoryEntity::getNotifyConfigId, notifyConfig.getId())
                .ge(StorePushedHistoryEntity::getCreateTime, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0))
                .last("limit 1")
                .oneOpt().isPresent();

    }

    /**
     * 指定门店活动提醒
     */
    @Override
    protected List<StoreInfo> doRunSingle(MonitorConfigEntity notifyConfig, TaskExecHistoryEntity execHistory, LocationEntity location) {
        execHistory.setNotifyType(MonitorTypeEnums.STORE_ACTIVITY);
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
}
