package io.github.xiaocan.model.vo;

import io.github.xiaocan.model.StoreInfo;
import lombok.Data;

import java.util.List;

/**
 * 小蚕-美团赏金
 * @author wangxiaodong
 * @date 2026/7/15
 */
@Data
public class XcMeituanshangjinPageVO {

    private String pagePvId;

    private String meituanPvId;

    private List<StoreInfo> storeInfos;
}
