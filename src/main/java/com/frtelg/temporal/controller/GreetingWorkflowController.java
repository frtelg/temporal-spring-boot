package com.frtelg.temporal.controller;

import com.frtelg.temporal.dto.NameFromWorkflowResponse;
import com.frtelg.temporal.dto.WorkflowResponse;
import com.frtelg.temporal.workflow.GreetingWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@RestController
@RequestMapping("/workflow")
public class GreetingWorkflowController {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final WorkflowClient workflowClient;
    private final WorkflowOptions workflowOptions;

    public GreetingWorkflowController(WorkflowClient workflowClient, WorkflowOptions.Builder workflowOptionsBuilder) {
        this.workflowClient = workflowClient;
        this.workflowOptions = workflowOptionsBuilder.setTaskQueue(GreetingWorkflow.TASK_LIST)
                .build();
    }

    @GetMapping
    public ResponseEntity<WorkflowResponse> startWorkflow() {
        GreetingWorkflow workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class, workflowOptions);
        WorkflowExecution execution = WorkflowClient.start(workflow::greet);
        WorkflowResponse responseBody = WorkflowResponse.success(execution.getWorkflowId());

        return ResponseEntity.ok(responseBody);
    }

    @PutMapping("/{workflowId}/{name}")
    public ResponseEntity<WorkflowResponse> changeNameInWorkflow(@PathVariable String workflowId,
                                                                 @PathVariable String name) {
        return safeCallWorkflow(() -> {
            GreetingWorkflow workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class, workflowId);
            String currentName = workflow.getCurrentName();

            if (currentName.equals(name)) {
                return ResponseEntity.badRequest()
                        .body(WorkflowResponse.error(workflowId, String.format("Name already is %s", name)));
            }

            workflow.changeName(name);
            return ResponseEntity.ok(WorkflowResponse.success(workflowId));
        });
    }

    @GetMapping("{workflowId}/current-name")
    public ResponseEntity<NameFromWorkflowResponse> getCurrentNameFromWorkflow(@PathVariable String workflowId) {
        return safeCallWorkflow(() -> {
            GreetingWorkflow workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class, workflowId);
            String currentName = workflow.getCurrentName();
            return ResponseEntity.ok(new NameFromWorkflowResponse(currentName, workflowId));
        });
    }

    @DeleteMapping("/{workflowId}")
    public ResponseEntity<WorkflowResponse> terminateWorkflow(@PathVariable String workflowId) {
        return safeCallWorkflow(() -> {
            GreetingWorkflow workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class, workflowId);
            workflow.terminate();

            return ResponseEntity.ok(WorkflowResponse.success(workflowId));
        });
    }

    private <T> ResponseEntity<T> safeCallWorkflow(Supplier<ResponseEntity<T>> workflowCall) {
        try {
            return workflowCall.get();
        } catch (WorkflowNotFoundException e) {
            log.error("Workflow not found, perhaps workflowId is incorrect or workflow has timed out", e);
            return ResponseEntity.notFound()
                    .build();
        }
    }
}
