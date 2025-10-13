package io.github.xiaochan.controller;

import io.github.xiaochan.config.BusinessException;
import io.github.xiaochan.model.BaseResult;
import io.github.xiaochan.model.NotifyConfig;
import io.github.xiaochan.model.dto.NotifyConfigDTO;
import io.github.xiaochan.service.NotifyService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jodd.util.StringUtil;
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
    private NotifyService notifyService;

    /**
     * 保存通知配置
     *
     * @param notifyConfigDTO 通知配置DTO
     * @return 保存结果
     */
    @PostMapping("/config")
    public BaseResult<String> saveNotifyConfig(@Valid @RequestBody NotifyConfigDTO notifyConfigDTO) {
        log.info("保存通知配置请求: {}", notifyConfigDTO);
        if (StringUtil.isBlank(notifyConfigDTO.getLocation().getSpt())) {
            throw new BusinessException("推送SPT不能为空");
        }
        String configId = notifyService.saveNotifyConfig(notifyConfigDTO);
        return BaseResult.ok(configId);
    }

    /**
     * 获取所有通知配置
     *
     * @return 通知配置列表
     */
    @GetMapping("/config/list")
    public BaseResult<List<NotifyConfig>> getNotifyConfigList() {
        List<NotifyConfig> configList = notifyService.getNotifyConfigList();
        return BaseResult.ok(configList);
    }

    /**
     * 删除通知配置
     *
     * @param configId 配置ID
     * @return 删除结果
     */
    @DeleteMapping("/config/{configId}")
    public BaseResult<String> deleteNotifyConfig(@PathVariable String configId) {
        log.info("删除通知配置请求，配置ID: {}", configId);
        notifyService.deleteNotifyConfig(configId);
        return BaseResult.ok();
    }
}