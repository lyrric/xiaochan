package io.github.xiaochan.service.impl;

import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.model.vo.QueryListVO;
import io.github.xiaochan.service.XiaoChanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class XiaoChanServiceImpl implements XiaoChanService {

    private final XiaochanHttp xiaochanHttp = new XiaochanHttp();

    private static final int DEFAULT_PAGE_SIZE = 30;
    /**
     * 门店最大距离
     */
    private static final int MAX_SIZE = 300;
    /**
     * 门店最长距离
     */
    private static final int MAX_DISTANCE = 4000;

    @Resource
    @Lazy
    private XiaoChanService xiaoChanService;

    @Override
    public List<StoreInfo> query(QueryListVO queryListVO) {
        List<StoreInfo> result;
        Integer pageNum = queryListVO.getPageNum();
        pageNum = pageNum == null ? 0 : pageNum - 1;

        if (StringUtils.isNotBlank(queryListVO.getName())) {
            //搜索走专门的接口
            if (pageNum > 0) {
                return Collections.emptyList();
            }
            result = xiaoChanService.searchList(queryListVO.getName(), queryListVO.getCityCode(), queryListVO.getLongitude(), queryListVO.getLatitude());
        }else{
            if (queryListVO.getOrderType() != null && queryListVO.getOrderType() != 1) {
                //排序不为空，则获取所有活动再排序过滤
                if (pageNum > 0) {
                    return Collections.emptyList();
                }
                result = xiaoChanService.getList(queryListVO.getCityCode(), queryListVO.getLongitude(), queryListVO.getLatitude(), MAX_SIZE);
                sortStoreList(result, queryListVO.getOrderType());
                result = filter(result, queryListVO);
            }else{
                //排序为空，则走官方分页接口
                result = xiaoChanService.getListByOffset(queryListVO.getCityCode(), queryListVO.getLongitude(), queryListVO.getLatitude(), queryListVO.getPageSize() * pageNum);
            }
        }
        return result;
    }

    @Override
    public List<StoreInfo> searchList(String keyword, Integer cityCode, String longitude, String latitude) {
        return xiaochanHttp.searchList(keyword, cityCode, longitude, latitude, 0, 15);
    }


    @Override
    @Cacheable(cacheNames = "xiaoChan", key = "#cityCode+#longitude+#latitude+#maxSize")
    public List<StoreInfo> getList(Integer cityCode, String longitude, String latitude, int maxSize){
        log.info("请求小产列表，城市: {}, 经度: {}, 纬度: {}, 最大数量: {}", cityCode, longitude, latitude, maxSize);
        int offset = 0;
        List<StoreInfo> result = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            List<StoreInfo> list = doGetList(cityCode, longitude, latitude, offset);
            result.addAll(list);
            if (result.size() >= maxSize) {
                break;
            }
            if (!hasNext(list)) {
                break;
            }
            offset += DEFAULT_PAGE_SIZE;
        }
        return result;
    }

    @Override
    public List<StoreInfo> getListByOffset(Integer cityCode, String longitude, String latitude, int offset) {
        return doGetList(cityCode, longitude, latitude, offset);
    }
    private boolean hasNext(List<StoreInfo> list){
        if (list.size() < DEFAULT_PAGE_SIZE) {
            return false;
        }
        long overDistanceCount = list.stream()
                .filter(t -> t.getDistance() > MAX_DISTANCE)
                .count();
        int size = list.size();
        //有一半的店距离超过MAX_DISTANCE，则不再查找下一页
        return overDistanceCount <= (size / 2);

    }



    private List<StoreInfo> doGetList(Integer cityCode, String longitude, String latitude, int offset){
        try {
            List<StoreInfo> list = xiaochanHttp.getList(cityCode, longitude, latitude, offset);
            return list;
        } catch (Exception e) {
            log.error("请求小产列表时发生错误 ",e);
        }
        return Collections.emptyList();
    }


    /**
     * 根据排序类型对门店列表进行排序
     * @param list 门店列表
     * @param orderType 排序类型，1：默认，2：返现金额排序，3：返现比例排序
     */
    private void sortStoreList(List<StoreInfo> list, Integer orderType) {
        if (orderType == null || orderType == 1) {
            // 默认排序，不处理
            list.sort(Comparator.comparing(StoreInfo::getDistance));
        }else if (orderType == 2) {
            // 按返现金额倒序排序
            list.sort(Comparator.comparing(StoreInfo::getRebatePrice, Comparator.nullsLast(Comparator.reverseOrder())));
        } else if (orderType == 3) {
            // 按返现比例倒序排序
            list.sort(Comparator.comparing(this::calculateRebateRatio, Comparator.nullsLast(Comparator.reverseOrder())));
        }
    }

    private List<StoreInfo> filter(List<StoreInfo> list, QueryListVO queryListVO) {
        if (StringUtils.isNotBlank(queryListVO.getName())) {
            list = list.stream().filter(storeInfo -> storeInfo.getName().contains(queryListVO.getName())).toList();
        }
        
        // 只看可抢过滤（剩余数量大于0且活动时间在当前时间范围内）
        if (queryListVO.getOnlyAvailable() != null && queryListVO.getOnlyAvailable()) {
            list = list.stream().filter(storeInfo -> {
                // 检查剩余数量
                boolean hasStock = storeInfo.getLeftNumber() != null && storeInfo.getLeftNumber() > 0;
                // 检查活动时间
                boolean inActiveTime = isInActiveTime(storeInfo);
                
                return hasStock && inActiveTime;
            }).toList();
        }
        
        return list;
    }

    /**
     * 判断门店活动时间是否在当前时间范围内
     * @param storeInfo 门店信息
     * @return true表示在活动时间内，false表示不在活动时间内
     */
    private boolean isInActiveTime(StoreInfo storeInfo) {
        if (StringUtils.isBlank(storeInfo.getStartTime()) || StringUtils.isBlank(storeInfo.getEndTime())) {
            // 如果没有设置活动时间，默认认为可用
            return true;
        }
        
        try {
            LocalTime now = LocalTime.now();
            LocalTime startTime = LocalTime.parse(storeInfo.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = LocalTime.parse(storeInfo.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
            
            // 处理跨日的情况（如：23:00-02:00）
            if (endTime.isBefore(startTime)) {
                // 跨日情况：当前时间在开始时间之后或结束时间之前
                return now.isAfter(startTime) || now.isBefore(endTime) || now.equals(startTime) || now.equals(endTime);
            } else {
                // 正常情况：当前时间在开始和结束时间之间
                return (now.isAfter(startTime) || now.equals(startTime)) && (now.isBefore(endTime) || now.equals(endTime));
            }
        } catch (Exception e) {
            log.warn("解析活动时间失败，门店: {}, 开始时间: {}, 结束时间: {}", storeInfo.getName(), storeInfo.getStartTime(), storeInfo.getEndTime(), e);
            // 解析失败时默认认为可用
            return true;
        }
    }

    /**
     * 计算返现比例
     * @param storeInfo 门店信息
     * @return 返现比例 (rebatePrice/price)
     */
    private BigDecimal calculateRebateRatio(StoreInfo storeInfo) {
        if (storeInfo.getPrice() == null || storeInfo.getRebatePrice() == null || storeInfo.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return storeInfo.getRebatePrice().divide(storeInfo.getPrice(), 4, RoundingMode.DOWN);
    }
}
