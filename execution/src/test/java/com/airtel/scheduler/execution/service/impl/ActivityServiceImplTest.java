package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.ActivityDefinition;
import com.airtel.scheduler.execution.repository.ActivityDefinitionRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ActivityServiceImplTest {

    @InjectMocks
    private ActivityServiceImpl activityService;

    @Mock
    private ActivityDefinitionRepository activityDefinitionRepository;

    @Test
    public void reload() {
        ActivityDefinition activityDefinition = new ActivityDefinition();
        activityDefinition.setActive(Boolean.TRUE);
        activityDefinition.setEvent("Test Event");
        List<ActivityDefinition> activityDefinitionList = new ArrayList<>();
        activityDefinitionList.add(activityDefinition);
        Mockito.when(this.activityDefinitionRepository.findAll()).thenReturn(activityDefinitionList);
        Assert.assertTrue(this.activityService.reload().isSuccess());
    }

    @Test(expected = SchedulerException.class)
    public void reloadException() {
        Mockito.when(this.activityDefinitionRepository.findAll()).thenThrow(new SchedulerException());
        this.activityService.reload();
    }

    @Test
    public void getAcvityListForEventType() {
        this.activityService.getActivityListForEventType("TestEvent");
    }

    @Test
    public void createActivity() {
        Assert.assertTrue(this.activityService.createActivity(Mockito.mock(ActivityDefinition.class)).isSuccess());
    }

    @Test(expected = SchedulerException.class)
    public void createActivityException() {
        ActivityDefinition activityDefinition = Mockito.mock(ActivityDefinition.class);
        Mockito.when(this.activityDefinitionRepository.save(activityDefinition)).thenThrow(new SchedulerException("Exception Test"));
        this.activityService.createActivity(activityDefinition);
    }
}