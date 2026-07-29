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
     * 上一页pvid
     */
    private String pagePvId;

    /**
     * 店铺信息列表
     */
    private List<StoreInfo> storeInfos;
}
