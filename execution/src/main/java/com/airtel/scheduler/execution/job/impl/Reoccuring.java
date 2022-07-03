package com.airtel.scheduler.execution.job.impl;

import com.airtel.scheduler.execution.dto.JobData;
import com.airtel.scheduler.execution.enums.JobType;
import com.airtel.scheduler.execution.job.Jobs;
import com.airtel.scheduler.execution.jobgroup.JobGroupFactory;
import com.airtel.scheduler.execution.model.ScheduledJob;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Arun Singh
 * <p>
 * Implementation for Reoccuring Time Job.
 * This Class is Responsible for Scheduling Job which are Recursive.
 * These Jobs will execute until Unscheduled at Interval provided.
 */

@Component
public class Reoccuring implements Jobs {

    private final Logger logger = LoggerFactory.getLogger(Reoccuring.class);

    private JobScheduler jobScheduler;

    @Autowired
    public Reoccuring(@Qualifier("jobSchedulerBean") JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    @Override
    public JobType getJobType() {
        return JobType.REOCCURING;
    }

    @Override
    public void unScheduleJob(String jobIdentifier) {
        this.jobScheduler.delete(jobIdentifier);
    }

    @Override
    @Job(name = "Reoccurring Job")
    public void scheduleJob(ScheduledJob scheduledJob) {
        logger.info("Scheduling Reoccuring Time Job with Meta as : {}", scheduledJob);
        UUID jobId = UUID.randomUUID();
        scheduledJob.setId(jobId.toString());
        JobData jobData = this.buildJobContext(scheduledJob);
        this.jobScheduler.scheduleRecurrently(jobId.toString(), scheduledJob.getCronExpression(), () -> this.execute(jobData));
    }

    @Override
    public void execute(JobData jobData) {
        logger.info("Reoccurring Job Execution Starts For Job Name : {}", jobData.getJobName());
        JobGroupFactory.getJobGroup(jobData.getJobGroup()).execute(jobData);
    }
}