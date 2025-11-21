package io.github.xiaochan.http;

import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.xiaochan.config.BusinessException;
import io.github.xiaochan.model.StoreInfo;
import io.github.xiaochan.model.vo.AddressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class XiaochanHttp {


    private static final String BASE_URL = "https://gw.xiaocantech.com/rpc";
    private static final String SERVER_NAME = "SilkwormRec";
    private static final String METHOD_NAME = "RecService.GetStorePromotionList";

    private static final String NAMI = "acec123456789";


    private static final int PAGE_SIZE = 30;

    /**
     * 获取Ashe
     * @param timeMillis X-Garen
     * @return
     */
    private String getAshe(Long timeMillis, String serverName, String methodName) {
        String x = MD5.create().digestHex((serverName + "." + methodName).toLowerCase());
        return MD5.create().digestHex(x + timeMillis + NAMI);
    }


    public List<StoreInfo> getList(Integer cityCode, String longitude, String latitude, int offset){
        String reqBody = getBody(cityCode, longitude, latitude, offset);
        String resBody = postWithRes(BASE_URL, reqBody, cityCode, SERVER_NAME, METHOD_NAME);
        return parseListBody(resBody);
    }



    public List<StoreInfo> searchList(String keyword, Integer cityCode, String longitude, String latitude, int offset, Integer number) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("silk_id", 897154359);
        bodyMap.put("latitude", new BigDecimal(latitude));
        bodyMap.put("longitude", new BigDecimal(longitude));
        bodyMap.put("promotion_sort", 1);
        bodyMap.put("store_platform", 0);
        bodyMap.put("store_type", 99);
        bodyMap.put("offset",offset);
        bodyMap.put("number",number);
        bodyMap.put("keyword", keyword);
        bodyMap.put("promotion_category", 0);
        bodyMap.put("app_id",20);
        String resBody = postWithRes(BASE_URL, JSONObject.toJSONString(bodyMap), cityCode, "SilkwormRec", "RecService.SearchStorePromotionList");
        return parseListBody(resBody);

    }

    private String postWithRes(String url, String body, Integer cityCode, String serverName, String methodName) {
        Long timeMillis = System.currentTimeMillis();
        String ashe = getAshe(timeMillis, serverName, methodName);
        HttpResponse response = HttpUtil.createPost(url)
                .headerMap(getHeaders(timeMillis, ashe, cityCode, serverName, methodName), true)
                .timeout(3000)
                .body(body)
                .execute();
        if (!response.isOk()) {
            log.error("状态码错误: {}, body: {}", response.getStatus(), response.body());
            throw new BusinessException("状态码错误:" + response.getStatus());
        }
        String resBody = response.body();
        response.close();
        return resBody;
    }


    /**
     * 搜索地址
     */
    public List<AddressVO> searchAddress(Integer cityCode, String keyword){
        final String serverName = "SilkwormLbs";
        final String methodName = "SilkwormLbsService.Suggestion";
        Map<String, Object> bodyMap = Map.of("silk_id", 897154359, "keyword", keyword,
                "region", "", "page_size", 20, "page", 1, "app_id", 20);
        try {
            Long timeMillis = System.currentTimeMillis();
            String ashe = getAshe(timeMillis, serverName, methodName);
            HttpResponse response = HttpUtil.createPost(BASE_URL)
                    .headerMap(getHeaders(timeMillis, ashe, cityCode, serverName, methodName), true)
                    .timeout(3000)
                    .body(JSONObject.toJSONString(bodyMap))
                    .execute();
            if (!response.isOk()) {
                throw new BusinessException("状态码错误:" + response.getStatus());
            }
            return parseBodyToAddress(response.body());
        } catch (Exception e) {
            log.error("{} error", methodName, e);
            throw e;
        }
    }


    /**
     * 获取活动详情
     * 内容比较丰富，可按需索取
     * 返回数据格式：{"status":{"code":0},"promotion_detail":{"store":{"store_id":259158,"name":"烤来屋(烧烤·生蚝·双流店)","meituan_id":"https://oss.simian-scrm.com/qrcode/silkworm_tool/531_qr_1755824398_hXa3C5tjY8M2Ox9w.png","eleme_id":"https://oss.simian-scrm.com/qrcode/silkworm_tool/531_qr_1733999462_vTFs4zieY1kfNEkm.png","icon":"https://img10.360buyimg.com/imagetools/jfs/t1/256581/8/19429/110476/67b61b2cF4b8cfb82/baf72511b2c2d93f.jpg","contact_name":"1","contact_phone":"180****4429","longitude":103.911615,"latitude":30.575397,"province":"四川省","city":"成都市","district":"双流区","address":"双流区一杆旗小区(五洞桥南路一段东)","address_detail":"四川省成都市双流区东升街道五洞桥南路一段27","city_code":510116,"mini_meituan_id":"18746192","mini_eleme_id":"E17272805750223283875","opening_hours":"00:00-23:59","if_auth":true,"sl":{"seven_days_limit":1,"days_limit":3},"jd_id":"https://oss.simian-scrm.com/qrcode/silkworm_tool/531_qr_1755824409_c74VVtSt4DCRnfsa.png","mini_jd_id":"venderId=19130190&storeId=1017907745","dishes":[{"name":"烤土豆片1串","icon":"https://p1.meituan.net/xianfu/0c3435ad9c401ef233d84629952f4b3b382324.jpg@200w","op":"4","p":"4"},{"name":"坨坨牛肉串","icon":"https://p1.meituan.net/wmproduct/81f0a66226e59c4bc7a8a7abe6e5b91a999321.jpg@200w","op":"4","p":"4"},{"name":"烤苕皮","icon":"https://p1.meituan.net/wmproduct/457be57b3d71ce9feab45edec3edcd3d733419.jpg@200w","op":"6","p":"6"},{"name":"烤鱿鱼板1串","icon":"https://p1.meituan.net/xianfu/4f56b612ae79c77f74cc1a357277c0f5513003.jpg@200w","op":"6","p":"6"},{"name":"招牌卤耙鸡爪2个","icon":"https://p1.meituan.net/xianfu/9f7a44ca395248e2a2981ce0414edb17370474.jpg@200w","op":"11","p":"11"}]},"promotion_id":74213510,"meituan_status":1,"meituan_order_money":2000,"meituan_left_number":2,"meituan_user_rebate":1500,"eleme_status":0,"eleme_left_number":0,"start_date":"2025-09-22","end_date":"2025-09-22","start_time_hour":0,"start_time_minute":0,"end_time_hour":23,"end_time_minute":59,"start_date_timestamp":1758470400,"end_date_timestamp":1758556740,"rebate_condition":2,"rebate_condition_str":"用餐反馈（需含字含图）","promotion_status":1,"detail_list":["https://oss.guojiangmedia.com/freelunch/new-detail-3.png"],"promotion_pay_status":0,"promotion_type":0,"if_can_advance_order":true,"tags":["宝子们，要3图15字哈"],"red_pack_dt":7200}}
     * @param promotionId
     * @return
     */
    public StoreInfo GetStorePromotionDetail(Integer promotionId){
        Map<String, Integer> reqMap = Map.of("silk_id", 897154359,
                "promotion_id", promotionId,
                "app_id", 20);
        String resBody = postWithRes(BASE_URL, JSONObject.toJSONString(reqMap), null, "Silkworm", "SilkwormService.GetStorePromotionDetail");
        JSONObject jsonObject = checkResult(resBody);
        List<StoreInfo> storeInfos = parsePromotion(jsonObject.getJSONObject("promotion_detail"));
        return storeInfos.get(0);
    }
    private List<AddressVO> parseBodyToAddress(String body) {
        JSONObject jsonObject = JSONObject.parseObject(body);
        if (jsonObject.getJSONObject("status").getInteger("code") != 0) {
            log.error("parseBodyToAddress error body: {} ", body);
            throw new BusinessException("状态码错误:" + jsonObject.getJSONObject("status").getInteger("code"));
        }
        JSONArray jsonArray = jsonObject.getJSONArray("result");
        List<AddressVO> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            AddressVO addressVO = AddressVO.builder()
                    .id(obj.getString("id"))
                    .title(obj.getString("title"))
                    .address(obj.getString("address"))
                    .latitude(obj.getJSONObject("location").getString("lat"))
                    .longitude(obj.getJSONObject("location").getString("lng"))
                    .cityCode(obj.getInteger("adcode"))
                    .province(obj.getString("province"))
                    .city(obj.getString("city"))
                    .district(obj.getString("district"))
                    .build();
            result.add(addressVO);
        }
        return result;
    }



    private void sendWithoutRes(String body, Integer cityCode, String serverName, String methodName) {
        sendWithoutRes(BASE_URL, cityCode, body, serverName, methodName);
    }

    private void sendWithoutRes(String url, Integer cityCode, String body, String serverName, String methodName) {
        try {
            Long timeMillis = System.currentTimeMillis();
            String ashe = getAshe(timeMillis, serverName, methodName);
            HttpResponse response = HttpUtil.createPost(url)
                    .headerMap(getHeaders(timeMillis, ashe, cityCode, serverName, methodName), true)
                    .timeout(3000)
                    .body(body)
                    .execute();
            if (!response.isOk()) {
                throw new BusinessException("状态码错误:" + response.getStatus());
            }
        } catch (Exception e) {
            log.error("{} error", methodName, e);
        }
    }
    private static String getBody(Integer cityCode, String longitude, String latitude, int offset){
        Map<String, Object> body = new HashMap<>();
        body.put("latitude", new BigDecimal(latitude));
        body.put("longitude", new BigDecimal(longitude));
        body.put("promotion_sort", 3);
        body.put("store_type", 0);
        body.put("offset", offset);
        body.put("number", PAGE_SIZE);
        body.put("silk_id", 89715435);
        body.put("promotion_filter", 0);
        body.put("promotion_category", 0);
        body.put("city_code", cityCode);
        body.put("store_category", 0);
        body.put("store_platform", 0);
        body.put("app_id", 20);
        return JSONObject.toJSONString(body);
    }


    private Map<String, String> getHeaders(Long timeMillis, String ashe, Integer cityCode, String serverName, String methodName){
        Map<String, String> headers = new HashMap<>();
        headers.put("x-City", String.valueOf(cityCode));
        headers.put("X-Garen", String.valueOf(timeMillis));
        headers.put("X-Nami", NAMI);
        headers.put("version", "3.11.1.44");
        headers.put("appid", "20");
        headers.put("x-Vayne", "0");
        headers.put("x-Annie", "XC");
        headers.put("xweb_xhr", "1");
        headers.put("x-Teemo", "0");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Sec-Fetch-Site", "cross-site");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 MicroMessenger/7.0.20.1781(0x6700143B) NetType/WIFI MiniProgramEnv/Windows WindowsWechat/WMPF WindowsWechat(0x63090a13) UnifiedPCWindowsWechat(0xf2541411) XWEB/16965");
        headers.put("servername", serverName);
        headers.put("methodname", methodName);
        headers.put("X-Ashe", ashe);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private List<StoreInfo> parsePromotion(JSONObject jsonObject){
        List<StoreInfo> result = new ArrayList<>();
        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setName(jsonObject.getJSONObject("store").getString("name"));
        storeInfo.setOpenHours(jsonObject.getJSONObject("store").getString("opening_hours"));
        storeInfo.setPromotionId(jsonObject.getInteger("promotion_id"));
        storeInfo.setRebateCondition(jsonObject.getInteger("rebate_condition"));
        storeInfo.setStartTime(formatStartEndTime(jsonObject.getInteger("start_time_hour"), jsonObject.getInteger("start_time_minute")));
        storeInfo.setEndTime(formatStartEndTime(jsonObject.getInteger("end_time_hour") ,jsonObject.getInteger("end_time_minute")));
        storeInfo.setDistance(jsonObject.getInteger("distance") );
        storeInfo.setIcon(jsonObject.getJSONObject("store").getString("icon") );
        storeInfo.setStoreId(jsonObject.getJSONObject("store").getInteger("store_id") );
        //美团
        if (jsonObject.getInteger("meituan_status") == 1) {
            StoreInfo meituanStoreInfo = new StoreInfo();
            BeanUtils.copyProperties(storeInfo, meituanStoreInfo);
            meituanStoreInfo.setType(1);
            meituanStoreInfo.setLeftNumber(jsonObject.getInteger("meituan_left_number"));
            meituanStoreInfo.setPrice(safeDivide(jsonObject.getBigDecimal("meituan_order_money"), BigDecimal.valueOf(100)));
            meituanStoreInfo.setRebatePrice(safeDivide(jsonObject.getBigDecimal("meituan_user_rebate"), BigDecimal.valueOf(100)));
            result.add(meituanStoreInfo);
        }
        //饿了么
        if (jsonObject.getInteger("eleme_status") == 1) {
            StoreInfo eleStoreInfo = new StoreInfo();
            BeanUtils.copyProperties(storeInfo, eleStoreInfo);
            eleStoreInfo.setType(2);
            eleStoreInfo.setLeftNumber(jsonObject.getInteger("eleme_left_number"));
            eleStoreInfo.setPrice(safeDivide(jsonObject.getBigDecimal("eleme_order_money"), BigDecimal.valueOf(100)));
            eleStoreInfo.setRebatePrice(safeDivide(jsonObject.getBigDecimal("eleme_user_rebate"),BigDecimal.valueOf(100)));
            result.add(eleStoreInfo);
        }
        // 京东
        if (jsonObject.containsKey("tp_promotion")) {
            JSONObject tpPromotion = jsonObject.getJSONObject("tp_promotion");
            if (tpPromotion.getInteger("tp_status") == 1) {
                StoreInfo eleStoreInfo = new StoreInfo();
                BeanUtils.copyProperties(storeInfo, eleStoreInfo);
                eleStoreInfo.setType(3);
                eleStoreInfo.setLeftNumber(tpPromotion.getInteger("tp_left_number"));
                eleStoreInfo.setPrice(safeDivide(tpPromotion.getBigDecimal("tp_order_money"), BigDecimal.valueOf(100)));
                eleStoreInfo.setRebatePrice(safeDivide(tpPromotion.getBigDecimal("tp_user_rebate"),BigDecimal.valueOf(100)));
                result.add(eleStoreInfo);
            }
        }
        return result;
    }

    private JSONObject checkResult(String body){
        JSONObject jsonBody = JSONObject.parseObject(body);
        if (jsonBody.getJSONObject("status").getInteger("code") != 0) {
            String msg = jsonBody.getJSONObject("status").getString("msg");
            log.error("请求失败: {}", body);
            throw new BusinessException("请求失败:" + msg);
        }
        return jsonBody;
    }
    private List<StoreInfo> parseListBody(String body){
        JSONObject jsonBody = checkResult(body);
        List<StoreInfo> result = new ArrayList<>();
        JSONArray promotionList = jsonBody.getJSONArray("promotion_list");
        if (promotionList == null) {
            return result;
        }
        for (int i = 0; i < promotionList.size(); i++) {
            JSONObject jsonObject =  promotionList.getJSONObject(i);
            List<StoreInfo> storeInfos = parsePromotion(jsonObject);
            result.addAll(storeInfos);
        }
        return result;
    }

    private String formatStartEndTime(Integer hour, Integer minute){
        return String.format("%02d", hour) + ":" + String.format("%02d", minute);
    }

    private BigDecimal safeDivide(BigDecimal b1, BigDecimal b2){
        if (b1 == null || b2 == null) {
            return BigDecimal.ZERO;
        }
        if (b2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return b1.divide(b2, 2, RoundingMode.DOWN);
    }

}
