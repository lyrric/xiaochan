package io.github.xiaocan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.dto.TaskExecHistoryQueryDTO;
import io.github.xiaocan.model.vo.TaskExecHistoryVO;
import io.github.xiaocan.service.TaskExecHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务执行历史记录接口
 * @author wangxiaodong
 * @date 2026/4/20
 */
@RestController
@RequestMapping(value = "/api/task-exec-history")
public class TaskExecHistoryController {

    @Resource
    private TaskExecHistoryService taskExecHistoryService;

    /**
     * 分页查询任务执行历史记录
     */
    @PostMapping("/page")
    public BaseResult<Page<TaskExecHistoryVO>> page(@RequestBody TaskExecHistoryQueryDTO dto) {
        return BaseResult.ok(taskExecHistoryService.pageByUser(dto));
    }

}
