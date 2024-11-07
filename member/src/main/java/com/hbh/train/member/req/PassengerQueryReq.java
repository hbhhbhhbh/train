package com.hbh.train.member.req;

import com.hbh.train.common.req.PageReq;

public class PassengerQueryReq extends PageReq {

    public Long getMemberId() {
        return memberId;
    }

    @Override
    public String toString() {
        return "PassengerQueryReq{" +
                "memberId=" + memberId +
                '}';
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    private Long memberId;

}