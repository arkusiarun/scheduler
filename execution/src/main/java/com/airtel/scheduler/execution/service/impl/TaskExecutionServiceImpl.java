package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.actions.ActionFactory;
import com.airtel.scheduler.execution.constants.CommonConstants;
import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.dto.*;
import com.airtel.scheduler.execution.enums.ResponseCodes;
import com.airtel.scheduler.execution.enums.Status;
import com.airtel.scheduler.execution.enums.ValidationErrorCodes;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.model.ActivityDefinition;
import com.airtel.scheduler.execution.model.EventConfig;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.repository.CustomRepository;
import com.airtel.scheduler.execution.repository.TaskRepository;
import com.airtel.scheduler.execution.service.ActionService;
import com.airtel.scheduler.execution.service.ActivityService;
import com.airtel.scheduler.execution.service.EventService;
import com.airtel.scheduler.execution.service.TaskExecutionService;
import com.airtel.scheduler.execution.utils.CommonUtils;
import com.airtel.scheduler.execution.utils.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class TaskExecutionServiceImpl implements TaskExecutionService {

    private final Logger logger = LoggerFactory.getLogger(TaskExecutionServiceImpl.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ActionService actionService;

    @Autowired
    private EventService eventService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private CustomRepository customRepository;

    @Override
    public ResponseEntity<Object> createTaskForScheduler(TaskRequest taskRequest) {
        ResponseEntity<Object> responseEntity;
        Response<List<SchedulerResponse>> schedulerResponses = pushTasksForExecution(taskRequest);
        if (schedulerResponses.isSuccess()) {
            responseEntity = new ResponseEntity<>(schedulerResponses, HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity<>(schedulerResponses, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }

    @Override
    public Response<List<SchedulerResponse>> pushTasksForExecution(TaskRequest taskRequest) {
        List<SchedulerResponse> schedulerResponseList = new ArrayList<>();
        List<Task> taskList = new ArrayList<>();
        boolean isSuccess = Boolean.TRUE;
        try {
            String referenceKeysHash = getTaskUniqueHash(taskRequest);
            List<ActivityDefinition> activityDefinitions = this.activityService.getActivityListForEventType(taskRequest.getEventType()).orElseThrow(() -> new SchedulerException(ValidationErrorCodes.ACTIVITY_NOT_FOUND.getErrorMessage()));
            for (ActivityDefinition activityDefinition : activityDefinitions) {
                SchedulerResponse schedulerResponse;
                List<Error> errorList = new ArrayList<>();
                logger.info("Creating Tasks Corresponding to Activity Request :{}", activityDefinition);
                Task task = SchedulerUtils.createTaskFromActivity(activityDefinition, taskRequest, errorList);
                task.setReferenceKeysHash(referenceKeysHash);
                if (!CommonUtils.isEmptyList(errorList)) {
                    isSuccess = Boolean.FALSE;
                    schedulerResponse = SchedulerUtils.buildSchedulerResponse(Status.FAILED, errorList, task.getId(), activityDefinition.getId());
                } else {
                    schedulerResponse = SchedulerUtils.buildSchedulerResponse(Status.CREATED, null, task.getId(), activityDefinition.getId());
                    taskList.add(task);
                }
                schedulerResponseList.add(schedulerResponse);
            }
            if (!CommonUtils.isEmptyList(taskList) && isSuccess) {
                this.deleteAndCreateNewTasks(taskList, taskRequest.getEventType(), referenceKeysHash);
            }
        } catch (Exception e) {
            isSuccess = Boolean.FALSE;
            schedulerResponseList.add(SchedulerUtils.buildSchedulerResponse(Status.FAILED, Arrays.asList(new Error(ValidationErrorCodes.TASK_CREATION_FAILED.toString(), e.getMessage())), null, null));
        }
        return isSuccess ? Response.successResponse(schedulerResponseList) : Response.failureResponseWithBody(schedulerResponseList);
    }

    @Override
    public SchedulerResponse validateTask(String identifier) {
        SchedulerResponse schedulerResponse = new SchedulerResponse();
        try {
            Task task = this.fetchTaskById(identifier);
            schedulerResponse.setIdentifier(task.getId());
            schedulerResponse.setStatus(task.getStatus());
            schedulerResponse.setComments(task.getComments());
        } catch (Exception e) {
            logger.error("Exception Occurred while Validating Task : {}", identifier);
            throw new SchedulerException(new Error(ValidationErrorCodes.TASK_VALIDATION_FAILED.toString(), e.getMessage()));
        }
        return schedulerResponse;
    }

    @Override
    public List<SchedulerResponse> cancelTask(CancelTaskRequest cancelTaskRequest) {
        List<SchedulerResponse> schedulerResponseList = new ArrayList<>();
        List<Task> taskList = new ArrayList<>();
        try {
            if (!StringUtils.isEmpty(cancelTaskRequest.getId())) {
                Task task = this.fetchTaskById(cancelTaskRequest.getId());
                taskList.add(task);
            } else {
                EventConfig eventConfig = this.eventService.getConfigsForEventType(cancelTaskRequest.getEventType());
                if (eventConfig != null && !StringUtils.isEmpty(eventConfig.getReferenceKeys())) {
                    taskList = this.customRepository.fetchExistingTasks(cancelTaskRequest.getEventType(), getReferenceKeysHash(cancelTaskRequest.getMeta(), eventConfig.getReferenceKeys()));
                }
            }
            for (Task task : taskList) {
                task.setStatus(Status.USER_CANCELLED);
                task.setComments(CommonConstants.USER_CANCELLED);
                task.setActive(Boolean.FALSE);
                SchedulerResponse schedulerResponse = new SchedulerResponse();
                schedulerResponse.setIdentifier(task.getId());
                schedulerResponse.setStatus(Status.USER_CANCELLED);
                schedulerResponse.setComments(CommonConstants.USER_CANCELLED);
                schedulerResponseList.add(schedulerResponse);
            }
            if (!taskList.isEmpty()) {
                this.taskRepository.saveAll(taskList);
            } else {
                throw new SchedulerException("No Task Found Corresponding to Given Data");
            }
        } catch (Exception e) {
            throw new SchedulerException(new Error(ValidationErrorCodes.TASK_VALIDATION_FAILED.toString(), e.getMessage()));
        }
        return schedulerResponseList;
    }

    @Override
    public void executeTask(Task task, JobData jobData) {
        logger.info("Executing Task : {}", task);
        try {
            this.initiateExecution(task, this.actionService.getActionFromName(task.getAction()));
        } catch (SchedulerException e) {
            this.handleRetryForFailure(task, jobData, e.getMessage());
            throw e;
        } catch (Exception e) {
            this.markTaskAsFailed(task, e.getMessage());
            throw e;
        }
    }

    @Override
    public void markTaskAsFailed(Task task, String comments) {
        logger.error("Retry Task Cannot be Created for : {}, Marking as Failed", task);
        SchedulerUtils.populateTaskPostProcessing(task, Status.FAILED, comments);
        this.taskRepository.save(task);
    }

    @Override
    public void handleRetryForFailure(Task failedTask, JobData jobData, String errorMessage) {
        logger.info("Building new Task For Retry :{}", failedTask);
        if (SchedulerUtils.checkForTask(failedTask) && (SchedulerUtils.checkForParentJob(jobData))) {
            this.saveFailedTask(failedTask, errorMessage);
            this.buildTaskForRetry(failedTask);
        } else {
            this.markTaskAsFailed(failedTask, errorMessage);
        }
    }

    @Override
    public void saveCompletedTask(Task task, TaskResponse taskResponse) {
        logger.info("Response Received For Task with Id: {}, {}", task.getId(), taskResponse);
        String comment = CommonConstants.IN_ACTION_COMMENT;
        Status status = Status.PENDING;
        if (taskResponse != null && taskResponse.getStatus() != null) {
            if (taskResponse.getStatus().isHigherType(task.getStatus())) {
                comment = taskResponse.getComments();
                status = taskResponse.getStatus();
            } else {
                throw new SchedulerException(ValidationErrorCodes.INVALID_STATUS_UPDATE.getErrorMessage());
            }
        } else {
            EventConfig eventConfig = this.eventService.getConfigsForEventType(task.getEventType());
            if (eventConfig != null && !Boolean.TRUE.equals(eventConfig.getCallbackRequired())) {
                comment = CommonConstants.NO_CALLBACK_REQUIRED;
                status = Status.COMPLETED;
            }
        }
        this.populateAndUpdateTask(task, status, comment);
    }

    @Override
    public SchedulerResponse saveCallback(TaskResponse taskResponse) {
        SchedulerResponse schedulerResponse = new SchedulerResponse();
        try {
            Task task = this.fetchTaskById(taskResponse.getTaskId());
            this.saveCompletedTask(task, taskResponse);
            schedulerResponse.setIdentifier(taskResponse.getTaskId());
            schedulerResponse.setComments(taskResponse.getComments());
            schedulerResponse.setStatus(taskResponse.getStatus());
        } catch (Exception e) {
            logger.error("Exception Occurred While Saving Callback:{}", taskResponse);
            throw new SchedulerException(new Error(ValidationErrorCodes.INVALID_STATUS_UPDATE.toString(), e.getMessage()));
        }
        return schedulerResponse;
    }

    @Override
    public Task fetchTaskById(String taskId) {
        return this.taskRepository.findById(taskId).orElseThrow(() -> new SchedulerException(ResponseCodes.NO_RECORD_FOUND, "Failed while Fetching Task"));
    }

    @Override
    public void initiateExecution(Task task, Action action) {
        Optional.ofNullable(this.fetchTaskById(task.getId()))
                .filter(scheduledTask -> (scheduledTask.getActive() && Status.SUBMITTED.equals(scheduledTask.getStatus())))
                .ifPresent(scheduledTask -> {
                    TaskResponse taskResponse = ActionFactory.getAction(action.getType()).execute(task, action, SchedulerUtils.transformMeta(task, action));
                    this.saveCompletedTask(task, taskResponse);
                });
    }

    private void deleteAndCreateNewTasks(List<Task> taskList, String eventType, String referenceKeysHash) {
        if(referenceKeysHash != null) {
            List<Task> existingTasks = this.customRepository.fetchExistingTasks(eventType, referenceKeysHash);
            if (!CommonUtils.isEmptyList(existingTasks)) {
                logger.info("Deleting : {} Old Tasks", existingTasks.size());
                this.taskRepository.deleteAll(existingTasks);
            }
        }

        logger.info("Creating : {} new Tasks", taskList.size());
        this.taskRepository.insert(taskList);
    }

    private String getTaskUniqueHash(TaskRequest taskRequest) {
        EventConfig eventConfig = this.eventService.getConfigsForEventType(taskRequest.getEventType());

        return (eventConfig != null && !StringUtils.isEmpty(eventConfig.getReferenceKeys())) ?
                getReferenceKeysHash(taskRequest.getMeta(), eventConfig.getReferenceKeys()) :
                null;
    }

    private void buildTaskForRetry(Task failedTask) {
        logger.info("Building Retry Task For : {}", failedTask);
        this.taskRepository.insert(SchedulerUtils.buildRetryTask(failedTask));
    }

    private void saveFailedTask(Task failedTask, String errorMessage) {
        SchedulerUtils.populateTaskPostProcessing(failedTask, Status.RETRY, errorMessage);
        this.taskRepository.save(failedTask);
    }

    private void populateAndUpdateTask(Task task, Status status, String comments) {
        SchedulerUtils.populateTaskPostProcessing(task, status, comments);
        this.taskRepository.save(task);
    }

    private String getReferenceKeysHash(Map<String, Object> meta, List<String> referenceKeys) {
        Map<String, Object> referenceKeysMap = new HashMap<>();
        for(String referenceKey: referenceKeys) {
            referenceKeysMap.put(referenceKey, meta.get(referenceKey));
        }
        return CommonUtils.getSHA256Hex(referenceKeysMap);
    }
}