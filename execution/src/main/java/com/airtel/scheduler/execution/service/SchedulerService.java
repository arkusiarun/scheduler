package com.airtel.scheduler.execution.service;

import com.airtel.scheduler.execution.dto.ScheduledJobRequest;
import com.airtel.scheduler.execution.dto.SchedulerResponse;
import com.airtel.scheduler.execution.model.ScheduledJob;

public interface SchedulerService {

    SchedulerResponse createNewJob(ScheduledJobRequest scheduledJobRequest);

    Boolean unScheduleJob(String jobId);

    ScheduledJob fetchScheduledJobById(String scheduledJobId);

}