package io.github.xiaocan.controller;

import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.FavoriteStoreQueryDTO;
import io.github.xiaocan.model.dto.RemoveFavoriteDTO;
import io.github.xiaocan.model.dto.SaveFavoriteDTO;
import io.github.xiaocan.service.FavoriteStoreService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping(value = "/api/favorite")
public class FavoriteStoreController {

    @Resource
    private FavoriteStoreService favoriteStoreService;

    @PostMapping(value = "/save")
    public BaseResult<Long> save(@RequestBody @Valid SaveFavoriteDTO dto) {
        return BaseResult.ok(favoriteStoreService.saveFavorite(dto));
    }

    @PostMapping(value = "/remove")
    public BaseResult<Void> remove(@RequestBody @Valid RemoveFavoriteDTO dto) {
        favoriteStoreService.removeFavorite(dto);
        return BaseResult.ok();
    }

    /**
     * 根据收藏记录ID取消收藏
     * @param favoriteId 收藏记录ID
     */
    @DeleteMapping(value = "/{favoriteId}")
    public BaseResult<Void> removeById(@PathVariable Long favoriteId) {
        favoriteStoreService.removeFavoriteById(favoriteId);
        return BaseResult.ok();
    }

    @PostMapping(value = "/stores")
    public BaseResult<Page<StoreInfo>> queryFavoriteStores(@RequestBody @Validated FavoriteStoreQueryDTO dto) {
        return BaseResult.ok(favoriteStoreService.queryFavoriteStores(dto));
    }

}
