package io.github.xiaocan.service.impl;

import io.github.xiaocan.http.WmmtHttp;
import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.vo.WmPageVO;
import io.github.xiaocan.service.StoreInventoryHistoryService;
import io.github.xiaocan.service.WmmtService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 歪麦（waimaimingtang）门店服务实现
 */
@Service
@Slf4j
public class WmmtServiceImpl implements WmmtService {

    /**
     * 歪麦接口固定使用长沙市
     */
    private static final String CITY = "长沙市";

    @Resource
    private StoreInventoryHistoryService storeInventoryHistoryService;

    @Override
    public WmPageVO getShopList(WmmtShopListDTO dto) {
        WmPageVO vo = WmmtHttp.getShopList(null, CITY, dto);
        if (vo.getStoreInfos() != null && !vo.getStoreInfos().isEmpty()) {
            storeInventoryHistoryService.insertBatch(vo.getStoreInfos());
        }
        return vo;
    }
}
