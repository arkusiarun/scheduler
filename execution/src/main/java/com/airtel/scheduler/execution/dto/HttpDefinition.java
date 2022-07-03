package com.airtel.scheduler.execution.dto;

import com.airtel.scheduler.execution.utils.CommonUtils;
import lombok.Data;
import org.springframework.http.HttpMethod;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class HttpDefinition {

    @NotNull(message = "http method can not be null")
    private HttpMethod method;
    @NotNull(message = "http end point can not be null")
    private String endpoint;
    private String subOrdinateEndpoint;
    private Map<String, String> requestParams;
    private List<String> pathParams;
    private Map<String, String> headers;
    private Map<String, Object> body;

    @Override
    public String toString() {
        return CommonUtils.getJson(this);
    }
}