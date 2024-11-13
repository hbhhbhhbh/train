package com.hbh.train.business.controller;

import com.hbh.train.business.resp.TrainQueryResp;
import com.hbh.train.business.service.TrainSeatService;
import com.hbh.train.business.service.TrainService;
import com.hbh.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/train")
public class TrainController {

    @Resource
    private TrainService trainService;
    @Resource
    private TrainSeatService trainSeatService;


    @GetMapping("/query-all")
    public CommonResp<List<TrainQueryResp>> queryAll() {
        List<TrainQueryResp> list = trainService.queryAll();
        return new CommonResp<>(list);
    }

}
