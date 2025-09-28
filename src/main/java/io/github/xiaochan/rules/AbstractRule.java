package io.github.xiaochan.rules;

import io.github.xiaochan.model.NotifyConfig;
import io.github.xiaochan.model.StoreInfo;

import java.util.List;

public abstract class AbstractRule {

    /**
     * 配置是否可用
     * @param notifyConfig
     * @return
     */
    public abstract boolean notifyAvailable(NotifyConfig notifyConfig);

    /**
     * 过滤门店信息
     * @param notifyConfig 配置信息
     * @param storeInfos 活动列表
     * @return 过滤后的活动信息
     */
    public abstract List<StoreInfo> filter(NotifyConfig notifyConfig, List<StoreInfo> storeInfos);
}
