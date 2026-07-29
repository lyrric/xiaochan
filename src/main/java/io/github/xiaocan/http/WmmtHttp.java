package io.github.xiaocan.http;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.xiaocan.config.BusinessException;
import io.github.xiaocan.controller.ImageProxyController;
import io.github.xiaocan.model.StoreInfo;
import io.github.xiaocan.model.dto.WmmtShopListDTO;
import io.github.xiaocan.model.enums.StoreTypeEnum;
import io.github.xiaocan.model.vo.WmPageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * 歪麦霸王餐（waimaimingtang）小程序接口 HTTP 客户端
 * <p>
 * 对应小程序  的加密请求逻辑：
 * 1. fetchKeys: POST /api/v2/index/newServiceConfig 拉取动态 RSA 密钥（旧 request 接口，固定 AES 密钥）
 * 2. getShopList: POST /bwc/waimaimt-web-bwc/shopIndex/getShopList 获取门店列表（encryptedRequest，动态 AES 密钥）
 */
@Slf4j
public class WmmtHttp {

    // ====== 源码兜底密钥（小程序启动后会通过 newServiceConfig 接口刷新）======
    private static final String SOURCE_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKoR8mX0rGKLqzcWmOzbfj64K8ZIgOdHnzkXSOVOZbFu/TJhZ7rFAN+eaGkl3C4buccQd/EjEsj9ir7ijT7h96MCAwEAAQ==\n" +
            "-----END PUBLIC KEY-----";

    private static final String SOURCE_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAmc3CuPiGL/LcIIm7zryCEIbl1SPzBkr75E2VMtxegyZ1lYRD+7TZGAPkvIsBcaMs6Nsy0L78n2qh+lIZMpLH8wIDAQABAkEAk82Mhz0tlv6IVCyIcw/s3f0E+WLmtPFyR9/WtV3Y5aaejUkU60JpX4m5xNR2VaqOLTZAYjW8Wy0aXr3zYIhhQQIhAMfqR9oFdYw1J9SsNc+CrhugAvKTi0+BF6VoL6psWhvbAiEAxPPNTmrkmrXwdm/pQQu3UOQmc2vCZ5tiKpW10CgJi8kCIFGkL6utxw93Ncj4exE/gPLvKcT+1Emnoox+O9kRXss5AiAMtYLJDaLEzPrAWcZeeSgSIzbL+ecokmFKSDDcRske6QIgSMkHedwND1olF8vlKsJUGK3BcdtM8w4Xq7BpSBwsloE=\n" +
            "-----END PRIVATE KEY-----";

    // sign 的 AES 密钥
    private static final String SIGN_AES_KEY = "asdf545asdf4545d";
    // 旧 request 接口（newServiceConfig）的固定 AES 密钥
    private static final String LEGACY_AES_KEY = "jnd674751fh6fkgu";

    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    // 接口地址
    private static final String BASE_URL = "https://fz-gateway.waimaimingtang.com/api/";
    private static final String BASE_URL_V2 = "https://wmapp-api-v2.waimaimingtang.com/api";


    // 缓存的动态密钥
    private static volatile String cachedPublicKey;
    private static volatile String cachedPrivateKey;
    private static volatile String cachedH5PublicKey;

    // ====== 对外暴露的方法 ======

    private static void checkAndFetchKeys(String token, String city) {
        if (cachedPublicKey == null || cachedPrivateKey == null) {
            fetchKeys(token, city);
        }
    }
    /**
     * 拉取并缓存服务端下发的最新 RSA 密钥
     * <p>
     * 对应小程序 app.js#getVersion / index.js#getVersion
     *
     * @param token 登录 token
     * @param city  城市名
     */
    private static void fetchKeys(String token, String city) {
        // 旧 request 接口：{json: AES(JSON.stringify({city: ...}))}
        JSONObject reqBody = new JSONObject();
        reqBody.put("city", city != null ? city : "");
        String encryptedJson = aesEncrypt(reqBody.toJSONString(), LEGACY_AES_KEY);

        JSONObject wrapper = new JSONObject();
        wrapper.put("json", encryptedJson);
        String requestBody = wrapper.toJSONString();

        Map<String, String> headers = buildCommonHeaders(token, city);
        headers.put("content-type", "application/json");
        final String CONFIG_URL = BASE_URL + "api/v2/index/newServiceConfig";
        try (HttpResponse response = HttpUtil.createPost(CONFIG_URL)
                .headerMap(headers, true)
                .timeout(10000)
                .body(requestBody)
                .execute()) {

            String resBody = response.body();
            log.info("newServiceConfig 状态码: {}, 响应: {}", response.getStatus(), resBody);

            if (!response.isOk()) {
                log.error("拉取密钥失败, 状态码: {}", response.getStatus());
                throw new BusinessException("拉取密钥失败: " + response.getStatus());
            }

            JSONObject outer = JSONObject.parseObject(resBody);
            if (outer.getIntValue("code") != 1) {
                log.error("拉取密钥业务失败: {}", resBody);
                throw new BusinessException("拉取密钥业务失败: " + outer.getString("message"));
            }

            // data 字段是 AES 加密后的字符串
            String encryptedData = outer.getString("data");
            String decryptedData = aesDecrypt(encryptedData, LEGACY_AES_KEY);
            JSONObject data = JSONObject.parseObject(decryptedData);

            cachedPrivateKey = data.getString("privateKey");
            cachedPublicKey = data.getString("publicKey");
            cachedH5PublicKey = data.getString("h5PublicKey");

            log.info("密钥拉取成功, publicKey长度: {}, privateKey长度: {}",
                    cachedPublicKey != null ? cachedPublicKey.length() : 0,
                    cachedPrivateKey != null ? cachedPrivateKey.length() : 0);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("拉取密钥异常", e);
            throw new BusinessException("拉取密钥异常: " + e.getMessage());
        }
    }

    /**
     * 获取门店列表（封装请求参数版本）
     *
     * @param token 登录 token
     * @param city  城市名（如"长沙市"）
     * @param dto   请求参数
     * @return 解密后的响应
     */
    public static WmPageVO getShopList(String token, String city, WmmtShopListDTO dto) {
        Object scrollPageData = null;
        if (dto.getPvId() != null && !dto.getPvId().isEmpty()) {
            JSONObject pageData = new JSONObject();
            pageData.put("overbearScrollId", dto.getPvId());
            scrollPageData = pageData;
        }
        return getShopList(token, city, dto.getLongitude(), dto.getLatitude(), scrollPageData, dto.getName());
    }

    /**
     * 获取门店列表（对应小程序 mem.js#newStoreList）
     *
     * @param token      登录 token
     * @param city       城市名
     * @param longitude  经度
     * @param latitude   纬度
     * @param scrollPageData 上一页游标（首页传 null）
     * @return 解密后的响应 JSON
     */
    public static WmPageVO getShopList(String token, String city, String longitude, String latitude,
                                         Object scrollPageData, String name) {
        checkAndFetchKeys(token, city);
        // 优先使用缓存的动态密钥，否则回退到源码兜底密钥
        String publicKeyPem = cachedPublicKey != null ? cachedPublicKey : SOURCE_PUBLIC_KEY;
        String privateKeyPem = cachedPrivateKey != null ? cachedPrivateKey : SOURCE_PRIVATE_KEY;

        try {
            PublicKey serverPublicKey = loadPublicKey(publicKeyPem);
            PrivateKey clientPrivateKey = loadPrivateKey(privateKeyPem);

            // 构造请求参数
            JSONObject params = new JSONObject();
            params.put("userLongitude", longitude != null ? longitude : "104.08329");
            params.put("userLatitude", latitude != null ? latitude : "30.65618");
            params.put("city", city != null ? city : "长沙市");
            params.put("limit", 15);
            params.put("sortWay", "comprehensive");
            params.put("categoryId", "");
            params.put("shopName", name);
            params.put("secKillFlag", 1);
            params.put("signUpFlag", 2);
            params.put("highRebatesFlag", 0);
            params.put("noCommentFlag", 0);
            params.put("takeawayPlatform", "");
            params.put("userTypes", new int[]{1, 2, 3});
            params.put("packageType", "");
            params.put("scrollPageData", scrollPageData);
            params.put("threeKmFlag", "");
            params.put("tabType", "bwc");

            // 生成随机 AES 密钥并加密请求体
            String aesKey = generateRandomString(32);
            String encryptedBody = aesEncrypt(params.toJSONString(), aesKey);
            String encryptKey = rsaEncryptBase64Key(aesKey, serverPublicKey);

            // 构建请求头
            Map<String, String> headers = buildCommonHeaders(token, city);
            headers.put("content-type", "application/json");
            headers.put("encrypt-key", encryptKey);

            final String SHOP_LIST_URL = BASE_URL_V2 + "/bwc/waimaimt-web-bwc/shopIndex/getShopList";
            try (HttpResponse response = HttpUtil.createPost(SHOP_LIST_URL)
                    .headerMap(headers, true)
                    .timeout(10000)
                    .body(encryptedBody)
                    .execute()) {

                if (!response.isOk()) {
                    log.error("getShopList 请求失败, 状态码: {}, body: {}", response.getStatus(), response.body());
                    throw new BusinessException("请求失败: " + response.getStatus());
                }

                // 解密响应
                String responseEncryptKey = response.header("encrypt-key");
                if (responseEncryptKey == null || responseEncryptKey.isEmpty()) {
                    responseEncryptKey = response.header("Encrypt-Key");
                }

                String resBody = response.body();
                if (responseEncryptKey != null && !responseEncryptKey.isEmpty()) {
                    try {
                        String responseAesKey = rsaDecryptEncryptKey(responseEncryptKey, clientPrivateKey);
                        String decryptedResponse = aesDecrypt(resBody, responseAesKey);
                        log.info("getShopList 解密响应: {}", decryptedResponse);
                        return parseShopListResponse(JSONObject.parseObject(decryptedResponse));
                    } catch (Exception e) {
                        log.error("响应解密失败", e);
                        throw new BusinessException("响应解密失败: " + e.getMessage());
                    }
                }

                // 没有 encrypt-key 头，尝试直接解析
                log.warn("响应无 encrypt-key 头, 直接返回原始 body");
                return parseShopListResponse(JSONObject.parseObject(resBody));
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("getShopList 异常", e);
            throw new BusinessException("getShopList 异常: " + e.getMessage());
        }
    }

    // ====== 内部工具方法 ======

    /**
     * 构建通用请求头（nonce/timestamp/sign 等）
     */
    private static Map<String, String> buildCommonHeaders(String token, String city) {
        Map<String, String> headers = new HashMap<>();
        headers.put("application", "overbear_one");
        if (token != null && !token.isEmpty()) {
            headers.put("token", token);
        }
        String nonce = generateRandomCode(16);
        String timestamp = String.valueOf(System.currentTimeMillis());
        headers.put("nonce", nonce);
        headers.put("timestamp", timestamp);
        headers.put("sign", aesEncrypt(timestamp + nonce, SIGN_AES_KEY));
        headers.put("city", URLUtil.encode(city != null ? city : "长沙市"));
        headers.put("appversion", "1.1.175");
        return headers;
    }

    /**
     * AES-128/ECB/PKCS7Padding 加密
     */
    private static String aesEncrypt(String plainText, String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES 加密失败", e);
            throw new BusinessException("AES 加密失败: " + e.getMessage());
        }
    }


    private static WmPageVO parseShopListResponse(JSONObject response) {
        if(response.getIntValue("code") != 200){
            log.error("获取数据失败: {}", response);
            throw new BusinessException("获取数据失败: " + response.getString("msg"));
        }
        WmPageVO wmPageVO = new WmPageVO();
        wmPageVO.setPagePvId(response.getJSONObject("data").getJSONObject("scrollPageData").getString("overbearScrollId"));
        JSONArray array = response.getJSONObject("data").getJSONArray("data");
        List<StoreInfo> storeInfoList = new ArrayList<>();
        wmPageVO.setStoreInfos(storeInfoList);
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            StoreInfo storeInfo = StoreInfo.builder()
                    .name(item.getString("shopName"))
                    .uniqId(item.getString("id"))
                    .storeTypeEnum(StoreTypeEnum.WM_MANJIAN)
                    .icon(item.getString("logoAddress"))
                    .distanceStr(item.getString("distance")).build();
                    //shopPlatformType 1满减，2：百分比返现
            if (item.getInteger("shopPlatformType") == 1) {
                storeInfo.setStoreTypeEnum(StoreTypeEnum.WM_MANJIAN);
            }else{
                storeInfo.setStoreTypeEnum(StoreTypeEnum.WM_MTSJ);
            }
            JSONArray skus = item.getJSONArray("overbearfoodList");
            for (int j = 0; j < skus.size(); j++) {
                JSONObject sku = skus.getJSONObject(j);
                StoreInfo skuStoreinfo = new StoreInfo();
                BeanUtils.copyProperties(storeInfo, skuStoreinfo);

                skuStoreinfo.setLeftNumber(sku.getInteger("surplusNumber"));
                skuStoreinfo.setType(getType(sku.getString("takeawayPlatform")));
                //releaseNumber 发布数量
                if (skuStoreinfo.getStoreTypeEnum() == StoreTypeEnum.WM_MANJIAN) {
                    skuStoreinfo.setPrice(sku.getJSONObject("maxGradeRebate").getBigDecimal("fullMoney"));
                    skuStoreinfo.setRebatePrice(sku.getJSONObject("maxGradeRebate").getBigDecimal("rebateMoney"));
                    skuStoreinfo.setPromotionId(sku.getString("id"));
                }else{
                    skuStoreinfo.setRebateRatio(sku.getBigDecimal("meituanRatio"));
                    //meituanVipRatio：会员返现比例
                    skuStoreinfo.setRebateMax(sku.getBigDecimal("maxReturnMoney"));
                    //美团赏金的没返，暂时用uniqId来替代
                    skuStoreinfo.setPromotionId(skuStoreinfo.getUniqId());
                }
                String mealType = sku.getString("mealType");
                skuStoreinfo.setDistanceStr(switch (mealType) {
                    case "overbear_food_no_evaluate", "return_money_food" -> "无需品鉴意见";
                    case "overbear_food_evaluate", "overbear_food_picture_evaluate" -> "需品鉴意见";
                    case "overbear_food_star_picture" -> "需星级带图";
                    case "overbear_food_star_word" -> "需星级文字";
                    case "overbear_food_word_picture" -> "需文字带图";
                    default -> "未知";
                });
                //releaseTimeQuantum:00:00:00-23:59:59
                String timeRange = sku.getString("releaseTimeQuantum");
                skuStoreinfo.setStartTime(StringUtils.isBlank(timeRange) ? "00:00" : timeRange.substring(0, 5));
                skuStoreinfo.setEndTime(StringUtils.isBlank(timeRange) ? "23:59" : timeRange.substring(9, 14));
                rewriteIconToProxy(skuStoreinfo);
                storeInfoList.add(skuStoreinfo);
            }
        }

        return wmPageVO;
    }

    /**
     * 将满减门店的原始 icon 拼接到图片中转接口上，前端直接访问中转接口
     */
    private static void rewriteIconToProxy(StoreInfo storeInfo) {
        if (storeInfo.getStoreTypeEnum() == StoreTypeEnum.WM_MANJIAN && StringUtils.isNotBlank(storeInfo.getIcon())) {
            storeInfo.setIcon(ImageProxyController.PROXY_PATH
                    + URLEncoder.encode(storeInfo.getIcon(), StandardCharsets.UTF_8));
        }
    }

    private static int getType(String platformType){
        //1:美团，2：饿了么，3京东
        return switch (platformType) {
            case "meituan" -> 1;
            case "ele" -> 2;
            default -> 3;
        };
    }
    /**
     * AES-128/ECB/PKCS7Padding 解密
     */
    private static String aesDecrypt(String cipherText, String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 解密失败", e);
            throw new BusinessException("AES 解密失败: " + e.getMessage());
        }
    }

    /**
     * 生成 encrypt-key：Base64(aesKey) -> RSA 公钥加密
     */
    private static String rsaEncryptBase64Key(String aesKey, PublicKey publicKey) throws Exception {
        String base64Key = Base64.getEncoder().encodeToString(aesKey.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(base64Key.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密 encrypt-key：RSA 私钥解密 -> Base64 解码 -> 得到原始 AES 密钥
     */
    private static String rsaDecryptEncryptKey(String encryptKey, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptKey));
        String base64Key = new String(decrypted, StandardCharsets.UTF_8);
        return new String(Base64.getDecoder().decode(base64Key), StandardCharsets.UTF_8);
    }

    private static PublicKey loadPublicKey(String pem) throws Exception {
        String key = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    private static PrivateKey loadPrivateKey(String pem) throws Exception {
        String key = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    /**
     * 与源码 generateRandomString(32) 一致：a-zA-Z0-9 中随机取 32 个字符
     */
    private static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 与源码 randomCode() 一致：16 位 0-9 随机数字
     */
    private static String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

}
