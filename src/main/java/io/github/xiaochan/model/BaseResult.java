package io.github.xiaochan.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BaseResult<T> {
    /**
     * 是否成功
     */
    private Boolean success ;

    private Integer code;
    /**
     * 错误消息
     */
    private String msg;
    /**
     * 数据
     */
    private T data;


    public static <T> BaseResult<T> ok(T data){
        return new BaseResult<>(true, 200, null, data);
    }
    public static <T> BaseResult<T> error(String msg){
        return new BaseResult<>(false, 500, msg, null);
    }
    public static <T> BaseResult<T> ok(){
        return new BaseResult<>(true, 200, null, null);
    }

    public Boolean getSuccess() {
        return success == null ? Objects.equals(code, 200) : success;
    }
}
