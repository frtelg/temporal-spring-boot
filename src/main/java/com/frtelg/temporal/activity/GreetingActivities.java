package com.frtelg.temporal.activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface GreetingActivities {
    void sayHi(String name);
}
