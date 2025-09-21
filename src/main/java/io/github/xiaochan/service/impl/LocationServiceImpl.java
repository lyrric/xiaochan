package io.github.xiaochan.service.impl;

import com.alibaba.fastjson2.JSON;
import io.github.xiaochan.constant.RedisConstant;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.service.LocationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LocationServiceImpl implements LocationService {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public String add(Location location) {
        // 生成时间戳作为ID
        String id = String.valueOf(System.currentTimeMillis());
        location.setId(id);
        // 获取Redis Map
        RMap<String, String> addressMap = getAddressMap();
        // 将Location对象转为JSON字符串存储
        String locationJson = JSON.toJSONString(location);
        addressMap.put(id, locationJson);
        log.info("地址新增成功，ID: {}, 地址信息: {}", id, location.getName());
        return id;
    }
    private RMap<String, String> getAddressMap() {
        return redissonClient.getMap(RedisConstant.LOCATION, StringCodec.INSTANCE);
    }

    @Override
    public boolean delete(String id) {
        if (!StringUtils.hasText(id)) {
            return false;
        }
        RMap<String, String> addressMap = getAddressMap();
        String removed = addressMap.remove(id);
        boolean success = removed != null;
        log.info("地址删除结果，ID: {}, 成功: {}", id, success);
        return success;
    }

    @Override
    public boolean update(String id, String spt, Boolean pushSwitch) {
        if (!StringUtils.hasText(id)) {
            return false;
        }
        RMap<String, String> addressMap = getAddressMap();
        String locationJson = addressMap.get(id);

        if (locationJson == null) {
            log.warn("地址不存在，ID: {}", id);
            return false;
        }

        // 解析JSON为Location对象
        Location location = JSON.parseObject(locationJson, Location.class);

        // 只更新spt和pushSwitch字段
        if (spt != null) {
            location.setSpt(spt);
        }
        if (pushSwitch != null) {
            location.setPushSwitch(pushSwitch);
        }

        // 更新到Redis
        String updatedJson = JSON.toJSONString(location);
        addressMap.put(id, updatedJson);

        log.info("地址更新成功，ID: {}, spt: {}, pushSwitch: {}", id, spt, pushSwitch);
        return true;
    }

    @Override
    public List<Location> getAll() {
        RMap<String, String> addressMap = getAddressMap();
        List<Location> locations = new ArrayList<>();

        for (Map.Entry<String, String> entry : addressMap.entrySet()) {
            try {
                Location location = JSON.parseObject(entry.getValue(), Location.class);
                locations.add(location);
            } catch (Exception e) {
                log.warn("解析地址数据失败，ID: {}", entry.getKey(), e);
            }
        }
        
        // 按照ID排序，确保返回顺序的一致性
        locations.sort((a, b) -> {
            try {
                Long idA = Long.parseLong(a.getId());
                Long idB = Long.parseLong(b.getId());
                return idA.compareTo(idB);
            } catch (NumberFormatException e) {
                // 如果ID不是数字，则按字符串排序
                return a.getId().compareTo(b.getId());
            }
        });
        
        return locations;
    }
}
