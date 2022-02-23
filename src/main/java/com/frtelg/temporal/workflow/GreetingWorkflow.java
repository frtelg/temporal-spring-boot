package com.frtelg.temporal.workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface GreetingWorkflow {
    String TASK_LIST = "Example";

    /**
     * A workflow method describes the main workflow. Calling the workflow will start the workflow, the workflow ends
     * when the WorkflowMethod is complete (or when the timeout has exceeded)
     */
    @WorkflowMethod
    void greet();

    /**
     * A signal method can be used to trigger the workflow from outside, in order to change its state.
     * Return type should always be void
     */
    @SignalMethod
    void changeName(String name);

    @SignalMethod
    void terminate();

    /**
     * A query method can be used to get workflow state
     */
    @QueryMethod
    String getCurrentName();
}