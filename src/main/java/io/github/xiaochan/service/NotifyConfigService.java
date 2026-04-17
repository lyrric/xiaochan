package io.github.xiaochan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaochan.model.dto.NotifyConfigDTO;
import io.github.xiaochan.model.entity.NotifyConfigEntity;
import io.github.xiaochan.model.enums.NotifyConfigStatusEnums;
import io.github.xiaochan.model.enums.NotifyTypeEnums;
import io.github.xiaochan.model.vo.NotifyConfigVO;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author xiaochan
 */
public interface NotifyConfigService extends IService<NotifyConfigEntity> {


    List<NotifyConfigVO> listByUserId();

    List<NotifyConfigEntity> list(NotifyTypeEnums type, NotifyConfigStatusEnums enums);

    void addUpdateConfig(NotifyConfigDTO dto);

    void updateConfig(int id, NotifyConfigStatusEnums statusEnums, String remark);

    void deleteById(Integer configId);
}