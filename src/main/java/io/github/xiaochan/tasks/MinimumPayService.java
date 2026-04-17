package io.github.xiaochan.tasks;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import io.github.xiaochan.model.MinimumPayExtNotifyConfig;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.model.dto.TaskExecHistoryEntity;
import io.github.xiaochan.model.entity.LocationEntity;
import io.github.xiaochan.model.entity.NotifyConfigEntity;
import io.github.xiaochan.model.enums.NotifyConfigStatusEnums;
import io.github.xiaochan.model.enums.NotifyTypeEnums;
import io.github.xiaochan.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MinimumPayService extends BaseTask {

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

    @Override
    protected List<StoreInfo> doRunSingle(NotifyConfigEntity notifyConfig, TaskExecHistoryEntity execHistory, LocationEntity location) {
        execHistory.setNotifyType(NotifyTypeEnums.MINIMUM_PAY);
        MinimumPayExtNotifyConfig extNotifyConfig = JSON.parseObject(notifyConfig.getExtConfig(), MinimumPayExtNotifyConfig.class);
        List<StoreInfo> storeInfos = xiaoChanService.getList(location.getCityCode(), location.getLongitude(), location.getLatitude(), DEFAULT_MAX_SIZE);
        return storeInfos
                .stream()
                .filter(storeInfo -> storeInfo.getLeftNumber() > 0)
                .filter(storeInfo -> storeInfo.getPrice().subtract(storeInfo.getRebatePrice()).compareTo(extNotifyConfig.getMinimumPay()) <= 0)
                .filter(storeInfo -> storePushedHistoryService.findByNotifyIdAndStoreIdAll(notifyConfig.getId(), storeInfo.getStoreId()) == null)
                .toList();

    }



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
                runSingle(notifyConfig);
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
}
