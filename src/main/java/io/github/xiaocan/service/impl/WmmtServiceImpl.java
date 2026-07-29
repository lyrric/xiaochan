package io.github.xiaocan.service.impl;

import io.github.xiaocan.constant.StoreConstant;
import io.github.xiaocan.http.WmmtHttp;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import io.github.xiaocan.model.vo.WmPageVO;
import io.github.xiaocan.service.StoreInventoryHistoryService;
import io.github.xiaocan.service.WmmtService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<StoreInfo> fetchWmStoreInfos(StoreTypeEnum storeType, LocationEntity location, String keyword) {
        WmmtShopListDTO dto = new WmmtShopListDTO();
        dto.setName(keyword);
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        List<StoreInfo> storeInfos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            WmPageVO vo = getShopList(dto);
            if (vo.getStoreInfos() == null || vo.getStoreInfos().isEmpty()) {
                break;
            }
            storeInfos.addAll(vo.getStoreInfos());
            if (storeInfos.size() >= StoreConstant.MAX_SIZE) {
                break;
            }
            if (!hasNext(vo.getStoreInfos())) {
                break;
            }
            if (vo.getScrollPageData() == null) {
                break;
            }
            dto.setScrollPageData(vo.getScrollPageData());
        }
        return storeInfos.stream()
                .filter(storeInfo -> storeInfo.getStoreTypeEnum() == storeType)
                .toList();
    }

    private boolean hasNext(List<StoreInfo> list) {
        long overDistanceCount = list.stream()
                .filter(t -> t.getDistance() > StoreConstant.MAX_DISTANCE)
                .count();
        int size = list.size();
        //有一半的店距离超过MAX_DISTANCE，则不再查找下一页
        return overDistanceCount <= (size / 2);
    }
}
