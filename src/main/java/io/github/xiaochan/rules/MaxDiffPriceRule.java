package io.github.xiaochan.rules;

import io.github.xiaochan.constant.RedisConstant;
import io.github.xiaochan.model.MaxDiffPriceExtNotifyConfig;
import io.github.xiaochan.model.NotifyConfig;
import io.github.xiaochan.model.StoreInfo;
import jakarta.annotation.Resource;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class MaxDiffPriceRule extends AbstractRule{

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean notifyAvailable(NotifyConfig notifyConfig) {
        return notifyConfig.getType() == 2 && notifyConfig.getStatus() == 1;
    }

    @Override
    public List<StoreInfo> filter(NotifyConfig notifyConfig, List<StoreInfo> storeInfos) {
        MaxDiffPriceExtNotifyConfig extNotifyConfig = notifyConfig.getMaxDiffPriceExtNotifyConfig();
        RMapCache<String, Object> mapCache = redissonClient.getMapCache(RedisConstant.STORE_NOTIFY_RECORD, new CompositeCodec(StringCodec.INSTANCE, JsonJacksonCodec.INSTANCE));
        return storeInfos
                .stream()
                .filter(storeInfo -> storeInfo.getLeftNumber() > 0)
                .filter(storeInfo -> storeInfo.getPrice().subtract(storeInfo.getRebatePrice()).compareTo(extNotifyConfig.getMaxDiffPrice()) <= 0)
                .filter(storeInfo -> !mapCache.containsKey(getFieldName(storeInfo)))
                .peek(storeInfo -> mapCache
                        .put(getFieldName(storeInfo), storeInfo, extNotifyConfig.getIdleDay(), TimeUnit.DAYS))
                .toList();
    }

    private String getFieldName(StoreInfo storeInfo) {
        return storeInfo.getStoreId() + ":" + storeInfo.getType();
    }
}
