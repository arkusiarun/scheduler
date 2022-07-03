package com.airtel.scheduler.execution.enums;

public enum InternalJobsEnum {

    ARCHIVAL("Archival");

    private String description;

    public String getDescription() {
        return description;
    }

    InternalJobsEnum(String description) {
        this.description = description;
    }
}