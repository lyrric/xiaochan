package io.github.xiaocan.utils;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.github.xiaocan.model.vo.CityCodeVO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 城市区编码工具（基于 city-code.json）
 * <p>
 * 提供 城市编码 -> 城市名称 的查询能力。
 * 针对省-市-区县三级结构：
 * <ul>
 *     <li>若 code 对应“市”级别，返回该市名称</li>
 *     <li>若 code 对应“县/区”级别，返回其父“市”名称</li>
 *     <li>若 code 对应“省”或找不到，返回 null</li>
 * </ul>
 */
public final class CityCodeUtil {

    /** code -> 城市名称 缓存 */
    private static volatile Map<String, String> cityNameMap;

    private CityCodeUtil() {
    }

    /**
     * 根据城市区编码解析城市名称（带“市”后缀）
     *
     * @param code 城市区编码
     * @return 城市名称，无法解析时返回 null
     */
    public static String getCityName(Integer code) {
        if (code == null) {
            return null;
        }
        return getCityName(String.valueOf(code));
    }

    public static String getCityName(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        ensureLoaded();
        return cityNameMap.get(code);
    }

    private static void ensureLoaded() {
        if (cityNameMap != null) {
            return;
        }
        synchronized (CityCodeUtil.class) {
            if (cityNameMap != null) {
                return;
            }
            Map<String, String> map = new ConcurrentHashMap<>();
            try {
                ClassPathResource resource = new ClassPathResource("city-code.json");
                String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                List<CityCodeVO> root = JSONObject.parseObject(json, new TypeReference<List<CityCodeVO>>() {
                });
                if (root != null) {
                    for (CityCodeVO province : root) {
                        buildMap(province, null, map);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("加载 city-code.json 失败", e);
            }
            cityNameMap = map;
        }
    }

    /**
     * 递归构建 code -> 城市名称 的映射。
     * 逻辑：若节点有 child，说明它是“省/市”层级；若节点无 child，说明是叶子节点（县/区）。
     * 对于“市”级别节点：把自己和所有叶子节点都映射到自己名称。
     * 对于“省”级别节点（含直辖市，如北京市、上海市等）：
     *   - 直辖市下直接是区县，把区县映射到直辖市名称；
     *   - 其他省跳过省本身，仅递归到市/区县层级处理。
     */
    private static void buildMap(CityCodeVO node, CityCodeVO parent, Map<String, String> map) {
        if (node == null) {
            return;
        }
        boolean isProvinceLevel = isProvinceCode(node.getCode());
        boolean hasChildren = node.getChild() != null && !node.getChild().isEmpty();

        if (!hasChildren) {
            // 叶子节点：县/区级
            // 优先取最近的“市级”祖先（即 parent）
            String cityName = parent != null ? parent.getName() : node.getName();
            map.put(node.getCode(), cityName);
            return;
        }

        if (!isProvinceLevel) {
            // 市级别节点：把自身 code 映射到自身名称；同时把其下所有叶子（区县）也映射到自身名称
            map.put(node.getCode(), node.getName());
            for (CityCodeVO child : node.getChild()) {
                map.put(child.getCode(), node.getName());
            }
        } else {
            // 省级节点：
            // - 直辖市（如 110000 北京市）的 child 是区县，没有二级市级节点
            // - 普通省份的 child 是市级节点
            boolean municipality = isMunicipality(node.getCode());
            if (municipality) {
                // 直辖市：将区县直接映射到省名
                for (CityCodeVO child : node.getChild()) {
                    map.put(child.getCode(), node.getName());
                }
            } else {
                // 普通省份：继续递归到市级节点
                for (CityCodeVO child : node.getChild()) {
                    buildMap(child, node, map);
                }
            }
        }
    }

    /**
     * 判断是否是省级编码（前两位非 00，后四位全为 0）
     */
    private static boolean isProvinceCode(String code) {
        if (code == null || code.length() != 6) {
            return false;
        }
        return code.substring(2).equals("0000");
    }

    /**
     * 判断是否是直辖市（北京 11、天津 12、上海 31、重庆 50）
     */
    private static boolean isMunicipality(String code) {
        if (code == null || code.length() < 2) {
            return false;
        }
        String head = code.substring(0, 2);
        return "11".equals(head) || "12".equals(head) || "31".equals(head) || "50".equals(head);
    }
}
