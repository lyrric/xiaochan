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
        xieQuHttpProxy.refreshIp();

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

    private List<StoreInfo> getList(Location location, int offset){
        if (httpProxyInfo == null) {
            httpProxyInfo = xieQuHttpProxy.getOne();
        }
        for (int i = 0; i < 10; i++) {
            try {
                return xiaochanHttp.getList(location, offset, httpProxyInfo);
            } catch (Exception e) {
                if (e.getMessage().contains("timed out") && i < 9) {
                    httpProxyInfo = xieQuHttpProxy.getOne();
                }else{
                    log.error("",e);
                }

            }
        }
        return Collections.emptyList();

    }

    @Scheduled(cron = "0 0 9,10,11,12,13,14,15,16,17,18,19 * * ? ")
    public void refreshIp(){
        xieQuHttpProxy.refreshIp();
    }
}
