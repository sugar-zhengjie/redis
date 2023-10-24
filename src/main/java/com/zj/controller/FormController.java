package com.zj.controller;

import com.zj.annotation.AntiReplay;
import com.zj.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhengjie
 * @version 1.0
 * @date 2023/10/24 17:00
 */
@RestController
public class FormController {

    @Autowired
    private FormService formService;

    @AntiReplay
    @PostMapping("/{id}")
    public Object addOrder(@PathVariable String id){
        return formService.addOrder(id);
    }
}
