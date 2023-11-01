package com.zj.annotation;

import com.zj.aspect.DistributedLockAspect;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/11/1 15:07
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DistributedLockAspect.class})
public @interface EnableDistributedLock {
}
