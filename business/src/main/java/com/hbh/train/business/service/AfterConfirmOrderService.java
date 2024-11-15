package com.hbh.train.business.service;

import com.hbh.train.business.domain.DailyTrainSeat;
import com.hbh.train.business.domain.DailyTrainTicket;
import com.hbh.train.business.mapper.ConfirmOrderMapper;
import com.hbh.train.business.mapper.DailyTrainSeatMapper;
import com.hbh.train.business.mapper.cust.DailyTrainTicketMapperCust;
import com.hbh.train.common.util.DateToSqlUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AfterConfirmOrderService {
    @Resource
    DailyTrainTicketMapperCust dailyTrainTicketMapperCust;
    @Resource
    DailyTrainCarriageService dailyTrainCarriageService;
    private static final Logger LOG = LoggerFactory.getLogger(AfterConfirmOrderService.class);
    @Resource
    private DailyTrainTicketService dailyTrainTicketService;
    @Resource
    private ConfirmOrderMapper confirmOrderMapper;
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;
    // 选座
    // 座位表修改售卖情况sell；
    // 余票详情表修改余票；
    // 为会员增加购票记录
    // 更新确认订单为成功
    @Transactional
    public void afterDoConfirm(DailyTrainTicket dailyTrainTicket,List<DailyTrainSeat>finalSeatList) {
        for (DailyTrainSeat dailyTrainSeat : finalSeatList) {
            DailyTrainSeat dailyTrainSeat1 = new DailyTrainSeat();
            dailyTrainSeat1.setId(dailyTrainSeat.getId());
            dailyTrainSeat1.setSell(dailyTrainSeat.getSell());
            dailyTrainSeat1.setUpdateTime(new Date());
            LOG.info("修改座位表{}", dailyTrainSeat1);
            dailyTrainSeatMapper.updateByPrimaryKeySelective(dailyTrainSeat1);
            //影响区间：最大连续序列

            Integer startIndex=dailyTrainTicket.getStartIndex();
            Integer endIndex=dailyTrainTicket.getEndIndex();
            LOG.info("该票区间：{}~{}",startIndex,endIndex);
            String cursell=dailyTrainSeat.getSell();
            Integer minstartIndex=0;
            Integer minendIndex=startIndex+1;
            Integer maxstartIndex=endIndex-1;
            Integer maxendIndex=cursell.length();
            /**
             * 000110000
             *
             *
             */
            for(int i=startIndex-1;i>=0;i--)
            {
                if(cursell.charAt(i)=='1')
                {
                    minstartIndex=i+1;
                    break;
                }
            }
            for(int i=endIndex;i<cursell.length();i++)
            {
                if(cursell.charAt(i)=='1')
                {
                    maxendIndex=i;
                    break;
                }
            }
            Date date= DateToSqlUtil.dataToSql(dailyTrainSeat.getDate());

            LOG.info("影响出发站区间："+minstartIndex+"-"+maxstartIndex);
            LOG.info("影响到达站区间："+minendIndex+"-"+maxendIndex);
            dailyTrainTicketMapperCust.updateCountBySell(
                    date
                    ,dailyTrainSeat.getTrainCode()
                    ,dailyTrainSeat.getSeatType()
                    ,minstartIndex
                    ,maxstartIndex
                    ,minendIndex
                    ,maxendIndex
                   );
        }
    }

}
