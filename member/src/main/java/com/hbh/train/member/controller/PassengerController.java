package com.hbh.train.member.controller;

import com.hbh.train.common.context.LoginMemberContext;
import com.hbh.train.common.resp.CommonResp;
import com.hbh.train.common.resp.PageResp;
import com.hbh.train.member.req.PassengerQueryReq;
import com.hbh.train.member.req.PassengerSaveReq;
import com.hbh.train.member.resp.PassengerQueryResp;
import com.hbh.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/query-list")
    public CommonResp<PageResp<PassengerQueryResp>> queryList(@Valid PassengerQueryReq req) {
        req.setMemberId(LoginMemberContext.getId());
        PageResp<PassengerQueryResp> list = PassengerService.queryList(req);
        return new CommonResp<>(list);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id)
    {
        PassengerService.delete(id);
        return new CommonResp<>();
    }
}
