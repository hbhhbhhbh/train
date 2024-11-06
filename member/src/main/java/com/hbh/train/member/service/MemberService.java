package com.hbh.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.hbh.train.common.exception.BusinessException;
import com.hbh.train.common.exception.BusinessExceptionEnum;
import com.hbh.train.common.util.JwtUtil;
import com.hbh.train.common.util.SnowUtil;
import com.hbh.train.member.domain.Member;
import com.hbh.train.member.domain.MemberExample;
import com.hbh.train.member.mapper.MemberMapper;
import com.hbh.train.member.req.MemberLoginReq;
import com.hbh.train.member.req.MemberRegisterReq;
import com.hbh.train.member.req.MemberSendCodeReq;
import com.hbh.train.member.resp.MemberLoginResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberService.class);
    @Resource
    private MemberMapper memberMapper;
    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }
    //注册接口
    public long register(MemberRegisterReq req)
    {
        String mobile =req.getMobile();
        Member list = selectByMobile(mobile);
        if(ObjectUtil.isNotNull(list))
        {
//            return list.get(0).getId();
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }
        Member member=new Member();
        member.setId(SnowUtil.getSnowflakeNextId());
        member.setMobile(mobile);
        memberMapper.insert(member);
        return member.getId();
    }
    public void sendCode(MemberSendCodeReq req)
    {
        String mobile =req.getMobile();
        Member list = selectByMobile(mobile);
        //如果手机号不存在，则插入记录
        if(ObjectUtil.isNull(list))
        {
            LOG.info("手机号不存在，插入");
            Member member=new Member();
            member.setId(SnowUtil.getSnowflakeNextId());
            member.setMobile(mobile);
            memberMapper.insert(member);
//            return list.get(0).getId();
            }
        else {
            LOG.info("手机号存在，不插入");
        }
        //生成验证码
        String code="8888";
        LOG.info("生成短信验证码：{}",code);
        //保存短信记录表：手机号，验证码，有效期，是否已使用，业务类型（注册，找回密码），发送时间，使用时间
        //对接短信通道，发送短信
        //return code;
    }
    public MemberLoginResp login(MemberLoginReq req)
    {
        String mobile =req.getMobile();
        String code =req.getCode();
        Member list = selectByMobile(mobile);
        //如果手机号不存在，则插入记录
        if(ObjectUtil.isNull(list))
        {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }
        //校验短信验证码
        if(!"8888".equals(code))
        {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);

        }
        MemberLoginResp memberLoginResp= BeanUtil.copyProperties(list, MemberLoginResp.class);

        String token= JwtUtil.createToken(memberLoginResp.getId(),memberLoginResp.getMobile());
        memberLoginResp.setToken(token);
        return memberLoginResp;
    }

    private Member selectByMobile(String mobile) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member>list=memberMapper.selectByExample(memberExample);
        if(CollUtil.isEmpty(list))
        {
           return null;
        }
        else {
            return list.get(0);
        }
    }
}
