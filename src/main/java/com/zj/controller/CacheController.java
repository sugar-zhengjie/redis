package com.zj.controller;

import com.zj.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 15:31
 */
@RestController
public class CacheController {

    @Autowired
    private CacheService cacheService;

    @GetMapping("/name/{id}")
    public Object getName(@PathVariable String id){
        return cacheService.getName(id);
    }

    @GetMapping("/email/{id}")
    public Object getEmail(@PathVariable String id){
        return cacheService.getEmail(id);
    }

    @PostMapping("/update/{id}")
    public Object saveUser(@PathVariable String id){
        return cacheService.saveUser(id);
    }
}
