package com.hbh.train.business.req;

import com.hbh.train.common.req.PageReq;

public class TrainSeatQueryReq extends PageReq {
    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    @Override
    public String toString() {
        return "TrainCarriageQueryReq{" +
                "trainCode='" + trainCode + '\'' +
                "} " + super.toString();
    }

    private String trainCode;
}
