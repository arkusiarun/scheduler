package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.enums.Duration;
import com.airtel.scheduler.execution.enums.JobExecutionType;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.enums.JobType;
import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;

@Data
public class JobData {

    private String scheduledJobId;
    private String jobName;
    private JobGroup jobGroup;
    private Integer offset;
    private long maxFetchDuration;
    private String eventType;
    private JobExecutionType jobExecutionType;
    private Duration duration;
    private JobType jobType;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}
