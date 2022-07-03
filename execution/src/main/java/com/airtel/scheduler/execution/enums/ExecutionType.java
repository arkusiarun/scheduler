package com.airtel.scheduler.execution.enums;

public enum ExecutionType {

    ABSOLUTE("ABSOLUTE"),
    RELATIVE("RELATIVE");

    private String description;

    public String getDescription() {
        return description;
    }

    ExecutionType(String description) {
        this.description = description;
    }
}