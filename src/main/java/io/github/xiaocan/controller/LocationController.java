package io.github.xiaocan.controller;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.github.xiaocan.http.XiaochanHttp;
import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.dto.LocationDTO;
import io.github.xiaocan.model.vo.AddressVO;
import io.github.xiaocan.model.vo.CityCodeVO;
import io.github.xiaocan.model.vo.LocationVO;
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
import java.util.List;

@RestController
@RequestMapping(value = "/api/location")
@Validated
public class LocationController {

    @Resource
    private LocationService locationService;

    private final XiaochanHttp xiaochanHttp = new XiaochanHttp();

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
        return BaseResult.ok(xiaochanHttp.searchAddress(cityCode, keyword));
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

    private String readCityCode() throws IOException {
        ClassPathResource resource = new ClassPathResource("city-code.json");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

}
