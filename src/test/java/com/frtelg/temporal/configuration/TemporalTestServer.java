package com.frtelg.temporal.configuration;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.testing.TestEnvironmentOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;

@TestConfiguration
public class TemporalTestServer {
    private final TestWorkflowEnvironment testWorkflowEnvironment;

    public TemporalTestServer(WorkflowClientOptions workflowClientOptions,
                              WorkerFactoryOptions workerFactoryOptions,
                              WorkflowServiceStubsOptions workflowServiceStubsOptions) {
        var options = TestEnvironmentOptions.newBuilder()
                .setWorkflowClientOptions(workflowClientOptions)
                .setWorkerFactoryOptions(workerFactoryOptions)
                .setWorkflowServiceStubsOptions(workflowServiceStubsOptions)
                .build();
        this.testWorkflowEnvironment = TestWorkflowEnvironment.newInstance(options);
    }

    @Bean
    public WorkflowClient testWorkflowClient() {
        return testWorkflowEnvironment.getWorkflowClient();
    }

    @Bean
    public WorkflowServiceStubs temporalTestService() {
        return testWorkflowEnvironment.getWorkflowService();
    }

    @Bean
    public TestWorkflowEnvironment testWorkflowEnvironment() {
        return testWorkflowEnvironment;
    }

    @Bean
    public WorkerFactory testWorkerFactory() {
        return testWorkflowEnvironment.getWorkerFactory();
    }

    @PreDestroy
    public void closeWorkflowService() {
        testWorkflowEnvironment.close();
    }
}