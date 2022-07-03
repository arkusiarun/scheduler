package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.repository.ActionRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActionServiceImplTest {

    @InjectMocks
    private ActionServiceImpl actionService;

    @Mock
    private ActionRepository actionRepository;

    private String actionName = "Test Action";

    @Test
    public void getActionFromName() {
        Mockito.when(this.actionRepository.findById(actionName)).thenReturn(java.util.Optional.of(Mockito.mock(Action.class)));
        Assert.assertNotNull(this.actionService.getActionFromName(actionName));
    }

    @Test(expected = SchedulerException.class)
    public void getActionFromNameException() {
        Mockito.when(this.actionRepository.findById(actionName)).thenThrow(new SchedulerException("Exception Test"));
        this.actionService.getActionFromName(actionName);
    }

    @Test
    public void createAction() {
        Assert.assertTrue(this.actionService.createAction(Mockito.mock(Action.class)).isSuccess());
    }

    @Test(expected = SchedulerException.class)
    public void createActionException() {
        Action action = Mockito.mock(Action.class);
        Mockito.when(this.actionRepository.save(action)).thenThrow(new SchedulerException("Exception Test"));
        this.actionService.createAction(action);
    }
}