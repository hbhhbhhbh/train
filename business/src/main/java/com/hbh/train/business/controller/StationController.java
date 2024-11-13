package com.hbh.train.business.controller;

import com.hbh.train.business.resp.StationQueryResp;
import com.hbh.train.business.service.StationService;
import com.hbh.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/station")
public class StationController {

    @Resource
    private StationService stationService;

    @GetMapping("/query-all")
    public CommonResp<List<StationQueryResp>> queryAll() {
        List<StationQueryResp> list =stationService.queryAll();
        return new CommonResp<>(list);
    }

}
