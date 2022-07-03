package com.airtel.scheduler.execution.controllers;

import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.model.ActivityDefinition;
import com.airtel.scheduler.execution.model.EventConfig;
import com.airtel.scheduler.execution.service.ActionService;
import com.airtel.scheduler.execution.service.ActivityService;
import com.airtel.scheduler.execution.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class InternalController {

    private final Logger logger = LoggerFactory.getLogger(InternalController.class);

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActionService actionService;

    @Autowired
    private EventService eventService;

    @PostMapping(path = "/new/activity")
    public ResponseEntity<Object> createNewJob(@Valid @RequestBody ActivityDefinition activityDefinition) {
        logger.info("Request Received for Creating New Activity :{}", activityDefinition);
        return new ResponseEntity<>(this.activityService.createActivity(activityDefinition), HttpStatus.OK);
    }

    @PostMapping(path = "/new/action")
    public ResponseEntity<Object> createNewJob(@Valid @RequestBody Action action) {
        logger.info("Request Received for Creating New Action :{}", action);
        return new ResponseEntity<>(this.actionService.createAction(action), HttpStatus.OK);
    }

    @GetMapping(path = "/reload/activity")
    public ResponseEntity<Object> reloadActivity() {
        logger.info("Reloading All Activities");
        return new ResponseEntity<>(this.activityService.reload(), HttpStatus.OK);
    }

    @PostMapping(path = "/new/event")
    public ResponseEntity<Object> createNewEvent(@Valid @RequestBody EventConfig eventConfig) {
        logger.info("Request Received for Creating New Event :{}", eventConfig);
        return new ResponseEntity<>(this.eventService.createNewEvent(eventConfig), HttpStatus.OK);
    }

    @GetMapping(path = "/reload/event")
    public ResponseEntity<Object> reloadEventConfig() {
        logger.info("Reloading All Events");
        return new ResponseEntity<>(this.eventService.reload(), HttpStatus.OK);
    }
}