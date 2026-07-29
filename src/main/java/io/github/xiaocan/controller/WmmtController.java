package io.github.xiaocan.controller;

import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.vo.WmPageVO;
import io.github.xiaocan.service.WmmtService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 歪麦（waimaimingtang）门店接口
 */
@RestController
@RequestMapping(value = "/api/wmmt")
public class WmmtController {

    @Resource
    private WmmtService wmmtService;

    /**
     * 歪麦门店列表（支持游标翻页）
     */
    @PostMapping(value = "/shopList")
    public BaseResult<WmPageVO> getShopList(@RequestBody @Validated WmmtShopListDTO dto) {
        return BaseResult.ok(wmmtService.getShopList(dto));
    }
}
