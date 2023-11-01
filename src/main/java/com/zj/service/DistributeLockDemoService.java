package com.zj.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/11/1 16:35
 */
@Service
public class DistributeLockDemoService {

    @Autowired
    RedissonClient redissonClient;

    /**
     * 获取到锁的执行，未获取到的放弃操作
     * @return
     */
    public String threadLock() {
        RLock lock = redissonClient.getLock("vehicle-lock");
        System.out.println("当前线程:" + Thread.currentThread().getName());
        //加锁
        try {
            if (!lock.tryLock(1, TimeUnit.SECONDS)) {
                String message = "线程:" + Thread.currentThread().getName() + "未获取到锁,直接返回";
                System.out.println(message);
                return message;
            }
            System.out.println("线程:" + Thread.currentThread().getName() + "获取到锁!");
            Thread.sleep(4000_0);
            System.out.println("线程:" + Thread.currentThread().getName() + "业务结束!");
        } catch (InterruptedException e) {
            throw new RuntimeException(String.format("出现异常：%s",e.getMessage()));
        } finally {
            //判断 拿到了锁的才释放锁,否则会报错!
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                System.out.println("线程:" + Thread.currentThread().getName() + "释放锁!");
                lock.unlock();
            }
        }
        return "线程:" + Thread.currentThread().getName() + "业务结束!";
    }

    /**
     *获取到锁的执行，未获取到待锁释放后再争抢获取锁执行
     */
    public String threadLock2() {
        RLock lock = redissonClient.getLock("vehicle-lock");
        System.out.println("当前线程:" + Thread.currentThread().getName());
        //加锁
        lock.lock();
        try {
            System.out.println("线程:" + Thread.currentThread().getName() + "获取到锁!");
            Thread.sleep(10000);
            System.out.println("线程:" + Thread.currentThread().getName() + "业务结束!");
        } catch (InterruptedException e) {
            throw new RuntimeException(String.format("出现异常：%s",e.getMessage()));
        } finally {
            //判断 拿到了锁的才释放锁,否则会报错!
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                System.out.println("线程:" + Thread.currentThread().getName() + "释放锁!");
                lock.unlock();
            }
        }
        return "线程:" + Thread.currentThread().getName() + "业务结束!";
    }


    /**
     *公平锁使用详情示例
     */
    public String threadFairLock() {
        RLock lock = redissonClient.getFairLock("vehicle-fair-lock");
        System.out.println("当前线程:" + Thread.currentThread().getName());
        //加锁
        lock.lock();
        try {
            System.out.println("线程:" + Thread.currentThread().getName() + "获取到锁!");
            Thread.sleep(1000_0);
            System.out.println("线程:" + Thread.currentThread().getName() + "业务结束!");
        } catch (InterruptedException e) {
            throw new RuntimeException(String.format("出现异常：%s",e.getMessage()));
        } finally {
            //判断 拿到了锁的才释放锁,否则会报错!
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                System.out.println("线程:" + Thread.currentThread().getName() + "释放锁!");
                lock.unlock();
            }
        }
        return "线程:" + Thread.currentThread().getName() + "业务结束!";
    }

}
