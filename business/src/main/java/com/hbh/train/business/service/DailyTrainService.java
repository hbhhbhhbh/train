package com.hbh.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hbh.train.business.domain.DailyTrain;
import com.hbh.train.business.domain.DailyTrainExample;
import com.hbh.train.business.domain.Train;
import com.hbh.train.business.mapper.DailyTrainMapper;
import com.hbh.train.business.req.DailyTrainQueryReq;
import com.hbh.train.business.req.DailyTrainSaveReq;
import com.hbh.train.business.resp.DailyTrainQueryResp;
import com.hbh.train.common.resp.PageResp;
import com.hbh.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DailyTrainService {
    @Resource
    DailyTrainStationService dailyTrainStation;
    @Resource
    DailyTrainCarriageService dailyTrainCarriage;
    @Resource
    DailyTrainSeatService dailyTrainSeat;
    @Resource
    TrainService trainService;
    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainService.class);

    @Resource
    private DailyTrainMapper dailyTrainMapper;

    public void save(DailyTrainSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(req, DailyTrain.class);
        if (ObjectUtil.isNull(dailyTrain.getId())) {
            dailyTrain.setId(SnowUtil.getSnowflakeNextId());
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.insert(dailyTrain);
        } else {
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.updateByPrimaryKey(dailyTrain);
        }
    }

    public PageResp<DailyTrainQueryResp> queryList(DailyTrainQueryReq req) {
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.setOrderByClause("date desc ,code asc");
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getDate()))
        {
            criteria.andDateEqualTo(req.getDate());
        }
        if(ObjectUtil.isNotNull(req.getCode())&&!req.getCode().isEmpty())
        {
            criteria.andCodeEqualTo(req.getCode());

        }
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrain> dailyTrainList = dailyTrainMapper.selectByExample(dailyTrainExample);

        PageInfo<DailyTrain> pageInfo = new PageInfo<>(dailyTrainList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainQueryResp> list = BeanUtil.copyToList(dailyTrainList, DailyTrainQueryResp.class);

        PageResp<DailyTrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainMapper.deleteByPrimaryKey(id);
    }

    /**
     * 生成某日所有车次信息，车次，车站，车厢，座位
     * @param date
     */
    public void genDaily(Date date)
    {

        List<Train> trainList = trainService.selectAll();
        if(CollUtil.isEmpty(trainList))
        {
            LOG.info("没有车次信息，无法生成每日车次信息");
            return;
        }
        for(Train train:trainList)
        {
            //删除该车次已有数据
            genDailyTrain(date, train);

        }

    }

    public void genDailyTrain(Date date, Train train) {
        LOG.info("生成每日车次信息，日期：{}，车次：{}", date, train.getCode());
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.createCriteria()
                .andDateEqualTo(date)
                .andCodeEqualTo(train.getCode());
        dailyTrainMapper.deleteByExample(dailyTrainExample);
        //生成车次数据
        DateTime now=DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
        dailyTrain.setId(SnowUtil.getSnowflakeNextId());
        dailyTrain.setCreateTime(now);
        dailyTrain.setUpdateTime(now);
        dailyTrain.setDate(date);
        dailyTrainMapper.insert(dailyTrain);

        //生成车站
        dailyTrainStation.genDaily(date, train.getCode());
        dailyTrainCarriage.genDaily(date, train.getCode());
        dailyTrainSeat.genDaily(date, train.getCode());
        LOG.info("生成每日车次信息结束，日期：{}，车次：{}", date, train.getCode());
    }
}
