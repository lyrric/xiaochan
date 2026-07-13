package io.github.xiaocan.tasks;

import com.alibaba.fastjson2.JSON;
import io.github.xiaocan.model.MinimumPayExtNotifyConfig;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.entity.MonitorConfigEntity;
import io.github.xiaocan.model.entity.TaskExecHistoryEntity;
import io.github.xiaocan.model.enums.MonitorConfigStatusEnums;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import io.github.xiaocan.service.MonitoryConfigService;
import io.github.xiaocan.service.StorePushedHistoryService;
import io.github.xiaocan.service.XiaoChanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MinimumPayService extends BaseTask {

    @Resource
    private XiaoChanService xiaoChanService;
    @Resource
    private MonitoryConfigService monitoryConfigService;
    @Resource
    private StorePushedHistoryService storePushedHistoryService;



    @Override
    protected List<StoreInfo> fetchStoreInfos(MonitorConfigEntity notifyConfig, TaskExecHistoryEntity execHistory, LocationEntity location) {
        execHistory.setNotifyType(MonitorTypeEnums.MINIMUM_PAY);
        return xiaoChanService.getList(location.getCityCode(), location.getLongitude(), location.getLatitude(), 500);
    }

    @Override
    protected List<StoreInfo> filterStoreInfos(MonitorConfigEntity notifyConfig, List<StoreInfo> storeInfos) {
        MinimumPayExtNotifyConfig extNotifyConfig = JSON.parseObject(notifyConfig.getExtConfig(), MinimumPayExtNotifyConfig.class);
        return storeInfos
                .stream()
                .filter(storeInfo -> storeInfo.getLeftNumber() > 0)
                .filter(storeInfo -> storeInfo.getPrice().subtract(storeInfo.getRebatePrice()).compareTo(extNotifyConfig.getMinimumPay()) <= 0)
                .filter(storeInfo -> storePushedHistoryService.findByNotifyIdAndStoreIdAll(notifyConfig.getId(), storeInfo.getStoreId()) == null)
                .toList();
    }



    /**
     * 最小实付（静态兜底调度，仅处理未配置 cron 的配置）
     */
    @Scheduled(cron = "0 45 * * * ? ")
    public void start(){
        try {
            //获取所有配置信息
            log.info("开始执行 最小实付活动 定时任务");
            List<MonitorConfigEntity> notifyConfigList = monitoryConfigService.listWithoutCron(MonitorTypeEnums.MINIMUM_PAY, MonitorConfigStatusEnums.ENABLE);
            for (MonitorConfigEntity notifyConfig : notifyConfigList) {
                execute(notifyConfig, false);
            }
        }catch (Exception e){
            log.error("发生异常 ", e);
        }
    }

    /**
     * 按配置执行任务入口
     *
     * @param cronDriven true 表示由 cron 动态调度器触发，跳过时间窗口和静默期检查
     */
    public void execute(MonitorConfigEntity notifyConfig, boolean cronDriven) {
        runSingle(notifyConfig, cronDriven);
    }
}
