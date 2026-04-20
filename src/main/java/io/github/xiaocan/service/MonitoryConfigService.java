package io.github.xiaocan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaocan.model.dto.monitorConfigDTO;
import io.github.xiaocan.model.entity.MonitorConfigEntity;
import io.github.xiaocan.model.enums.MonitorConfigStatusEnums;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import io.github.xiaocan.model.vo.NotifyConfigVO;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author xiaochan
 */
public interface MonitoryConfigService extends IService<MonitorConfigEntity> {


    List<NotifyConfigVO> listByUserId();

    List<MonitorConfigEntity> list(MonitorTypeEnums type, MonitorConfigStatusEnums enums);

    void addUpdateConfig(monitorConfigDTO dto);

    void updateConfig(int id, MonitorConfigStatusEnums statusEnums, String remark);

    void deleteById(Integer configId);
}