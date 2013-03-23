package com.invectorate.open.queutures;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ForwardingExecutorService;

/**
 * Default implementation for {@link QueutureExecutorService} that simply forwards to a delegate executor service.
 * 
 * @author Noah Fontes <nfontes@invectorate.com>
 */
public class DelegatedQueutureExecutorService extends ForwardingExecutorService implements QueutureExecutorService {

    private final ExecutorService delegate;

    /**
     * Create a new forwarding executor service with support for queutures.
     * 
     * @param delegate
     *            The executor service to delegate all executions to.
     */
    public DelegatedQueutureExecutorService(final ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    protected ExecutorService delegate() {
        return this.delegate;
    }

    /**
     * Create a new queuture that can be executed later using a default queue implementation, an unbounded
     * {@link LinkedBlockingQueue}.
     * 
     * This method might be overridden in subclasses to change the queue implementation or to use an alternate strategy
     * for managing concurrent access to the results of the queuture.
     * 
     * @param informable
     *            The informable object that is being submitted.
     * 
     * @return A new {@link RunnableQueuture} that will be executed by this object's delegate.
     */
    protected <V> RunnableQueuture<V> newTaskFor(final Informable<QueutureBox<V>> informable) {
        return new QueutureTask<V>(informable, Queues.<V> newLinkedBlockingQueue());
    }

    @Override
    public <V> Queuture<V> submit(final Informable<QueutureBox<V>> informable) {
        Preconditions.checkNotNull(informable, "informable must be specified");

        RunnableQueuture<V> queuture = this.newTaskFor(informable);
        this.execute(queuture);
        return queuture;
    }

}
