package io.github.xiaocan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.vo.UserVO;

public interface UserService extends IService<UserEntity> {


    UserEntity getByToken(String token);

    UserVO getVOByToken(String token);

    UserVO register(String spt, String code);

    UserEntity getByCurrentRequest();
}
