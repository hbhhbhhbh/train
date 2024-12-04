package com.hbh.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hbh.train.business.domain.*;
import com.hbh.train.business.enums.ConfirmOrderStatusEnum;
import com.hbh.train.business.enums.SeatColEnum;
import com.hbh.train.business.enums.SeatTypeEnum;
import com.hbh.train.business.mapper.ConfirmOrderMapper;
import com.hbh.train.business.req.ConfirmOrderDoReq;
import com.hbh.train.business.req.ConfirmOrderQueryReq;
import com.hbh.train.business.req.ConfirmOrderTicketReq;
import com.hbh.train.business.resp.ConfirmOrderQueryResp;
import com.hbh.train.common.context.LoginMemberContext;
import com.hbh.train.common.exception.BusinessException;
import com.hbh.train.common.exception.BusinessExceptionEnum;
import com.hbh.train.common.resp.PageResp;
import com.hbh.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConfirmOrderService {
    @Resource
    DailyTrainCarriageService dailyTrainCarriageService;
    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);
    @Resource
    private DailyTrainTicketService dailyTrainTicketService;
    @Resource
    private ConfirmOrderMapper confirmOrderMapper;
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    @Resource
    private AfterConfirmOrderService afterConfirmOrderService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    public void save(ConfirmOrderDoReq req) {

        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    @SentinelResource(value="doConfirm",blockHandler = "doConfirmBlock")
    public void doConfirm(ConfirmOrderDoReq req) {
        // 省略业务数据校验，如：车次是否存在，余票是否存在，车次是否在有效期内，tickets条数>0，同乘客同车次是否已买过
        String lockKey= DateUtil.formatDate(req.getDate())+"-"+req.getTrainCode();
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 10, TimeUnit.SECONDS);
        if(setIfAbsent){
            LOG.info("恭喜抢到了");
        }else{
            LOG.info("很遗憾，没抢到");
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
        }
        try {
            ConfirmOrder confirmOrder = new ConfirmOrder();
            DateTime now = DateTime.now();
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setMemberId(LoginMemberContext.getId());
            Date date = req.getDate();
            List<ConfirmOrderTicketReq> tickets = req.getTickets();
            confirmOrder.setDate(date);
            String traincode = req.getTrainCode();
            confirmOrder.setTrainCode(traincode);
            String start = req.getStart();
            confirmOrder.setStart(start);
            String end = req.getEnd();
            confirmOrder.setEnd(end);
            confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
            confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrder.setTickets(JSON.toJSONString(tickets));

            // 保存确认订单表，状态初始
            confirmOrderMapper.insert(confirmOrder);
            // 查出余票记录，需要得到真实的库存
            DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, traincode, start, end);
            LOG.info("余票:{}", dailyTrainTicket);
            // 扣减余票数量，并判断余票是否足够,这只是测试判断，不是真实选票
            reduceTicket(req, dailyTrainTicket);

            //最终选座结果
            List<DailyTrainSeat> finalSeatList = new ArrayList<>();
            //计算相对第一个座位的偏离值
            //比如选择C1，D2，偏移值为[0,5]
            ConfirmOrderTicketReq ticketreq0 = tickets.get(0);
            if (StrUtil.isNotBlank(ticketreq0.getSeat())) {
                LOG.info("有选座");
                List<SeatColEnum> colsByType = SeatColEnum.getColsByType(ticketreq0.getSeatTypeCode());
                LOG.info("座位类型：{}", colsByType);

                //组成和前端一样的列表，用于作参照的座位列表
                List<String> referSeatList = new ArrayList<>();
                for (int i = 1; i <= 2; i++) {
                    for (SeatColEnum col : colsByType) {
                        referSeatList.add(col.getCode() + i);
                    }
                }
                LOG.info("参照座位列表：{}", referSeatList);
                //计算绝对偏移值
                List<Integer> SeatAbsoluteOffsetList = new ArrayList<>();
                for (ConfirmOrderTicketReq ticketReq : tickets) {
                    String seat = ticketReq.getSeat();
                    int index = referSeatList.indexOf(seat);
                    SeatAbsoluteOffsetList.add(index);
                }
                LOG.info("绝对偏移值：{}", SeatAbsoluteOffsetList);
                //计算相对偏移值
                List<Integer> SeatRelativeOffsetList = new ArrayList<>();

                for (int i = 0; i < SeatAbsoluteOffsetList.size(); i++) {
                    int i1 = SeatAbsoluteOffsetList.get(i) - SeatAbsoluteOffsetList.get(0);
                    SeatRelativeOffsetList.add(i1);
                }
                LOG.info("相对偏移值：{}", SeatRelativeOffsetList);
                getSeat(finalSeatList,
                        date
                        , traincode
                        , ticketreq0.getSeatTypeCode()
                        , ticketreq0.getSeat().split("")[0]
                        , SeatRelativeOffsetList
                        , dailyTrainTicket.getStartIndex(),
                        dailyTrainTicket.getEndIndex());
            } else {
                LOG.info("未选座");
                for (ConfirmOrderTicketReq ticketReq : tickets) {
                    getSeat(finalSeatList, date
                            , traincode
                            , ticketReq.getSeatTypeCode()
                            , null
                            , null
                            , dailyTrainTicket.getStartIndex(),
                            dailyTrainTicket.getEndIndex());
                }
            }
            LOG.info("最终list：{}", finalSeatList);
            // 选座
            // 座位表修改售卖情况sell；
            try {
                afterConfirmOrderService.afterDoConfirm(dailyTrainTicket, finalSeatList, tickets, confirmOrder);

            } catch (Exception e) {
                LOG.error("确认订单失败", e);
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EXCEPTION);
            }
        } finally {
            LOG.info("流程结束，释放锁");
            redisTemplate.delete(lockKey);
        }




                // 余票详情表修改余票；
                // 为会员增加购票记录
                // 更新确认订单为成功
    }
    private void getSeat(List<DailyTrainSeat> finalSeatList,Date date,String trainCode,String seatType ,String column
            ,List<Integer>offsetList,Integer startIndex,
                         Integer endIndex)
    {
        //临时选中表
        List<DailyTrainSeat> TempSeatList=new ArrayList<>();
        List<DailyTrainCarriage> dailyTrainCarriages = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);
        LOG.info("共查出{}个符合条件的车厢",dailyTrainCarriages.size());
        for(DailyTrainCarriage dailyTrainCarriage:dailyTrainCarriages){
            LOG.info("车厢信息：{}",dailyTrainCarriage.getIndex());
            // 获取座位数据
            List<DailyTrainSeat> dailyTrainSeats = dailyTrainSeatService.selectByCarriage(date, trainCode, dailyTrainCarriage.getIndex());
            LOG.info("车厢{}座位数信息：{}",dailyTrainCarriage.getIndex(),dailyTrainSeats.size());

            for(DailyTrainSeat dailyTrainSeat:dailyTrainSeats){
                TempSeatList.clear();
                String col=dailyTrainSeat.getCol();
                Integer seatIndex=dailyTrainSeat.getCarriageSeatIndex();
                boolean alreadyChooseFlag=false;
                for(DailyTrainSeat finalSeat:finalSeatList){
                    if(finalSeat.getCarriageSeatIndex().equals(seatIndex)){
                            LOG.info("座位{}已选",seatIndex);
                            alreadyChooseFlag=true;
                            break;
                    }
                }
                if(alreadyChooseFlag){
                    LOG.info("座位{}被选中",seatIndex);
                    continue;
                }
                //判断列
                if(StrUtil.isBlank(column)){
                    LOG.info("无选座");
                }
                else {
                    if(!column.equals(col)){
                        LOG.info("座位{}不满足,当前列：{}，目标列：{}",seatIndex,col,column);
                        continue;
                    }
                }
                boolean ischoose = calSell(dailyTrainSeat, startIndex, endIndex);
                if(ischoose){
                    LOG.info("选座{}成功",dailyTrainSeat.getCarriageSeatIndex());
                    TempSeatList.add(dailyTrainSeat);
                }
                else
                {
                    LOG.info("选座失败");
                    continue;
                }
                LOG.info("开始根据偏移值选位");
                //根据offset选座剩下的座位
                boolean isGetAllOffsetSeat =true;
                if (CollUtil.isNotEmpty(offsetList)){
                    LOG.info("offsetList不为空{}",offsetList);
                    for(int i=1;i<offsetList.size();i++){
                        Integer offset=offsetList.get(i);
                        int nextIndex=seatIndex+offset-1;
                        LOG.info("nextIndex:{}",nextIndex);
                        //必须在同一车厢
                        if(nextIndex>=dailyTrainSeats.size()){
                            LOG.info("座位{}不在同一车厢",nextIndex);
                            isGetAllOffsetSeat=false;
                            break;
                        }
                        DailyTrainSeat nextdailyTrainSeat = dailyTrainSeats.get(nextIndex);
                        boolean ischoosenext = calSell(nextdailyTrainSeat, startIndex, endIndex);
                        if(ischoosenext){
                            LOG.info("选座{}成功",nextdailyTrainSeat.getCarriageSeatIndex());
                            TempSeatList.add(nextdailyTrainSeat);
                        }
                        else
                        {
                            LOG.info("选座{}失败",nextdailyTrainSeat.getCarriageSeatIndex());
                            isGetAllOffsetSeat=false;
                            break;
                        }

                    }
                }
                if(!isGetAllOffsetSeat){
                    TempSeatList.clear();
                    continue;
                }
                finalSeatList.addAll(TempSeatList);
                return;
            }
        }
    }

    /**
     * 计算某座位在区间是否可卖
     * 例如：sell=1000，看区间内有无1，有1则不可卖，没有则可以
     * @param dailyTrainSeat
     */
    private boolean calSell(DailyTrainSeat dailyTrainSeat ,Integer startIndex,
                         Integer endIndex) {
        String sell= dailyTrainSeat.getSell();
        String sellPart=sell.substring(startIndex,endIndex);
        LOG.info("原sell:{}",sell);
        if(sellPart.contains("1")){
            LOG.info("座位{}~{}不可卖",startIndex,endIndex);
            return false;
        }else {
            LOG.info("座位{}~{}可卖",startIndex,endIndex);
            String curSell = sellPart.replace('0', '1');
            //将curSell对应内容部会原sell

            sell=sell.substring(0,startIndex)+curSell+sell.substring(endIndex,sell.length());
            LOG.info("更新后的sell:{}",sell);
            dailyTrainSeat.setSell(
                    sell
            );
            return true;
        }
    }
    private static void reduceTicket(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for(ConfirmOrderTicketReq ticketreq: req.getTickets())
        {
            String seatTypeCode = ticketreq.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch(seatTypeEnum) {
                case YDZ ->{
                    int countLeft = dailyTrainTicket.getYdz()-1;
                    if(countLeft<0)
                    {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYdz(countLeft);
                }
                case EDZ ->{
                    int countLeft = dailyTrainTicket.getEdz()-1;
                    if(countLeft<0)
                    {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setEdz(countLeft);
                }
                case RW ->{
                    int countLeft = dailyTrainTicket.getRw()-1;
                    if(countLeft<0)
                    {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setRw(countLeft);
                }
                case YW ->{
                    int countLeft = dailyTrainTicket.getYw()-1;
                    if(countLeft<0)
                    {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    }
                    dailyTrainTicket.setYw(countLeft);
                }
            }
        }
    }
    public void doConfirmBlock(ConfirmOrderDoReq req, BlockException e)
    {
        LOG.info("购票请求被限流{}",req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }
}
