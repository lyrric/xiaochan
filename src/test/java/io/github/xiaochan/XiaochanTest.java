package io.github.xiaochan;

import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.service.impl.XiaoChanServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class XiaochanTest {


    @Test
    public void test() {
        List<StoreInfo> list = new XiaoChanServiceImpl().getList(
                510116,
                "30.57862",
                "104.00647",
                150);
        log.info("门店数量:{}", list.size());
    }

    @Test
    public void test1() {
        new XiaochanHttp().getClientCfg();
        new XiaochanHttp().meituanShangjinGetPoiList("30.574471", "103.923767", 510116);
    }
}
