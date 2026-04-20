package io.github.xiaocan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.dto.NotifyHistoryQueryDTO;
import io.github.xiaocan.model.vo.StorePushedHistoryVO;
import io.github.xiaocan.service.StorePushedHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知历史记录接口
 */
@RestController
@RequestMapping(value = "/api/notify-history")
public class NotifyHistoryController {

    @Resource
    private StorePushedHistoryService notifyHistoryService;


    /**
     * 分页查询通知历史记录（当前用户）
     */
    @PostMapping("/page")
    public BaseResult<Page<StorePushedHistoryVO>> page(@RequestBody NotifyHistoryQueryDTO dto) {
        return BaseResult.ok(notifyHistoryService.pageByUser(dto));
    }

}
