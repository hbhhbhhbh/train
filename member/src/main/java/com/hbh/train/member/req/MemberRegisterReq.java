package com.hbh.train.member.req;

import jakarta.validation.constraints.NotBlank;

public class MemberRegisterReq {
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "MemberRegisterReq{" +
                "mobile='" + mobile + '\'' +
                '}';
    }
    @NotBlank(message="手机号不能为空")
    private String mobile;

}
