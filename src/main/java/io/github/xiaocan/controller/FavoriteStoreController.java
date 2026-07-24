package io.github.xiaocan.controller;

import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.FavoriteStoreQueryDTO;
import io.github.xiaocan.model.dto.RemoveFavoriteDTO;
import io.github.xiaocan.model.dto.SaveFavoriteDTO;
import io.github.xiaocan.model.vo.FavoriteStoreVO;
import io.github.xiaocan.service.FavoriteStoreService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/favorite")
public class FavoriteStoreController {

    @Resource
    private FavoriteStoreService favoriteStoreService;

    @PostMapping(value = "/save")
    public BaseResult<Void> save(@RequestBody @Valid SaveFavoriteDTO dto) {
        favoriteStoreService.saveFavorite(dto);
        return BaseResult.ok();
    }

    @PostMapping(value = "/remove")
    public BaseResult<Void> remove(@RequestBody @Valid RemoveFavoriteDTO dto) {
        favoriteStoreService.removeFavorite(dto);
        return BaseResult.ok();
    }

    @GetMapping(value = "/list")
    public BaseResult<List<FavoriteStoreVO>> list(@RequestParam(required = true) Long locationId,
                                                   @RequestParam(required = true) String storeType) {
        return BaseResult.ok(favoriteStoreService.listFavorites(locationId, storeType));
    }

    @PostMapping(value = "/stores")
    public BaseResult<List<StoreInfo>> queryFavoriteStores(@RequestBody @Validated FavoriteStoreQueryDTO dto) {
        return BaseResult.ok(favoriteStoreService.queryFavoriteStores(dto));
    }
}
