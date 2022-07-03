package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.enums.JobGroup;
import lombok.Data;

@Data
public class JobDef {

    private String jobName;
    private JobGroup jobGroup;

}