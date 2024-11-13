package com.hbh.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hbh.train.business.domain.DailyTrainCarriage;
import com.hbh.train.business.domain.DailyTrainCarriageExample;
import com.hbh.train.business.domain.TrainCarriage;
import com.hbh.train.business.enums.SeatColEnum;
import com.hbh.train.business.mapper.DailyTrainCarriageMapper;
import com.hbh.train.business.req.DailyTrainCarriageQueryReq;
import com.hbh.train.business.req.DailyTrainCarriageSaveReq;
import com.hbh.train.business.resp.DailyTrainCarriageQueryResp;
import com.hbh.train.common.resp.PageResp;
import com.hbh.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainCarriageService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainCarriageService.class);
    @Resource
    private TrainCarriageService TrainCarriageService;
    @Resource
    private DailyTrainCarriageMapper dailyTrainCarriageMapper;

    public void save(DailyTrainCarriageSaveReq req) {
        DateTime now = DateTime.now();
        List<SeatColEnum>seatColEnums=SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(req.getColCount()*req.getRowCount());

        DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(req, DailyTrainCarriage.class);
        if (ObjectUtil.isNull(dailyTrainCarriage.getId())) {
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        } else {
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.updateByPrimaryKey(dailyTrainCarriage);
        }
    }

    public PageResp<DailyTrainCarriageQueryResp> queryList(DailyTrainCarriageQueryReq req) {
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.setOrderByClause("date desc ,train_code asc,`index` asc");
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getDate()))
        {
            criteria.andDateEqualTo(req.getDate());
        }
        if(ObjectUtil.isNotNull(req.getCode())&&!req.getCode().isEmpty())
        {
            criteria.andTrainCodeEqualTo(req.getCode());

        }
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainCarriage> dailyTrainCarriageList = dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);

        PageInfo<DailyTrainCarriage> pageInfo = new PageInfo<>(dailyTrainCarriageList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainCarriageQueryResp> list = BeanUtil.copyToList(dailyTrainCarriageList, DailyTrainCarriageQueryResp.class);

        PageResp<DailyTrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainCarriageMapper.deleteByPrimaryKey(id);
    }
    public void genDaily(Date date, String code)
    {
        LOG.info("生成每日车厢信息，日期：{}，车厢：{}", date, code);
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(code);
        dailyTrainCarriageMapper.deleteByExample(dailyTrainCarriageExample);
        List<TrainCarriage> trainCarriages = TrainCarriageService.selectByTrainCode(code);
        if (CollUtil.isEmpty(trainCarriages)
        ) {
            LOG.info("车厢信息为空，不生成每日车厢信息，日期：{}，车次：{}", date, code);
            return;
        }
        for(TrainCarriage trainCarriage: trainCarriages)
        {
            DateTime now=DateTime.now();
            DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(trainCarriage, DailyTrainCarriage.class);
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriage.setDate(date);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        }
    }
}
