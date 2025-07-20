package io.github.xiaochan.proxy;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import io.github.xiaochan.core.BusinessException;
import io.github.xiaochan.model.HttpProxyInfo;
import io.github.xiaochan.utils.IPUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Created on 2020-10-30.
 * 携趣http代理 https://www.xiequ.cn/
 * @author wangxiaodong
 */
@Slf4j
@Component
public class XieQuHttpProxy implements HttpProxy {

    @Value("${xiequ.key}")
    private String key;

    @Value("${xiequ.refresh.key}")
    private String refreshKey;

    private final CloseableHttpClient httpclient;

    private final int TIMEOUT = 10000;

    public XieQuHttpProxy() {
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(TIMEOUT)
                .build();
        httpclient = HttpClients.custom()
                .setDefaultSocketConfig(socketConfig)
                .build();
    }

    @Override
    public List<HttpProxyInfo> getList(int count) {
        //重试五次，避免因网络波动导致获取失败
        for (int i = 0; i < 5; i++) {
            try {
                String json = get(
                        "http://api.xiequ.cn/VAD/GetIp.aspx?act=get&uid=49226&vkey=" + key + "&num=" + count + "&time=60&plat=0&re=0&type=1&so=1&ow=1&spl=1&addr=&db=1");
                log.info("获取ip代理成功:{}", json);
                JSONObject jsonObject = JSONObject.parseObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                List<ProxyInfo> proxyInfoList = jsonArray.toList(ProxyInfo.class);

                List<HttpProxyInfo> result = new ArrayList<>(proxyInfoList.size());
                for (ProxyInfo proxyInfo : proxyInfoList) {
                    HttpProxyInfo info = new HttpProxyInfo();
                    info.setIp(proxyInfo.getIp());
                    info.setPort(proxyInfo.getPort());
                    info.setScheme("http");
                    info.setExpiry(new Date());
                    result.add(info);
                }
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
        synchronized (this){
            final List<HttpProxyInfo> httpProxyInfos= getList(1);
            return httpProxyInfos.get(0);
        }
    }

    @Data
    private static class ProxyInfo{
        /**
         * 代理ip地址
         */
        @JSONField(name = "IP")
        private String ip;
        /**
         * 代理端口
         */
        @JSONField(name = "Port")
        private Integer port;
    }


    public String get(String url) throws IOException {
        return get(url, null,null);
    }
    public String get(String url, Map<String, String> params, List<Header> headers) throws IOException {
        if(params != null && params.size() !=0){
            StringBuilder paramStr = new StringBuilder("?");
            params.forEach((key,value)->{
                paramStr.append(key).append("=").append(value).append("&");
            });
            String t = paramStr.toString();
            if(t.endsWith("&")){
                t = t.substring(0, t.length()-1);
            }
            url+=t;
        }
        HttpGet httpGet = new HttpGet(url);
        setConfig(httpGet, headers);
        return execute(httpGet);
    }



    private void setConfig(HttpRequestBase http, List<Header> headers){
        if(headers != null && headers.size() > 0){
            http.setHeaders(headers.toArray(new Header[0]));
            http.addHeader("Connection", "close");
        }
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setRedirectsEnabled(false)
                //我猜这里的单位是毫秒
                .setConnectTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT);

        http.setConfig(builder.build());
    }

    private String execute(HttpRequestBase http) throws IOException {
        try (CloseableHttpResponse response = httpclient.execute(http)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, Charsets.UTF_8);
            } else {
                throw new BusinessException("状态码异常" + statusCode);
            }
        }
    }

    public void refreshIp() {
        deleteAllIp();
        String ip = IPUtil.getIp();
        addIp(ip);
    }

    private void deleteAllIp(){
        HttpUtil.createGet("http://op.xiequ.cn/IpWhiteList.aspx?uid=49226&ukey=" + refreshKey + "&act=del&ip=all")
                .execute();

    }
    private void addIp(String ip){
        HttpUtil.createGet("http://op.xiequ.cn/IpWhiteList.aspx?uid=49226&ukey=" + refreshKey + "&act=add&ip=" + ip)
                .execute();

    }


}
