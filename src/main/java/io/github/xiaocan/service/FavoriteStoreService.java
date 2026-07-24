package io.github.xiaocan.service;

import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.FavoriteStoreQueryDTO;
import io.github.xiaocan.model.dto.RemoveFavoriteDTO;
import io.github.xiaocan.model.dto.SaveFavoriteDTO;
import io.github.xiaocan.model.vo.FavoriteStoreVO;

import java.util.List;

public interface FavoriteStoreService {

    /**
     * 保存收藏门店
     * @param dto 收藏信息
     */
    void saveFavorite(SaveFavoriteDTO dto);

    /**
     * 取消收藏
     * @param dto 收藏标识信息
     */
    void removeFavorite(RemoveFavoriteDTO dto);

    /**
     * 查询当前用户的收藏记录
     * @param locationId 地址ID
     * @param storeType 门店类型
     * @return 收藏记录列表
     */
    List<FavoriteStoreVO> listFavorites(Long locationId, String storeType);

    /**
     * 查询收藏门店的实时信息
     * @param dto 查询条件
     * @return 门店列表
     */
    List<StoreInfo> queryFavoriteStores(FavoriteStoreQueryDTO dto);
}
