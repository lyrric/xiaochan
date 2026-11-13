package io.github.xiaocan.service.impl;

import cn.hutool.core.io.IORuntimeException;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.config.XieQuProxyConfig;
import io.github.xiaocan.constant.StoreConstant;
import io.github.xiaocan.constant.StorePlatformEnum;
import io.github.xiaocan.http.XiaochanHttp;
import io.github.xiaocan.model.HttpProxyInfo;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.XiaochanListDTO;
import io.github.xiaocan.model.entity.LocationEntity;
import io.github.xiaocan.model.entity.MonitorConfigEntity;
import io.github.xiaocan.model.entity.StorePushedHistoryEntity;
import io.github.xiaocan.model.entity.UserEntity;
import io.github.xiaocan.model.enums.MonitorTypeEnums;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import io.github.xiaocan.model.vo.QueryListVO;
import io.github.xiaocan.model.vo.XcMeituanshangjinPageVO;
import io.github.xiaocan.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class XiaoChanServiceImpl implements XiaoChanService {

    @Resource
    private StoreInventoryHistoryService storeInventoryHistoryService;
    @Lazy
    @Resource
    private FavoriteStoreService favoriteStoreService;
    @Resource
    private UserService userService;
    @Resource
    private XieQuProxyConfig xieQuProxyConfig;
    @Resource
    private ProxyService proxyService;

    @Resource
    private MonitoryConfigService monitoryConfigService;
    @Resource
    private LocationService locationService;
    // 历史和消息服务会经收藏服务依赖本服务，延迟注入避免循环依赖。
    @Lazy
    @Resource
    private StorePushedHistoryService storePushedHistoryService;
    @Lazy
    @Resource
    private MessageService messageService;

    /**
     * 按当前用户监控位置查询美团满减与赏金活动，按名称完全相同匹配并按名称排序推送。
     * 手动推送不应用实付金额、库存、时间窗口及历史去重条件。
     *
     * @param configId 当前用户的最小实付、小蚕满减监控配置ID
     * @return 匹配并提交推送的门店数，同名门店的多个活动只计一次
     */
    @Override
    public int pushSameNameStores(Integer configId) {
        UserEntity user = userService.getByCurrentRequest();
        MonitorConfigEntity config = monitoryConfigService.getById(configId);
        if (config == null || !user.getId().equals(config.getUserId())) {
            throw new BusinessException("监控配置不存在或无权操作");
        }
        if (config.getType() != MonitorTypeEnums.MINIMUM_PAY || config.getStoreType() != StoreTypeEnum.XC_MANJIAN) {
            throw new BusinessException("仅最小实付类型的小蚕满减监控支持同名门店推送");
        }
        LocationEntity location = locationService.getById(config.getLocationId());
        if (location == null || !user.getId().equals(location.getUserId())) {
            throw new BusinessException("监控位置不存在或无权操作");
        }
        if (StringUtils.isBlank(user.getSpt())) {
            throw new BusinessException("请先配置推送 SPT");
        }

        List<StoreInfo> manjianStores = fetchManjianStores(location).stream().toList();
        log.info("美团满减门店数：{}", manjianStores.size());
        List<StoreInfo> bountyStores = fetchRebateStores(location);
        log.info("美团比例门店数：{}", bountyStores.size());
        Set<String> manjianNames = manjianStores.stream().map(StoreInfo::getName)
                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        Set<String> matchedNames = bountyStores.stream().map(StoreInfo::getName)
                .filter(manjianNames::contains).collect(Collectors.toSet());
        List<StoreInfo> matchedStores = new ArrayList<>();
        manjianStores.stream().filter(store -> matchedNames.contains(store.getName())).forEach(matchedStores::add);
        bountyStores.stream().filter(store -> matchedNames.contains(store.getName())).forEach(matchedStores::add);
        if (matchedStores.isEmpty()) {
            return 0;
        }
        // 稳定排序，使同一门店的满减活动与赏金活动相邻，满减在前、赏金在后。
        matchedStores.sort(Comparator.comparing(StoreInfo::getName));
        // 仅为本次手动推送使用独立类型，原监控配置仍保留最小实付类型。
        MonitorConfigEntity pushConfig = new MonitorConfigEntity();
        BeanUtils.copyProperties(config, pushConfig);
        pushConfig.setType(MonitorTypeEnums.SAME_NAME_STORE_ACTIVITY);
        String batchId = savePushedHistory(pushConfig, matchedStores);
        messageService.sendImmediately(pushConfig, matchedStores, location, batchId);
        return matchedNames.size();
    }

    private List<StoreInfo> fetchManjianStores(LocationEntity location) {
        List<StoreInfo> stores = new ArrayList<>();
        for (int pageNum = 1; stores.size() < 200; pageNum++) {
            List<StoreInfo> page = getListByOffset(XiaochanListDTO.builder()
                    .cityCode(location.getCityCode())
                    .longitude(location.getLongitude())
                    .latitude(location.getLatitude())
                    .pageNum(pageNum)
                    .storePlatforms(List.of(StorePlatformEnum.MEITUAN))
                    .build());
            if (page.isEmpty()) {
                break;
            }
            stores.addAll(page);
            if (!hasNext(page)) {
                break;
            }
        }
        return stores;
    }

    private List<StoreInfo> fetchRebateStores(LocationEntity location) {
        List<StoreInfo> stores = new ArrayList<>();
        XiaochanListDTO dto = new XiaochanListDTO();
        dto.setCityCode(location.getCityCode());
        dto.setLongitude(location.getLongitude());
        dto.setLatitude(location.getLatitude());
        while (stores.size() < 100) {
            XcMeituanshangjinPageVO page = getRebateList(dto);
            if (page.getStoreInfos() == null || page.getStoreInfos().isEmpty()) {
                break;
            }
            List<StoreInfo> items = page.getStoreInfos();
            stores.addAll(items);
            String cursor = page.getPagePvId();
            dto.setPagePvId(cursor);
            dto.setPageNum(dto.getPageNum() + 1);
        }
        return stores;
    }

    private String savePushedHistory(MonitorConfigEntity notifyConfig, List<StoreInfo> storeInfos){
        List<StorePushedHistoryEntity> entities = storeInfos.stream().map(storeInfo -> {
            StorePushedHistoryEntity entity = new StorePushedHistoryEntity();
            BeanUtils.copyProperties(storeInfo, entity);
            entity.setId(null);
            entity.setUserId(notifyConfig.getUserId());
            entity.setNotifyConfigId(notifyConfig.getId());
            entity.setLocationId(notifyConfig.getLocationId());
            entity.setNotifyType(notifyConfig.getType());
            return entity;
        }).toList();
        return storePushedHistoryService.saveBatchAndReturnBatchId(entities);
    }

    /**
     * 封禁冷却时间：10分钟
     */
    private static final long BLOCK_COOLDOWN_MILLIS = 10 * 60 * 1000L;

    /**
     * 被封禁的时间戳，null表示未被封禁
     */
    private volatile Long blockedTime = null;

    /**
     * 是否处于封禁状态，距封禁超过10分钟则自动解封
     */
    private boolean isBlocked() {
        Long time = blockedTime;
        if (time == null) {
            return false;
        }
        if (System.currentTimeMillis() - time > BLOCK_COOLDOWN_MILLIS) {
            blockedTime = null;
            return false;
        }
        return true;
    }


    @Override
    public List<StoreInfo> query(QueryListVO queryListVO) {
        List<StoreInfo> result;
        Integer pageNum = queryListVO.getPageNum();
        pageNum = pageNum == null ? 1 : pageNum;

        if (StringUtils.isNotBlank(queryListVO.getName())) {
            //搜索走专门的接口
            if (pageNum > 1) {
                return Collections.emptyList();
            }
            result = searchList(queryListVO.getName(), queryListVO.getCityCode(), queryListVO.getLongitude(), queryListVO.getLatitude());
        }else{
            //走官方分页接口
            result = getListByOffset(XiaochanListDTO.builder()
                    .cityCode(queryListVO.getCityCode())
                    .longitude(queryListVO.getLongitude())
                    .latitude(queryListVO.getLatitude())
                    .pageNum(pageNum)
                    .pageSize(queryListVO.getPageSize())
                    .build());
        }
        // 填充收藏ID
        if (queryListVO.getLocationId() != null) {
            Integer userId = userService.getByCurrentRequest().getId();
            favoriteStoreService.fillFavoriteIds(result, userId, queryListVO.getLocationId());
        }
        return result;
    }

    @Override
    public List<StoreInfo> searchList(String keyword, Integer cityCode, String longitude, String latitude) {
        List<StoreInfo> storeInfos = XiaochanHttp.searchList(keyword, cityCode, longitude, latitude, 0, 15);
        storeInventoryHistoryService.insertBatch(storeInfos);
        return storeInfos;
    }


    @Override
    public List<StoreInfo> getList(Integer cityCode, String longitude, String latitude, int maxSize){
        log.info("请求小产列表，城市: {}, 经度: {}, 纬度: {}, 最大数量: {}", cityCode, longitude, latitude, maxSize);
        List<StoreInfo> result = new ArrayList<>();
        for (int pageNum = 1; pageNum <= 100; pageNum++) {
            List<StoreInfo> list = getListByOffset(XiaochanListDTO.builder()
                    .cityCode(cityCode)
                    .longitude(longitude)
                    .latitude(latitude)
                    .pageNum(pageNum)
                    .pageSize(StoreConstant.DEFAULT_PAGE_SIZE)
                    .build());
            result.addAll(list);
            if (result.size() >= maxSize) {
                break;
            }
            if (!hasNext(list)) {
                break;
            }
        }
        storeInventoryHistoryService.insertBatch(result);
        return result;
    }

    /**
     * 按平台筛选分页查询活动，直连和代理请求使用相同的平台条件。
     *
     * @param dto 列表查询参数，平台列表不传或为空表示全部平台
     * @return 活动列表
     */
    @Override
    public List<StoreInfo> getListByOffset(XiaochanListDTO dto) {
        if (!isBlocked()) {
            try {
                return XiaochanHttp.getList(dto);
            }catch (Exception e){
                if (e.getMessage().contains("状态码错误: 403")) {
                    blockedTime = System.currentTimeMillis();
                }
                log.error("请求小产列表时发生错误 {}",e.getMessage());
            }
        }
        if (!xieQuProxyConfig.getEnable()) {
            return Collections.emptyList();
        }
        for (int i = 0; i < 10; i++) {
            HttpProxyInfo proxyInfo = null;
            try {
                proxyInfo = proxyService.getOne();
                log.info("使用代理 {} 请求小产列表", proxyInfo);
                dto.setHttpProxyInfo(proxyInfo);
                return XiaochanHttp.getList(dto);
            }catch (IORuntimeException | BusinessException e){
                log.error("proxy 获取小产列表时发生错误 {} {}", proxyInfo, e.getMessage());
                if (proxyInfo != null) {
                    proxyService.removeProxy(proxyInfo);
                }
            }catch (Exception e){
                log.error("proxy 请求小产列表时发生错误 {} ", proxyInfo, e);
                if (proxyInfo != null) {
                    proxyService.removeProxy(proxyInfo);
                }
            }finally {
                dto.setHttpProxyInfo(null);
            }
        }

        return Collections.emptyList();
    }


    @Override
    public XcMeituanshangjinPageVO getRebateList(XiaochanListDTO dto) {
        XcMeituanshangjinPageVO vo = new XcMeituanshangjinPageVO();
        dto.setRebateType(2);
        dto.setStorePlatforms(Collections.singletonList(StorePlatformEnum.MEITUAN));
        List<StoreInfo> list = getListByOffset(dto);
        // 填充收藏ID
        if (dto.getLocationId() != null) {
            Integer userId = userService.getByCurrentRequest().getId();
            favoriteStoreService.fillFavoriteIds(list, userId, dto.getLocationId());
        }
        vo.setStoreInfos(list);
        vo.setPagePvId(list.isEmpty() ? null : list.get(list.size() - 1).getPagePvId());
        vo.setMeituanPvId(list.isEmpty() ? null : list.get(list.size() - 1).getMtPvId());
        return vo;
    }

    private boolean hasNext(List<StoreInfo> list){
        if (list.isEmpty()) {
            return false;
        }
        long overDistanceCount = list.stream()
                .filter(t -> t.getDistance() > StoreConstant.MAX_DISTANCE)
                .count();
        int size = list.size();
        //有一半的店距离超过MAX_DISTANCE，则不再查找下一页
        return overDistanceCount <= (size / 2);

    }

}
