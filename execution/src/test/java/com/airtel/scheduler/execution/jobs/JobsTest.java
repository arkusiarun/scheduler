package com.airtel.scheduler.execution.jobs;

import com.airtel.scheduler.execution.dto.ExecutionData;
import com.airtel.scheduler.execution.dto.JobData;
import com.airtel.scheduler.execution.dto.JobDef;
import com.airtel.scheduler.execution.dto.RetryDef;
import com.airtel.scheduler.execution.enums.ExecutionType;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.job.JobFactory;
import com.airtel.scheduler.execution.job.Jobs;
import com.airtel.scheduler.execution.job.impl.OnDemand;
import com.airtel.scheduler.execution.job.impl.Reoccuring;
import com.airtel.scheduler.execution.job.impl.SchedulingJob;
import com.airtel.scheduler.execution.jobgroup.JobGroupFactory;
import com.airtel.scheduler.execution.jobgroup.JobGroups;
import com.airtel.scheduler.execution.jobgroup.impl.BatchJobImpl;
import com.airtel.scheduler.execution.jobgroup.impl.CustomJobImpl;
import com.airtel.scheduler.execution.jobgroup.impl.GenericJobImpl;
import com.airtel.scheduler.execution.model.ActivityDefinition;
import com.airtel.scheduler.execution.model.ScheduledJob;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.repository.CustomRepository;
import com.airtel.scheduler.execution.repository.ScheduledJobRepository;
import com.airtel.scheduler.execution.repository.TaskRepository;
import com.airtel.scheduler.execution.service.TaskExecutionService;
import com.airtel.scheduler.execution.service.impl.ActivityServiceImpl;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class JobsTest {

    private Task task;
    private JobData jobData;
    private ScheduledJob scheduledJob;
    private ActivityDefinition activityDefinition;

    private OnDemand onDemand;
    private Reoccuring reoccuring;
    private BatchJobImpl batchJob;
    private CustomJobImpl customJob;
    private GenericJobImpl genericJob;
    private SchedulingJob schedulingJob;

    @Mock
    private JobScheduler jobScheduler;
    @Mock
    private CustomRepository customRepository;
    @Mock
    private ActivityServiceImpl activityService;
    @Mock
    private ScheduledJobRepository scheduledJobRepository;
    @Mock
    private TaskExecutionService taskExecutionService;
    @Mock
    private TaskRepository taskRepository;

    @Before
    public void init() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        initializeJobGroupFactory();
        initializeJobsFactory();
        task = Mockito.mock(Task.class);
        jobData = Mockito.mock(JobData.class);
        scheduledJob = Mockito.mock(ScheduledJob.class);
        Mockito.when(this.jobData.getScheduledJobId()).thenReturn("DummyJobId");
        Mockito.when(this.scheduledJobRepository.findById("DummyJobId")).thenReturn(Optional.of(scheduledJob));
        List<Task> taskList = new ArrayList<>();
        taskList.add(task);
        activityDefinition = new ActivityDefinition();
        activityDefinition.setEvent("DummyEvent");
        activityDefinition.setAction("DummyAction");
        activityDefinition.setActive(Boolean.TRUE);
        RetryDef retryDef = new RetryDef();
        retryDef.setRetryEnabled(Boolean.TRUE);
        retryDef.setRetriesLeft(100);
        retryDef.setRetryInterval(Long.parseLong("100"));
        JobDef jobDef = new JobDef();
        jobDef.setJobName("DummyJob");
        jobDef.setJobGroup(JobGroup.CUSTOM);
        activityDefinition.setJobDef(jobDef);
        ExecutionData executionData = new ExecutionData();
        executionData.setExecutionType(ExecutionType.ABSOLUTE);
        executionData.setExecutionAfterInSeconds(Long.parseLong("100"));
        executionData.setExpiryInSeconds(Long.parseLong("100"));
        activityDefinition.setExecutionData(executionData);
        activityDefinition.setRetryDef(retryDef);
        List<ActivityDefinition> activityDefinitionList = new ArrayList<>();
        activityDefinitionList.add(activityDefinition);
        Mockito.when(jobData.getEventType()).thenReturn("DummyEvent");
        Mockito.when(this.activityService.getActivityListForEventType("DummyEvent")).thenReturn(Optional.ofNullable(activityDefinitionList));
        Mockito.when(this.customRepository.findTasksForGenericExecution(null, 0, null, 0, null)).thenReturn(taskList);
        Mockito.when(this.customRepository.findTasksForBatchExecution(null, 0, null, 0, null)).thenReturn(taskList);
    }

    private void initializeJobGroupFactory() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        batchJob = new BatchJobImpl(customRepository, taskExecutionService, jobScheduler);
        customJob = new CustomJobImpl(taskExecutionService, activityService, taskRepository, jobScheduler);
        genericJob = new GenericJobImpl(customRepository, jobScheduler, taskExecutionService);

        List<JobGroups> jobGroups = new ArrayList<>();
        jobGroups.add(batchJob);
        jobGroups.add(customJob);
        jobGroups.add(genericJob);

        Class jobGroupFactoryClass = JobGroupFactory.class;
        Constructor jobGroupFactoryClassDeclaredConstructor = jobGroupFactoryClass.getDeclaredConstructors()[0];
        jobGroupFactoryClassDeclaredConstructor.setAccessible(true);
        jobGroupFactoryClassDeclaredConstructor.newInstance(jobGroups);
    }

    private void initializeJobsFactory() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        reoccuring = new Reoccuring(jobScheduler);
        onDemand = new OnDemand(jobScheduler, scheduledJobRepository);
        schedulingJob = new SchedulingJob(jobScheduler, scheduledJobRepository);

        List<Jobs> jobs = new ArrayList<>();
        jobs.add(reoccuring);
        jobs.add(onDemand);
        jobs.add(schedulingJob);

        Class jobFactoryClass = JobFactory.class;
        Constructor jobFactoryClassDeclaredConstructor = jobFactoryClass.getDeclaredConstructors()[0];
        jobFactoryClassDeclaredConstructor.setAccessible(true);
        jobFactoryClassDeclaredConstructor.newInstance(jobs);
    }

    @Test
    public void OnDemandBatchJobExecutionEmptyTaskTest() {
        Mockito.when(this.customRepository.findTasksForBatchExecution(null, 0, null, 0, null)).thenReturn(new ArrayList<>());
        Mockito.when(jobData.getJobGroup()).thenReturn(JobGroup.BATCH);
        this.onDemand.execute(jobData);
    }

    @Test
    public void OnDemandBatchJobExecutionTest() {
        Mockito.when(jobData.getJobGroup()).thenReturn(JobGroup.BATCH);
        this.onDemand.execute(jobData);
    }

    @Test
    public void OnDemandGenericJobExecutionEmptyTaskTest() {
        Mockito.when(this.customRepository.findTasksForGenericExecution(null, 0, null, 0, null)).thenReturn(new ArrayList<>());
        Mockito.when(jobData.getJobGroup()).thenReturn(JobGroup.GENERIC);
        this.onDemand.execute(jobData);
    }

    @Test
    public void OnDemandGenericJobExecutionTest() {
        Mockito.when(jobData.getJobGroup()).thenReturn(JobGroup.GENERIC);
        this.onDemand.execute(jobData);
    }

    @Test(expected = SchedulerException.class)
    public void OnDemandCustomJobExecutionExceptionTest() {
        Mockito.when(jobData.getJobGroup()).thenReturn(JobGroup.CUSTOM);
        Mockito.when(this.activityService.getActivityListForEventType("DummyEvent")).thenThrow(new SchedulerException());
        this.onDemand.execute(jobData);
    }

    @Test
    public void OnDemandCustomJobExecutionTest() {
        Mockito.when(jobData.getJobGroup()).thenReturn(JobGroup.CUSTOM);
        this.onDemand.execute(jobData);
    }

    @Test
    public void ReoccurringExecuteTest() {
        Mockito.when(jobData.getJobGroup()).thenReturn(JobGroup.CUSTOM);
        this.reoccuring.execute(jobData);
    }

    @Test
    public void SchedulingJobExecuteTest() {
        Mockito.when(jobData.getJobGroup()).thenReturn(JobGroup.CUSTOM);
        this.schedulingJob.execute(jobData);
    }
}