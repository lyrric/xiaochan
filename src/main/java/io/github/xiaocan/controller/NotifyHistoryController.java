package io.github.xiaocan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.dto.NotifyHistoryQueryDTO;
import io.github.xiaocan.model.vo.StorePushedHistoryVO;
import io.github.xiaocan.service.MessageBatchRecordService;
import io.github.xiaocan.service.StorePushedHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知历史记录接口
 */
@RestController
@RequestMapping(value = "/api/notify-history")
public class NotifyHistoryController {

    @Resource
    private StorePushedHistoryService notifyHistoryService;
    @Resource
    private MessageBatchRecordService messageBatchRecordService;


    /**
     * 分页查询通知历史记录（当前用户）
     */
    @PostMapping("/page")
    public BaseResult<Page<StorePushedHistoryVO>> page(@RequestBody NotifyHistoryQueryDTO dto) {
        return BaseResult.ok(notifyHistoryService.pageByUser(dto));
    }

    /**
     * 根据消息批次记录ID查询关联的门店推送历史
     * @param msgId 消息记录ID
     * @return 门店推送历史列表
     */
    @GetMapping("/pushed-history")
    public BaseResult<List<StorePushedHistoryVO>> getPushedHistoryByRecordId(@RequestParam Long msgId) {
        return BaseResult.ok(messageBatchRecordService.getPushedHistoryByRecordId(msgId));
    }

}
