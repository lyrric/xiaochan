package io.github.xiaochan.http;

import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import io.github.xiaochan.core.BusinessException;
import io.github.xiaochan.model.HttpProxyInfo;
import io.github.xiaochan.model.Location;
import io.github.xiaochan.model.StoreInfo;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String getAshe(Long timeMillis) {
        String x = MD5.create().digestHex((SERVER_NAME + "." + METHOD_NAME).toLowerCase());
        return MD5.create().digestHex(x + timeMillis + NAMI);
    }

    public List<StoreInfo> getList(Location location, int offset, HttpProxyInfo httpProxyInfo){
        Long timeMillis = System.currentTimeMillis();
        String ashe = getAshe(timeMillis);
        HttpResponse response = HttpUtil.createPost(BASE_URL)
                .headerMap(getHeaders(timeMillis, ashe, location.getCityCode()), true)
                .timeout(3000)
                .setHttpProxy(httpProxyInfo.getIp(), httpProxyInfo.getPort())
                .body(getBody(location, offset))
                .execute();
        if (!response.isOk()) {
            throw new BusinessException("状态码错误:" + response.getStatus());
        }
        String body = response.body();
        response.close();
        return parseBody(body);
    }

    private static String getBody(Location location, int offset){
        Map<String, Object> body = new HashMap<>();
        body.put("latitude", new BigDecimal(location.getLatitude()));
        body.put("longitude", new BigDecimal(location.getLongitude()));
        body.put("promotion_sort", 3);
        body.put("store_type", 0);
        body.put("offset", offset);
        body.put("number", PAGE_SIZE);
        body.put("silk_id", 89715435);
        body.put("promotion_filter", 0);
        body.put("promotion_category", 0);
        body.put("city_code", location.getCityCode());
        body.put("store_category", 0);
        body.put("store_platform", 0);
        body.put("app_id", 20);
        return JSONObject.toJSONString(body);
    }


    private Map<String, String> getHeaders(Long timeMillis, String ashe, Integer cityCode){
        Map<String, String> headers = new HashMap<>();
        headers.put("x-City", String.valueOf(cityCode));
        headers.put("X-Garen", String.valueOf(timeMillis));
        headers.put("X-Nami", NAMI);
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 MicroMessenger/7.0.20.1781(0x6700143B) NetType/WIFI MiniProgramEnv/Windows WindowsWechat/WMPF WindowsWechat(0x63090c33)XWEB/13487");
        headers.put("servername", SERVER_NAME);
        headers.put("methodname", METHOD_NAME);
        headers.put("X-Ashe", ashe);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private List<StoreInfo> parseBody(String body){
        JSONObject jsonBody = JSONObject.parseObject(body);
        if (jsonBody.getJSONObject("status").getInteger("code") != 0) {
            throw new BusinessException("请求失败:" + body);
        }
        List<StoreInfo> result = new ArrayList<>();
        JSONArray promotionList = jsonBody.getJSONArray("promotion_list");
        for (int i = 0; i < promotionList.size(); i++) {
            JSONObject jsonObject =  promotionList.getJSONObject(i);
            StoreInfo storeInfo = new StoreInfo();
            storeInfo.setName(jsonObject.getJSONObject("store").getString("name"));
            storeInfo.setOpenHours(jsonObject.getJSONObject("store").getString("opening_hours"));
            storeInfo.setPromotionId(jsonObject.getInteger("promotion_id"));
            storeInfo.setRebateCondition(jsonObject.getInteger("rebate_condition"));
            storeInfo.setStartTime(jsonObject.getString("start_time_hour") + ":" + jsonObject.getString("start_time_minute"));
            storeInfo.setEndTime(jsonObject.getString("end_time_hour") + ":" + jsonObject.getString("end_time_minute"));
            storeInfo.setDistance(jsonObject.getInteger("distance") );
            if (jsonObject.getInteger("meituan_status") == 1) {
                StoreInfo meituanStoreInfo = new StoreInfo();
                BeanUtils.copyProperties(storeInfo, meituanStoreInfo);
                meituanStoreInfo.setType(1);
                meituanStoreInfo.setLeftNumber(jsonObject.getInteger("meituan_left_number"));
                meituanStoreInfo.setPrice(safeDivide(jsonObject.getBigDecimal("meituan_order_money"), BigDecimal.valueOf(100)));
                meituanStoreInfo.setRebatePrice(safeDivide(jsonObject.getBigDecimal("meituan_user_rebate"), BigDecimal.valueOf(100)));
                result.add(meituanStoreInfo);
            }
            if (jsonObject.getInteger("eleme_status") == 1) {
                StoreInfo eleStoreInfo = new StoreInfo();
                BeanUtils.copyProperties(storeInfo, eleStoreInfo);
                eleStoreInfo.setType(2);
                eleStoreInfo.setLeftNumber(jsonObject.getInteger("eleme_left_number"));
                eleStoreInfo.setPrice(safeDivide(jsonObject.getBigDecimal("eleme_order_money"), BigDecimal.valueOf(100)));
                eleStoreInfo.setRebatePrice(safeDivide(jsonObject.getBigDecimal("eleme_user_rebate"),BigDecimal.valueOf(100)));
                result.add(eleStoreInfo);
            }
        }
        return result;
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
