package com.hbh.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.hbh.train.common.context.LoginMemberContext;
import com.hbh.train.common.util.SnowUtil;
import com.hbh.train.member.domain.Passenger;
import com.hbh.train.member.domain.PassengerExample;
import com.hbh.train.member.mapper.PassengerMapper;
import com.hbh.train.member.req.PassengerQueryReq;
import com.hbh.train.member.req.PassengerSaveReq;
import com.hbh.train.member.resp.PassengerQueryResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService
{
    @Resource
    private PassengerMapper passengerMapper;
    public void save(PassengerSaveReq req)
    {
        DateTime now= DateTime.now();
        Passenger passenger=BeanUtil.copyProperties(req, Passenger.class);
        passenger.setMemberId(LoginMemberContext.getId());
        passenger.setId(SnowUtil.getSnowflakeNextId());
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        passengerMapper.insert(passenger);
    }
    public List<PassengerQueryResp> queryList(PassengerQueryReq req)
    {
        PassengerExample passengerexample=new PassengerExample();
        PassengerExample.Criteria criteria= passengerexample.createCriteria();
        if(ObjectUtil.isNotNull(req.getMemberId()))
        {
            criteria.andMemberIdEqualTo(req.getMemberId());
        }
        PageHelper.startPage(req.getPage(),req.getSize());//分页,查询第一条，查两条
        List<Passenger>passengerList=passengerMapper.selectByExample(passengerexample);
        return BeanUtil.copyToList(passengerList,PassengerQueryResp.class);
    }
}
