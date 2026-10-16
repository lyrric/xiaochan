package io.github.xiaocan.http;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.config.XieQuProxyConfig;
import io.github.xiaocan.model.HttpProxyInfo;
import lombok.Data;
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

import java.io.IOException;
import java.util.*;

/**
 * 携趣 HTTP 请求工具
 */
public final class XieQuHttp {

    private static final int TIMEOUT = 10000;

    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom()
            .setDefaultSocketConfig(SocketConfig.custom()
                    .setSoTimeout(TIMEOUT)
                    .build())
            .build();


    /**
     * 获取ip代理列表
     */
    public static List<HttpProxyInfo> getIp(XieQuProxyConfig config) throws IOException {

        String json = get(config.getGetIpParams(), null);
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<ProxyInfo> proxyInfoList = jsonArray.toList(ProxyInfo.class);

        List<HttpProxyInfo> result = new ArrayList<>(proxyInfoList.size());
        Date expireTime = DateUtil.offsetMinute(new Date(), 3*60).toJdkDate();
        for (ProxyInfo proxyInfo : proxyInfoList) {
            HttpProxyInfo info = new HttpProxyInfo();
            info.setIp(proxyInfo.getIp());
            info.setPort(proxyInfo.getPort());
            info.setScheme("http");
            info.setExpiry(expireTime);
            result.add(info);
        }
        return result;
    }

    public static String deleteAllIp(XieQuProxyConfig config) {
        return getBody(buildIpWhiteListUrl("all", "del", config.getUkey(), config.getUid()));
    }

    public static String getAllIp(XieQuProxyConfig config) {
        return getBody(buildIpWhiteListUrl(null, "getjson", config.getUkey(), config.getUid()));
    }

    public static String addIp(String ip, XieQuProxyConfig config) {
        return getBody(buildIpWhiteListUrl(ip, "add", config.getUkey(), config.getUid()));
    }

    private static String get(String params, List<Header> headers) throws IOException {
        String GET_IP_URL = "http://api.xiequ.cn/VAD/GetIp.aspx";
        String url = GET_IP_URL + "?" + params;
        HttpGet httpGet = new HttpGet(url);
        setConfig(httpGet, headers);
        return execute(httpGet);
    }

    private static String getBody(String url) {
        return HttpUtil.createGet(url).execute().body();
    }

    private static String buildIpWhiteListUrl(String ip, String action, String ukey, String uid) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("uid", uid);
        params.put("ukey", ukey);
        params.put("act", action);
        if (ip != null) {
            params.put("ip", ip);
        }
        return "http://op.xiequ.cn/IpWhiteList.aspx" + buildParams(params);
    }

    private static String buildParams(Map<String, String> params) {
        StringBuilder paramStr = new StringBuilder("?");
        params.forEach((key, value) -> paramStr.append(key).append("=").append(value).append("&"));
        String result = paramStr.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static void setConfig(HttpRequestBase http, List<Header> headers) {
        if (headers != null && !headers.isEmpty()) {
            http.setHeaders(headers.toArray(new Header[0]));
            http.addHeader("Connection", "close");
        }
        RequestConfig requestConfig = RequestConfig.custom()
                .setRedirectsEnabled(false)
                .setConnectTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .build();
        http.setConfig(requestConfig);
    }

    private static String execute(HttpRequestBase http) throws IOException {
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(http)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, Charsets.UTF_8);
            }
            throw new BusinessException("状态码异常" + statusCode);
        }
    }

    @Data
    private static class ProxyInfo {
        @JSONField(name = "IP")
        private String ip;
        @JSONField(name = "Port")
        private Integer port;
    }
}
