package io.github.xiaocan.model.vo;

import io.github.xiaocan.model.StoreInfo;
import lombok.Data;

import java.util.List;

/**
 * 歪麦翻页
 * @author auto
 * @date 2026/7/28
 */
@Data
public class WmPageVO {

    /**
     * 分页游标数据，接口原样返回给前端，前端翻页时原样传回，不做任何修改
     */
    private Object scrollPageData;

    /**
     * 店铺信息列表
     */
    private List<StoreInfo> storeInfos;
}
