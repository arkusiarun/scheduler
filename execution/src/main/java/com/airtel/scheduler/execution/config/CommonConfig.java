package com.airtel.scheduler.execution.config;

import com.airtel.scheduler.execution.interceptor.LoggingRequestInterceptor;
import com.airtel.scheduler.execution.properties.HttpConfiguration;
import com.airtel.scheduler.execution.properties.ThreadPoolConfiguration;
import com.airtel.scheduler.scheduling.configurations.CustomMapperModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class CommonConfig {

    @Bean(name = "taskExecutionThreadPoolExecutor")
    public Executor taskExecution(ThreadPoolConfiguration threadPoolConfiguration) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolConfiguration.getExecutionCoreThreadCount());
        executor.setMaxPoolSize(threadPoolConfiguration.getExecutionMaxThreadCount());
        executor.setQueueCapacity(threadPoolConfiguration.getExecutionQueueCapacity());
        executor.setKeepAliveSeconds(threadPoolConfiguration.getExecutionKeepAliveTimeInSec());
        executor.setThreadFactory(this.createNamedThreadFactory("schedulerTaskExecutor-"));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean(name = "taskCreationThreadPoolExecutor")
    public Executor taskCreation(ThreadPoolConfiguration threadPoolConfiguration) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolConfiguration.getCreationCoreThreadCount());
        executor.setMaxPoolSize(threadPoolConfiguration.getCreationMaxThreadCount());
        executor.setQueueCapacity(threadPoolConfiguration.getCreationQueueCapacity());
        executor.setKeepAliveSeconds(threadPoolConfiguration.getCreationKeepAliveTimeInSec());
        executor.setThreadFactory(this.createNamedThreadFactory("schedulerTaskCreator-"));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Primary
    @Bean(name = "schedulerRestTemplate")
    public RestTemplate externalRestTemplate(HttpConfiguration httpConfiguration) {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(httpConfiguration.getMaxConnections());
        connManager.setDefaultMaxPerRoute(httpConfiguration.getMaxConnectionsPerHost());
        RequestConfig config = RequestConfig.custom().setConnectTimeout(httpConfiguration.getConnectionTimeout())
                .setSocketTimeout(httpConfiguration.getSocketTimeout()).setConnectionRequestTimeout(httpConfiguration.getConnectionRequestTimeout()).build();
        CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().setConnectionManager(connManager)
                .setDefaultRequestConfig(config).useSystemProperties().build();
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(client)));
        restTemplate.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));
        return restTemplate;
    }

    @Bean(name = "schedulerObjectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(new JavaTimeModule(), new JsonOrgModule(), new JodaModule(), new CustomMapperModule());
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        return mapper;
    }

    private ThreadFactory createNamedThreadFactory(String name) {
        return new ThreadFactoryBuilder().setNameFormat(name + "-%d").build();
    }
}