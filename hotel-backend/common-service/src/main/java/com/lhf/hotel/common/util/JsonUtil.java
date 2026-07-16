package com.lhf.hotel.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

/**
 * JSON 工具 —— 基于 FastJSON2
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    /** 对象 → JSON 字符串 */
    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    /** 对象 → 格式化 JSON（含缩进） */
    public static String toPrettyJson(Object obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
    }

    /** JSON 字符串 → 对象 */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
}
