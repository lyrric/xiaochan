package io.github.xiaocan.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 同名门店查询及推送统计。
 */
@Data
@AllArgsConstructor
public class SameNameStorePushResultVO {

    /**
     * 本次获取的美团满减门店数量。
     */
    private int manjianStoreCount;

    /**
     * 本次获取的美团比例返门店数量。
     */
    private int rebateStoreCount;

    /**
     * 成功推送的门店数量，同名门店的多个活动只计一次。
     */
    private int pushedStoreCount;
}
