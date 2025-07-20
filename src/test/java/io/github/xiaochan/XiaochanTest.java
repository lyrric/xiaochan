package io.github.xiaochan;

import com.alibaba.fastjson2.JSONObject;
import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.HttpProxyInfo;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.proxy.XieQuHttpProxy;
import org.junit.jupiter.api.Test;

import java.util.List;

public class XiaochanTest {


    @Test
    public void test() {
        XieQuHttpProxy xieQuHttpProxy = new XieQuHttpProxy();
        xieQuHttpProxy.refreshIp();
        HttpProxyInfo httpProxyInfo = xieQuHttpProxy.getOne();
        XiaochanHttp xiaochanHttp = new XiaochanHttp();
        List<StoreInfo> list = xiaochanHttp.getList(Location.builder().cityCode(510116)
                .longitude("104.00647")
                .latitude("30.57862").build(), 0, httpProxyInfo);
        System.out.println(JSONObject.toJSONString(list));
    }
}
