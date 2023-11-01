package com.zj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 11:11
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomCacheable {
    // 类名（去除包名）.方法
    String methodKey() default "";
    // 关键字段 #字段1+#字段2
    String fieldKey() default "";
    long expire() default 300;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    // 泛型的Class类型
    Class<?> type() default Exception.class;
}
