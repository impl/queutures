package com.invectorate.open.queutures;

import com.google.common.base.Function;

/**
 * An object that performs an arbitrary computation when it is given input of a certain type.
 * <p>
 * An {@code Informable} is conceptually similar to a {@link Runnable} or {@link Function}, except that it accepts a
 * single parameter from an external source and returns nothing.
 * 
 * @author Noah Fontes <nfontes@invectorate.com>
 * 
 * @param <T>
 *            The type of object which this class is interested in.
 */
public interface Informable<T> {

    /**
     * Perform an arbitrary computation based on given input.
     * 
     * @param object
     *            The informative object to use in the computation.
     */
    public void inform(T object);

}
