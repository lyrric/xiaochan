package io.github.xiaocan.http;

import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.vo.AddressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
public class XiaochanHttp {


    private static final String BASE_URL = "https://gw.xiaocantech.com/rpc";
    private static final String SERVER_NAME = "SilkwormRec";
    private static final String METHOD_NAME = "RecService.GetStorePromotionList";


    private static final int PAGE_SIZE = 30;

    /**
     * 获取Ashe
     * @param timeMillis X-Garen
     * @return
     */
    private static String getAshe(Long timeMillis, String serverName, String methodName, String nami) {
        String x = MD5.create().digestHex((serverName + "." + methodName).toLowerCase());
        return MD5.create().digestHex(x + timeMillis + nami);
    }


    public List<StoreInfo> getList(Integer cityCode, String longitude, String latitude, int offset){
        String reqBody = getBody(cityCode, longitude, latitude, offset, 0, 0);
        String resBody = postWithRes(BASE_URL, reqBody, cityCode, SERVER_NAME, METHOD_NAME);
        return parseListBody(resBody);
    }



    public List<StoreInfo> searchList(String keyword, Integer cityCode, String longitude, String latitude, int offset, Integer number) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("silk_id", 0);
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
        String nami = getNami();
        String ashe = getAshe(timeMillis, serverName, methodName,nami);
        HttpResponse response = HttpUtil.createPost(url)
                .headerMap(getHeaders(timeMillis, ashe, cityCode, serverName, methodName, nami), true)
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
        Map<String, Object> bodyMap = Map.of("silk_id", 0, "keyword", keyword,
                "region", "", "page_size", 20, "page", 1, "app_id", 20);
        try {
            Long timeMillis = System.currentTimeMillis();
            String nami = getNami();
            String ashe = getAshe(timeMillis, serverName, methodName,nami);
            HttpResponse response = HttpUtil.createPost(BASE_URL)
                    .headerMap(getHeaders(timeMillis, ashe, cityCode, serverName, methodName,nami), true)
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
     * @param promotionId
     * @return
     */
    public StoreInfo getStorePromotionDetail(Integer promotionId){
        Map<String, Integer> reqMap = Map.of("silk_id", 0,
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


    private static String getNami(){
        String uuid = generateUuid();
        uuid = uuid.replace("-", "");
        String silkId = "0";
        return uuid.substring(0, 4) + silkId + uuid.substring(4, 20 - silkId.length() - 4);
    }

    private static String getBody(Integer cityCode, String longitude, String latitude, int offset, int promotionCategory, int storeCategory) {
        Map<String, Object> body = new HashMap<>();
        body.put("latitude", new BigDecimal(latitude));
        body.put("longitude", new BigDecimal(longitude));
        body.put("promotion_sort", 3);
        body.put("store_type", 0);
        body.put("offset", offset);
        body.put("number", PAGE_SIZE);
        body.put("silk_id", 0);
        body.put("promotion_filter", 0);
        body.put("promotion_category", promotionCategory);
        body.put("city_code", cityCode);
        body.put("store_category", storeCategory);
        body.put("store_platform", 0);
        body.put("app_id", 20);
        return JSONObject.toJSONString(body);
    }


    private Map<String, String> getHeaders(Long timeMillis, String ashe, Integer cityCode, String serverName, String methodName, String nami){
        Map<String, String> headers = new HashMap<>();
        headers.put("x-City", String.valueOf(cityCode));
        headers.put("X-Garen", String.valueOf(timeMillis));
        headers.put("X-Nami",nami);
        headers.put("X-Platform","mini");
        headers.put("version", "3.15.7.1");
        headers.put("appid", "20");
        headers.put("X-Model", "microsoft microsoft");
        headers.put("x-Annie", "XC");
        headers.put("xweb_xhr", "1");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Sec-Fetch-Site", "cross-site");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 MicroMessenger/7.0.20.1781(0x6700143B) NetType/WIFI MiniProgramEnv/Windows WindowsWechat/WMPF WindowsWechat(0x63090a13) UnifiedPCWindowsWechat(0xf254173b) XWEB/19027");
        headers.put("servername", serverName);
        headers.put("methodname", methodName);
        headers.put("X-Ashe", ashe);
        headers.put("Referer", "https://servicewechat.com/wx52ae84595214/965/page-frame.html");
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

    /**
     * 生成UUID字符串，模仿原始JavaScript版本的行为
     * @return UUID字符串
     */
    public static String generateUuid() {
        char[] chars = new char[36];
        String hexChars = "0123456789abcdef";
        Random random = new Random();
        // 填充随机十六进制字符
        for (int i = 0; i < 36; i++) {
            chars[i] = hexChars.charAt(random.nextInt(16));
        }
        // 设置特定位置确保UUID格式正确
        chars[14] = '4';  // UUID版本
        chars[19] = hexChars.charAt((chars[19] & 0x3) | 0x8);  // UUID变体
        chars[8] = chars[13] = chars[18] = chars[23] = '-';   // 分隔符
        return new String(chars);
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
