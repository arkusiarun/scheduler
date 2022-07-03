package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;

@Data
public class RetryDef {

    private Boolean retryEnabled;
    private Integer retriesLeft;
    private long retryInterval;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}