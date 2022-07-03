package com.airtel.scheduler.execution.job;

import com.airtel.scheduler.execution.dto.JobData;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.enums.JobType;
import com.airtel.scheduler.execution.model.ScheduledJob;

public interface Jobs {

    JobType getJobType();

    void unScheduleJob(String jobIdentifier);

    void scheduleJob(ScheduledJob scheduledJob);

    void execute(JobData jobData);

    /*
    Building Custom Job Data That will be used when Scheduling
     */
    default JobData buildJobContext(ScheduledJob scheduledJob) {
        JobData jobData = new JobData();
        jobData.setScheduledJobId(scheduledJob.getId());
        jobData.setJobGroup(scheduledJob.getJobGroup());
        jobData.setJobName(scheduledJob.getJobName());
        jobData.setJobType(scheduledJob.getJobType());
        if (!JobGroup.CUSTOM.equals(scheduledJob.getJobGroup())) {
            jobData.setOffset(scheduledJob.getSchedulingInfo().getOffset());
            jobData.setMaxFetchDuration(scheduledJob.getSchedulingInfo().getMaxFetchDuration());
            jobData.setJobExecutionType(scheduledJob.getSchedulingInfo().getJobExecutionType());
            jobData.setDuration(scheduledJob.getSchedulingInfo().getDuration());
        } else {
            jobData.setEventType(scheduledJob.getEventType());
        }
        return jobData;
    }
}