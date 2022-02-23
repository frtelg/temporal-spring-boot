package com.frtelg.temporal.workflow;

import com.frtelg.temporal.activity.GreetingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Objects;

public class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities greetingActivities = Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(10L))
                    .validateAndBuildWithDefaults()
    );

    private String name = "Stranger";
    private boolean active = true;

    @Override
    public void greet() {
        while (active) {
            String oldName = name;
            greetingActivities.sayHi(name);
            Workflow.await(() -> !Objects.equals(oldName, name) || !active);
        }
    }

    @Override
    public void changeName(String name) {
        this.name = name;
    }

    @Override
    public void terminate() {
        this.active = false;
    }

    @Override
    public String getCurrentName() {
        return this.name;
    }
}
