package com.invectorate.open.queutures;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The opaque interface that {@link Informable}s that are expected to be used to generate data for {@link Queuture}s use
 * to submit new information.
 * 
 * When an object is {@link #put(Object)} into a {@code QueutureBox}, it eventually becomes available to consumers of a
 * {@link Queuture} via {@link Queuture#next()}.
 * 
 * This object is typically backed by some sort of {@link BlockingQueue}.
 * 
 * @author Noah
 * 
 * @param <V>
 */
public interface QueutureBox<V> {

    /**
     * Add a new object into this box.
     * 
     * Requests to add objects into the box may block indefinitely depending on the queuing semantics of the underlying
     * {@link Queuture}.
     * 
     * @param object
     *            The object to store.
     * 
     * @throws NullPointerException
     *             If the given object is null.
     * @throws InterruptedException
     *             If the current thread is interrupted while attempting to store data.
     */
    public void put(V object) throws InterruptedException;

    /**
     * Add a new object into this box, failing if it takes longer than the specified timeout.
     * 
     * Requests to add objects into the box may block for time periods up to the timeout specified depending on the
     * queuing semantics of the underlying {@link Queuture}.
     * 
     * @param object
     *            The object to store.
     * @param timeout
     *            The maximum time to wait.
     * @param unit
     *            The time unit of the {@code timeout} parameter.
     * 
     * @throws NullPointerException
     *             If the given object is null.
     * @throws TimeoutException
     *             If the data could not be stored within the constraints of the given timeout.
     * @throws InterruptedException
     *             If the current thread is interrupted while attempting to store data.
     */
    public void put(V object, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;

}
