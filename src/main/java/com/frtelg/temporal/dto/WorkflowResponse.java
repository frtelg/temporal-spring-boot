package com.frtelg.temporal.dto;

import java.util.Optional;

public record WorkflowResponse(String workflowId,
                               Optional<String> errorMessage) {

    public static WorkflowResponse success(String workflowId) {
        return new WorkflowResponse(workflowId, Optional.empty());
    }

    public static WorkflowResponse error(String workflowId, String errorMessage) {
        return new WorkflowResponse(workflowId, Optional.of(errorMessage));
    }
}