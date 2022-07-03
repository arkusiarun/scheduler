package com.airtel.scheduler.execution.jobgroup.impl;

import com.airtel.scheduler.execution.constants.CommonConstants;
import com.airtel.scheduler.execution.dto.JobData;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.enums.JobType;
import com.airtel.scheduler.execution.enums.Status;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.jobgroup.JobGroups;
import com.airtel.scheduler.execution.model.ActivityDefinition;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.repository.TaskRepository;
import com.airtel.scheduler.execution.service.ActivityService;
import com.airtel.scheduler.execution.service.TaskExecutionService;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Arun Singh
 * <p>
 * Custom jobs are different from Batch and Generic Job
 * in aspect that their Data Source is Not Tasks.
 * These will Create Push Tasks while Executing.
 * And Tasks will be moved to terminal State when Job is Finished.
 */
@Component
public class CustomJobImpl implements JobGroups {

    private final Logger logger = LoggerFactory.getLogger(CustomJobImpl.class);

    private TaskRepository taskRepository;

    private ActivityService activityService;

    private TaskExecutionService taskExecutionService;

    private JobScheduler jobScheduler;

    @Value("${custom.task.archivalInSeconds}")
    private long archivalTime;

    @Autowired
    public CustomJobImpl(TaskExecutionService taskExecutionService, ActivityService activityService, TaskRepository taskRepository, @Qualifier("jobSchedulerBean") JobScheduler jobScheduler) {
        this.taskExecutionService = taskExecutionService;
        this.activityService = activityService;
        this.taskRepository = taskRepository;
        this.jobScheduler = jobScheduler;
    }

    @Override
    public JobGroup getJobGroup() {
        return JobGroup.CUSTOM;
    }

    @Override
    @Job(name = "Custom Job")
    public void execute(JobData jobData) {
        try {
            logger.info("Executing Custom Job : {}", jobData.getJobName());
            List<Task> taskList = this.createTaskForCustomJob(jobData);
            logger.info("Executing :{} Tasks corresponding to Custom Job :{}", taskList.size(), jobData.getJobName());
            for (Task task : taskList) {
                this.executeTasks(task, jobData);
            }
        } catch (Exception e) {
            logger.error("Exception Occurred while Creating Tasks For Custom Job : {}", jobData.getJobName());
            throw new SchedulerException(e.getMessage());
        }
    }

    @Async("taskExecutionThreadPoolExecutor")
    void executeTasks(Task task, JobData jobData) {
        logger.info("Executing Task With Meta as : {}, at Time :{}", task, System.currentTimeMillis());
        try {
            this.taskRepository.insert(task);
            UUID jobId = UUID.fromString(task.getId());
            this.jobScheduler.enqueue(jobId, () -> this.taskExecutionService.executeTask(task, jobData));
        } catch (Exception e) {
            logger.error("Failure While Executing Task For Batch Job: {}", task, e);
        }
    }

    private List<Task> createTaskForCustomJob(JobData jobData) {
        logger.info("Creating Task For Custom Job : {}", jobData.getJobName());
        List<Task> taskList = new ArrayList<>();
        Optional<List<ActivityDefinition>> activityDefinitionList = this.activityService.getActivityListForEventType(jobData.getEventType());
        activityDefinitionList.ifPresent(activityDefinitions -> activityDefinitions.forEach(activityDefinition -> {
            logger.info("Creating :{}, Task for Request ", activityDefinitions.size());
            Task task = new Task();
            task.setId(UUID.randomUUID().toString());
            task.setEventType(jobData.getEventType());
            task.setAction(activityDefinition.getAction());
            task.setComments(CommonConstants.TASK_SUBMITTED);
            task.setJobGroup(jobData.getJobGroup());
            task.setStatus(Status.SUBMITTED);
            task.setActive(Boolean.TRUE);
            task.setRetryDef(activityDefinition.getRetryDef());
            task.setJobName(jobData.getJobName());
            task.setJobType(JobType.ONDEMAND);
            task.setScheduledTime(LocalDateTime.now());
            if (activityDefinition.getExecutionData() != null) {
                task.setArchivalTime(LocalDateTime.now().plusSeconds(activityDefinition.getExecutionData().getExpiryInSeconds()));
            } else {
                task.setArchivalTime(LocalDateTime.now().plusSeconds(archivalTime));
            }
            task.setMeta(new HashMap<>());
            taskList.add(task);
        }));
        return taskList;
    }
}