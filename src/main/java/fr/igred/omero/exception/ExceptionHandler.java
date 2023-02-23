/*
 *  Copyright (C) 2020-2023 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero.exception;


import omero.ServerError;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;

import java.util.Objects;


/**
 * Class to handle and convert OMERO exceptions.
 */
@SuppressWarnings("ReturnOfThis")
public class ExceptionHandler<T> {

    private final Exception exception;
    private final T         value;


    /**
     * Private class constructor.
     *
     * @param value     Object to process.
     * @param exception Caught exception.
     */
    protected ExceptionHandler(T value, Exception exception) {
        this.value = value;
        this.exception = exception;
    }


    /**
     * Creates an ExceptionHandler from an object and a function.
     *
     * @param <I>    Input argument type.
     * @param <R>    Returned object type.
     * @param input  Object to process.
     * @param mapper Lambda to apply on object.
     *
     * @return ExceptionHandler wrapping the returned object.
     */
    public static <I, R> ExceptionHandler<R> of(I input,
                                                ThrowingFunction<? super I, ? extends R, ? extends Exception> mapper) {
        Objects.requireNonNull(mapper);
        Exception e = null;

        R result = null;
        try {
            result = mapper.apply(input);
        } catch (Exception ex) {
            e = ex;
        }
        return new ExceptionHandler<>(result, e);
    }


    /**
     * Creates an ExceptionHandler from an object and a function with no return value.
     *
     * @param <I>      Input argument type.
     * @param input    Object to process.
     * @param consumer Lambda to apply on object.
     *
     * @return ExceptionHandler wrapping the object to process.
     */
    public static <I> ExceptionHandler<I> ofConsumer(I input,
                                                     ThrowingConsumer<? super I, ? extends Exception> consumer) {
        Objects.requireNonNull(consumer);
        Exception e = null;

        try {
            consumer.apply(input);
        } catch (Exception ex) {
            e = ex;
        }
        return new ExceptionHandler<>(input, e);
    }


    /**
     * Sneakily throws an exception.
     *
     * @param t   The exception to throw.
     * @param <E> Type of Exception thrown
     *
     * @throws E Exception thrown.
     */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void doThrow(Exception t) throws E {
        throw (E) t;
    }


    /**
     * Throws an exception from the specified type, if one was caught.
     *
     * @param type The exception class.
     * @param <E>  The type of the exception.
     *
     * @return The same ExceptionHandler.
     *
     * @throws E An exception from the specified type.
     */
    public <E extends Throwable> ExceptionHandler<T> rethrow(Class<E> type) throws E {
        if (type.isInstance(exception))
            throw type.cast(exception);
        return this;
    }


    /**
     * Throws an exception converted from the specified type, if one was caught.
     *
     * @param <E>     The type of the exception.
     * @param <F>     The type of the exception thrown.
     * @param type    The exception class.
     * @param mapper  Lambda to convert the caught exception.
     * @param message Error message.
     *
     * @return The same ExceptionHandler.
     *
     * @throws F A converted Exception.
     */
    public <E extends Throwable, F extends Throwable> ExceptionHandler<T>
    rethrow(Class<E> type, ExceptionWrapper<? super E, ? extends F> mapper, String message)
    throws F {
        if (type.isInstance(exception))
            throw mapper.apply(message, type.cast(exception));
        return this;
    }


    /**
     * Throws:
     * <ul><li>{@link ServiceException} if {@link DSOutOfServiceException} was caught</li>
     * <li>{@link OMEROServerError} if {@link ServerError} was caught</li></ul>
     *
     * @param message Error message.
     *
     * @return The same ExceptionHandler.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError Server error.
     */
    public ExceptionHandler<T> handleServiceOrServer(String message)
    throws ServiceException, OMEROServerError {
        return this.rethrow(DSOutOfServiceException.class, ServiceException::new, message)
                   .rethrow(ServerError.class, OMEROServerError::new, message);
    }


    /**
     * Throws:
     * <ul><li>{@link ServiceException} if {@link DSOutOfServiceException} was caught</li>
     * <li>{@link AccessException} if {@link DSAccessException} was caught</li></ul>
     *
     * @param message Error message.
     *
     * @return The same ExceptionHandler.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public ExceptionHandler<T> handleServiceOrAccess(String message)
    throws ServiceException, AccessException {
        return this.rethrow(DSOutOfServiceException.class, ServiceException::new, message)
                   .rethrow(DSAccessException.class, AccessException::new, message);
    }


    /**
     * Throws:
     * <ul><li>{@link AccessException} if {@link DSAccessException} was caught</li>
     * <li>{@link ServiceException} if {@link DSOutOfServiceException} was caught</li>
     * <li>{@link OMEROServerError} if {@link ServerError} was caught</li></ul>
     *
     * @param message Error message.
     *
     * @return The same ExceptionHandler.
     *
     * @throws AccessException  Cannot access data.
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public ExceptionHandler<T> handleException(String message)
    throws ServiceException, AccessException, OMEROServerError {
        return this.rethrow(DSOutOfServiceException.class, ServiceException::new, message)
                   .rethrow(DSAccessException.class, AccessException::new, message)
                   .rethrow(ServerError.class, OMEROServerError::new, message);
    }


    /**
     * Rethrows exception if one was caught (to not swallow it).
     */
    public void rethrow() {
        if (exception != null) doThrow(exception);
    }


    /**
     * Returns the contained object.
     *
     * @return See above.
     */
    public T get() {
        rethrow();
        return value;
    }


    @Override
    public String toString() {
        return "ExceptionHandler{" +
               "exception=" + exception +
               ", value=" + value +
               "}";
    }


    /**
     * @param <T> The input type.
     * @param <R> The output type.
     * @param <E> The exception type.
     */
    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Throwable> {

        R apply(T t) throws E;

    }


    /**
     * @param <T> The input type.
     * @param <E> The exception type.
     */
    @FunctionalInterface
    public interface ThrowingConsumer<T, E extends Throwable> {

        void apply(T t) throws E;

    }


    /**
     * @param <T> The input type.
     * @param <E> The exception type.
     */
    @FunctionalInterface
    public interface ExceptionWrapper<T, E extends Throwable> {

        E apply(String message, T t);

    }

}
