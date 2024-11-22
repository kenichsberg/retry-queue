package com.github.kenichsberg.RetryQueue;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;

public class RetryQueue implements AutoDequeueingQueue<Retryable<?>> {
    private final BlockingQueue<Retryable<?>> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private Future<?> dequeueingThreadFuture;
    private Semaphore sem;

    public RetryQueue() { }

    public RetryQueue(int concurrentLimit) {
        this.sem = new Semaphore(concurrentLimit);
    }

    /**
     * @return
     */
    @Override
    public synchronized boolean startDequeueing() {
        if (dequeueingThreadFuture != null) return false;

        dequeueingThreadFuture = executor.submit(() -> {
            while (!(Thread.currentThread().isInterrupted())) {
                try {
                    final Retryable<?> retryable = queue.take();
                    if (sem != null) {
                        retryable.setSemaphore(sem);
                    }
                    executor.submit(retryable);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    dequeueingThreadFuture = null;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        return true;
    }

    /**
     * @return boolean
     */
    @Override
    public synchronized boolean stopDequeueing() throws ExecutionException, InterruptedException, TimeoutException {
        if (dequeueingThreadFuture == null) return true;
        if (dequeueingThreadFuture.cancel(true) || dequeueingThreadFuture.isDone()) {
            dequeueingThreadFuture = null;
            return true;
        }

        dequeueingThreadFuture.get(3, TimeUnit.SECONDS);
        if (dequeueingThreadFuture.isDone()) {
            dequeueingThreadFuture = null;
            return true;
        }

        return false;
    }

    /**
     * @param retryable
     * @throws InterruptedException
     */
    @Override
    public void put(Retryable<?> retryable) throws InterruptedException {
        queue.put(retryable);
    }

    /**
     * @param retryable
     * @return
     */
    @Override
    public boolean add(Retryable<?> retryable) {
        return queue.add(retryable);
    }

    /**
     * @param retryable
     * @return
     */
    @Override
    public boolean offer(Retryable<?> retryable) {
        return queue.offer(retryable);
    }

    /**
     * @param retryable
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    @Override
    public boolean offer(Retryable<?> retryable, long timeout, TimeUnit unit) throws InterruptedException {
        return queue.offer(retryable, timeout, unit);
    }

    /**
     * @return 
     */
    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    /**
     * @param collection 
     * @return
     */
    @Override
    public boolean addAll(Collection<? extends Retryable<?>> collection) {
        return queue.addAll(collection);
    }

    /**
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        return queue.equals(o);
    }

    /**
     * @return
     */
    @Override
    public int hashCode() {
        return queue.hashCode();
    }

    /**
     * @return 
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * @return 
     */
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * @return 
     */
    @Override
    public Iterator<Retryable<?>> iterator() {
        return queue.iterator();
    }

    /**
     * @return 
     */
    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    /**
     * @param ts 
     * @param <T>
     * @return
     */
    @Override
    public <T> T[] toArray(T[] ts) {
        return queue.toArray(ts);
    }

    /**
     * @param collection 
     * @return
     */
    @Override
    public int drainTo(Collection<? super Retryable<?>> collection) {
        return queue.drainTo(collection);
    }

    /**
     * @param collection 
     * @param i
     * @return
     */
    @Override
    public int drainTo(Collection<? super Retryable<?>> collection, int i) {
        return queue.drainTo(collection, i);
    }


}
