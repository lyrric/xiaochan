package io.github.xiaocan.controller;

import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.dto.RegisterUserDTO;
import io.github.xiaocan.model.vo.UserVO;
import io.github.xiaocan.service.SptService;
import io.github.xiaocan.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author wangxiaodong
 * @date 2026/4/15
 */
@RestController
@RequestMapping(value = "/api/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private SptService sptService;

    /**
     * 用户注册接口
     * @return 注册成功返回用户信息（包含token）
     */
    @PostMapping("/register")
    public BaseResult<UserVO> register(@RequestBody RegisterUserDTO dto) {
        return BaseResult.ok(userService.register(dto.getSpt(), dto.getCode()));
    }
    /**
     * 发送验证码接口
     * @param spt 用户SPT
     * @return 发送结果
     */
    @PostMapping("/sendSptCode")
    public BaseResult<Void> sendSptCode(@RequestParam String spt) {
        sptService.sendSptCode(spt);
        return BaseResult.ok();
    }

    /**
     * 登录接口
     * @param token 验证码
     * @return 登录成功返回用户信息（包含token）
     */
    @GetMapping("/getUserInfo")
    public BaseResult<UserVO> getUserInfo(@RequestParam String token) {
        return BaseResult.ok(userService.getVOByToken(token));
    }
}
