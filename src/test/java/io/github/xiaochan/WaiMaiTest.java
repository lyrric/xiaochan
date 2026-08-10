package io.github.xiaochan;

/**
 * @author wangxiaodong
 * @date 2026/8/10
 */

import io.github.xiaocan.http.WmmtHttp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class WaiMaiTest {



    @Test
    public void testGetStoreList(){
        WmmtHttp.getShopList("", "传给你都市", null);
    }
}
