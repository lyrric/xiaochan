package io.github.xiaochan;

import com.alibaba.fastjson2.JSONObject;
import io.github.xiaochan.enums.LocationEnum;
import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.StoreInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

public class XiaochanTest {


    @Test
    public void test() {
        List<StoreInfo> list = new XiaochanHttp().getList(LocationEnum.WXD_WORK);
        System.out.println(JSONObject.toJSONString(list));
    }
}
