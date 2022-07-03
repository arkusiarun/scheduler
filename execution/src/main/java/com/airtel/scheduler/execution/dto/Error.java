package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;

@Data
public class Error {

    private String code;
    private String errorMessage;

    public Error() {
    }

    public Error(String errorCode, String errorMessage) {
        this.code = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}
