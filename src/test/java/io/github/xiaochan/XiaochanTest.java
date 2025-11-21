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
        StoreInfo storeInfo = new XiaochanHttp().GetStorePromotionDetail(74213510);
        log.info(storeInfo.toString());
    }

    @Test
    public void test1() {
    }
}
