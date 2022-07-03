package com.airtel.scheduler.execution.actions;

import com.airtel.scheduler.execution.dto.TaskResponse;
import com.airtel.scheduler.execution.enums.ActionType;
import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.model.Task;

import java.util.Map;

public interface Actions {

    ActionType getType();

    TaskResponse execute(Task task, Action action, Map<String, Object> map);
}