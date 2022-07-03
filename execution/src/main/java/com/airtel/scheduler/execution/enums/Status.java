package com.airtel.scheduler.execution.enums;

/**
 * @author Arun Singh
 */
public enum Status {
    CREATED(1), SUBMITTED(1), PENDING(2), COMPLETED(3), FAILED(3), RETRY(3), USER_CANCELLED(1);

    private int priority;

    Status(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isHigherType(Status status) {
        return this.getPriority() > status.getPriority();
    }
}