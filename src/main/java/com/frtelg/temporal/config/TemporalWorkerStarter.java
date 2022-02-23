package com.frtelg.temporal.config;

import com.frtelg.temporal.activity.GreetingActivitiesImpl;
import com.frtelg.temporal.workflow.GreetingWorkflow;
import com.frtelg.temporal.workflow.GreetingWorkflowImpl;
import com.google.protobuf.Duration;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.temporal.api.workflowservice.v1.DescribeNamespaceResponse;
import io.temporal.api.workflowservice.v1.ListNamespacesRequest;
import io.temporal.api.workflowservice.v1.RegisterNamespaceRequest;
import io.temporal.api.workflowservice.v1.RegisterNamespaceResponse;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.List;

import static com.frtelg.temporal.config.TemporalConfiguration.NAMESPACE;

@Component
public class TemporalWorkerStarter {
    private static final Class<?>[] WORKFLOW_IMPLEMENTATION_TYPES = new Class[] { GreetingWorkflowImpl.class };
    private static final Object[] ACTIVITY_IMPLEMENTATIONS = new Object[] { new GreetingActivitiesImpl(System.out) };

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final WorkflowServiceStubs workflowServiceStubs;
    private final WorkerFactory workerFactory;
    private final WorkerOptions workerOptions;

    public TemporalWorkerStarter(WorkflowServiceStubs workflowServiceStubs,
                                 WorkerFactory workerFactory,
                                 WorkerOptions workerOptions) {
        this.workflowServiceStubs = workflowServiceStubs;
        this.workerFactory = workerFactory;
        this.workerOptions = workerOptions;
    }

    @PostConstruct
    public void startWorkerFactory() {
        if (!domainExists()) {
            registerDomain();
        }

        createWorkers();

        log.info("Starting Temporal Worker Factory");
        workerFactory.start();
    }

    @PreDestroy
    public void shutdownWorkerFactory() {
        log.info("Shutdown Temporal Worker Factory");
        workerFactory.shutdown();
    }

    private void registerDomain() {
        RegisterNamespaceRequest request = RegisterNamespaceRequest.newBuilder()
                .setDescription(NAMESPACE)
                .setNamespace(NAMESPACE)
                .setWorkflowExecutionRetentionPeriod(Duration.newBuilder().setSeconds(60).build())
                .build();

        RegisterNamespaceResponse response = workflowServiceStubs.blockingStub().registerNamespace(request);
        log.info("Successfully registered namespace \"{}\"",
                NAMESPACE);
        log.info("Server returned response: {}", response);
    }

    private void createWorkers() {
        Worker worker = workerFactory.newWorker(GreetingWorkflow.TASK_LIST, workerOptions);

        worker.registerWorkflowImplementationTypes(WORKFLOW_IMPLEMENTATION_TYPES);
        worker.registerActivitiesImplementations(ACTIVITY_IMPLEMENTATIONS);
    }

    private boolean domainExists() {
        try {
            List<DescribeNamespaceResponse> nameSpaces = workflowServiceStubs.blockingStub()
                    .listNamespaces(ListNamespacesRequest.getDefaultInstance())
                    .getNamespacesList();

            return nameSpaces.stream()
                    .anyMatch(d -> d.getNamespaceInfo().getName().equals(NAMESPACE));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNIMPLEMENTED) {
                log.warn("Listing or registering domains is not supported when using a local embedded test server, " +
                        "these steps will be skipped");
                return true; // evaluate as true so domain won't be registered.
            }

            throw e;
        }
    }
}
