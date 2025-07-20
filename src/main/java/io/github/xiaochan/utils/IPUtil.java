package io.github.xiaochan.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;

public class IPUtil {

    public static String getIp() {
        HttpRequest post = HttpUtil.createGet("https://ip.3322.net/");
        post.timeout(3000);
        String body = post
                .execute().body();
        return body;
    }

    public static void main(String[] args) {
        System.out.println(getIp());
    }
}
