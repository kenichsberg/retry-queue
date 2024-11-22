package com.github.kenichsberg.RetryQueue;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class RetryableTest {
    final BlockingQueue<String> acc = new LinkedBlockingQueue<>();
    final RetryableCallback<String> callback = new RetryableCallback<>() {
        @Override
        public void onSuccess(Retryable<String> retryable, String result) {
            boolean isSuccess = result.equals("Success");
            if (isSuccess) {
                try {
                    acc.put("SUCCESS");
                } catch(InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        @Override
        public void onFailure(Retryable<String> retryable, Throwable throwable) {
            try {
                acc.put(throwable.getMessage());
            } catch(InterruptedException e) {
                System.out.println(e.getMessage());
            }
            retryable.run();
        }
    };

    @BeforeEach
    void setUp() {
        acc.clear();
    }

    @Test
    void run() throws Exception {
        final Callable<String> f = () -> "Success";
        final RetryableBuilder<String> builder = new RetryableBuilder<>(f);
        final Retryable<String> retryable = builder.setCallback(callback).build();
        retryable.run();

        final String elem = acc.poll(1, TimeUnit.SECONDS);
        System.out.println(elem);
        assumeTrue(Objects.equals(elem, "SUCCESS"));

    }

    @Test
    void runWithFailures() throws Exception {
        final Callable<String> f = () -> {
            throw(new Exception("Something is wrong!"));
        };
        final RetryableBuilder<String> builder = new RetryableBuilder<>(f);
        final Retryable<String> retryable = builder.setMaxRetries(3)
                .setDelayOnRetyrMs(10)
                .setCallback(callback)
                .build();
        try {
            retryable.run();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        String result;
        for (int i = 0; i < 4; i++){
            System.out.println(i);
            result = acc.poll(1, TimeUnit.SECONDS);
            assumeTrue(Objects.equals(result, "Something is wrong!"));
        }
        final String elem = acc.poll(1, TimeUnit.SECONDS);
        System.out.println(elem);
        assumeTrue(Objects.equals(elem, null));

    }
}