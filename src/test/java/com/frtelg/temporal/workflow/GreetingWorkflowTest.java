package com.frtelg.temporal.workflow;

import com.frtelg.temporal.activity.GreetingActivitiesImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class GreetingWorkflowTest {
    private TestWorkflowEnvironment workflowEnvironment;
    private WorkflowClient workflowClient;
    private final Class<?> workflowImplementationClass = GreetingWorkflowImpl.class;
    private PrintStream printStream;

    private final WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
            .setTaskQueue(GreetingWorkflow.TASK_LIST)
            .setWorkflowExecutionTimeout(Duration.of(10, ChronoUnit.SECONDS))
            .build();

    @BeforeEach
    void setup() {
        printStream = mock(PrintStream.class);
        var greetingActivities = new GreetingActivitiesImpl(printStream);

        workflowEnvironment = TestWorkflowEnvironment.newInstance();
        var worker = workflowEnvironment.newWorker(GreetingWorkflow.TASK_LIST);
        worker.registerWorkflowImplementationTypes(workflowImplementationClass);
        worker.registerActivitiesImplementations(greetingActivities);

        workflowClient = workflowEnvironment.getWorkflowClient();

        workflowEnvironment.start();
    }

    @AfterEach
    void shutdown() {
        workflowEnvironment.close();
    }

    @Test
    void testWorkflow() throws InterruptedException, ExecutionException, TimeoutException {
        var workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class, workflowOptions);
        var expectedName = "Handige Harry";

        // Start workflow
        var execution = WorkflowClient.execute(workflow::greet);
        assertEquals("Stranger", workflow.getCurrentName());

        // Send signal
        workflow.changeName(expectedName);

        await().atMost(200, TimeUnit.MILLISECONDS)
                .until(() -> workflow.getCurrentName().equals(expectedName));

        var currentName = workflow.getCurrentName();
        assertEquals(expectedName, currentName);

        // Terminate workflow
        workflow.terminate();
        execution.get(50, TimeUnit.MILLISECONDS);

        assertTrue(execution.isDone());
        verify(printStream, times(1)).println("Hi Stranger");
        verify(printStream, times(1)).println("Hi Handige Harry");
    }
}
