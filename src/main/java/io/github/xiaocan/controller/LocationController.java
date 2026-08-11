package io.github.xiaocan.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.github.xiaocan.http.XiaochanHttp;
import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.dto.LocationDTO;
import io.github.xiaocan.model.vo.AddressVO;
import io.github.xiaocan.model.vo.CityCodeVO;
import io.github.xiaocan.model.vo.LocationVO;
import io.github.xiaocan.model.vo.RegionItemVO;
import io.github.xiaocan.service.LocationService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/location")
@Validated
public class LocationController {

    @Resource
    private LocationService locationService;

    /**
     * 新增地址
     * @return 新增结果，包含地址ID
     */
    @PostMapping
    public BaseResult<Integer> add(@Valid @RequestBody LocationDTO dto) {
        Integer id = locationService.add(dto);
        return BaseResult.ok(id);

    }

    /**
     * 删除地址
     * @param id 地址ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public BaseResult<String> delete(@PathVariable @NotBlank(message = "地址ID不能为空") String id) {
        locationService.delete(id);
        return BaseResult.ok();
    }

    /**
     * 查询所有地址
     * @return 地址列表
     */
    @GetMapping
    public BaseResult<List<LocationVO>> getAll() {
        return BaseResult.ok(locationService.getAll());
    }
    /**
     * 搜索地址
     * @param keyword
     * @param cityCode
     * @return
     */
    @GetMapping(value = "/searchAddress")
    public BaseResult<List<AddressVO>> searchAddress(@RequestParam String keyword, @RequestParam Integer cityCode){
        return BaseResult.ok(XiaochanHttp.searchAddress(cityCode, keyword));
    }
    /**
     * 获取行政区划代码
     */
    @GetMapping(value = "/cityCode")
    public BaseResult<List<CityCodeVO>> getCityCode() throws IOException {
        String json = readCityCode();
        List<CityCodeVO> data = JSONObject.parseObject(json, new TypeReference<List<CityCodeVO>>() {
        });
        return BaseResult.ok(data);
    }

    /**
     * 搜索省市区（扁平化列表，按key模糊匹配）
     * @param key 搜索关键字
     * @return 匹配的省市区列表
     */
    @GetMapping(value = "/searchRegion")
    public BaseResult<List<RegionItemVO>> searchRegion(@RequestParam(required = false, defaultValue = "") String key) throws IOException {
        String json = readCityCode();
        List<CityCodeVO> tree = JSONObject.parseObject(json, new TypeReference<List<CityCodeVO>>() {
        });
        List<RegionItemVO> allItems = flattenRegion(tree);
        if (key == null || key.trim().isEmpty()) {
            return BaseResult.ok(allItems.size() > 10 ? allItems.subList(0, 10) : allItems);
        }
        String lowerKey = key.trim().toLowerCase();
        List<RegionItemVO> filtered = allItems.stream()
                .filter(item -> item.getName().toLowerCase().contains(lowerKey))
                .limit(10)
                .collect(Collectors.toList());
        return BaseResult.ok(filtered);
    }

    /**
     * 将省市区树形结构扁平化为列表
     */
    private List<RegionItemVO> flattenRegion(List<CityCodeVO> provinces) {
        List<RegionItemVO> result = new ArrayList<>();
        if (provinces == null) return result;
        for (CityCodeVO province : provinces) {
            if (province.getChild() == null || province.getChild().isEmpty()) {
                result.add(new RegionItemVO(province.getName(), province.getCode()));
                continue;
            }
            for (CityCodeVO city : province.getChild()) {
                if (city.getChild() == null || city.getChild().isEmpty()) {
                    result.add(new RegionItemVO(province.getName() + " " + city.getName(), city.getCode()));
                    continue;
                }
                for (CityCodeVO district : city.getChild()) {
                    String name = province.getName() + " " + city.getName() + " " + district.getName();
                    result.add(new RegionItemVO(name, district.getCode()));
                }
            }
        }
        return result;
    }

    private String readCityCode() throws IOException {
        ClassPathResource resource = new ClassPathResource("city-code.json");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

}
