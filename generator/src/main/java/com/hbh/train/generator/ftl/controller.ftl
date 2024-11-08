package com.hbh.train.member.controller;

import com.hbh.train.common.context.LoginMemberContext;
import com.hbh.train.common.resp.CommonResp;
import com.hbh.train.common.resp.PageResp;
import com.hbh.train.member.req.${Domain}QueryReq;
import com.hbh.train.member.req.${Domain}SaveReq;
import com.hbh.train.member.resp.${Domain}QueryResp;
import com.hbh.train.member.service.${Domain}Service;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${do_main}")
public class ${Domain}Controller {
    @Resource
    private ${Domain}Service ${Domain}Service;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody ${Domain}SaveReq req)
    {

         ${Domain}Service.save(req);
        return new CommonResp<>();
    }
    @GetMapping("/query-list")
    public CommonResp<PageResp<${Domain}QueryResp>> queryList(@Valid ${Domain}QueryReq req) {
        req.setMemberId(LoginMemberContext.getId());
        PageResp<${Domain}QueryResp> list = ${Domain}Service.queryList(req);
        return new CommonResp<>(list);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id)
    {
        ${Domain}Service.delete(id);
        return new CommonResp<>();
    }
}
