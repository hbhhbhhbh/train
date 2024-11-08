package com.hbh.train.member.controller;

import com.hbh.train.common.resp.CommonResp;
import com.hbh.train.member.req.MemberLoginReq;
import com.hbh.train.member.req.MemberRegisterReq;
import com.hbh.train.member.req.MemberSendCodeReq;
import com.hbh.train.member.resp.MemberLoginResp;
import com.hbh.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberService memberService;
    @GetMapping ("/count")
    public CommonResp<Integer> count()
    {
        int count= memberService.count();
        CommonResp<Integer>commonResp=new CommonResp<>();
        commonResp.setContent(count);
        return commonResp;
    }
    @PostMapping("/register")
    public CommonResp<Long> register(@Valid MemberRegisterReq req)
    {

        long resp= memberService.register(req);
//        CommonResp<Long>commonResp=new CommonResp<>();
//        commonResp.setContent(resp);
//        return commonResp;
        return new CommonResp<Long>(resp);
    }
    @PostMapping("/send-code")
    public CommonResp<Long> sendCode(@Valid @RequestBody MemberSendCodeReq req)
    {
       memberService.sendCode(req);
//        CommonResp<Long>commonResp=new CommonResp<>();
//        commonResp.setContent(resp);
//        return commonResp;
        return new CommonResp<Long>();
    }

    @PostMapping("/login")
    public CommonResp<MemberLoginResp> login(@Valid @RequestBody MemberLoginReq req)
    {
        MemberLoginResp resp= memberService.login(req);
//        CommonResp<Long>commonResp=new CommonResp<>();
//        commonResp.setContent(resp);
//        return commonResp;
        return new CommonResp<MemberLoginResp>(resp);
    }
}
