package com.airtel.scheduler.execution.job.impl;

import com.airtel.scheduler.execution.dto.JobData;
import com.airtel.scheduler.execution.enums.JobType;
import com.airtel.scheduler.execution.enums.ResponseCodes;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.job.Jobs;
import com.airtel.scheduler.execution.jobgroup.JobGroupFactory;
import com.airtel.scheduler.execution.model.ScheduledJob;
import com.airtel.scheduler.execution.repository.ScheduledJobRepository;
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
 *
 * Implementation for Reoccuring Time Job.
 * This Class is Responsible for Scheduling Job which are to be,
 * executed at precise time.
 * These Jobs will execute one time Only.
 */

@Component
public class SchedulingJob implements Jobs {

    private final Logger logger = LoggerFactory.getLogger(SchedulingJob.class);

    private JobScheduler jobScheduler;

    private ScheduledJobRepository scheduledJobRepository;

    @Autowired
    public SchedulingJob(@Qualifier("jobSchedulerBean") JobScheduler jobScheduler, ScheduledJobRepository scheduledJobRepository) {
        this.jobScheduler = jobScheduler;
        this.scheduledJobRepository = scheduledJobRepository;
    }

    @Override
    public JobType getJobType() {
        return JobType.SCHEDULED;
    }

    @Override
    public void unScheduleJob(String jobIdentifier) {
        this.jobScheduler.delete(UUID.fromString(jobIdentifier));
    }

    @Override
    @Job(name = "Scheduling Job")
    public void scheduleJob(ScheduledJob scheduledJob) {
        logger.info("Scheduling Scheduled Time Job with Meta as : {}", scheduledJob);
        UUID jobId = UUID.randomUUID();
        scheduledJob.setId(jobId.toString());
        JobData jobData = this.buildJobContext(scheduledJob);
        this.jobScheduler.schedule(jobId, scheduledJob.getDateTime(), () -> this.execute(jobData));
    }

    @Override
    public void execute(JobData jobData) {
        logger.info("Scheduling Job Execution Starts For Job Name : {}", jobData.getJobName());
        JobGroupFactory.getJobGroup(jobData.getJobGroup()).execute(jobData);
        logger.info("Marking Job as Inactive with Id : {}", jobData.getScheduledJobId());
        ScheduledJob scheduledJob = this.scheduledJobRepository.findById(jobData.getScheduledJobId()).orElseThrow(() -> new SchedulerException(ResponseCodes.NO_RECORD_FOUND));
        scheduledJob.setActive(Boolean.FALSE);
        this.scheduledJobRepository.save(scheduledJob);
    }
}