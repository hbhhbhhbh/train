package com.hbh.train.business.req;

import com.hbh.train.common.req.PageReq;

public class TrainStationQueryReq extends PageReq {
    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    @Override
    public String toString() {
        return "TrainStationQueryReq{" +
                "trainCode='" + trainCode + '\'' +
                "} " + super.toString();
    }

    private String trainCode;

}
