package com.zj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 13:21
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomCacheEvict {

    /**
     * 放入缓存时的value
     */
    String cachePutValue() default "";

    String keyField() default "";

}
