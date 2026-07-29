package io.github.xiaocan.service;

import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import io.github.xiaocan.model.vo.WmPageVO;

import java.util.List;

/**
 * 歪麦（waimaimingtang）门店服务
 */
public interface WmmtService {

    /**
     * 获取歪麦门店列表（支持游标翻页）
     *
     * @param dto 请求参数
     * @return 门店列表 + 下一页游标
     */
    WmPageVO getShopList(WmmtShopListDTO dto);

    /**
     * 获取歪麦门店活动信息，列表混合了满减和美团赏金，按门店类型过滤
     *
     * @param keyword 门店名模糊搜索，为空时拉取全量列表
     */
    List<StoreInfo> fetchWmStoreInfos(StoreTypeEnum storeType, LocationEntity location, String keyword);
}
