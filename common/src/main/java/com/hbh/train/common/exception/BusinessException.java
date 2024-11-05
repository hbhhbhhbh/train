package com.hbh.train.common.exception;

public class BusinessException extends RuntimeException {
    public BusinessExceptionEnum getEnums() {
        return enums;
    }

    public void setEnums(BusinessExceptionEnum enums) {
        this.enums = enums;
    }

    public BusinessException(BusinessExceptionEnum enums) {
        this.enums = enums;
    }

    private BusinessExceptionEnum enums;
}
