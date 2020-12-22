package com.anmi.spring.batch.config;

import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

@Component
public class FutureTaskDecorator implements TaskDecorator {

    private Thread taskThread;

    @Override
    public Runnable decorate(Runnable runnable) {
        return () -> {
            taskThread = Thread.currentThread();
            runnable.run();
        };
    }

    public void interruptTask() {
        if (taskThread != null) {
            taskThread.interrupt();
        }
    }
}


