package io.github.xiaocan.service.impl;

import io.github.xiaocan.http.WmmtHttp;
import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.vo.WmPageVO;
import io.github.xiaocan.service.StoreInventoryHistoryService;
import io.github.xiaocan.service.WmmtService;
import io.github.xiaocan.utils.CityCodeUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 歪麦（waimaimingtang）门店服务实现
 */
@Service
@Slf4j
public class WmmtServiceImpl implements WmmtService {

    @Resource
    private StoreInventoryHistoryService storeInventoryHistoryService;

    @Override
    public WmPageVO getShopList(WmmtShopListDTO dto) {
        String city = CityCodeUtil.getCityName(dto.getCityCode());
        WmPageVO vo =WmmtHttp.getShopList(null, city, dto);
        if (vo.getStoreInfos() != null && !vo.getStoreInfos().isEmpty()) {
            storeInventoryHistoryService.insertBatch(vo.getStoreInfos());
        }
        return vo;
    }
}
