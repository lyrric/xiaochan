package io.github.xiaocan.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门店关键字监控扩展配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreKeywordExtNotifyConfig extends AbstractExtNotifyConfig {

    /**
     * 门店关键字
     */
    @NotEmpty
    private String keyword;

    /**
     * 是否限制距离（超过3500米的门店过滤掉），默认true
     */
    private Boolean limitDistance = true;
}
