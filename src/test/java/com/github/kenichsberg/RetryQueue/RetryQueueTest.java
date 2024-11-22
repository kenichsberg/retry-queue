package com.github.kenichsberg.RetryQueue;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.lang.String;
import java.util.Objects;
import java.util.concurrent.*;

class RetryQueueTest {
    RetryQueue retryQueue = new RetryQueue();
    final BlockingQueue<String> results = new LinkedBlockingQueue<>();
    final RetryableCallback<String> callback = new RetryableCallback<>() {
        @Override
        public void onSuccess(Retryable<String> retryable, String result) {
            boolean isSuccess = result.equals("Success");
            if (isSuccess) {
                try {
                    results.put("SUCCESS");
                } catch(InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        @Override
        public void onFailure(Retryable<String> retryable, Throwable throwable) {
            try {
                results.put(throwable.getMessage());
            } catch(InterruptedException e) {
                System.out.println(e.getMessage());
            }
            retryable.run();
        }
    };

    @BeforeEach
    void setUp() {
        results.clear();
        retryQueue.startDequeueing();
    }

    @AfterEach
    void tearDown() throws ExecutionException, InterruptedException, TimeoutException {
        retryQueue.stopDequeueing();
    }

    @Test
    void put() throws InterruptedException {
        final Callable<String> f = () -> "Success";
        final RetryableBuilder<String> builder = new RetryableBuilder<>(f);
        final Retryable<String> retryable = builder.setCallback(callback).build();
        final boolean wasPut = retryQueue.offer(retryable);
        assumeTrue(wasPut);

        final String result = results.poll(1, TimeUnit.SECONDS);
        assumeTrue(Objects.equals(result, "SUCCESS"));
    }

    @Test
    void putWithRetries() throws InterruptedException {
        final Callable<String> f = () -> {
            throw(new Exception("Something is wrong!"));
        };
        final RetryableBuilder<String> builder = new RetryableBuilder<>(f);
        final Retryable<String> retryable = builder.setMaxRetries(3)
                .setDelayOnRetyrMs(10)
                .setCallback(callback)
                .build();
        final boolean wasPut = retryQueue.offer(retryable);
        assumeTrue(wasPut);

        String result;
        for (int i = 0; i < 4; i++){
            System.out.println(i);
            result = results.poll(1, TimeUnit.SECONDS);
            assumeTrue(Objects.equals(result, "Something is wrong!"));
        }
        assumeTrue(
                Objects.equals( results.poll(1, TimeUnit.SECONDS), null)
        );
    }

    @Test
    void stop() throws ExecutionException, InterruptedException, TimeoutException {
        assumeTrue(retryQueue.stopDequeueing());
    }

}