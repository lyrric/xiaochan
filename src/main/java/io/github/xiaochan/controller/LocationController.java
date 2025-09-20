package io.github.xiaochan.controller;

import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.BaseResult;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.dto.UpdateLocationDTO;
import io.github.xiaochan.model.vo.AddressVO;
import io.github.xiaochan.service.LocationService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * @param location 地址信息
     * @return 新增结果，包含地址ID
     */
    @PostMapping
    public BaseResult<String> add(@Valid @RequestBody Location location) {
        String id = locationService.add(location);
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
     * 修改地址（只能修改spt和pushSwitch）
     * @param id 地址ID
     * @param updateDTO 更新信息
     * @return 修改结果
     */
    @PutMapping("/{id}")
    public BaseResult<String> update(@PathVariable @NotBlank(message = "地址ID不能为空") String id, 
                                      @Valid @RequestBody UpdateLocationDTO updateDTO) {
        locationService.update(id, updateDTO.getSpt(), updateDTO.getPushSwitch());
        return BaseResult.ok();
    }

    /**
     * 查询所有地址
     * @return 地址列表
     */
    @GetMapping
    public BaseResult<List<Location>> getAll() {
        List<Location> locations = locationService.getAll();
        return BaseResult.ok(locations);
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

}
