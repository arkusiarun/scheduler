package com.airtel.scheduler.execution.internal.impl;

import com.airtel.scheduler.execution.enums.InternalJobsEnum;
import com.airtel.scheduler.execution.exception.GenericException;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.properties.InternalJobConfigurations;
import com.airtel.scheduler.execution.properties.JobConfiguration;
import com.airtel.scheduler.execution.repository.ArchivedTaskRepository;
import com.airtel.scheduler.execution.repository.CustomRepository;
import com.airtel.scheduler.execution.repository.TaskRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ArchivalJobTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CustomRepository customRepository;
    @Mock
    private ArchivedTaskRepository archivedTaskRepository;
    @Mock
    private InternalJobConfigurations jobConfiguration;

    private ArchivalJob archivalJob;
    Map<String, Object> map = new HashMap();

    @Before
    public void init() {
        Mockito.when(this.jobConfiguration.getExcludeStatusKey()).thenReturn("exclusionStatuses");
        Mockito.when(this.jobConfiguration.getArchivalLimitKey()).thenReturn("limit");
        archivalJob = new ArchivalJob(taskRepository, customRepository, archivedTaskRepository, jobConfiguration);
        map.put("exclusionStatuses", Arrays.asList("SUBMITTED"));
        map.put("limit", 1000);
    }

    @Test
    public void moveTasksToArchive() {
        Task task = Mockito.mock(Task.class);
        List<Task> taskList = new ArrayList<>();
        taskList.add(task);
        Mockito.when(this.customRepository.fetchTasksForArchival(Mockito.any(), Mockito.any())).thenReturn(taskList);
        this.archivalJob.execute(map);
    }

    @Test
    public void moveTasksToArchiveExcpetionInArchivingTask() {
        Task task = Mockito.mock(Task.class);
        Task task2 = Mockito.mock(Task.class);
        List<Task> taskList = new ArrayList<>();
        taskList.add(task);
        taskList.add(task2);
        Mockito.when(this.customRepository.fetchTasksForArchival(Mockito.any(), Mockito.any())).thenReturn(taskList);
        Mockito.when(task2.getScheduledTime()).thenThrow(new GenericException(""));
        this.archivalJob.execute(map);
    }

    @Test
    public void moveTasksToArchiveNoTask() {
        this.archivalJob.execute(map);
    }

    @Test
    public void getInternalJobTest(){
        Assert.assertEquals(InternalJobsEnum.ARCHIVAL, this.archivalJob.getInternalJob());
    }
}