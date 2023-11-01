package com.zj.distributedLock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Objects;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/11/1 14:00
 *
 * 通过注解@AllArgsConstructor与@Getter的方式自动生成字段的构造函数
 *
 * AutoCloseable是Java 7引入的一个接口，用于处理需要关闭的资源，用途是在try-with-resources语句中，这是一种异常处理的模式，可以自动关闭在try块中打开的资源
 */
@AllArgsConstructor
public class ILock implements AutoCloseable{
    /**
     * 持有的锁对象
     */
    @Getter
    private Object lock;

    /**
     * 分布式锁接口
     */
    @Getter
    private IDistributedLock distributedLock;

    @Override
    public void close() throws Exception {
        if(Objects.nonNull(lock)){
            distributedLock.unLock(lock);
        }
    }
}
