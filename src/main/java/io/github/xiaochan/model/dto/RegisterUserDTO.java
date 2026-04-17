package io.github.xiaochan.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author wangxiaodong
 * @date 2026/4/15
 */
@Data
public class RegisterUserDTO {
    @NotEmpty
    private String spt;
    @NotEmpty
    private String code;
}
