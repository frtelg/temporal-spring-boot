package com.frtelg.temporal.dto;

import lombok.Value;

@Value
public class NameFromWorkflowResponse {
    String name;
    String workflowId;
}
