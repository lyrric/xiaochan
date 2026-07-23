package io.github.xiaocan.controller;

import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.vo.StoreInventoryHistoryVO;
import io.github.xiaocan.service.StoreInventoryHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门店库存记录接口
 */
@RestController
@RequestMapping(value = "/api/store-inventory-history")
public class StoreInventoryHistoryController {

    @Resource
    private StoreInventoryHistoryService storeInventoryHistoryService;

    /**
     * 查询当前用户当天指定门店的库存记录
     * @param uniqueId 门店唯一标识
     */
    @GetMapping("/{uniqueId}")
    public BaseResult<List<StoreInventoryHistoryVO>> listToday(@PathVariable("uniqueId") String uniqueId) {
        return BaseResult.ok(storeInventoryHistoryService.listTodayByUniqueId(uniqueId));
    }

}
