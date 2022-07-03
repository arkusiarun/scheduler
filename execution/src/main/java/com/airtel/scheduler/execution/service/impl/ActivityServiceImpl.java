package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.enums.ValidationErrorCodes;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.ActivityDefinition;
import com.airtel.scheduler.execution.repository.ActivityDefinitionRepository;
import com.airtel.scheduler.execution.service.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActivityServiceImpl implements ActivityService {

    private final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

    private Map<String, List<ActivityDefinition>> eventActivityMap = new ConcurrentHashMap<>();

    @Autowired
    private ActivityDefinitionRepository activityDefinitionRepository;

    @PostConstruct
    private void loadActivityDefinition() {
        Map<String, List<ActivityDefinition>> eventActivityMapTemp = new HashMap<>();
        Optional.of(this.activityDefinitionRepository.findAll()).ifPresent(activityList -> activityList.forEach(activity -> {
            logger.info("Loading Activity :{}", activity.getId());
            if (Boolean.TRUE.equals(activity.getActive())) {
                String key = activity.getEvent();
                List<ActivityDefinition> activityDefinitions;
                if (!eventActivityMapTemp.containsKey(key)) {
                    activityDefinitions = new ArrayList<>();
                } else {
                    activityDefinitions = eventActivityMapTemp.get(key);
                    activityDefinitions.removeIf(activityDefinition -> activityDefinition.getId().equals(activity.getId()));
                }
                activityDefinitions.add(activity);
                eventActivityMapTemp.put(key, activityDefinitions);
            }
        }));
        eventActivityMap = eventActivityMapTemp;
    }

    @Override
    public Response reload() {
        try {
            loadActivityDefinition();
            logger.debug("Activity reloaded Successfully.");
        } catch (Exception e) {
            logger.error("Exception Occurred while Reloading Activity.");
            throw new SchedulerException(new Error(ValidationErrorCodes.RELOAD_ERROR.toString(), e.getMessage()));
        }
        return Response.successResponse();
    }

    @Override
    public Optional<List<ActivityDefinition>> getActivityListForEventType(String eventType) {
        return Optional.ofNullable(eventActivityMap.get(eventType))
                .map(ArrayList::new);
    }

    @Override
    public Response createActivity(ActivityDefinition activityDefinition) {
        try {
            this.activityDefinitionRepository.save(activityDefinition);
        } catch (Exception e) {
            logger.error("Failure Occurred while Creating New Activity. : {}", activityDefinition);
            throw new SchedulerException(new Error(ValidationErrorCodes.CREATION_FAILED.toString(), e.getMessage()));
        }
        return Response.successResponse();
    }
}