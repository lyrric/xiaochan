package io.github.xiaochan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaochan.model.entity.UserEntity;
import io.github.xiaochan.model.vo.UserVO;

public interface UserService extends IService<UserEntity> {


    UserEntity getByToken(String token);

    UserVO getVOByToken(String token);

    UserVO register(String spt, String code);

    UserEntity getByCurrentRequest();
}
