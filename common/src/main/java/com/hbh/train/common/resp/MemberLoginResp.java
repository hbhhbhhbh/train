package com.hbh.train.common.resp;

public class MemberLoginResp {
    private Long id;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "MemberLoginResp{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", mobile='" + mobile + '\'' +
                '}';
    }

    private String token;
    private String mobile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

}