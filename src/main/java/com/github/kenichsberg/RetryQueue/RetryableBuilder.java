package com.github.kenichsberg.RetryQueue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RetryableBuilder<V> {
    final private Callable<V> callable;
    private long attemptsAvailable = 1;
    private long delayOnRetryMs = 2000;
    private RetryableCallback<V> callback;
    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public RetryableBuilder(Callable<V> callable) {
        this.callable = callable;
    }


    public long getAttemptsAvailable() {
        return attemptsAvailable;
    }

    public long getDelayOnRetryMs() {
        return delayOnRetryMs;
    }

    public Callable<V> getCallable() {
        return callable;
    }

    public RetryableCallback<V> getCallback() {
        return callback;
    }

    public ExecutorService getExecutor() {
        return executor;
    }


    public RetryableBuilder<V> setMaxRetries(long maxRetries){
        this.attemptsAvailable = ++maxRetries;
        return this;
    }

    public RetryableBuilder<V> setDelayOnRetyrMs(long delayOnRetryMs){
        this.delayOnRetryMs = delayOnRetryMs;
        return this;
    }

    public RetryableBuilder<V> setCallback(RetryableCallback<V> callback){
        this.callback = callback;
        return this;
    }

    public RetryableBuilder<V> setExecutor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }


    public Retryable<V> build() {
        return new Retryable<>(this);
    }
}
