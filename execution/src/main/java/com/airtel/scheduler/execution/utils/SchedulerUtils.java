package com.airtel.scheduler.execution.utils;

import com.airtel.scheduler.execution.constants.CommonConstants;
import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.dto.*;
import com.airtel.scheduler.execution.enums.*;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SchedulerUtils {

    private SchedulerUtils() {
    }

    public static ArchivedTask convertTaskToArchived(Task task) {
        ArchivedTask archivedTask = new ArchivedTask();
        archivedTask.setId(task.getId());
        archivedTask.setAction(task.getAction());
        archivedTask.setScheduledTime(task.getScheduledTime());
        archivedTask.setArchivalTime(task.getArchivalTime());
        archivedTask.setTaskCreationDate(task.getCreationDate());
        archivedTask.setComments(task.getComments());
        archivedTask.setEventType(task.getEventType());
        archivedTask.setJobGroup(task.getJobGroup());
        archivedTask.setJobName(task.getJobName());
        archivedTask.setJobGroup(task.getJobGroup());
        archivedTask.setStatus(task.getStatus());
        archivedTask.setRetryDef(task.getRetryDef());
        archivedTask.setParentTaskId(task.getParentTaskId());
        archivedTask.setMeta(task.getMeta());
        return archivedTask;
    }

    public static Task buildRetryTask(Task failedTask) {
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setActive(Boolean.TRUE);
        RetryDef retryDef = new RetryDef();
        Integer retriesLeft = failedTask.getRetryDef().getRetriesLeft() - 1;
        retryDef.setRetryEnabled((retriesLeft == 0) ? Boolean.FALSE : Boolean.TRUE);
        retryDef.setRetriesLeft(retriesLeft);
        retryDef.setRetryInterval(failedTask.getRetryDef().getRetryInterval());
        task.setRetryDef(retryDef);
        task.setStatus(Status.SUBMITTED);
        task.setJobType(failedTask.getJobType());
        task.setJobName(failedTask.getJobName());
        task.setJobGroup(failedTask.getJobGroup());
        task.setArchivalTime(failedTask.getArchivalTime().plusSeconds(failedTask.getRetryDef().getRetryInterval()));
        task.setAction(failedTask.getAction());
        task.setMeta(failedTask.getMeta());
        task.setScheduledTime(LocalDateTime.now().plusSeconds(failedTask.getRetryDef().getRetryInterval()));
        task.setParentTaskId((failedTask.getParentTaskId() == null) ? failedTask.getId() : failedTask.getParentTaskId());
        return task;
    }

    public static ScheduledJob buildScheduledJobModel(ScheduledJobRequest scheduledJobRequest) {
        ScheduledJob scheduledJob = new ScheduledJob();
        scheduledJob.setId(UUID.randomUUID().toString());
        scheduledJob.setJobGroup(scheduledJobRequest.getJobGroup());
        scheduledJob.setJobName(scheduledJobRequest.getJobName());
        if (StringUtils.isEmpty(scheduledJobRequest.getCronExpression())) {
            scheduledJob.setDateTime(CommonUtils.convertEpochToLocalDateTime(scheduledJobRequest.getScheduledTime()));
        } else {
            scheduledJob.setCronExpression(scheduledJobRequest.getCronExpression());
        }
        scheduledJob.setJobType(scheduledJobRequest.getJobType());
        scheduledJob.setActive(Boolean.TRUE);
        scheduledJob.setSchedulingInfo(scheduledJobRequest.getSchedulingInfo());
        if (JobGroup.CUSTOM.equals(scheduledJobRequest.getJobGroup())) {
            scheduledJob.setEventType(scheduledJobRequest.getEventType());
        }
        return scheduledJob;
    }

    public static Task createTaskFromActivity(ActivityDefinition activityDefinition, TaskRequest taskRequest, List<Error> errorList) {
        Task task = new Task();
        String id = UUID.randomUUID().toString();
        try {
            task.setId(id);
            task.setEventType(taskRequest.getEventType());
            task.setAction(activityDefinition.getAction());
            task.setComments(CommonConstants.TASK_SUBMITTED);
            task.setJobGroup(activityDefinition.getJobDef().getJobGroup());
            task.setStatus(Status.SUBMITTED);
            task.setActive(Boolean.TRUE);
            task.setRetryDef(activityDefinition.getRetryDef());
            task.setJobName(StringUtils.isEmpty(taskRequest.getJobName()) ? activityDefinition.getJobDef().getJobName() : taskRequest.getJobName());
            task.setJobType(JobType.ONDEMAND);
            task.setMeta(taskRequest.getMeta());
            if (activityDefinition.getExecutionData() != null) {
                task.setArchivalTime(CommonUtils.convertEpochToLocalDateTime(taskRequest.getReferenceTime()).plusSeconds(activityDefinition.getExecutionData().getExpiryInSeconds()));
                if (ExecutionType.ABSOLUTE.equals(activityDefinition.getExecutionData().getExecutionType())) {
                    task.setScheduledTime(CommonUtils.convertEpochToLocalDateTime(taskRequest.getReferenceTime()));
                } else {
                    task.setScheduledTime(CommonUtils.convertEpochToLocalDateTime(taskRequest.getReferenceTime()).plusSeconds(activityDefinition.getExecutionData().getExecutionAfterInSeconds()));
                }
            }
        } catch (Exception e) {
            errorList.add(new Error(ValidationErrorCodes.TASK_CREATION_FAILED.toString(), e.getMessage()));
        }
        return task;
    }

    public static Map<String, Object> transformMeta(Task task, Action action) {
        Map<String, Object> result = new HashMap<>();
        ActionDefinition actionDefinition = action.getActionDefinition();
        switch (action.getType()) {
            case HTTP:
                if (!CommonUtils.isNullOrEmptyMap(actionDefinition.getHttpDefinition().getBody())) {
                    result.putAll(action.getActionDefinition().getHttpDefinition().getBody());
                }
                break;
            case WORKER:
                if (!CommonUtils.isNullOrEmptyMap(actionDefinition.getWorkerDefinition().getBody())) {
                    result.putAll(action.getActionDefinition().getWorkerDefinition().getBody());
                }
                break;
            case INTERNAL:
                if(!CommonUtils.isNullOrEmptyMap(actionDefinition.getInternalActionDefinition().getBody())) {
                    result.putAll(action.getActionDefinition().getInternalActionDefinition().getBody());
                }
                break;
            default:
                throw new SchedulerException(new Error(ValidationErrorCodes.INVALID_ACTION_TYPE.toString(), ValidationErrorCodes.INVALID_ACTION_TYPE.getErrorMessage()));
        }
        if (!CommonUtils.isNullOrEmptyMap(task.getMeta())) {
            result.putAll(task.getMeta());
        }
        return result;
    }

    public static SchedulerResponse buildSchedulerResponse(Status status, List<Error> errorList, String identifier, String activityName) {
        SchedulerResponse schedulerResponse = new SchedulerResponse();
        if (!CommonUtils.isEmptyList(errorList)) {
            schedulerResponse.setError(errorList);
        }
        schedulerResponse.setIdentifier(identifier);
        schedulerResponse.setStatus(status);
        schedulerResponse.setSchedulerKey(activityName);
        return schedulerResponse;
    }

    public static void populateTaskPostProcessing(Task task, Status status, String comments) {
        task.setStatus(status);
        task.setComments(comments);
        task.setActive(Boolean.FALSE);
    }

    public static Boolean checkForTask(Task failedTask) {
        if (failedTask.getRetryDef() != null && failedTask.getRetryDef().getRetryEnabled() && failedTask.getRetryDef().getRetriesLeft() > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static Boolean checkForParentJob(JobData jobData) {
        if (jobData != null && (JobGroup.CUSTOM.equals(jobData.getJobGroup()) || JobType.SCHEDULED.equals(jobData.getJobType()) || JobType.ONDEMAND.equals(jobData.getJobType()))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
