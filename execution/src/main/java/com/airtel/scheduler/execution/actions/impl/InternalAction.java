package com.airtel.scheduler.execution.actions.impl;

import com.airtel.scheduler.execution.actions.Actions;
import com.airtel.scheduler.execution.constants.CommonConstants;
import com.airtel.scheduler.execution.dto.TaskResponse;
import com.airtel.scheduler.execution.enums.ActionType;
import com.airtel.scheduler.execution.enums.InternalJobsEnum;
import com.airtel.scheduler.execution.enums.Status;
import com.airtel.scheduler.execution.exception.GenericException;
import com.airtel.scheduler.execution.internal.InternalJobFactory;
import com.airtel.scheduler.execution.internal.InternalJobService;
import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InternalAction implements Actions {

    private final Logger logger = LoggerFactory.getLogger(InternalAction.class);

    @Override
    public ActionType getType() {
        return ActionType.INTERNAL;
    }

    @Override
    public TaskResponse execute(Task task, Action action, Map<String, Object> meta) {
        logger.info("Executing Internal Action For For Meta {}", meta);
        TaskResponse taskResponse = new TaskResponse();
        try {
            taskResponse.setTaskId(task.getId());
            String beanName = action.getActionDefinition().getInternalActionDefinition().getBeanName();
            InternalJobService internalJobService = InternalJobFactory.getInernalJob(InternalJobsEnum.valueOf(beanName));
            internalJobService.execute(meta);
            taskResponse.setStatus(Status.COMPLETED);
            taskResponse.setComments(CommonConstants.INTERNAL_ACTION_SUCCESS_MESSAGE);
        } catch (Exception e) {
            logger.error("Exception Occured While Execution Internal Task :{}", task);
            throw new GenericException(e.getMessage());
        }
        return taskResponse;
    }
}