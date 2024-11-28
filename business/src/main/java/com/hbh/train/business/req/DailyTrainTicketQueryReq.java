package com.hbh.train.business.req;

import com.hbh.train.common.req.PageReq;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyTrainTicketQueryReq that = (DailyTrainTicketQueryReq) o;
        return Objects.equals(date, that.date) && Objects.equals(start, that.start)
                && Objects.equals(end, that.end) && Objects.equals(trainCode, that.trainCode)
                && Objects.equals(((DailyTrainTicketQueryReq) o).getPage(), that.getPage()) && Objects.equals(((DailyTrainTicketQueryReq) o).getSize(), that.getSize());
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, start, end, trainCode,getPage(),getSize());
    }
}
