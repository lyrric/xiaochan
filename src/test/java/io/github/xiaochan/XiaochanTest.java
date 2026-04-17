package io.github.xiaochan;

import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.StoreInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class XiaochanTest {


    @Test
    void test() {
        StoreInfo storeInfo = new XiaochanHttp().GetStorePromotionDetail(74213510);
        log.info(storeInfo.toString());
    }

}
