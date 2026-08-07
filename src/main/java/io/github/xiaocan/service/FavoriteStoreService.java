package io.github.xiaocan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.FavoriteStoreQueryDTO;
import io.github.xiaocan.model.dto.RemoveFavoriteDTO;
import io.github.xiaocan.model.dto.SaveFavoriteDTO;
import io.github.xiaocan.model.entity.FavoriteStoreEntity;
import io.github.xiaocan.model.vo.StorePushedHistoryVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FavoriteStoreService {

    /**
     * 保存收藏门店
     * @param dto 收藏信息
     * @return 收藏记录ID
     */
    Long saveFavorite(SaveFavoriteDTO dto);

    /**
     * 取消收藏
     * @param dto 收藏标识信息
     */
    void removeFavorite(RemoveFavoriteDTO dto);

    /**
     * 根据收藏记录ID取消收藏
     * @param favoriteId 收藏记录ID
     */
    void removeFavoriteById(Long favoriteId);

    /**
     * 查询收藏门店的实时信息
     * @param dto 查询条件
     * @return 门店分页列表
     */
    Page<StoreInfo> queryFavoriteStores(FavoriteStoreQueryDTO dto);

    /**
     * 批量查询收藏门店ID
     * @param userId 用户ID
     * @param locationId 位置ID
     * @param uniqIds 门店唯一ID集合
     * @return key: uniqId, value: FavoriteStoreEntity
     */
    Map<String, FavoriteStoreEntity> batchQueryFavoriteIds(Integer userId, Long locationId, Collection<String> uniqIds);

    /**
     * 填充StoreInfo列表的favoriteId
     * @param storeInfos 门店信息列表
     * @param userId 用户ID
     * @param locationId 位置ID
     */
    void fillFavoriteIds(List<StoreInfo> storeInfos, Integer userId, Long locationId);

    /**
     * 填充StorePushedHistoryVO列表的favoriteId（支持多locationId）
     * @param voList 推送历史VO列表
     * @param userId 用户ID
     */
    void fillFavoriteIdsForPushedHistory(List<StorePushedHistoryVO> voList, Integer userId);
}
