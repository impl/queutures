package com.invectorate.open.queutures;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * An {@link ExecutorService} that also supports submitting instances of {@link Informable} to create {@link Queuture}s.
 * 
 * @author Noah Fontes <nfontes@invectorate.com>
 */
public interface QueutureExecutorService extends ExecutorService {

    /**
     * Submit a new computation to be asynchronously executed.
     * <p>
     * As the computation is executed, results will become available on a first-come first-serve basis in the returned
     * {@link Queuture}.
     * 
     * @param informable
     *            The computation to asynchronously execute.
     * 
     * @return A {@link Queuture} representing the multiple results of the computation.
     * 
     * @throws RejectedExecutionException
     *             If the computation cannot be scheduled for execution.
     * @throws NullPointerException
     *             If the computation is null.
     */
    public <V> Queuture<V> submit(Informable<QueutureBox<V>> informable);

}
