package com.zj.distributedLockDemo;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/11/2 9:30
 *
 * redis实现分布式锁的探索
 *
 */
@Component
public class Demo {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    public void one(){
        // 抢占锁
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock","123");
        if(Boolean.TRUE.equals(lock)){
            // 占锁成功，执行业务
            System.out.println("开始执行业务代码！！！");
            // 解锁
            redisTemplate.delete("lock");
        }else{
            // 抢占锁失败，自定义处理方法
            System.out.println("我没抢占到锁！！！");
        }
    }

    public void two(){
        // 抢占锁
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock","123");
        if(Boolean.TRUE.equals(lock)){
            // 给抢占的锁设置过期时间
            redisTemplate.expire("lock",10, TimeUnit.SECONDS);
            // 占锁成功，执行业务
            System.out.println("开始执行业务代码！！！");
            // 解锁
            redisTemplate.delete("lock");
        }else{
            // 抢占锁失败，自定义处理方法
            System.out.println("我没抢占到锁！！！");
        }
    }

    public void three(){
        // 抢占锁 同时设置过期时间
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock","123",10,TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(lock)){
            // 占锁成功，执行业务
            System.out.println("开始执行业务代码！！！");
            // 解锁
            redisTemplate.delete("lock");
        }else{
            // 抢占锁失败，自定义处理方法
            System.out.println("我没抢占到锁！！！");
        }
    }

    public void four(){
        // 设置唯一ID
        String id = UUID.randomUUID().toString();
        // 抢占锁
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock",id,10,TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(lock)){
            // 占锁成功，执行业务
            System.out.println("开始执行业务代码！！！");
            // 业务执行完成 获取该锁的值
            String lockValue = redisTemplate.opsForValue().get("lock");
            if(id.equals(lockValue)){
                redisTemplate.delete("lock");
            }
            redisTemplate.delete("lock");
        }else{
            // 抢占锁失败，自定义处理方法
            System.out.println("我没抢占到锁！！！");
        }
    }

    public void five(){
        // 设置唯一ID
        String id = UUID.randomUUID().toString();
        // 抢占锁
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock",id,10,TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(lock)){
            // 占锁成功，执行业务
            System.out.println("开始执行业务代码！！！");
            // 业务执行完成 脚本解锁 保证原子性
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Collections.singletonList("lock"), id);

            redisTemplate.delete("lock");
        }else{
            // 抢占锁失败，自定义处理方法
            System.out.println("我没抢占到锁！！！");
        }
    }

    public void six() throws InterruptedException {
        //获取锁(可重入)，指定锁的名称
        RLock lock = redissonClient.getLock("anyLock");
        //尝试获取锁，参数分别是：获取锁的最大等待时间(期间会重试)，锁自动释放时间，时间单位
        boolean isLock = lock.tryLock(1,10,TimeUnit.SECONDS);
        //判断获取锁成功
        if(isLock){
            try{
                System.out.println("执行业务");
            }finally{
                //释放锁
                if(lock.isHeldByCurrentThread() && lock.isLocked()){
                    lock.unlock();
                }
            }

        }
    }

}
