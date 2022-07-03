package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class InternalActionDefinition {

    @NotNull
    private String beanName;
    @NotNull
    private Map<String, Object> body;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}