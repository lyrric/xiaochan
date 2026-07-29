package io.github.xiaocan.controller;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import io.github.xiaocan.config.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 图片中转接口
 * 用于代理第三方图片（如歪麦门店 logo），规避防盗链/跨域问题
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/image")
public class ImageProxyController {

    /**
     * 中转接口路径，供业务层拼接 icon 使用
     */
    public static final String PROXY_PATH = "/api/image/proxy?url=";

    private static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) " +
            "AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.49";

    /**
     * 图片中转
     *
     * @param url 原始图片地址（http/https）
     * @return 图片二进制流
     */
    @GetMapping(value = "/proxy")
    public ResponseEntity<byte[]> proxy(@RequestParam("url") String url) {
        if (StringUtils.isBlank(url) || !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new BusinessException("非法的图片地址");
        }
        try (HttpResponse response = HttpUtil.createGet(url)
                .header("User-Agent", USER_AGENT)
                .timeout(10000)
                .execute()) {
            if (!response.isOk()) {
                log.warn("图片中转失败, url: {}, 状态码: {}", url, response.getStatus());
                return ResponseEntity.status(response.getStatus()).build();
            }
            byte[] body = response.bodyBytes();
            String contentType = response.header("Content-Type");
            MediaType mediaType = MediaType.IMAGE_JPEG;
            if (StringUtils.isNotBlank(contentType) && contentType.startsWith("image/")) {
                mediaType = MediaType.parseMediaType(contentType);
            }
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                    .body(body);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("图片中转异常, url: {}", url, e);
            throw new BusinessException("图片中转异常: " + e.getMessage());
        }
    }
}
