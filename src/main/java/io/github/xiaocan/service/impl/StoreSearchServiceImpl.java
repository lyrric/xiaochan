package io.github.xiaocan.service.impl;

import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.StoreSearchDTO;
import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.dto.XcMeituanshangjinDTO;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import io.github.xiaocan.model.vo.WmPageVO;
import io.github.xiaocan.model.vo.XcMeituanshangjinPageVO;
import io.github.xiaocan.service.StoreSearchService;
import io.github.xiaocan.service.WmmtService;
import io.github.xiaocan.service.XiaoChanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用门店搜索服务实现
 */
@Service
@Slf4j
public class StoreSearchServiceImpl implements StoreSearchService {

    @Resource
    private XiaoChanService xiaoChanService;

    @Resource
    private WmmtService wmmtService;

    @Override
    public List<StoreInfo> search(StoreSearchDTO dto) {
        List<StoreInfo> result = new ArrayList<>();

        //小蚕满减搜索，来源是小蚕满减时跳过
        if (dto.getStoreTypeEnum() != StoreTypeEnum.XC_MANJIAN) {
            try {
                result.addAll(xiaoChanService.searchList(dto.getName(), dto.getCityCode(), dto.getLongitude(), dto.getLatitude()));
            } catch (Exception e) {
                log.error("小蚕满减搜索失败", e);
            }
        }

        //小蚕美团赏金搜索，来源是小蚕美团赏金时跳过
        if (dto.getStoreTypeEnum() != StoreTypeEnum.XC_MTSJ) {
            try {
                XcMeituanshangjinDTO mtsjDto = new XcMeituanshangjinDTO();
                mtsjDto.setName(dto.getName());
                mtsjDto.setLatitude(dto.getLatitude());
                mtsjDto.setLongitude(dto.getLongitude());
                XcMeituanshangjinPageVO pageVO = xiaoChanService.getXcMeituanshangjinPageVO(mtsjDto);
                if (pageVO.getStoreInfos() != null) {
                    result.addAll(pageVO.getStoreInfos());
                }
            } catch (Exception e) {
                log.error("小蚕美团赏金搜索失败", e);
            }
        }

        //歪麦搜索
        try {
            WmmtShopListDTO wmmtDto = new WmmtShopListDTO();
            wmmtDto.setName(dto.getName());
            wmmtDto.setLatitude(dto.getLatitude());
            wmmtDto.setLongitude(dto.getLongitude());
            WmPageVO wmPageVO = wmmtService.getShopList(wmmtDto);
            if (wmPageVO.getStoreInfos() != null) {
                result.addAll(wmPageVO.getStoreInfos());
            }
        } catch (Exception e) {
            log.error("歪麦搜索失败", e);
        }

        //过滤掉与来源门店 storeTypeEnum + uniqId 相同的记录
        if (dto.getStoreTypeEnum() == null || dto.getUniqId() == null) {
            return result;
        }
        return result.stream()
                .filter(storeInfo -> storeInfo.getStoreTypeEnum() != dto.getStoreTypeEnum()
                        || !dto.getUniqId().equals(storeInfo.getUniqId()))
                .toList();
    }
}
