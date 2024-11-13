package com.hbh.train.business.req;

import com.hbh.train.common.req.PageReq;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class DailyTrainTicketQueryReq extends PageReq {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private String start;

    public String getStart() {
        return start;
    }

    @Override
    public String toString() {
        return "DailyTrainQueryReq{" +
                "date=" + date +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", trainCode='" + trainCode + '\'' +
                "} " + super.toString();
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    private String end;
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String gettrainCode() {
        return trainCode;
    }

    public void settrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    private String trainCode;
}
