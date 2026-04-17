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
public class MinimumPayExtNotifyConfig extends AbstractExtNotifyConfig{

    /**
     * 最小实付，大于等于1
     * 最小实付=返现门槛-返现金额
     */
    @Min(value = 1)
    private BigDecimal minimumPay;
}
