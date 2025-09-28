package io.github.xiaochan.rules;

import cn.hutool.core.date.DateUtil;
import io.github.xiaochan.model.NotifyConfig;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.service.NotifyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SpecifyStoreRule extends AbstractRule{

    @Resource
    private NotifyService notifyService;

    @Override
    public boolean notifyAvailable(NotifyConfig notifyConfig) {
        if(notifyConfig.getStatus() != 1 || notifyConfig.getType() != 1)
            return false;
        Date now = new Date();
        if (notifyConfig.getLastNotifyTime() != null) {
            //如果上次通知时间在今天内，则不进行通知
            if (DateUtil.isSameDay(now, notifyConfig.getLastNotifyTime())) {
                return false;
            }
        }
        Date createTime = notifyConfig.getCreateTime();
        Integer expireDay = notifyConfig.getStoreExtNotifyConfig().getExpireDay();
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

    @Override
    public List<StoreInfo> filter(NotifyConfig notifyConfig, List<StoreInfo> storeInfos) {
        return storeInfos
                .stream()
                //同一个门店
                .filter(storeInfo -> notifyConfig.getStoreExtNotifyConfig().getStoreInfo().getStoreId().equals(storeInfo.getStoreId()))
                .filter(storeInfo -> storeInfo.getLeftNumber() > 0)
                //返现金额必须大于等于之前的返现金额
                .filter(storeInfo -> storeInfo.getRebatePrice().compareTo(notifyConfig.getStoreExtNotifyConfig().getStoreInfo().getRebatePrice()) >= 0)
                //价格必须小于等于之前的价格
                .filter(storeInfo -> storeInfo.getPrice().compareTo(notifyConfig.getStoreExtNotifyConfig().getStoreInfo().getPrice()) <= 0)
                .toList();
    }
}
