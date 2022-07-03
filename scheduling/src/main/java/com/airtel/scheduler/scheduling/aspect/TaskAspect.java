package com.airtel.scheduler.scheduling.aspect;

import com.airtel.scheduler.scheduling.annotations.ScheduleTask;
import com.airtel.scheduler.scheduling.components.TaskPublisher;
import com.airtel.scheduler.scheduling.dto.SchedulerTask;
import com.airtel.scheduler.scheduling.dto.TaskRequest;
import com.airtel.scheduler.scheduling.enums.ExceptionEnum;
import com.airtel.scheduler.scheduling.exception.SchedulingException;
import com.airtel.scheduler.scheduling.utilities.ThreadLocalContext;
import com.airtel.scheduler.scheduling.utilities.Utils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
@EnableAspectJAutoProxy
public class TaskAspect {

    @Autowired
    @Qualifier("schedulerTaskPublisher")
    private TaskPublisher taskPublisher;

    private Map<String, String> kafkaHeaders = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(TaskAspect.class);

    @Around("@annotation(com.airtel.scheduler.scheduling.annotations.ScheduleTask)")
    public Object createTask(ProceedingJoinPoint point) {
        Object retVal = null;
        logger.info("Scheduler Task Creation Starts : {}", System.currentTimeMillis());
        try {
            CodeSignature codeSignature = (CodeSignature) point.getSignature();
            ScheduleTask scheduleTaskAnnotataion = this.getAnnotation(codeSignature, point);
            retVal = Optional.ofNullable(point.proceed()).orElse(null);
            kafkaHeaders = new HashMap<>();
            TaskRequest taskRequest = this.createTaskRequest(point, scheduleTaskAnnotataion, retVal);
            this.taskPublisher.publishTask(taskRequest, kafkaHeaders);
        } catch (SchedulingException e) {
            throw e;
        } catch (Throwable th) {
            throw new SchedulingException(ExceptionEnum.GENERIC_INVOCATION_EXCEPTION, th.getMessage());
        } finally {
            ThreadLocalContext.clear();
        }
        return retVal;
    }

    private ScheduleTask getAnnotation(CodeSignature codeSignature, ProceedingJoinPoint point) throws NoSuchMethodException {
        Class<?> clazz = point.getTarget().getClass();
        String methodName = point.getSignature().getName();
        return clazz.getMethod(methodName, codeSignature.getParameterTypes()).getAnnotation(ScheduleTask.class);
    }

    private TaskRequest createTaskRequest(ProceedingJoinPoint joinPoint, ScheduleTask scheduleTask, Object referenceTime) {
        TaskRequest taskRequest = Utils.getBasicTaskRequest(scheduleTask.eventType(), scheduleTask.jobName(), (Long) referenceTime);
        Object[] arguments = joinPoint.getArgs();
        for (Object arg : arguments) {
            if (arg instanceof SchedulerTask) {
                Utils.createTaskMeta(arg, taskRequest);
            }
            if(arg instanceof Map) {
                kafkaHeaders = (Map<String, String>) arg;
            }
        }
        ThreadLocalContext.put("taskScheduling", taskRequest);
        return taskRequest;
    }
}