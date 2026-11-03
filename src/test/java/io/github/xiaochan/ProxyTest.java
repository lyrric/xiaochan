package io.github.xiaochan;

import io.github.xiaocan.http.XiaochanHttp;
import io.github.xiaocan.model.StoreInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author lyrric
 * @date 2026/10/15
 */
@Slf4j
public class ProxyTest {


    @Test
    public void testProxy() throws IOException {
        List<StoreInfo> list1 = XiaochanHttp.getList(510116, "104.063049", "30.569082", 0);
        List<StoreInfo> list2 = XiaochanHttp.getList(510116, "104.063049", "30.569082", 20);
        list1.stream().filter(data1 -> list2.stream().anyMatch(data2 -> Objects.equals(data1.getUniqId(), data2.getUniqId()))).forEach(System.out::println);



    }
}
