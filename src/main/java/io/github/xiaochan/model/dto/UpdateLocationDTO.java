package io.github.xiaochan.model.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址更新DTO
 * 只允许修改spt和pushSwitch字段
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLocationDTO {
    
    /**
     * 推送参数
     */
    private String spt;
    
    /**
     * 是否推送
     */
    private Boolean pushSwitch;
    
    /**
     * 自定义校验：至少需要提供一个可更新的字段
     */
    @AssertTrue(message = "至少需要提供一个可更新的字段（spt或pushSwitch）")
    public boolean isAtLeastOneFieldProvided() {
        return spt != null || pushSwitch != null;
    }
}