package com.airtel.scheduler.scheduling.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolRejectedExecutionHandlerImpl implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolRejectedExecutionHandlerImpl.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.error("{} - Handler rejected", r);
    }
}