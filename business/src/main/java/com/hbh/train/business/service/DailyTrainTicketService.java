package com.hbh.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hbh.train.business.domain.DailyTrainTicket;
import com.hbh.train.business.domain.DailyTrainTicketExample;
import com.hbh.train.business.domain.TrainStation;
import com.hbh.train.business.mapper.DailyTrainTicketMapper;
import com.hbh.train.business.req.DailyTrainTicketQueryReq;
import com.hbh.train.business.req.DailyTrainTicketSaveReq;
import com.hbh.train.business.resp.DailyTrainTicketQueryResp;
import com.hbh.train.common.resp.PageResp;
import com.hbh.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class DailyTrainTicketService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketService.class);
    @Resource
    private TrainStationService trainStationService;
    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    public void save(DailyTrainTicketSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(req, DailyTrainTicket.class);
        if (ObjectUtil.isNull(dailyTrainTicket.getId())) {
            dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        } else {
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.updateByPrimaryKey(dailyTrainTicket);
        }
    }

    public PageResp<DailyTrainTicketQueryResp> queryList(DailyTrainTicketQueryReq req) {
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.setOrderByClause("id desc");
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainTicket> dailyTrainTicketList = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);

        PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(dailyTrainTicketList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainTicketQueryResp> list = BeanUtil.copyToList(dailyTrainTicketList, DailyTrainTicketQueryResp.class);

        PageResp<DailyTrainTicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainTicketMapper.deleteByPrimaryKey(id);
    }
    @Transactional
    public void genDaily(Date date, String code)
    {
        LOG.info("生成每日余票信息，日期：{}，车次：{}", date, code);
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(code);
        dailyTrainTicketMapper.deleteByExample(dailyTrainTicketExample);
        //查询途径站点
        List<TrainStation> trainStations = trainStationService.selectByTrainCode(code);
        if (CollUtil.isEmpty(trainStations)
        ) {
            LOG.info("余票信息为空，不生成每日余票信息，日期：{}，车次：{}", date, code);
            return;
        }
        DateTime now= DateTime.now();
       for(int i=0;i<trainStations.size();i++){
           TrainStation start = trainStations.get(i);
           for(int j=i+1;j<trainStations.size();j++){
               TrainStation end = trainStations.get(j);
               DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();
               dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
               dailyTrainTicket.setDate(date);
               dailyTrainTicket.setTrainCode(code);
               dailyTrainTicket.setStart(start.getName());
               dailyTrainTicket.setStartPinyin(start.getNamePinyin());
               dailyTrainTicket.setStartTime(start.getOutTime());
               dailyTrainTicket.setStartIndex(start.getIndex());
               dailyTrainTicket.setEnd(end.getName());
               dailyTrainTicket.setEndPinyin(end.getNamePinyin());
               dailyTrainTicket.setEndTime(end.getInTime());
               dailyTrainTicket.setEndIndex(end.getIndex());
               dailyTrainTicket.setYdz(0);
               dailyTrainTicket.setYdzPrice(BigDecimal.ZERO);
               dailyTrainTicket.setEdz(0);
               dailyTrainTicket.setEdzPrice(BigDecimal.ZERO);
               dailyTrainTicket.setRw(0);
               dailyTrainTicket.setRwPrice(BigDecimal.ZERO);
               dailyTrainTicket.setYw(0);
               dailyTrainTicket.setYwPrice(BigDecimal.ZERO);
               dailyTrainTicket.setCreateTime(now);
               dailyTrainTicket.setUpdateTime(now);
               dailyTrainTicketMapper.insert(dailyTrainTicket);
           }
       }
        LOG.info("生成每日余票信息结束，日期：{}，车次：{}", date, code);
    }
}
