package com.airtel.scheduler.execution.enums;

public enum JobExecutionType {

    CURRENT("CURRENT"),
    BACKLOG("BACKLOG");

    private String description;

    public String getDescription() {
        return description;
    }

    JobExecutionType(String description) {
        this.description = description;
    }
}