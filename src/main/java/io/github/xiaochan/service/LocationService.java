package io.github.xiaochan.service;

import io.github.xiaochan.model.Location;

import java.util.List;

public interface LocationService {

    /**
     * 新增地址
     * @param location 地址信息
     * @return 地址ID
     */
    String add(Location location);

    /**
     * 删除地址
     * @param id 地址ID
     * @return 是否成功
     */
    boolean delete(String id);

    /**
     * 修改地址（只能修改spt和pushSwitch）
     * @param id 地址ID
     * @param spt 推送参数
     * @param pushSwitch 是否推送
     * @return 是否成功
     */
    boolean update(String id, String spt, Boolean pushSwitch);
    /**
     * 查询所有地址
     * @return 地址列表
     */
    List<Location> getAll();
}
