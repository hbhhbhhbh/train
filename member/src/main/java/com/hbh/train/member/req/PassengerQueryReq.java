package com.hbh.train.member.req;

public class PassengerQueryReq {

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