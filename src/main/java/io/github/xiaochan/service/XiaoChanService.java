package io.github.xiaochan.service;

import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.HttpProxyInfo;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.proxy.XieQuHttpProxy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class XiaoChanService {

    @Resource
    private XieQuHttpProxy xieQuHttpProxy;

    private final XiaochanHttp xiaochanHttp = new XiaochanHttp();

    private HttpProxyInfo httpProxyInfo = null;

    private static final int PAGE_SIZE = 30;


    public List<StoreInfo> getList(Location location){
        preCall(location.getCityCode());
        int offset = 0;
        List<StoreInfo> result = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            List<StoreInfo> list = getList(location, offset);
            result.addAll(list);
            if (result.size() > 150) {
                break;
            }
            offset += PAGE_SIZE;
        }

        return result;
    }

    private List<StoreInfo> getListProxy(Location location, int offset){
        if (httpProxyInfo == null) {
            httpProxyInfo = xieQuHttpProxy.getOne();
        }
        for (int i = 0; i < 10; i++) {
            try {
                List<StoreInfo> list = xiaochanHttp.getList(location, offset, httpProxyInfo);
                xiaochanHttp.meituanShangjinGetPoiList(location.getLatitude(), location.getLongitude(), location.getCityCode());
                xiaochanHttp.getClientUnionPromotions1(location.getCityCode());
                xiaochanHttp.getClientUnionPromotions2(location.getCityCode());
                return list;
            } catch (Exception e) {
                log.error("请求小产列表时发生错误 {}",e.getMessage());
                if (i < 9) {
                    httpProxyInfo = xieQuHttpProxy.getOne();
                }
            }
        }
        return Collections.emptyList();
    }

    private List<StoreInfo> getList(Location location, int offset){
        try {
            List<StoreInfo> list = xiaochanHttp.getList(location, offset, httpProxyInfo);
            xiaochanHttp.meituanShangjinGetPoiList(location.getLatitude(), location.getLongitude(), location.getCityCode());
            xiaochanHttp.getClientUnionPromotions1(location.getCityCode());
            xiaochanHttp.getClientUnionPromotions2(location.getCityCode());
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
}
