package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.constants.CommonConstants;
import com.airtel.scheduler.execution.dto.JobData;
import com.airtel.scheduler.execution.dto.ScheduledJobRequest;
import com.airtel.scheduler.execution.dto.SchedulerResponse;
import com.airtel.scheduler.execution.dto.SchedulingInfo;
import com.airtel.scheduler.execution.enums.Duration;
import com.airtel.scheduler.execution.enums.JobExecutionType;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.enums.JobType;
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
import com.airtel.scheduler.execution.model.ScheduledJob;
import com.airtel.scheduler.execution.repository.CustomRepository;
import com.airtel.scheduler.execution.repository.ScheduledJobRepository;
import com.airtel.scheduler.execution.repository.TaskRepository;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceImplTest {

    private OnDemand onDemand;
    private Reoccuring reoccuring;
    private BatchJobImpl batchJob;
    private CustomJobImpl customJob;
    private GenericJobImpl genericJob;
    private SchedulingJob schedulingJob;

    JobData jobData;
    ScheduledJob scheduledJob;
    ScheduledJobRequest scheduledJobRequest;
    private String dummyJobId = "b56ffeca-0a8d-4d35-bb7f-8219178ab97d";

    @Mock
    private ActivityServiceImpl activityService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private JobScheduler jobScheduler;
    @Mock
    private ScheduledJobRepository scheduledJobRepository;
    @Mock
    private TaskExecutionServiceImpl taskExecutionService;
    @Mock
    private CustomRepository customRepository;

    @InjectMocks
    private SchedulerServiceImpl schedulerService;

    @Before
    public void init() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        this.initializeJobsFactory();
        this.initializeJobGroupFactory();
        scheduledJobRequest = Mockito.mock(ScheduledJobRequest.class);
        SchedulingInfo schedulingInfo = new SchedulingInfo();
        schedulingInfo.setOffset(100);
        schedulingInfo.setMaxFetchDuration(100);
        schedulingInfo.setJobExecutionType(JobExecutionType.CURRENT);
        schedulingInfo.setDuration(Duration.SECONDS);
        Mockito.when(scheduledJobRequest.getSchedulingInfo()).thenReturn(schedulingInfo);
        jobData = Mockito.mock(JobData.class);
        scheduledJob = Mockito.mock(ScheduledJob.class);
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

    @Test(expected = SchedulerException.class)
    public void createNewJobFailureRespnse() {
        Mockito.when(scheduledJobRequest.getJobType()).thenReturn(JobType.ONDEMAND);
        Mockito.when(scheduledJobRequest.getJobGroup()).thenReturn(JobGroup.GENERIC);
        Mockito.when(this.scheduledJobRepository.insert(Mockito.any(ScheduledJob.class))).thenThrow(new SchedulerException());
        this.schedulerService.createNewJob(scheduledJobRequest);
    }

    @Test
    public void createNewJobOnDemad() {
        Mockito.when(scheduledJobRequest.getJobType()).thenReturn(JobType.ONDEMAND);
        Mockito.when(scheduledJobRequest.getJobGroup()).thenReturn(JobGroup.GENERIC);
        SchedulerResponse schedulerResponse = this.schedulerService.createNewJob(scheduledJobRequest);
        Assert.assertEquals(CommonConstants.CREATED_MESSAGE, schedulerResponse.getComments());
    }

    @Test
    public void createNewJobReoccurring() {
        Mockito.when(scheduledJobRequest.getJobType()).thenReturn(JobType.REOCCURING);
        Mockito.when(scheduledJobRequest.getJobGroup()).thenReturn(JobGroup.BATCH);
        SchedulerResponse schedulerResponse = this.schedulerService.createNewJob(scheduledJobRequest);
        Assert.assertEquals(CommonConstants.CREATED_MESSAGE, schedulerResponse.getComments());
    }

    @Test
    public void createNewJobScheduling() {
        Mockito.when(scheduledJobRequest.getJobType()).thenReturn(JobType.SCHEDULED);
        Mockito.when(scheduledJobRequest.getJobGroup()).thenReturn(JobGroup.CUSTOM);
        SchedulerResponse schedulerResponse = this.schedulerService.createNewJob(scheduledJobRequest);
        Assert.assertEquals(CommonConstants.CREATED_MESSAGE, schedulerResponse.getComments());
    }

    @Test
    public void createNewReoccurringInternalJob() {
        Mockito.when(scheduledJobRequest.getJobType()).thenReturn(JobType.REOCCURING);
        Mockito.when(scheduledJobRequest.getJobGroup()).thenReturn(JobGroup.INTERNAL);
        SchedulerResponse schedulerResponse = this.schedulerService.createNewJob(scheduledJobRequest);
        Assert.assertEquals(CommonConstants.CREATED_MESSAGE, schedulerResponse.getComments());
    }

    @Test(expected = SchedulerException.class)
    public void unscheduleJobFailureResponse() {
        Mockito.when(this.scheduledJobRepository.findById(dummyJobId)).thenReturn(Optional.ofNullable(scheduledJob));
        Mockito.when(scheduledJob.getJobType()).thenReturn(JobType.ONDEMAND);
        Mockito.when(scheduledJob.getId()).thenReturn(dummyJobId);
        Mockito.when(this.scheduledJobRepository.save(Mockito.any(ScheduledJob.class))).thenThrow(new SchedulerException());
        this.schedulerService.unScheduleJob(dummyJobId);
    }

    @Test
    public void unscheduleOnDemandJob() {
        Mockito.when(this.scheduledJobRepository.findById(dummyJobId)).thenReturn(Optional.ofNullable(scheduledJob));
        Mockito.when(scheduledJob.getJobType()).thenReturn(JobType.ONDEMAND);
        Mockito.when(scheduledJob.getId()).thenReturn(dummyJobId);
        Assert.assertTrue(this.schedulerService.unScheduleJob("b56ffeca-0a8d-4d35-bb7f-8219178ab97d"));
    }

    @Test
    public void unscheduleReoccurringJob() {
        Mockito.when(this.scheduledJobRepository.findById(dummyJobId)).thenReturn(Optional.ofNullable(scheduledJob));
        Mockito.when(scheduledJob.getJobType()).thenReturn(JobType.REOCCURING);
        Mockito.when(scheduledJob.getId()).thenReturn(dummyJobId);
        Assert.assertTrue(this.schedulerService.unScheduleJob(dummyJobId));
    }

    @Test
    public void unscheduleScheduledJob() {
        Mockito.when(this.scheduledJobRepository.findById(dummyJobId)).thenReturn(Optional.ofNullable(scheduledJob));
        Mockito.when(scheduledJob.getJobType()).thenReturn(JobType.SCHEDULED);
        Mockito.when(scheduledJob.getId()).thenReturn(dummyJobId);
        Assert.assertTrue(this.schedulerService.unScheduleJob(dummyJobId));
    }

    @Test
    public void fetchScheduledJobById() {
        ScheduledJob scheduledJob = new ScheduledJob();
        Mockito.when(this.scheduledJobRepository.findById(dummyJobId)).thenReturn(Optional.of(scheduledJob));
        Assert.assertEquals(scheduledJob, this.schedulerService.fetchScheduledJobById(dummyJobId));
    }

    @Test(expected = SchedulerException.class)
    public void fetchScheduledJobByIdException() {
        this.schedulerService.fetchScheduledJobById(dummyJobId);
    }
}