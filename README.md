# retry-queue
A queue implementation to execute any functions with flexible retrying functionality.

Each queue element is a `Retryable` (implementing `Runnable`) Object and runs on [virtual threads](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html).

## Usage
```java
class RetryQueueExample {

    final RetryQueue retryQueue = new RetryQueue();              // Without Semaphore
    // final RetryQueue retryQueue = new RetryQueue(100);        // With Semaphore
    
    final RetryableCallback<X> callback = new RetryableCallback<>() {
        @Override
        public void onSuccess(Retryable<X> retryable, X result) {
            ...
        }

        @Override
        public void onFailure(Retryable<X> retryable, Throwable throwable) {
            ...
            retryQueue.put(retryable);                          // Retries when dequeued
        }
    };


    final Callable<X> f = () -> {
        ...                                                     // The main operation
    };

    final RetryableBuilder<X> builder = new RetryableBuilder<>(f);

    final Retryable<X> retryable = builder.setMaxRetries(3)
           .setDelayOnRetyrMs(2000)
           .setCallback(callback)
           .build();

     retryQueue.put(retryable);
     // retryQueue.offer(retryable, 1, TimeUnit.SECONDS)

}
```
