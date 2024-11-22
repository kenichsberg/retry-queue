# retry-queue
A queue implementation to execute any functions with flexible retrying functionality.

Each queue element is a `Retryable` (implementing `Runnable`) Object and runs on [virtual threads](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html).

## Usage
```java
class RetryQueueExample {
    final RetryQueue retryQueue = new RetryQueue();
    
    final RetryableCallback<...> callback = new RetryableCallback<>() {
        @Override
        public void onSuccess(Retryable<...> retryable, String result) {
            ...
        }

        @Override
        public void onFailure(Retryable<...> retryable, Throwable throwable) {
            ...
            retryQueue.put(retryable);  // Retrys when dequeued
        }
    };

    final Callable<...> f = () -> {
        ...
    };
    final RetryableBuilder<...> builder = new RetryableBuilder<>(f);
    final Retryable<...> retryable = builder.setMaxRetries(3)
           .setDelayOnRetyrMs(10)
           .setCallback(callback)
           .build();
     retryQueue.put(retryable);

}
```
