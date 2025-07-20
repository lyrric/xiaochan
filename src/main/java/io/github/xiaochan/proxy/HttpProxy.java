package io.github.xiaochan.proxy;


import io.github.xiaochan.model.HttpProxyInfo;

import java.util.List;

/**
 * Created on 2020-10-30.
 * 代理工具类，可以实现接口，从而使用自己的代理源
 * @author wangxiaodong
 */
public interface HttpProxy {

    /**
     * 获取一批代理信息
     * @param count 数量
     * @return
     */
    List<HttpProxyInfo> getList(int count);

    /**
     * 获取一个代理信息
     * @return
     */
    HttpProxyInfo getOne();


}
