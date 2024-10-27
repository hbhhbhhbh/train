package com.hbh.train.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class MemberController {
    @GetMapping ("/hello")
    public String hello()
    {
        return "Hello,world!";
    }
}
