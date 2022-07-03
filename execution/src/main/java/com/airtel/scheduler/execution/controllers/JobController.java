package com.airtel.scheduler.execution.controllers;

import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.dto.ScheduledJobRequest;
import com.airtel.scheduler.execution.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class JobController {

    private final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private SchedulerService schedulerService;

    @PostMapping(path = "/new/schedule")
    public ResponseEntity<Object> createNewJob(@RequestBody @Valid ScheduledJobRequest scheduledJobRequest) {
        logger.info("Request Received for Creating New Job :{}", scheduledJobRequest);
        return new ResponseEntity<>(Response.successResponse(this.schedulerService.createNewJob(scheduledJobRequest)), HttpStatus.OK);
    }

    @GetMapping(path = "/unSchedule")
    public ResponseEntity<Object> unScheduleJob(@RequestParam(value = "jobId", required = true) String jobId) {
        logger.info("Request Received to Unschedule Job Id: {} ", jobId);
        return new ResponseEntity<>(this.schedulerService.unScheduleJob(jobId), HttpStatus.OK);
    }
}