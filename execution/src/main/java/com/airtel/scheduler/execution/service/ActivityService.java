package com.airtel.scheduler.execution.service;

import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.model.ActivityDefinition;

import java.util.List;
import java.util.Optional;

public interface ActivityService {

    Response reload();

    Optional<List<ActivityDefinition>> getActivityListForEventType(String eventType);

    Response createActivity(ActivityDefinition activityDefinition);
}