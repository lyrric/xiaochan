package io.github.xiaochan.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class CityCodeVO {
    /**
     * 名称
     */
    private String name;
    /**
     * code
     */
    private String code;

    private List<CityCodeVO> child;
}
