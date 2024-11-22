package com.github.kenichsberg.RetryQueue;

public interface RetryableCallback<V> {
    void onSuccess(Retryable<V> retryable, V result);
    void onFailure(Retryable<V> retryable, Throwable throwable);
}
