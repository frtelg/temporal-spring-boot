package com.frtelg.temporal.controller;

import com.frtelg.temporal.dto.NameFromWorkflowResponse;
import com.frtelg.temporal.dto.WorkflowResponse;
import com.frtelg.temporal.workflow.GreetingWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflow")
public class GreetingWorkflowController {

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
        GreetingWorkflow workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class, workflowId);
        String currentName = workflow.getCurrentName();

        if (currentName.equals(name)) {
            return ResponseEntity.badRequest()
                    .body(WorkflowResponse.error(workflowId, String.format("Name already is %s", name)));
        }

        workflow.changeName(name);
        WorkflowResponse responseBody = WorkflowResponse.success(workflowId);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("{workflowId}/current-name")
    public ResponseEntity<NameFromWorkflowResponse> getCurrentNameFromWorkflow(@PathVariable String workflowId) {
        GreetingWorkflow workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class, workflowId);
        String currentName = workflow.getCurrentName();
        NameFromWorkflowResponse responseBody = new NameFromWorkflowResponse(currentName, workflowId);

        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{workflowId}")
    public ResponseEntity<WorkflowResponse> terminateWorkflow(@PathVariable String workflowId) {
        GreetingWorkflow workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class, workflowId);
        workflow.terminate();

        WorkflowResponse responseBody = WorkflowResponse.success(workflowId);

        return ResponseEntity.ok(responseBody);
    }


}
