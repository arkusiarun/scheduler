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
 *
 * Batch jobs are Similar to Generic Job as their
 * DataSource is Task.
 * These Jons will Fetch from Task whose Job Type
 * is Batch Job with and Offset and execute them
 * Sequentially.
 * Difference Between Batch and Generic Job is
 * Batch Job will not trigger any Background Job.
 *
 */
@Component
public class BatchJobImpl implements JobGroups {

    private final Logger logger = LoggerFactory.getLogger(BatchJobImpl.class);

    private CustomRepository customRepository;

    private TaskExecutionService taskExecutionService;

    private JobScheduler jobScheduler;

    @Autowired
    public BatchJobImpl(CustomRepository customRepository, TaskExecutionService taskExecutionService, @Qualifier("jobSchedulerBean") JobScheduler jobScheduler) {
        this.customRepository = customRepository;
        this.taskExecutionService = taskExecutionService;
        this.jobScheduler = jobScheduler;
    }

    @Override
    public JobGroup getJobGroup() {
        return JobGroup.BATCH;
    }

    @Override
    @Job(name = "Batch Job")
    public void execute(JobData jobData) {
        logger.info("Batch Job Execution Starts Job Name :{}", jobData.getJobName());
        List<Task> taskList = this.customRepository.findTasksForBatchExecution(jobData.getJobName(), jobData.getOffset(), jobData.getJobExecutionType(), jobData.getMaxFetchDuration(), jobData.getDuration());
        if(CommonUtils.isEmptyList(taskList)) {
            logger.error("Currently No Tasks Present corresponding to Batch Job :{}", jobData.getJobName());
            return;
        }
        logger.info("Executing :{} Tasks corresponding to Job :{}", taskList.size(), jobData.getJobName());
        for(Task task: taskList) {
            this.executeTasks(task, jobData);
        }
    }

    @Async("taskExecutionThreadPoolExecutor")
    void executeTasks(Task task, JobData jobData) {
        logger.info("Executing Task With Meta as : {}, at Time :{}", task, System.currentTimeMillis());
        try {
            UUID jobId = UUID.fromString(task.getId());
            this.jobScheduler.enqueue(jobId, () -> this.taskExecutionService.executeTask(task, jobData));
        } catch (Exception e) {
            logger.error("Failure While Executing Task For Batch Job: {}", task, e);
        }
    }
}