package io.github.xiaocan.service;

import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.vo.WmPageVO;

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
}
