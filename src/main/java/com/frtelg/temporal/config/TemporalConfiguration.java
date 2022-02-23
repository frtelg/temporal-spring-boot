package com.frtelg.temporal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.JacksonJsonPayloadConverter;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class TemporalConfiguration {

    static final String NAMESPACE = "test";

    @Bean
    @ConditionalOnMissingBean(WorkflowClient.class)
    public WorkflowClient workflowClient(WorkflowServiceStubs workflowService, WorkflowClientOptions workflowClientOptions) {
        return WorkflowClient.newInstance(workflowService, workflowClientOptions);
    }

    @Bean
    public DataConverter jacksonDataConverter(ObjectMapper objectMapper) {
        return DefaultDataConverter
                .newDefaultInstance()
                .withPayloadConverterOverrides(new JacksonJsonPayloadConverter(objectMapper));
    }

    @Bean
    public WorkflowClientOptions workflowClientOptions(DataConverter dataConverter) {
        return WorkflowClientOptions.newBuilder()
                .setNamespace(NAMESPACE)
                .setDataConverter(dataConverter)
                .validateAndBuildWithDefaults();
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowServiceStubsOptions.class)
    public WorkflowServiceStubsOptions workflowServiceStubsOptions() {
        return WorkflowServiceStubsOptions.newBuilder()
                .setTarget("localhost:7233")
                .setRpcTimeout(Duration.of(60, ChronoUnit.SECONDS))
                .setQueryRpcTimeout(Duration.of(10, ChronoUnit.SECONDS))
                .validateAndBuildWithDefaults();
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowServiceStubs.class)
    public WorkflowServiceStubs temporalClient(WorkflowServiceStubsOptions workflowServiceStubsOptions) {
        return WorkflowServiceStubs.newInstance(workflowServiceStubsOptions);
    }

    @Bean
    public WorkerFactoryOptions workerFactoryOptions() {
        return WorkerFactoryOptions.newBuilder().build();
    }

    @Bean
    @ConditionalOnMissingBean(WorkerFactory.class)
    public WorkerFactory workerFactory(WorkflowClient workflowClient, WorkerFactoryOptions workerFactoryOptions) {
        return WorkerFactory.newInstance(workflowClient, workerFactoryOptions);
    }

    @Bean
    public WorkerOptions workerOptions() {
        return WorkerOptions.newBuilder().validateAndBuildWithDefaults();
    }

    @Bean
    public WorkflowOptions.Builder workflowOptions() {
        return WorkflowOptions.newBuilder()
                .setWorkflowExecutionTimeout(Duration.of(60, ChronoUnit.SECONDS));
    }
}
