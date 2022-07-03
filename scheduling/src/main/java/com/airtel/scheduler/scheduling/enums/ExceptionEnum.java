package com.airtel.scheduler.scheduling.enums;

public enum ExceptionEnum {

    KAFKA_ERROR("KAFKA_ERROR", "Error While Pushing Message to Kafka"),
    TASK_CREATION_ERROR("TASK_CREATION_ERROR", "Failed To create Task Request"),
    GENERIC_INVOCATION_EXCEPTION("GENERIC_INVOCATION_EXCEPTION", "Some General Error Occurred");

    private String errorCode;

    private String errorMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    ExceptionEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}