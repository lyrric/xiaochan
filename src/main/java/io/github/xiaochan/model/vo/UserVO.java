package io.github.xiaochan.model.vo;

import io.github.xiaochan.model.entity.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVO {

    private Integer id;

    private String token;

    private String spt;


    public static UserVO fromEntity(UserEntity entity) {
        UserVO userVO = new UserVO();
        userVO.id = entity.getId();
        userVO.token = entity.getToken();
        userVO.spt = entity.getSpt();
        return userVO;
    }
}
