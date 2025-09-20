package io.github.xiaochan.controller;

import io.github.xiaochan.http.XiaochanHttp;
import io.github.xiaochan.model.BaseResult;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.model.vo.AddressVO;
import io.github.xiaochan.model.vo.BookVO;
import io.github.xiaochan.model.vo.IgnoreStoreVO;
import io.github.xiaochan.model.vo.QueryListVO;
import io.github.xiaochan.service.XiaoChanService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/xiaochan")
public class XiaoChanController {

    @Resource
    private XiaoChanService xiaoChanService;


    /**
     * 查询所有
     * @param queryListVO
     * @return
     */
    @PostMapping(value = "/query")
    public BaseResult<List<StoreInfo>> query(@RequestBody QueryListVO queryListVO){
        return BaseResult.ok(xiaoChanService.query(queryListVO));
    }

    /**
     * 报名
     * @param promotionId 活动id
     * @return 成功或者失败
     */
    @PostMapping(value = "/apply/{promotionId}")
    public BaseResult<String> apply(@PathVariable Integer promotionId){
        return BaseResult.ok();
    }

    /**
     * 预约
     * @param bookVO 信息
     * @return 成功或者失败
     */
    @PostMapping(value = "/book")
    public BaseResult<String> book(@RequestBody BookVO bookVO){
        return BaseResult.ok();
    }


    /**
     * 忽略
     * @param ignoreStoreVO 忽略的门店信息
     * @return 成功或者失败
     */
    @PostMapping(value = "/ignore")
    public BaseResult<String> ignore(@RequestBody IgnoreStoreVO ignoreStoreVO){
        return BaseResult.ok();
    }




}
