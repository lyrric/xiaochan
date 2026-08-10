package io.github.xiaochan;

/**
 * @author wangxiaodong
 * @date 2026/8/10
 */

import io.github.xiaocan.http.WmmtHttp;
import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.vo.WmPageVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class WaiMaiTest {



    @Test
    public void testGetStoreList(){
        WmmtShopListDTO dto = new WmmtShopListDTO();
        dto.setLongitude("");
        dto.setLatitude("");
        WmPageVO wmPageVO = WmmtHttp.getShopList("", "成都市", dto);
    }
}
