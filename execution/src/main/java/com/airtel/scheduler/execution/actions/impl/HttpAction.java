package com.airtel.scheduler.execution.actions.impl;

import com.airtel.scheduler.execution.actions.Actions;
import com.airtel.scheduler.execution.constants.CommonConstants;
import com.airtel.scheduler.execution.dto.HttpDefinition;
import com.airtel.scheduler.execution.dto.Response;
import com.airtel.scheduler.execution.dto.TaskResponse;
import com.airtel.scheduler.execution.enums.ActionType;
import com.airtel.scheduler.execution.exception.GenericException;
import com.airtel.scheduler.execution.exception.SchedulerException;
import com.airtel.scheduler.execution.model.Action;
import com.airtel.scheduler.execution.model.Task;
import com.airtel.scheduler.execution.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Component
public class HttpAction implements Actions {

    private final Logger logger = LoggerFactory.getLogger(HttpAction.class);

    private RestTemplate restTemplate;

    private Environment environment;

    @Autowired
    public HttpAction(@Qualifier("schedulerRestTemplate") RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    @Override
    public ActionType getType() {
        return ActionType.HTTP;
    }

    @Override
    public TaskResponse execute(Task task, Action action, Map<String, Object> transformationResult) {
        logger.info("Executing HTTP Action for task: {}", task);
        Response<TaskResponse> response = null;
        try {
            String uri = this.buildURI(action.getActionDefinition().getHttpDefinition(), transformationResult, task.getId());
            HttpEntity<Map<String, Object>> entity = this.buildHttpBody(action, transformationResult);
            ResponseEntity<Response<TaskResponse>> responseBody = this.restTemplate.exchange(uri, action.getActionDefinition().getHttpDefinition().getMethod(),
                    entity, new ParameterizedTypeReference<Response<TaskResponse>>() {
                    });
            if (responseBody.getBody() != null) {
                response = responseBody.getBody();
            }
        } catch (RestClientException e) {
            logger.error("Status Code Exception Occurred While Executing Http Action:{}", task, e);
            this.handleExceptionBasedOnHttpCode(e);
        } catch (Exception e) {
            throw new GenericException(e.getMessage());
        }
        return (response == null) ? null : response.getBody();
    }

    private void buildHeaders(Action action, HttpHeaders httpHeaders) {
        if (!CommonUtils.isNullOrEmptyMap(action.getActionDefinition().getHttpDefinition().getHeaders())) {
            httpHeaders.setAll(action.getActionDefinition().getHttpDefinition().getHeaders());
        }
    }

    public HttpEntity<Map<String, Object>> buildHttpBody(Action action, Map<String, Object> body) {
        HttpHeaders httpHeaders = new HttpHeaders();
        this.buildHeaders(action, httpHeaders);
        return Optional.ofNullable(body).map(request -> new HttpEntity<>(request, httpHeaders))
                .orElseGet(() -> new HttpEntity<>(httpHeaders));
    }

    private String buildURI(HttpDefinition httpDefinition, Map<String, Object> meta, String taskId) {
        String endPoint = httpDefinition.getEndpoint();
        if (Arrays.asList(this.environment.getActiveProfiles()).contains(CommonConstants.DR_PROFILE) && !StringUtils.isEmpty(httpDefinition.getSubOrdinateEndpoint())) {
            endPoint = httpDefinition.getSubOrdinateEndpoint();
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endPoint.trim());
        Map<String, Object> pathParams = new HashMap<>();
        if (!CommonUtils.isEmptyList(httpDefinition.getPathParams())) {
            for (String param : httpDefinition.getPathParams()) {
                if (!CommonUtils.isNullOrEmptyMap(meta) && meta.containsKey(param)) {
                    pathParams.put(param, meta.get(param));
                    meta.remove(param);
                }
            }
        }
        builder.buildAndExpand(pathParams);
        if (!CommonUtils.isNullOrEmptyMap(httpDefinition.getRequestParams())) {
            Iterator<String> keys = httpDefinition.getRequestParams().keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                builder.queryParam(key, httpDefinition.getRequestParams().get(key));
            }
        }
        builder.queryParam(CommonConstants.TASK_ID, taskId);
        return builder.toUriString();
    }

    private void handleExceptionBasedOnHttpCode(RestClientException e) {
        Boolean retryable = Boolean.FALSE;
        if (e instanceof HttpStatusCodeException) {
            int httpStatusCode = ((HttpStatusCodeException) e).getRawStatusCode();
            if (!(HttpStatus.valueOf(httpStatusCode).is4xxClientError() && HttpStatus.valueOf(httpStatusCode) != HttpStatus.TOO_MANY_REQUESTS)) {
                retryable = Boolean.TRUE;
            }
        } else if (e instanceof ResourceAccessException) {
            retryable = Boolean.TRUE;
        }
        if (retryable) {
            throw new SchedulerException(e.getMessage());
        } else {
            throw new GenericException(e.getMessage());
        }
    }
}