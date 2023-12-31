package com.zj;

import com.zj.annotation.EnableDistributedLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 11:09
 */
@SpringBootApplication
@EnableDistributedLock
public class RedisApplication {
    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class,args);
    }
}
