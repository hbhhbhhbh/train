package com.hbh.train.member.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RefreshScope
@RestController
public class TestController {
    @Value("${test.nacos}")
    private String nacos;
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!"+nacos;
    }
}
