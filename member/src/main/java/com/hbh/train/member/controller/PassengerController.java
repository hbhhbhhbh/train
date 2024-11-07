package com.hbh.train.member.controller;

import com.hbh.train.common.resp.CommonResp;
import com.hbh.train.member.req.PassengerSaveReq;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hbh.train.member.service.PassengerService;
@RestController
@RequestMapping("/passenger")
public class PassengerController {
    @Resource
    private PassengerService PassengerService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody PassengerSaveReq req)
    {

         PassengerService.save(req);
        return new CommonResp<>();
    }

}
