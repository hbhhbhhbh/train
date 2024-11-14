package com.hbh.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hbh.train.business.domain.DailyTrain;
import com.hbh.train.business.domain.DailyTrainTicket;
import com.hbh.train.business.domain.DailyTrainTicketExample;
import com.hbh.train.business.domain.TrainStation;
import com.hbh.train.business.enums.SeatTypeEnum;
import com.hbh.train.business.enums.TrainTypeEnum;
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
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
public class DailyTrainTicketService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketService.class);
    @Resource
    private TrainStationService trainStationService;
    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
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
        LOG.info("查询车次：{}", req.gettrainCode());
        if(ObjectUtil.isNotEmpty(req.gettrainCode())){

            criteria.andTrainCodeEqualTo(req.gettrainCode());
        }
        if(ObjectUtil.isNotNull(req.getDate())){
            criteria.andDateEqualTo(req.getDate());
        }
        if(ObjectUtil.isNotEmpty(req.getStart())){
            criteria.andStartEqualTo(req.getStart());
        }
        if(ObjectUtil.isNotEmpty(req.getEnd())){
            criteria.andEndEqualTo(req.getEnd());
        }
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
    public void genDaily(DailyTrain train,Date date, String code)
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
           BigDecimal sumKM=BigDecimal.ZERO;
           for(int j=i+1;j<trainStations.size();j++){
               TrainStation end = trainStations.get(j);
               sumKM=sumKM.add(end.getKm());
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
               int YDZ=dailyTrainSeatService.countSeats(date,code, SeatTypeEnum.YDZ.getCode());
               int EDZ=dailyTrainSeatService.countSeats(date,code, SeatTypeEnum.EDZ.getCode());
               int RW=dailyTrainSeatService.countSeats(date,code, SeatTypeEnum.RW.getCode());
               int YW=dailyTrainSeatService.countSeats(date,code, SeatTypeEnum.YW.getCode());
                //票价=里程之和*座位单价*车次类型系数
               String traintype = train.getType();
               //计算票价系数
               BigDecimal priceRate = EnumUtil.getFieldBy(TrainTypeEnum::getPriceRate, TrainTypeEnum::getCode, traintype);

               BigDecimal ydzPrize = sumKM
                       .multiply(SeatTypeEnum.YDZ.getPrice())
                       .multiply(priceRate)
                       .setScale(2, RoundingMode.HALF_UP);
               BigDecimal edzPrize = sumKM
                       .multiply(SeatTypeEnum.EDZ.getPrice())
                       .multiply(priceRate)
                       .setScale(2, RoundingMode.HALF_UP);
               BigDecimal rwPrize = sumKM
                       .multiply(SeatTypeEnum.RW.getPrice())
                       .multiply(priceRate)
                       .setScale(2, RoundingMode.HALF_UP);
               BigDecimal ywPrize = sumKM
                       .multiply(SeatTypeEnum.YW.getPrice())
                       .multiply(priceRate)
                       .setScale(2, RoundingMode.HALF_UP);


               dailyTrainTicket.setYdz(YDZ);
               dailyTrainTicket.setYdzPrice(ydzPrize);
               dailyTrainTicket.setEdz(EDZ);
               dailyTrainTicket.setEdzPrice(edzPrize);
               dailyTrainTicket.setRw(RW);
               dailyTrainTicket.setRwPrice(rwPrize);
               dailyTrainTicket.setYw(YW);
               dailyTrainTicket.setYwPrice(ywPrize);
               dailyTrainTicket.setCreateTime(now);
               dailyTrainTicket.setUpdateTime(now);
               dailyTrainTicketMapper.insert(dailyTrainTicket);
           }
       }
        LOG.info("生成每日余票信息结束，日期：{}，车次：{}", date, code);
    }

    public DailyTrainTicket selectByUnique(Date date, String trainCode, String start, String end) {
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode)
                .andStartEqualTo(start)
                .andEndEqualTo(end);
        List<DailyTrainTicket> list = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return null;
        }
    }
}
