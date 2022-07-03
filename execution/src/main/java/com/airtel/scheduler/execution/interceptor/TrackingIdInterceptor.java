package com.airtel.scheduler.execution.interceptor;

import com.airtel.scheduler.execution.constants.CommonConstants;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Arun Singh
 */
public class TrackingIdInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        MDC.put(CommonConstants.TRACKING_ID,
                Optional.ofNullable(request.getHeader(CommonConstants.TRACKING_ID)).orElse(UUID.randomUUID().toString()));
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }
}