package io.github.xiaocan.service;

import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.XcMeituanshangjinDTO;
import io.github.xiaocan.model.vo.QueryListVO;
import io.github.xiaocan.model.vo.XcMeituanshangjinPageVO;

import java.util.List;


public interface XiaoChanService {

    /**
     * 查询当前用户监控位置的小蚕美团满减和赏金活动，按同名门店匹配、排序并立即推送。
     *
     * @param configId 当前用户的最小实付、小蚕满减监控配置ID
     * @return 推送的门店数，同名门店的多个活动只计一次
     */
    int pushSameNameStores(Integer configId);


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
     * 获取列表、分页方式
     *
     * @param cityCode cityCode
     * @param longitude 经度
     * @param latitude 纬度
     * @param offset  offset
     * @return 列表
     */
    List<StoreInfo> getListByOffset(Integer cityCode, String longitude, String latitude, int offset);

    /**
     * 获取列表
     * <p>
     * 只返回15个结果、不分页
     * @param keyword   关键字
     * @param cityCode  cityCode
     * @param longitude 经度
     * @param latitude  纬度
     * @return 列表
     */
    List<StoreInfo> searchList(String keyword, Integer cityCode, String longitude, String latitude);


    /**
     * 查询
     * @param queryListVO
     * @return
     */

    List<StoreInfo> query(QueryListVO queryListVO);

    /**
     * 获取比例返页面数据
     * @param dto
     * @return
     */
    XcMeituanshangjinPageVO getXcMeituanshangjinPageVO(XcMeituanshangjinDTO dto);
}
