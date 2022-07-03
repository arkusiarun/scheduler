package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.constants.CommonConstants;
import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.dto.ScheduledJobRequest;
import com.airtel.scheduler.execution.dto.SchedulerResponse;
import com.airtel.scheduler.execution.enums.Status;
import com.airtel.scheduler.execution.enums.ValidationErrorCodes;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.job.JobFactory;
import com.airtel.scheduler.execution.model.ScheduledJob;
import com.airtel.scheduler.execution.properties.JobConfiguration;
import com.airtel.scheduler.execution.repository.ScheduledJobRepository;
import com.airtel.scheduler.execution.service.SchedulerService;
import com.airtel.scheduler.execution.utils.SchedulerUtils;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SchedulerServiceImpl implements SchedulerService {

    private final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    @Autowired
    @Qualifier("jobSchedulerBean")
    private JobScheduler jobScheduler;

    @Autowired
    private JobConfiguration jobConfiguration;

    @Autowired
    private ScheduledJobRepository scheduledJobRepository;

    @Override
    public SchedulerResponse createNewJob(ScheduledJobRequest scheduledJobRequest) {
        SchedulerResponse schedulerResponse = new SchedulerResponse();
        try {
            ScheduledJob scheduledJob = SchedulerUtils.buildScheduledJobModel(scheduledJobRequest);
            JobFactory.getJob(scheduledJobRequest.getJobType()).scheduleJob(scheduledJob);
            schedulerResponse.setIdentifier(scheduledJob.getId());
            schedulerResponse.setStatus(Status.CREATED);
            schedulerResponse.setComments(CommonConstants.CREATED_MESSAGE);
            this.scheduledJobRepository.insert(scheduledJob);
        } catch (Exception e) {
            logger.error("Error Occurred While Creating New Job: {}", scheduledJobRequest);
            throw new SchedulerException(new Error(ValidationErrorCodes.GENERIC_ERROR.toString(), e.getMessage()));
        }
        return schedulerResponse;
    }

    @Override
    public Boolean unScheduleJob(String jobId) {
        try {
            ScheduledJob scheduledJob = this.fetchScheduledJobById(jobId);
            scheduledJob.setActive(Boolean.FALSE);
            JobFactory.getJob(scheduledJob.getJobType()).unScheduleJob(scheduledJob.getId());
            this.scheduledJobRepository.save(scheduledJob);
        } catch (Exception e) {
            logger.error("Failure while UnScheduling Job : {} with Error:", jobId, e);
            String message = StringUtils.isEmpty(e.getMessage()) ? ValidationErrorCodes.FAILED_TO_UNSCHEDULED.getErrorMessage() : e.getMessage();
            throw new SchedulerException(new Error(ValidationErrorCodes.FAILED_TO_UNSCHEDULED.toString(), message));
        }
        return true;
    }

    @Override
    public ScheduledJob fetchScheduledJobById(String scheduledJobId) {
        return this.scheduledJobRepository.findById(scheduledJobId).orElseThrow(() -> new SchedulerException(ValidationErrorCodes.JOB_NOT_FOUND.getErrorMessage()));
    }
}