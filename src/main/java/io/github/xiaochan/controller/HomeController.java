package io.github.xiaochan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 主页控制器
 * 用于处理主页访问
 */
@Controller
public class HomeController {

    /**
     * 主页重定向到门店管理页面
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }
}