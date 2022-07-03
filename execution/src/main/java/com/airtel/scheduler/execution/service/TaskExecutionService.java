package com.airtel.scheduler.execution.service;

import com.airtel.scheduler.execution.dto.*;
import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.model.Task;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TaskExecutionService {

    ResponseEntity<Object> createTaskForScheduler(TaskRequest taskRequest);

    Response<List<SchedulerResponse>> pushTasksForExecution(TaskRequest taskRequest);

    SchedulerResponse validateTask(String jobIdentifier);

    List<SchedulerResponse> cancelTask(CancelTaskRequest cancelTaskRequest);

    void executeTask(Task task, JobData jobData);

    void markTaskAsFailed(Task task, String comments);

    void handleRetryForFailure(Task failedTask, JobData jobData, String errorMessage);

    void saveCompletedTask(Task task, TaskResponse taskResponse);

    SchedulerResponse saveCallback(TaskResponse taskResponse);

    Task fetchTaskById(String taskId);

    void initiateExecution(Task task, Action action);
}