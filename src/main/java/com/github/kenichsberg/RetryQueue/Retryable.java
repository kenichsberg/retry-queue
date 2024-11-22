package com.github.kenichsberg.RetryQueue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Retryable<V> implements Runnable {
    final ReentrantLock lock = new ReentrantLock();
    private boolean isFirstAttempt = true;
    public long attemptsAvailable;
    public long delayOnRetryMs;
    private Callable<V> callable;
    private RetryableCallback<V> callback;
    private ExecutorService callbackExecutor;
    private Semaphore sem;

    public Retryable(RetryableBuilder<V> retryableBuilder) {
        this.callable = retryableBuilder.getCallable();
        this.attemptsAvailable = retryableBuilder.getAttemptsAvailable();
        this.delayOnRetryMs = retryableBuilder.getDelayOnRetryMs();
        this.callback = retryableBuilder.getCallback();
        this.callbackExecutor = retryableBuilder.getExecutor();
    }

    public void setSemaphore(Semaphore sem) {
        this.sem = sem;
    }

    // In Java 24, this can be written with `synchronized` keyword
    @Override
    public void run() {
        lock.lock();

        if (sem != null) {
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                lock.unlock();
                throw new RuntimeException(e);
            }
        }
        try {
            if (attemptsAvailable-- <= 0) return;

            if (isFirstAttempt) {
                isFirstAttempt = false;
            } else {
                try {
                    Thread.sleep(delayOnRetryMs);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            V result;
            try {
                result = callable.call();
            } catch (Exception e) {
                callbackExecutor.submit(
                        () -> callback.onFailure(this, e)
                );
                throw new RuntimeException(e);
            }

            callbackExecutor.submit(
                    () -> callback.onSuccess(this, result)
            );

        } finally {
            if (sem != null) {
                sem.release();
            }
            lock.unlock();
        }
    }
}