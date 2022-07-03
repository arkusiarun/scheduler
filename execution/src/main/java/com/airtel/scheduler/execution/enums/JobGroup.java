package com.airtel.scheduler.execution.enums;

public enum JobGroup {

    GENERIC("GENERIC"), CUSTOM("CUSTOM"), BATCH("BATCH"), INTERNAL("INTERNAL");

    private String description;

    public String getDescription() {
        return description;
    }

    JobGroup(String description) {
        this.description = description;
    }
}