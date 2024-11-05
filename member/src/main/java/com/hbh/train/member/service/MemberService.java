package com.hbh.train.member.service;

import cn.hutool.core.collection.CollUtil;
import com.hbh.train.common.exception.BusinessException;
import com.hbh.train.common.exception.BusinessExceptionEnum;
import com.hbh.train.common.util.SnowUtil;
import com.hbh.train.member.domain.Member;
import com.hbh.train.member.domain.MemberExample;
import com.hbh.train.member.mapper.MemberMapper;
import com.hbh.train.member.req.MemberRegisterReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    @Resource
    private MemberMapper memberMapper;
    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }
    //注册接口
    public long register(MemberRegisterReq req)
    {
        String mobile =req.getMobile();
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member>list=memberMapper.selectByExample(memberExample);
        if(CollUtil.isNotEmpty(list))
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
}
