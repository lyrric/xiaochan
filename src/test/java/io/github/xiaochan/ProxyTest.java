package io.github.xiaochan;

import io.github.xiaocan.config.XieQuProxyConfig;
import io.github.xiaocan.http.XiaochanHttp;
import io.github.xiaocan.http.XieQuHttp;
import io.github.xiaocan.model.HttpProxyInfo;
import io.github.xiaocan.model.StoreInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author lyrric
 * @date 2026/10/15
 */
@Slf4j
public class ProxyTest {


    @Test
    public void testProxy() throws IOException {
        XieQuProxyConfig xieQuProxyConfig = new XieQuProxyConfig();
        xieQuProxyConfig.setUkey("");
        String s = XieQuHttp.addIp("127.0.0.1", xieQuProxyConfig);
        System.out.println(s);
   /*     String key = "";
        List<HttpProxyInfo> ipList = XieQuHttp.getIp(1, key);
        HttpProxyInfo httpProxyInfo = ipList.get(0);
        System.out.println(httpProxyInfo);
        List<StoreInfo> list = XiaochanHttp.getList(510116, "104.063049", "30.569082", 0, httpProxyInfo);
        System.out.println(list.size());*/


    }
}
