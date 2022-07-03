package com.airtel.scheduler.execution.service;

import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.model.Action;

public interface ActionService {

    Action getActionFromName(String actionName);

    Response createAction(Action action);
}