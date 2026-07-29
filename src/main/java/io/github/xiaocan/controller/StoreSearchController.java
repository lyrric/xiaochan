package io.github.xiaocan.controller;

import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.StoreSearchDTO;
import io.github.xiaocan.service.StoreSearchService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通用门店搜索接口
 */
@RestController
@RequestMapping(value = "/api/store")
public class StoreSearchController {

    @Resource
    private StoreSearchService storeSearchService;

    /**
     * 聚合搜索小蚕满减、小蚕美团赏金、歪麦三个平台的门店，不分页
     */
    @PostMapping(value = "/search")
    public BaseResult<List<StoreInfo>> search(@RequestBody @Validated StoreSearchDTO dto) {
        return BaseResult.ok(storeSearchService.search(dto));
    }
}
