package io.github.xiaocan.service.impl;

import io.github.xiaocan.constant.StoreConstant;
import io.github.xiaocan.http.WmmtHttp;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import io.github.xiaocan.model.vo.WmPageVO;
import io.github.xiaocan.service.FavoriteStoreService;
import io.github.xiaocan.service.StoreInventoryHistoryService;
import io.github.xiaocan.service.UserService;
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

    @Resource
    private UserService userService;

    @Resource
    private FavoriteStoreService favoriteStoreService;

    @Override
    public WmPageVO getShopList(WmmtShopListDTO dto) {
        UserEntity currentUser = userService.getByCurrentRequest();
        String waimaiToken = currentUser.getWaimaiToken();
        WmPageVO vo = getShopList(waimaiToken, dto);
        if (dto.getLocationId() != null && vo.getStoreInfos() != null) {
            favoriteStoreService.fillFavoriteIds(vo.getStoreInfos(), currentUser.getId(), dto.getLocationId());
        }
        return vo;
    }

    /**
     * 获取歪麦门店列表，使用指定的歪麦token
     *
     * @param waimaiToken 歪麦token
     * @param dto         请求参数
     * @return 门店列表 + 下一页游标
     */
    public WmPageVO getShopList(String waimaiToken, WmmtShopListDTO dto) {
        WmPageVO vo = WmmtHttp.getShopList(waimaiToken, CITY, dto);
        if (vo.getStoreInfos() != null && !vo.getStoreInfos().isEmpty()) {
            storeInventoryHistoryService.insertBatch(vo.getStoreInfos());
        }
        return vo;
    }

    @Override
    public List<StoreInfo> fetchWmStoreInfos(StoreTypeEnum storeType, LocationEntity location, String keyword) {
        UserEntity user = userService.getById(location.getUserId());
        String waimaiToken = user.getWaimaiToken();
        WmmtShopListDTO dto = new WmmtShopListDTO();
        dto.setName(keyword);
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        List<StoreInfo> storeInfos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            WmPageVO vo = getShopList(waimaiToken, dto);
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
                //距离解析失败为null时按0处理
                .filter(t -> (t.getDistance() == null ? 0 : t.getDistance()) > StoreConstant.MAX_DISTANCE)
                .count();
        //排除距离为0或null的数据
        long validCount = list.stream()
                .filter(t -> t.getDistance() != null && t.getDistance() > 0)
                .count();
        //有一半的店距离超过MAX_DISTANCE，则不再查找下一页
        return overDistanceCount <= (validCount / 2);
    }
}
