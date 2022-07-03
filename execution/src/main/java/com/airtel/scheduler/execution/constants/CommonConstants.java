package com.airtel.scheduler.execution.constants;

/**
 * @author Arun Singh
 * Constants For Scheduler Service
 */

public class CommonConstants {
    private CommonConstants() { }
    public static final String TRACKING_ID = "uniqueTracking";
    public static final String MONGO_URI_PREFIX = "mongodb://";
    public static final String BASE_PACKAGE = "com.airtel.scheduler";
    public static final String TASK_SUBMITTED = "Task Submitted";
    public static final String HTTP_PREFIX = "http://";
    public static final String APPLICATION_NAME = "scheduler";
    public static final String DEFAULT_JOB = "POLLER";
    public static final String IN_ACTION_COMMENT = "Waiting For CallBack From External System";
    public static final String TASK_ID = "taskId";
    public static final String USER_CANCELLED = "Task Cancelled By User";
    public static final String NO_CALLBACK_REQUIRED = "CallBack Not Required Marking as Completed";
    public static final String DR_PROFILE =  "drprod";
    public static final String CREATED_MESSAGE = "Entity Creation SuccessFull";
    public static final String KAFKA_HEADER_TRACKING_KEY = "trackingId";
    public static final String INTERNAL_ACTION_SUCCESS_MESSAGE = "Task Completed";
    public static final String INTERNAL_ACTION_FAILURE_MESSAGE = "Task Failed with Errors";
}