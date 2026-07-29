package io.github.xiaocan.service;

import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.StoreSearchDTO;

import java.util.List;

/**
 * 通用门店搜索服务
 */
public interface StoreSearchService {

    /**
     * 聚合搜索小蚕满减、小蚕美团赏金、歪麦三个平台的门店
     * <p>
     * storeTypeEnum 为 XC_MANJIAN 时跳过小蚕满减搜索，为 XC_MTSJ 时跳过小蚕美团赏金搜索，
     * 并过滤掉与 storeTypeEnum + uniqId 相同的来源门店自身
     *
     * @param dto 请求参数
     * @return 聚合后的门店列表，不分页
     */
    List<StoreInfo> search(StoreSearchDTO dto);
}
