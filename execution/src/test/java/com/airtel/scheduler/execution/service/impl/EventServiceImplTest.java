package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.EventConfig;
import com.airtel.scheduler.execution.repository.EventConfigRepository;
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
public class EventServiceImplTest {

    @InjectMocks
    private EventServiceImpl eventService;

    @Mock
    private EventConfigRepository eventConfigRepository;

    @Test
    public void reload() {
        EventConfig eventConfig = new EventConfig();
        eventConfig.setId("DummyEvent");
        eventConfig.setActive(Boolean.TRUE);
        eventConfig.setDescription("Dummy Event");
        List<EventConfig> eventConfigList = new ArrayList<>();
        eventConfigList.add(eventConfig);
        Mockito.when(this.eventConfigRepository.findAll()).thenReturn(eventConfigList);
        Assert.assertTrue(this.eventService.reload().isSuccess());
    }

    @Test(expected = SchedulerException.class)
    public void reloadException() {
        Mockito.when(this.eventConfigRepository.findAll()).thenThrow(new SchedulerException());
        this.eventService.reload();
    }

    @Test
    public void getConfigsForEventType() {
        this.eventService.getConfigsForEventType("DummyEvent");
    }

    @Test
    public void createNewEvent() {
        Assert.assertTrue(this.eventService.createNewEvent(Mockito.mock(EventConfig.class)).isSuccess());
    }

    @Test(expected = SchedulerException.class)
    public void createNewEventException() {
        EventConfig eventConfig = Mockito.mock(EventConfig.class);
        Mockito.when(this.eventConfigRepository.save(eventConfig)).thenThrow(new SchedulerException("Exception Test"));
        this.eventService.createNewEvent(eventConfig);
    }
}