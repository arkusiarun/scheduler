package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;

@Data
public class ActionDefinition {

    private HttpDefinition httpDefinition;
    private WorkerDefinition workerDefinition;
    private InternalActionDefinition internalActionDefinition;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}