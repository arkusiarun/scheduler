package com.airtel.scheduler.scheduling.annotations;


import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

@InterceptorBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ScheduleTask {

    String eventType() default "";

    String jobName() default "";

}