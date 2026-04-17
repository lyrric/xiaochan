package io.github.xiaochan.controller;

import io.github.xiaochan.model.BaseResult;
import io.github.xiaochan.model.dto.NotifyConfigDTO;
import io.github.xiaochan.model.vo.NotifyConfigVO;
import io.github.xiaochan.service.NotifyConfigService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知配置控制器
 *
 * @author xiaochan
 */
@Slf4j
@RestController
@RequestMapping("/api/notify")
@Validated
public class NotifyController {

    @Resource
    private NotifyConfigService notifyConfigService;

    /**
     * 保存通知配置
     *
     * @param notifyConfigDTO 通知配置DTO
     * @return 保存结果
     */
    @PostMapping("/config")
    public BaseResult<Void> saveNotifyConfig(@Valid @RequestBody NotifyConfigDTO notifyConfigDTO) {
        log.info("保存通知配置请求: {}", notifyConfigDTO);
        notifyConfigService.addUpdateConfig(notifyConfigDTO);
        return BaseResult.ok();
    }

    /**
     * 获取所有通知配置
     *
     * @return 通知配置列表
     */
    @GetMapping("/config/list")
    public BaseResult<List<NotifyConfigVO>> getNotifyConfigList() {
        List<NotifyConfigVO> configList = notifyConfigService.listByUserId();
        return BaseResult.ok(configList);
    }

    /**
     * 删除通知配置
     *
     * @param configId 配置ID
     * @return 删除结果
     */
    @DeleteMapping("/config/{configId}")
    public BaseResult<Void> deleteNotifyConfig(@PathVariable Integer configId) {
        log.info("删除通知配置请求，配置ID: {}", configId);
        notifyConfigService.deleteById(configId);
        return BaseResult.ok();
    }
}