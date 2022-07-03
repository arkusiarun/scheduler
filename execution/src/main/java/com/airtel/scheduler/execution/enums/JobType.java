package com.airtel.scheduler.execution.enums;

public enum JobType {

    SCHEDULED("SCHEDULED"), ONDEMAND("ONDEMAND"), REOCCURING("REOCCURING");

    private String description;

    public String getDescription() {
        return description;
    }

    JobType(String description) {
        this.description = description;
    }
}