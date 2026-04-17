package io.github.xiaochan.service;

import io.github.xiaochan.model.dto.LocationDTO;
import io.github.xiaochan.model.entity.LocationEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.xiaochan.model.vo.LocationVO;

import java.util.List;

public interface LocationService extends IService<LocationEntity> {

    /**
     * 新增地址
     * @param locationDTO 地址信息
     * @return 地址ID
     */
    Integer add(LocationDTO locationDTO);

    /**
     * 删除地址
     * @param id 地址ID
     * @return 是否成功
     */
    void delete(String id);

    /**
     * 查询所有地址
     * @return 地址列表
     */
    List<LocationVO> getAll();
}
