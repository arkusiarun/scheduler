package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.actions.ActionFactory;
import com.airtel.scheduler.execution.actions.Actions;
import com.airtel.scheduler.execution.actions.impl.HttpAction;
import com.airtel.scheduler.execution.actions.impl.InternalAction;
import com.airtel.scheduler.execution.actions.impl.WorkerAction;
import com.airtel.scheduler.execution.dto.*;
import com.airtel.scheduler.execution.enums.ActionType;
import com.airtel.scheduler.execution.enums.ExecutionType;
import com.airtel.scheduler.execution.enums.JobGroup;
import com.airtel.scheduler.execution.enums.Status;
import com.airtel.scheduler.execution.exception.GenericException;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.internal.InternalJobFactory;
import com.airtel.scheduler.execution.internal.InternalJobService;
import com.airtel.scheduler.execution.internal.impl.ArchivalJob;
import com.airtel.scheduler.execution.kafka.KafkaProducer;
import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.model.ActivityDefinition;
import com.airtel.scheduler.execution.model.EventConfig;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.properties.InternalJobConfigurations;
import com.airtel.scheduler.execution.repository.ArchivedTaskRepository;
import com.airtel.scheduler.execution.repository.CustomRepository;
import com.airtel.scheduler.execution.repository.TaskRepository;
import com.airtel.scheduler.execution.service.ActionService;
import com.airtel.scheduler.execution.utils.CommonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskExecutionServiceImplTest {

    HttpAction httpAction;
    WorkerAction workerAction;
    InternalAction internalAction;

    Task task;
    Action action;
    JobData jobData;
    RetryDef retryDef;
    ActionDefinition actionDefinition;
    HttpDefinition httpDefinition;
    WorkerDefinition workerDefinition;
    InternalActionDefinition internalActionDefinition;
    TaskResponse taskResponse;
    TaskRequest taskRequest;
    EventConfig eventConfig;
    Response<TaskResponse> response;
    Map<String, Object> meta;
    ResponseEntity<Response<TaskResponse>> responseEntity;
    private String dummyActionName = "DummyAction";
    ActivityDefinition activityDefinition;
    private String dummyJobId = "b56ffeca-0a8d-4d35-bb7f-8219178ab97d";
    private ArchivalJob archivalJob;

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private ActionService actionService;
    @Mock
    private EventServiceImpl eventService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private Environment environment;
    @Mock
    private CustomRepository customRepository;
    @Mock
    private ActivityServiceImpl activityService;
    @Mock
    private ArchivedTaskRepository archivedTaskRepository;
    @Mock
    private InternalJobConfigurations jobConfiguration;

    @InjectMocks
    private TaskExecutionServiceImpl taskExecutionService;

    @Before
    public void init() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        initializeActionFactory();
        task = Mockito.mock(Task.class);
        taskRequest = Mockito.mock(TaskRequest.class);
        meta = new HashMap<>();
        meta.put("key", "value");
        Mockito.when(taskRequest.getReferenceTime()).thenReturn(Instant.now().getEpochSecond());
        Mockito.when(taskRequest.getEventType()).thenReturn("DummyEvent");
        Mockito.when(taskRequest.getMeta()).thenReturn(meta);
        activityDefinition = new ActivityDefinition();
        activityDefinition.setEvent("DummyEvent");
        activityDefinition.setAction("DummyAction");
        activityDefinition.setActive(Boolean.TRUE);
        retryDef = new RetryDef();
        retryDef.setRetryEnabled(Boolean.TRUE);
        retryDef.setRetriesLeft(100);
        retryDef.setRetryInterval(Long.parseLong("100"));
        JobDef jobDef = new JobDef();
        jobDef.setJobName("DummyJob");
        jobDef.setJobGroup(JobGroup.BATCH);
        activityDefinition.setJobDef(jobDef);
        ExecutionData executionData = new ExecutionData();
        executionData.setExecutionType(ExecutionType.ABSOLUTE);
        executionData.setExecutionAfterInSeconds(Long.parseLong("100"));
        executionData.setExpiryInSeconds(Long.parseLong("100"));
        activityDefinition.setExecutionData(executionData);
        activityDefinition.setRetryDef(retryDef);
        List<ActivityDefinition> activityDefinitionList = new ArrayList<>();
        activityDefinitionList.add(activityDefinition);
        Mockito.when(this.activityService.getActivityListForEventType(taskRequest.getEventType())).thenReturn(Optional.ofNullable(activityDefinitionList));
        Mockito.when(this.taskRepository.insert(Mockito.anyIterable())).thenReturn(null);
        Mockito.when(task.getAction()).thenReturn(dummyActionName);
        Mockito.when(task.getId()).thenReturn("DummyTaskId");
        Mockito.when(task.getEventType()).thenReturn("DummyEvent");
        Mockito.when(task.getRetryDef()).thenReturn(retryDef);
        action = Mockito.mock(Action.class);
        retryDef = new RetryDef();
        retryDef.setRetryEnabled(Boolean.TRUE);
        retryDef.setRetriesLeft(2);
        retryDef.setRetryInterval(100);
        actionDefinition = Mockito.mock(ActionDefinition.class);
        httpDefinition = Mockito.mock(HttpDefinition.class);
        workerDefinition = Mockito.mock(WorkerDefinition.class);
        Mockito.when(action.getActionDefinition()).thenReturn(actionDefinition);
        Map map = new HashMap();
        map.put("DummyKey", "DummyValue");
        Mockito.when(httpDefinition.getBody()).thenReturn(map);
        jobData = Mockito.mock(JobData.class);
        taskResponse = Mockito.mock(TaskResponse.class);
        Mockito.when(httpDefinition.getHeaders()).thenReturn(map);
        Mockito.when(httpDefinition.getRequestParams()).thenReturn(map);
        Mockito.when(httpDefinition.getBody()).thenReturn(map);
        Mockito.when(httpDefinition.getMethod()).thenReturn(HttpMethod.POST);
        Mockito.when(httpDefinition.getEndpoint()).thenReturn("http://localhost:8080/test");
        Mockito.when(httpDefinition.getSubOrdinateEndpoint()).thenReturn("http://localhost:8080/test");
        Mockito.when(actionDefinition.getHttpDefinition()).thenReturn(httpDefinition);
        Mockito.when(actionDefinition.getWorkerDefinition()).thenReturn(workerDefinition);
        Mockito.when(workerDefinition.getTopicName()).thenReturn("DummyTopic");
        Mockito.when(this.actionService.getActionFromName(dummyActionName)).thenReturn(action);
        responseEntity = (ResponseEntity<Response<TaskResponse>>) Mockito.mock(ResponseEntity.class);
        response = (Response<TaskResponse>) Mockito.mock(Response.class);
        Mockito.when(this.restTemplate.exchange(Mockito.anyString(), Mockito.any(),
                Mockito.any(),
                (org.springframework.core.ParameterizedTypeReference<Response<TaskResponse>>) Mockito.any()))
                .thenReturn(responseEntity);
        Mockito.when(responseEntity.getBody()).thenReturn(response);
        Mockito.when(response.getBody()).thenReturn(taskResponse);
        eventConfig = Mockito.mock(EventConfig.class);
        Mockito.when(eventConfig.getReferenceKeys()).thenReturn(Arrays.asList("key"));
        Mockito.when(task.getStatus()).thenReturn(Status.SUBMITTED);
        Mockito.when(task.getActive()).thenReturn(true);
        Mockito.when(this.taskRepository.findById("DummyTaskId")).thenReturn(Optional.ofNullable(task));
        Mockito.when(this.eventService.getConfigsForEventType("DummyEvent")).thenReturn(eventConfig);
        Mockito.when(this.environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        internalActionDefinition = Mockito.mock(InternalActionDefinition.class);
        Mockito.when(actionDefinition.getInternalActionDefinition()).thenReturn(internalActionDefinition);
    }

    private void initializeActionFactory() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        httpAction = new HttpAction(restTemplate, environment);
        workerAction = new WorkerAction(kafkaProducer);
        internalAction = new InternalAction();

        List<Actions> actions = new ArrayList<>();
        actions.add(httpAction);
        actions.add(workerAction);
        actions.add(internalAction);

        Class actionFactoryClass = ActionFactory.class;
        Constructor actionFactoryClassDeclaredConstructor = actionFactoryClass.getDeclaredConstructors()[0];
        actionFactoryClassDeclaredConstructor.setAccessible(true);
        actionFactoryClassDeclaredConstructor.newInstance(actions);

        archivalJob = new ArchivalJob(taskRepository, customRepository, archivedTaskRepository, jobConfiguration);
        List<InternalJobService> jobServices = new ArrayList<>();
        jobServices.add(archivalJob);

        Class internalJobFactoryClass = InternalJobFactory.class;
        Constructor internalJobFactoryClassDeclaredConstructor = internalJobFactoryClass.getDeclaredConstructors()[0];
        internalJobFactoryClassDeclaredConstructor.setAccessible(true);
        internalJobFactoryClassDeclaredConstructor.newInstance(jobServices);
    }

    @Test
    public void createTaskForSchedulerWithoutOldTasksSuccessTest() {
        ResponseEntity<Object> responseEntity = this.taskExecutionService.createTaskForScheduler(taskRequest);
        Assert.assertTrue(((Response) responseEntity.getBody()).isSuccess());
    }

    @Test
    public void createTaskForSchedulerWithOldTasksSuccessTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        Mockito.when(this.customRepository.fetchExistingTasks("DummyEvent", CommonUtils.getSHA256Hex(map))).thenReturn(Arrays.asList(task));
        ResponseEntity<Object> responseEntity = this.taskExecutionService.createTaskForScheduler(taskRequest);
        Assert.assertTrue(((Response) responseEntity.getBody()).isSuccess());
    }

    @Test
    public void createTaskForSchedulerErrorResponseTest() {
        Mockito.when(taskRequest.getReferenceTime()).thenThrow(new SchedulerException());
        ResponseEntity<Object> responseEntity = this.taskExecutionService.createTaskForScheduler(taskRequest);
        Assert.assertFalse(((Response) responseEntity.getBody()).isSuccess());
    }

    @Test
    public void createTaskForSchedulerNoActivityTest() {
        Mockito.when(this.activityService.getActivityListForEventType(taskRequest.getEventType())).thenReturn(Optional.ofNullable(null));
        ResponseEntity<Object> responseEntity = this.taskExecutionService.createTaskForScheduler(taskRequest);
        Assert.assertFalse(((Response) responseEntity.getBody()).isSuccess());
    }

    @Test(expected = SchedulerException.class)
    public void validateTaskFailureResponse() {
        Mockito.when(this.taskRepository.findById(dummyJobId)).thenThrow(new SchedulerException());
        this.taskExecutionService.validateTask(dummyJobId);
    }

    @Test
    public void validateTask() {
        SchedulerResponse schedulerResponse = this.taskExecutionService.validateTask("DummyTaskId");
        Assert.assertEquals(Status.SUBMITTED, schedulerResponse.getStatus());
    }

    @Test
    public void cancelTaskTestWithId() {
        CancelTaskRequest cancelTaskRequest = Mockito.mock(CancelTaskRequest.class);
        Mockito.when(cancelTaskRequest.getId()).thenReturn("DummyTaskId");
        List<SchedulerResponse> responses = this.taskExecutionService.cancelTask(cancelTaskRequest);
        Assert.assertEquals(1, responses.size());
    }

    @Test
    public void cancelTaskTestWithoutId() {
        Task task = Mockito.mock(Task.class);
        Task task2 = Mockito.mock(Task.class);
        List<Task> taskList = new ArrayList<>();
        taskList.add(task);
        taskList.add(task2);
        Map<String, Object> meta = new HashMap<>();
        meta.put("key", "value");
        CancelTaskRequest cancelTaskRequest = Mockito.mock(CancelTaskRequest.class);
        Mockito.when(cancelTaskRequest.getEventType()).thenReturn("DummyEvent");
        Mockito.when(cancelTaskRequest.getMeta()).thenReturn(meta);
        Mockito.when(customRepository.fetchExistingTasks(cancelTaskRequest.getEventType(), CommonUtils.getSHA256Hex(meta))).thenReturn(taskList);
        List<SchedulerResponse> schedulerResponses = this.taskExecutionService.cancelTask(cancelTaskRequest);
        Assert.assertEquals(2, schedulerResponses.size());
    }

    @Test(expected = SchedulerException.class)
    public void cancelTaskTestNoTaskFound() {
        Task task = Mockito.mock(Task.class);
        CancelTaskRequest cancelTaskRequest = Mockito.mock(CancelTaskRequest.class);
        Mockito.when(this.taskExecutionService.fetchTaskById(dummyJobId)).thenThrow(new SchedulerException());
        this.taskExecutionService.cancelTask(cancelTaskRequest);
    }

    @Test(expected = SchedulerException.class)
    public void cancelTaskTestEmptyTaskList() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("key", "value");
        CancelTaskRequest cancelTaskRequest = Mockito.mock(CancelTaskRequest.class);
        Mockito.when(cancelTaskRequest.getEventType()).thenReturn("DummyEvent");
        Mockito.when(cancelTaskRequest.getMeta()).thenReturn(meta);
        Mockito.when(customRepository.fetchExistingTasks(cancelTaskRequest.getEventType(), CommonUtils.getSHA256Hex(meta))).thenReturn(new ArrayList<>());
        this.taskExecutionService.cancelTask(cancelTaskRequest);
    }

    @Test(expected = NullPointerException.class)
    public void executeTaskWithHttpActionFailure() {
        Mockito.when(task.getAction()).thenReturn(null);
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test(expected = GenericException.class)
    public void executeTaskWithHttpActionFailureWithoutRetry() {
        Mockito.when(task.getAction()).thenReturn(dummyActionName);
        Mockito.when(action.getType()).thenReturn(ActionType.HTTP);
        Mockito.when(responseEntity.getBody()).thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST));
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test(expected = SchedulerException.class)
    public void executeTaskWithHttpFailureWithRetry() {
        Mockito.when(action.getType()).thenReturn(ActionType.HTTP);
        Mockito.when(task.getArchivalTime()).thenReturn(LocalDateTime.now());
        Mockito.when(responseEntity.getBody()).thenThrow(new HttpClientErrorException(HttpStatus.GATEWAY_TIMEOUT));
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test(expected = SchedulerException.class)
    public void executeTaskWithHttpFailureWithRetryDisabled() {
        Mockito.when(task.getAction()).thenReturn(dummyActionName);
        Mockito.when(action.getType()).thenReturn(ActionType.HTTP);
        Mockito.when(task.getArchivalTime()).thenReturn(LocalDateTime.now());
        Mockito.when(responseEntity.getBody()).thenThrow(new HttpClientErrorException(HttpStatus.GATEWAY_TIMEOUT));
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test(expected = SchedulerException.class)
    public void executeTaskWithHttpFailureResourseException() {
        Mockito.when(task.getAction()).thenReturn(dummyActionName);
        Mockito.when(action.getType()).thenReturn(ActionType.HTTP);
        Mockito.when(task.getArchivalTime()).thenReturn(LocalDateTime.now());
        Mockito.when(responseEntity.getBody()).thenThrow(new ResourceAccessException("Resourse Not Available"));
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test(expected = SchedulerException.class)
    public void executeTaskWithHttpFailureWithRetryNotAllowed() {
        Mockito.when(action.getType()).thenReturn(ActionType.HTTP);
        Mockito.when(jobData.getJobGroup()).thenReturn(JobGroup.CUSTOM);
        Mockito.when(responseEntity.getBody()).thenThrow(new HttpClientErrorException(HttpStatus.GATEWAY_TIMEOUT));
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test
    public void executeTaskWithHttpActionNullResponse() {
        Mockito.when(action.getType()).thenReturn(ActionType.HTTP);
        Mockito.when(this.environment.getActiveProfiles()).thenReturn(new String[]{"drprod"});
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test(expected = SchedulerException.class)
    public void executeTaskWithHttpActionWithValidResponseWithIncorrectStatus() {
        Mockito.when(action.getType()).thenReturn(ActionType.HTTP);
        Mockito.when(task.getArchivalTime()).thenReturn(LocalDateTime.now());
        Mockito.when(responseEntity.getBody()).thenReturn(response);
        Mockito.when(taskResponse.getStatus()).thenReturn(Status.SUBMITTED);
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test
    public void executeTaskWithHttpActionWithValidResponse() {
        Mockito.when(action.getType()).thenReturn(ActionType.HTTP);
        Mockito.when(responseEntity.getBody()).thenReturn(response);
        Mockito.when(taskResponse.getStatus()).thenReturn(Status.COMPLETED);
        Mockito.when(task.getStatus()).thenReturn(Status.SUBMITTED);
        List<String> pathParams = new ArrayList<>();
        pathParams.add("DummyKey");
        Mockito.when(httpDefinition.getPathParams()).thenReturn(pathParams);
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test(expected = GenericException.class)
    public void executeTaskWithHttpActionWithNullPointerInResponse() {
        Mockito.when(action.getType()).thenReturn(ActionType.HTTP);
        Mockito.when(task.getStatus()).thenReturn(Status.SUBMITTED);
        Mockito.when(httpDefinition.getPathParams()).thenThrow(new NullPointerException());
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test
    public void executeTaskWithWorkerAction() {
        Mockito.when(action.getType()).thenReturn(ActionType.WORKER);
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        Mockito.when(workerDefinition.getHeaders()).thenReturn(map);
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test(expected = SchedulerException.class)
    public void executeTaskWithWorkerActionExceptionTest() {
        Mockito.when(action.getType()).thenReturn(ActionType.WORKER);
        Mockito.when(task.getArchivalTime()).thenReturn(LocalDateTime.now());
        Mockito.when(workerDefinition.getTopicName()).thenThrow(new SchedulerException());
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test
    public void executeTaskWithWorkerActionException() {
        Mockito.when(task.getStatus()).thenReturn(Status.FAILED);
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test(expected = GenericException.class)
    public void executeTaskForInternalBeanNotFound() {
        Mockito.when(task.getAction()).thenReturn(dummyActionName);
        Mockito.when(action.getType()).thenReturn(ActionType.INTERNAL);
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test
    public void executeTaskForInternal() {
        Mockito.when(task.getAction()).thenReturn(dummyActionName);
        Mockito.when(action.getType()).thenReturn(ActionType.INTERNAL);
        Mockito.when(internalActionDefinition.getBeanName()).thenReturn("ARCHIVAL");
        Map<String, Object> map = new HashMap<>();
        map.put("exclusionStatuses", Arrays.asList("SUBMITTED"));
        map.put("limit", 1000);
        Mockito.when(internalActionDefinition.getBody()).thenReturn(map);
        this.taskExecutionService.executeTask(task, jobData);
    }

    @Test
    public void saveCallback() {
        Mockito.when(taskResponse.getTaskId()).thenReturn("DummyId");
        Mockito.when(taskResponse.getStatus()).thenReturn(Status.COMPLETED);
        Mockito.when(this.taskRepository.findById("DummyId")).thenReturn(Optional.of(task));
        Mockito.when(task.getStatus()).thenReturn(Status.SUBMITTED);
        SchedulerResponse schedulerResponse = this.taskExecutionService.saveCallback(taskResponse);
        Assert.assertEquals(Status.COMPLETED, schedulerResponse.getStatus());
    }

    @Test(expected = SchedulerException.class)
    public void saveCallbackErrorResponse() {
        Mockito.when(taskResponse.getTaskId()).thenReturn("DummyId");
        this.taskExecutionService.saveCallback(taskResponse);
    }
}