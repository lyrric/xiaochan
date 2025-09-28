package io.github.xiaochan.model;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 最大金额差提醒
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MaxDiffPriceExtNotifyConfig extends AbstractExtNotifyConfig{

    /**
     * 金额差，大于等于1
     * 金额差=返现门槛-返现金额
     */
    @Min(value = 1)
    private BigDecimal maxDiffPrice;
    /**
     * 同一个活动多少天内不再通知
     * 大于等于1
     */
    @Min(value = 1)
    private Integer idleDay;

}
