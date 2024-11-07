package com.hbh.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.hbh.train.common.util.SnowUtil;
import com.hbh.train.member.domain.Passenger;
import com.hbh.train.member.mapper.PassengerMapper;
import com.hbh.train.member.req.PassengerSaveReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PassengerService
{
    @Resource
    private PassengerMapper passengerMapper;
    public void save(PassengerSaveReq req)
    {
        DateTime now= DateTime.now();
        Passenger passenger=BeanUtil.copyProperties(req, Passenger.class);
        passenger.setId(SnowUtil.getSnowflakeNextId());
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        passengerMapper.insert(passenger);
    }
}
