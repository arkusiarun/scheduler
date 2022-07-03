package com.airtel.scheduler.execution.jobgroup;

import com.airtel.scheduler.execution.dto.JobData;
import com.airtel.scheduler.execution.enums.JobGroup;

public interface JobGroups {

    JobGroup getJobGroup();

    void execute(JobData jobData);
}