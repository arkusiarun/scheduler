package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.enums.Duration;
import com.airtel.scheduler.execution.enums.JobExecutionType;
import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SchedulingInfo {

    @NotNull
    private JobExecutionType jobExecutionType;
    @NotNull
    private Integer offset;
    @NotNull
    private long maxFetchDuration;
    @NotNull
    private Duration duration;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}
