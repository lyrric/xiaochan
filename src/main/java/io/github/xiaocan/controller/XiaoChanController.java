package io.github.xiaocan.controller;

import io.github.xiaocan.model.BaseResult;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.XcMeituanshangjinDTO;
import io.github.xiaocan.model.vo.XcMeituanshangjinPageVO;
import io.github.xiaocan.model.vo.QueryListVO;
import io.github.xiaocan.service.XiaoChanService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/xiaochan")
public class XiaoChanController {

    @Resource
    private XiaoChanService xiaoChanService;


    /**
     * 查询所有满减
     * @param queryListVO
     * @return
     */
    @PostMapping(value = "/query")
    public BaseResult<List<StoreInfo>> query(@RequestBody @Validated QueryListVO queryListVO){
        return BaseResult.ok(xiaoChanService.query(queryListVO));
    }

    /**
     * 美团赏金
     *
     * @param dto
     * @return
     */
    @PostMapping(value = "/mtsj")
    public BaseResult<XcMeituanshangjinPageVO> getXcMeituanshangjinPageVO(@RequestBody @Validated XcMeituanshangjinDTO dto) {
        return BaseResult.ok(xiaoChanService.getXcMeituanshangjinPageVO(dto));
    }


}
