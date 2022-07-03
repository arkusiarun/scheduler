package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.annotations.JobRequestValidate;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.enums.JobType;
import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JobRequestValidate
public class ScheduledJobRequest {

    @NotNull
    private String jobName;
    @NotNull
    private JobGroup jobGroup;
    @NotNull
    private JobType jobType;
    private String cronExpression;
    private long scheduledTime;
    private SchedulingInfo schedulingInfo;
    private String eventType;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}
