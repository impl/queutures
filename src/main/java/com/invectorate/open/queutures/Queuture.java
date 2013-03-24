package com.invectorate.open.queutures;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A representation of the outcome of an arbitrary asynchronous computation that may generate many results.
 * <p>
 * Conceptually very similar to a {@link Future}, a Queuture has many results, not just one. As these results are
 * generated by a computation, they become available in a thread-safe way through the {@link #next()} and
 * {@link #next(long, TimeUnit)} methods.
 * <p>
 * As its name implies, a Queuture behaves very much like a {@link BlockingQueue}. Once an element is read from a
 * Queuture in one thread, it is no longer available to any other consumers.
 * <p>
 * To read all of the results from a Queuture:
 * 
 * <pre>
 * {@code
 * Queuture<String> queuture = executorService.submit(...);
 * StringBuilder sb = new StringBuilder();
 *     
 * String v;
 * while((v = queuture.next()) != null)
 *     sb.append(v).append(" ");
 * 
 * System.out.println(sb);
 * }
 * </pre>
 * 
 * @author Noah Fontes <nfontes@invectorate.com>
 * 
 * @param <V>
 *            The type of object being generated by the computation.
 */
public interface Queuture<V> {

    /**
     * @see Future#cancel(boolean)
     */
    public boolean cancel(boolean mayInterruptIfRunning);

    /**
     * @see Future#isCancelled()
     */
    public boolean isCancelled();

    /**
     * @see Future#isDone()
     */
    public boolean isDone();

    /**
     * Returns the next result of the asynchronous computation.
     * <p>
     * If there are no more results to be returned, this method returns null. Subsequent calls to this method after
     * computation is complete will always return null.
     * <p>
     * This method will block until the next result is available, possibly indefinitely.
     * 
     * @return The next result of the asynchronous computation, or null if there are no more results.
     * 
     * @throws InterruptedException
     *             If the current thread is interrupted while waiting for the next result.
     * @throws ExecutionException
     *             If an error occurred during computation.
     */
    public V next() throws InterruptedException, ExecutionException;

    /**
     * Returns the next result of the asynchronous computation, waiting up to a given timeout for it to become
     * available.
     * <p>
     * If there are no more results to be returned, this method returns null. Subsequent calls to this method after
     * computation is complete will always return null.
     * <p>
     * This method will block until the next result is available, or until the timeout has expired.
     * 
     * @param timeout
     *            The maximum time to wait.
     * @param unit
     *            The time unit of the {@code timeout} parameter.
     * 
     * @return The next result of the asynchronous computation, or null if there are no more results.
     * 
     * @throws TimeoutException
     *             If the next result could not be computed within the constraints of the given timeout.
     * @throws InterruptedException
     *             If the current thread is interrupted while waiting for the next result.
     * @throws ExecutionException
     *             If an error occurred during computation.
     */
    public V next(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException, ExecutionException;

}
