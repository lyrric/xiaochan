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
}
