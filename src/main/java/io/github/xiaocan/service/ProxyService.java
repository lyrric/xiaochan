package io.github.xiaocan.service;


import io.github.xiaocan.model.HttpProxyInfo;

/**
 * Created on 2020-10-30.
 * 代理工具类，可以实现接口，从而使用自己的代理源
 * @author wangxiaodong
 */
public interface ProxyService {


    /**
     * 获取一个代理信息
     * @return
     */
    HttpProxyInfo getOne();

    /**
     * 移除一个代理信息
     * @param httpProxyInfo
     */
    void removeProxy(HttpProxyInfo httpProxyInfo);

}
