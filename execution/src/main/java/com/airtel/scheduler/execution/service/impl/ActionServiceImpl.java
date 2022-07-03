package com.airtel.scheduler.execution.service.impl;

import com.airtel.scheduler.execution.dto.Error;
import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.enums.ValidationErrorCodes;
import com.airtel.scheduler.execution.exception.GenericException;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.repository.ActionRepository;
import com.airtel.scheduler.execution.service.ActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionServiceImpl implements ActionService {

    private final Logger logger = LoggerFactory.getLogger(ActionServiceImpl.class);

    @Autowired
    private ActionRepository actionRepository;

    @Override
    public Action getActionFromName(String actionName) {
        return this.actionRepository.findById(actionName).orElseThrow(() -> new GenericException(ValidationErrorCodes.ACTION_NOT_FOUND.getErrorMessage()));
    }

    @Override
    public Response createAction(Action action) {
        try {
            this.actionRepository.save(action);
        } catch (Exception e) {
            logger.error("Failure Occurred while Creating New Action : {}", action);
            throw new SchedulerException(new Error(ValidationErrorCodes.CREATION_FAILED.toString(), e.getMessage()));
        }
        return Response.successResponse();
    }
}