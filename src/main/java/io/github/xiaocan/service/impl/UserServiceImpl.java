package io.github.xiaocan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.mapper.UserMapper;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.vo.UserVO;
import io.github.xiaocan.service.SptService;
import io.github.xiaocan.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Resource
    private SptService sptService;

    @Override
    public UserVO register(String spt, String code) {
        if (!sptService.checkSptCode(spt, code)) {
            throw new BusinessException("验证码错误");
        }
        if (this.lambdaQuery().eq(UserEntity::getSpt, spt).oneOpt().isPresent()) {
            throw new BusinessException("该spt已注册");
        }
        UserEntity user = new UserEntity();
        user.setSpt(spt);
        user.setToken(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        save(user);
        return UserVO.fromEntity(user);
    }

    @Override
    public UserEntity getByToken(String token) {
        return this.lambdaQuery().eq(UserEntity::getToken, token).oneOpt().orElseThrow(() -> new BusinessException(401, "用户不存在"));
    }

    @Override
    public UserVO getVOByToken(String token) {
        return this.lambdaQuery().eq(UserEntity::getToken, token).oneOpt().map(UserVO::fromEntity).orElseThrow(() -> new BusinessException(401, "用户不存在"));
    }

    @Override
    public UserEntity getByCurrentRequest() {
        //获取当前http请求上下文
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new BusinessException("未找到请求上下文");
        }
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");
        if(StringUtils.hasText(token)) {
            return getByToken(token);
        }
        throw new BusinessException(401, "无效token");
    }
}
