package io.github.xiaocan.service.impl;

import io.github.xiaocan.config.XieQuProxyConfig;
import io.github.xiaocan.http.XieQuHttp;
import io.github.xiaocan.model.HttpProxyInfo;
import io.github.xiaocan.service.ProxyService;
import io.github.xiaocan.utils.IPUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created on 2020-10-30.
 * 携趣http代理 https://www.xiequ.cn/
 * @author lyrric
 */
@Slf4j
@Service
public class XieQuProxyServiceImpl implements ProxyService {



    private final CopyOnWriteArrayList<HttpProxyInfo> httpProxyInfos = new CopyOnWriteArrayList<>();

    @Resource
    private XieQuProxyConfig xieQuProxyConfig;

    private List<HttpProxyInfo> getList() {
        checkAndAddIp();
        //重试五次，避免因网络波动导致获取失败
        for (int i = 0; i < 5; i++) {
            try {
                List<HttpProxyInfo> result = XieQuHttp.getIp(xieQuProxyConfig);
                log.info("获取ip代理成功:{}", result);
                return result;
            } catch (IOException e) {
                log.error("获取代理失败，尝试重试：{}", i, e);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public HttpProxyInfo getOne() {
        Iterator<HttpProxyInfo> iterator = httpProxyInfos.iterator();
        while (iterator.hasNext()) {
            HttpProxyInfo proxyInfo = iterator.next();
            if (proxyInfo.getExpiry().before(new Date())) {
                log.info("代理过期被移除：{}", proxyInfo);
                iterator.remove();
                continue;
            }
            if (proxyInfo.getRateLimiter().tryAcquire()) {
                return proxyInfo;
            }
        }
        HttpProxyInfo proxyInfo = getList().get(0);
        httpProxyInfos.add(proxyInfo);
        return proxyInfo;
    }



    @Override
    public void removeProxy(HttpProxyInfo httpProxyInfo){
        httpProxyInfos.remove(httpProxyInfo);
    }

    private void checkAndAddIp(){
        if (!xieQuProxyConfig.getEnable()) {
            return;
        }
        try{
            String json = getAllIp();
            String ip = IPUtil.getIp();
            if (json.contains(ip)) {
                return;
            }
            log.info("当前ip不在白名单，开始添加ip：{}", ip);
            String res = XieQuHttp.addIp(ip, xieQuProxyConfig);
            if (res.contains("ERR")) {
                //可能是ip上限了，删除所有ip重新添加
                log.error("直接添加ip失败：{}", res);
                res = XieQuHttp.deleteAllIp(xieQuProxyConfig);
                log.info("删除所有ip结果：{}", res);
                res = XieQuHttp.addIp(ip, xieQuProxyConfig);
                log.info("重新添加ip结果：{}", res);
            }
        }catch (Exception e){
            log.error("error checkAndAddIp", e);
        }

    }
    public String getAllIp(){
        try {
            return XieQuHttp.getAllIp(xieQuProxyConfig);
        }catch (Exception e){
            log.error("获取ip白名单失败", e);
        }
        return "";
    }




}
