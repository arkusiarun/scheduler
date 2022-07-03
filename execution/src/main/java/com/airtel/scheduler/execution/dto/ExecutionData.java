package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.enums.ExecutionType;
import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;

@Data
public class ExecutionData {

    private ExecutionType executionType;
    private long executionAfterInSeconds;
    private long expiryInSeconds;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}