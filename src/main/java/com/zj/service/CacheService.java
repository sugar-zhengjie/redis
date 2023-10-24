package com.zj.service;

import com.zj.annotation.CustomCacheEvict;
import com.zj.annotation.CustomCacheable;
import org.springframework.stereotype.Service;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 15:32
 */
@Service
public class CacheService {

    @CustomCacheable(methodKey = "cacheService.getName",fieldKey = "#id")
    public String getName(String id){
        return id + "zj";
    }

    @CustomCacheable(methodKey = "cacheService.getEmail",fieldKey = "#id")
    public String getEmail(String id){
        return id + "zj";
    }

    @CustomCacheEvict(cachePutValue = "cacheService.getName,cacheService.getEmail", keyField = "#id#lang")
    public Boolean saveUser(String id){
        return true;
    }
}
