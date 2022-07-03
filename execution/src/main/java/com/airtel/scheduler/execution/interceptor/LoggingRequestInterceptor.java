package com.airtel.scheduler.execution.interceptor;

import com.airtel.scheduler.execution.constants.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Arun Singh
 */
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        request.getHeaders().add(CommonConstants.TRACKING_ID,
                Optional.ofNullable(MDC.get(CommonConstants.TRACKING_ID)).orElse(UUID.randomUUID().toString()));
        long startTime = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        log(request, response, body, System.currentTimeMillis() - startTime);
        return response;
    }

    private void log(HttpRequest request, ClientHttpResponse response, byte[] requestBody, long requestTime)
            throws IOException {
        String requestBodyString = new String(requestBody, StandardCharsets.UTF_8);
        String responseBodyString = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
        logger.info("{} {} {} {} {}", request.getURI(), response.getStatusCode(), requestTime, responseBodyString, requestBodyString);
    }
}
