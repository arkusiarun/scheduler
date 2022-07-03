package com.airtel.scheduler.execution.enums;

public enum ValidationErrorCodes {

    CRON_ERROR("Invalid Cron Expression", "Cron Expression Should be Valid"),
    DUPLICATION_ERROR("Invalid Job Request", "Job Already Exists"),
    SCHEDULED_TIME_ERROR("Invalid Value for Scheduled Time", "Please Check Validity For Scheduled Time"),
    GENERIC_ERROR("Internal Error","Some Internal Error Occurred"),
    FAILED_TO_UNSCHEDULED("Failed To Unscheduled", "Please Check if Job Exists"),
    TASK_CREATION_FAILED("Task Creation Failed","Failure Occurred While Creating Task. Please Check Corresponding Activity"),
    TASK_VALIDATION_FAILED("Task Validation Failed", "Please Check if task with Given Task Exists"),
    ACTIVITY_NOT_FOUND("Activity Not Found","Please Check if Activity Configuration"),
    TIME_VALIDATION_FAILED("Time Validation Failed", "Please Check Cron and Scheduled Time Parameter"),
    INVALID_STATUS_UPDATE("Invalid Status Update", "Invalid Status Update"),
    SCHEDULING_INFO_VALIDATION_FAILED("Scheduling Info Validation Error", "For Only Custom Job Scheduling Info Not Required"),
    CREATION_FAILED("Creation Failed","Please Check if all Parameters are Correct"),
    ACTION_NOT_FOUND("No Action Found","Action Cannot Be Found With Given Name"),
    INVALID_ACTION_TYPE("Invalid Action Type","Invalid Action Type"),
    JOB_NOT_FOUND("Job Not Found", "Job Not Found"),
    RELOAD_ERROR("Reloading Error","Error While Reloading Entity");

    private String errorCode;

    private String errorMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    ValidationErrorCodes(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}