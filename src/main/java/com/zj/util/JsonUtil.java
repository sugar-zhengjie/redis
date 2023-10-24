package com.zj.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 14:21
 */
public class JsonUtil {
    private static ObjectMapper mapper;


    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    /**
     * 将对象序列化成json
     * @param obj
     * @return
     * @throws Exception
     */
    public static String serialize(Object obj) throws Exception {

        if (obj == null) {
            throw new IllegalArgumentException("obj should not be null");
        }

        return mapper.writeValueAsString(obj);
    }

    /**
     * 带泛型的反序列化，比如一个JSONArray反序列化成List<User>
     * @param jsonStr
     * @param collectionClass
     * @param elementClasses
     * @return
     * @throws Exception
     */
    public static <T> T deserialize(String jsonStr, Class<?> collectionClass,Class<?>... elementClasses) throws Exception {
        if("[]".equals(jsonStr)){
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
        return mapper.readValue(jsonStr, javaType);
    }

    /**
     * 将json字符串反序列化成对象
     * @param src 待反序列化的json字符串
     * @param t 反序列化成为的对象的class类型
     * @return
     * @throws Exception
     */
    public static <T> T deserialize(String src, Class<T> t) throws Exception {
        if (src == null) {
            throw new IllegalArgumentException("src should not be null");
        }

        if("{}".equals(src.trim())) {
            return null;
        }

        return mapper.readValue(src, t);
    }

}
