package com.airtel.scheduler.execution.controllers;

import com.airtel.scheduler.execution.dto.CancelTaskRequest;
import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.dto.TaskRequest;
import com.airtel.scheduler.execution.dto.TaskResponse;
import com.airtel.scheduler.execution.service.TaskExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class TaskController {

    private final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskExecutionService taskExecutionService;

    @PostMapping(path = "/push/task")
    public ResponseEntity<Object> createTask(@RequestBody @Valid TaskRequest taskRequest) {
        logger.info("Request Received for Creating New Task :{}", taskRequest);
        return this.taskExecutionService.createTaskForScheduler(taskRequest);
    }

    @GetMapping(path = "/validate/task")
    public ResponseEntity<Object> validateTask(@RequestParam @Valid String identifier) {
        logger.info("Validate TaskRequest Request Received :{}", identifier);
        return new ResponseEntity<>(
                Response.successResponse(this.taskExecutionService.validateTask(identifier)), HttpStatus.OK);
    }

    @PostMapping(path = "/task/callback")
    public ResponseEntity<Object> createTask(@RequestBody @Valid TaskResponse taskResponse) {
        logger.info("Callback Received For Task with Result :{}", taskResponse);
        return new ResponseEntity<>(
                Response.successResponse(this.taskExecutionService.saveCallback(taskResponse)), HttpStatus.OK);
    }

    @PostMapping(path = "/cancel/task")
    public ResponseEntity<Object> cancelTask(@RequestBody @Valid CancelTaskRequest cancelTaskRequest) {
        logger.info("Cancelling TaskRequest Request Received :{}", cancelTaskRequest);
        return new ResponseEntity<>(
                Response.successResponse(this.taskExecutionService.cancelTask(cancelTaskRequest)), HttpStatus.OK);
    }
}