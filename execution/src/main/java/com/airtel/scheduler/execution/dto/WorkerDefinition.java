package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;

import java.util.Map;

@Data
public class WorkerDefinition {

    private String topicName;
    private Map<String, String> headers;
    private Map<String, Object> body;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}