package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.enums.Status;
import com.airtel.scheduler.execution.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {

    private String taskId;
    private Status status;
    private String comments;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}