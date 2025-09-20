package io.github.xiaochan.service.impl;

import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.HttpProxyInfo;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.model.vo.QueryListVO;
import io.github.xiaochan.proxy.XieQuHttpProxy;
import io.github.xiaochan.service.XiaoChanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class XiaoChanServiceImpl implements XiaoChanService {

    private final XiaochanHttp xiaochanHttp = new XiaochanHttp();

    private static final int PAGE_SIZE = 30;

    @Resource
    @Lazy
    private XiaoChanService xiaoChanService;

    @Override
    public List<StoreInfo> query(QueryListVO queryListVO) {
        List<StoreInfo> list = xiaoChanService.getList(queryListVO.getCityCode(), queryListVO.getLongitude(), queryListVO.getLatitude(), PAGE_SIZE);
        sortStoreList(list, queryListVO.getOrderType());
        return list;
    }

    @Override
    @Cacheable(cacheNames = "xiaoChanList", key = "#cityCode+#longitude+#latitude+#maxSize")
    public List<StoreInfo> getList(Integer cityCode, String longitude, String latitude, int maxSize){
        preCall(cityCode);
        int offset = 0;
        List<StoreInfo> result = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            List<StoreInfo> list = doGetList(cityCode, longitude, latitude, offset);
            result.addAll(list);
            if (result.size() > maxSize) {
                break;
            }
            offset += PAGE_SIZE;
        }
        return result;
    }

    private List<StoreInfo> doGetList(Integer cityCode, String longitude, String latitude, int offset){
        try {
            List<StoreInfo> list = xiaochanHttp.getList(String.valueOf(cityCode), longitude, latitude, offset);
            xiaochanHttp.meituanShangjinGetPoiList(latitude, longitude, cityCode);
            xiaochanHttp.getClientUnionPromotions1(cityCode);
            xiaochanHttp.getClientUnionPromotions2(cityCode);
            return list;
        } catch (Exception e) {
            log.error("请求小产列表时发生错误 ",e);
        }
        return Collections.emptyList();
    }

    private void preCall(Integer cityCode){
        try {
            xiaochanHttp.getTabTag();
            xiaochanHttp.getClientCfg();
            xiaochanHttp.getClientCfg();
            xiaochanHttp.batchMatchPlacement1(cityCode);
            xiaochanHttp.batchMatchPlacement2(cityCode);
            xiaochanHttp.getFullRewardBanner();
            xiaochanHttp.KfAccountList();
            xiaochanHttp.geocoder();
            xiaochanHttp.geocoder();
            xiaochanHttp.getGlobalConfig2(cityCode);
            xiaochanHttp.batchMatchPlacement3(cityCode);
        }catch (Exception e){
            log.error("请求小产列表时发生错误 ",e);
        }
    }

    /**
     * 根据排序类型对门店列表进行排序
     * @param list 门店列表
     * @param orderType 排序类型，1：默认，2：返现金额排序，3：返现比例排序
     */
    private void sortStoreList(List<StoreInfo> list, Integer orderType) {
        if (orderType == null || orderType == 1) {
            // 默认排序，不处理
            return;
        }

        if (orderType == 2) {
            // 按返现金额倒序排序
            list.sort(Comparator.comparing(StoreInfo::getRebatePrice, Comparator.nullsLast(Comparator.reverseOrder())));
        } else if (orderType == 3) {
            // 按返现比例倒序排序
            list.sort(Comparator.comparing(this::calculateRebateRatio, Comparator.nullsLast(Comparator.reverseOrder())));
        }
    }

    /**
     * 计算返现比例
     * @param storeInfo 门店信息
     * @return 返现比例 (rebatePrice/price)
     */
    private BigDecimal calculateRebateRatio(StoreInfo storeInfo) {
        if (storeInfo.getPrice() == null || storeInfo.getRebatePrice() == null || storeInfo.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return storeInfo.getRebatePrice().divide(storeInfo.getPrice(), 4, RoundingMode.DOWN);
    }
}
