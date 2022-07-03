package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.enums.Status;
import com.airtel.scheduler.execution.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchedulerResponse {

    private String identifier;
    private List<Error> error;
    private Status status;
    private String comments;
    private String schedulerKey;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}