package com.airtel.scheduler.execution.jobgroup.impl;

import com.airtel.scheduler.execution.dto.JobData;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.jobgroup.JobGroups;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.repository.CustomRepository;
import com.airtel.scheduler.execution.service.TaskExecutionService;
import com.airtel.scheduler.execution.utils.CommonUtils;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @author Arun Singh
 * <p>
 * Generic Jobs are Simple Jobs That are Responsible
 * For Fetching Tasks and Scheduling them as per
 * their Job Type.
 * Since Tasks are supposed to be executed one Time
 * So for each Task will corresponds to a new
 * Background Job.
 */

@Component
public class GenericJobImpl implements JobGroups {

    private final Logger logger = LoggerFactory.getLogger(GenericJobImpl.class);

    private JobScheduler jobScheduler;

    private CustomRepository customRepository;

    private TaskExecutionService taskExecutionService;

    @Autowired
    public GenericJobImpl(CustomRepository customRepository, @Qualifier("jobSchedulerBean") JobScheduler jobScheduler, TaskExecutionService taskExecutionService) {
        this.customRepository = customRepository;
        this.jobScheduler = jobScheduler;
        this.taskExecutionService = taskExecutionService;
    }

    @Override
    public JobGroup getJobGroup() {
        return JobGroup.GENERIC;
    }

    @Override
    @Job(name = "Generic Job")
    public void execute(JobData jobData) {
        logger.info("Generic Job Execution Starts Job Name :{}", jobData.getJobName());
        List<Task> taskList = this.customRepository.findTasksForGenericExecution(jobData.getJobName(), jobData.getOffset(), jobData.getJobExecutionType(), jobData.getMaxFetchDuration(), jobData.getDuration());
        if (CommonUtils.isEmptyList(taskList)) {
            logger.error("Currently No Tasks Present corresponding to Generic Job :{}", jobData.getJobName());
            return;
        }
        logger.info("Scheduling :{} Tasks corresponding to Job :{}", taskList.size(), jobData.getJobName());
        for (Task task : taskList) {
            this.scheduleTasks(task, jobData);
        }
    }

    @Async("taskExecutionThreadPoolExecutor")
    void scheduleTasks(Task task, JobData jobData) {
        logger.info("Scheduling Task With Meta as : {} at Time :{}", task, System.currentTimeMillis());
        try {
            this.jobScheduler.schedule(UUID.fromString(task.getId()), task.getScheduledTime(), () -> this.taskExecutionService.executeTask(task, jobData));
        } catch (Exception e) {
            logger.error("Failure While Scheduling Task For Generic Job: {}", task, e);
        }
    }
}