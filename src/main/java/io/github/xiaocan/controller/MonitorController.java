package io.github.xiaocan.controller;

import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.dto.monitorConfigDTO;
import io.github.xiaocan.model.vo.NotifyConfigVO;
import io.github.xiaocan.service.MonitoryConfigService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控配置控制器
 *
 * @author xiaochan
 */
@Slf4j
@RestController
@RequestMapping("/api/notify")
@Validated
public class MonitorController {

    @Resource
    private MonitoryConfigService monitoryConfigService;

    /**
     * 保存监控配置
     *
     * @param monitorConfigDTO 通知配置DTO
     * @return 保存结果
     */
    @PostMapping("/config")
    public BaseResult<Void> addUpdateConfig(@Valid @RequestBody monitorConfigDTO monitorConfigDTO) {
        log.info("保存监控配置请求: {}", monitorConfigDTO);
        monitoryConfigService.addUpdateConfig(monitorConfigDTO);
        return BaseResult.ok();
    }

    /**
     * 获取所有监控配置
     *
     * @return 通知配置列表
     */
    @GetMapping("/config/list")
    public BaseResult<List<NotifyConfigVO>> getMonitorConfigList() {
        List<NotifyConfigVO> configList = monitoryConfigService.listByUserId();
        return BaseResult.ok(configList);
    }

    /**
     * 删除监控配置
     *
     * @param configId 配置ID
     * @return 删除结果
     */
    @DeleteMapping("/config/{configId}")
    public BaseResult<Void> deleteMonitorConfig(@PathVariable Integer configId) {
        log.info("删除监控配置请求，配置ID: {}", configId);
        monitoryConfigService.deleteById(configId);
        return BaseResult.ok();
    }
}