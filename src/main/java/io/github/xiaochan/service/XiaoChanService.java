package io.github.xiaochan.service;

import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.model.vo.QueryListVO;

import java.util.List;


public interface XiaoChanService {


    /**
     * 获取列表
     *
     * @param cityCode cityCode
     * @param longitude 经度
     * @param latitude 纬度
     * @param maxSize  最大数量，因为小蚕并不是按照距离排序返回的，有时候中间会穿插一些距离比较远的活动，所以这里限制一下数量
     * @return 列表
     */
    List<StoreInfo> getList(Integer cityCode, String longitude, String latitude, int maxSize);

    /**
     * 查询
     * @param queryListVO
     * @return
     */

    List<StoreInfo> query(QueryListVO queryListVO);
}
