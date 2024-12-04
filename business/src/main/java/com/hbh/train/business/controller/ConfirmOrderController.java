package com.hbh.train.business.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.hbh.train.business.req.ConfirmOrderDoReq;
import com.hbh.train.business.service.ConfirmOrderService;
import com.hbh.train.common.exception.BusinessExceptionEnum;
import com.hbh.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {
    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderService confirmOrderService;
    @SentinelResource(value="confirmOrderDo",blockHandler = "doConfirmBlock")
    @PostMapping("/do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }
    public CommonResp<Object> doConfirmBlock(ConfirmOrderDoReq req, BlockException e)
    {
        LOG.info("购票请求被限流{}",req);
//        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
        CommonResp<Object> resp = new CommonResp<>();
        resp.setSuccess(false);
        resp.setMessage(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION.getDesc());
        return resp;
    }

}
