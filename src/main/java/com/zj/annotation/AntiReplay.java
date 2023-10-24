package com.zj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 16:31
 *
 * 防止重复提交，通过分布式锁，限制同一个api并发时多次重复提交
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AntiReplay {
    /**
     * 获取锁重试时间，默认0ms
     * 发生竞争的时候，重试获取锁的最长等待时间，在该段时间内任然没有获取到锁，则失败
     */
    int tryLockTime() default 0;

    /**
     * 自定义key，不填的话，采用当前用户名+url，或者ip+url的形式
     * 如果需要自定义key，请填写此参数
     */
    String key() default "";
}

