package io.github.xiaocan.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionItemVO {
    /**
     * 省市区拼接名称，空格分隔
     */
    private String name;
    /**
     * 区县编码
     */
    private String code;
}
