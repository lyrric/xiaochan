package io.github.xiaochan;

import com.alibaba.fastjson2.JSONObject;
import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.StoreInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

public class XiaochanTest {


    @Test
    public void test() {
        XiaochanHttp xiaochanHttp = new XiaochanHttp();
        List<StoreInfo> list = xiaochanHttp.getList(Location.builder().cityCode(510107)
                .latitude("30.5702")
                .longitude("104.064758")
                .build());
        System.out.println(JSONObject.toJSONString(list));
    }
}
