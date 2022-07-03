package com.airtel.scheduler.execution.model;

import com.airtel.scheduler.execution.dto.ActionDefinition;
import com.airtel.scheduler.execution.enums.ActionType;
import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "actions")
public class Action {

    @Id
    private String id;
    private String description;
    private ActionType type;
    private ActionDefinition actionDefinition;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}