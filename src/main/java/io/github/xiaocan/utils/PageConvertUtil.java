package io.github.xiaocan.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.xiaocan.config.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * @author wangxiaodong
 * @date 2026/4/17
 */
@Slf4j
public class PageConvertUtil {

    private PageConvertUtil() {
    }

    public static <T> Page<T> convert(Page<?> page, Class<T> clazz) {
        Page<T> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        page.getRecords().forEach(item -> {
            try {
                T t = clazz.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(item, t);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error(e.getMessage(), e);
                throw new BusinessException("数据转换失败");
            }
        });
        return result;
    }
}
