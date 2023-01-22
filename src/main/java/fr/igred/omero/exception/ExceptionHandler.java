/*
 *  Copyright (C) 2020-2023 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.

 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

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
    private final String    error;


    /**
     * Private class constructor.
     *
     * @param value     Object to process.
     * @param exception Caught exception.
     * @param error     Error message.
     */
    protected ExceptionHandler(T value, Exception exception, String error) {
        this.value = value;
        this.exception = exception;
        this.error = error;
    }


    /**
     * Creates an ExceptionHandler from an object and a function.
     *
     * @param input        Object to process.
     * @param mapper       Lambda to apply on object.
     * @param errorMessage Error message.
     * @param <I>          Input argument type.
     * @param <R>          Returned object type.
     *
     * @return ExceptionHandler wrapping the returned object.
     */
    public static <I, R> ExceptionHandler<R> of(I input,
                                                ThrowingFunction<? super I, ? extends R, ? extends Exception> mapper,
                                                String errorMessage) {
        Objects.requireNonNull(mapper);
        Exception e = null;

        R result = null;
        try {
            result = mapper.apply(input);
        } catch (Exception ex) {
            e = ex;
        }
        return new ExceptionHandler<>(result, e, errorMessage);
    }


    /**
     * Creates an ExceptionHandler from an object and a function with no return value.
     *
     * @param input        Object to process.
     * @param consumer     Lambda to apply on object.
     * @param errorMessage Error message.
     * @param <I>          Input argument type.
     *
     * @return ExceptionHandler wrapping the object to process.
     */
    public static <I> ExceptionHandler<I> ofConsumer(I input,
                                                     ThrowingConsumer<? super I, ? extends Exception> consumer,
                                                     String errorMessage) {
        Objects.requireNonNull(consumer);
        Exception e = null;

        try {
            consumer.apply(input);
        } catch (Exception ex) {
            e = ex;
        }
        return new ExceptionHandler<>(input, e, errorMessage);
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
     * Applies a function to the specified object and return the result or throw {@link ServiceException} or
     * {@link AccessException}.
     *
     * @param value  Object to process.
     * @param mapper Lambda to apply on object.
     * @param error  Error message if an exception is thrown.
     * @param <T>    Object type.
     * @param <U>    Lambda result type.
     *
     * @return Whatever the lambda returns.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public static <T, U> U handleServiceAndAccess(T value,
                                                  ThrowingFunction<? super T, ? extends U, ? extends Exception> mapper,
                                                  String error)
    throws ServiceException, AccessException {
        return of(value, mapper, error)
                .rethrow(DSOutOfServiceException.class, ServiceException::new)
                .rethrow(DSAccessException.class, AccessException::new)
                .get();
    }


    /**
     * Applies a function to the specified object and return the result or throw {@link ServiceException} or
     * {@link OMEROServerError}.
     *
     * @param value  Object to process.
     * @param mapper Lambda to apply on object.
     * @param error  Error message if an exception is thrown.
     * @param <T>    Object type.
     * @param <U>    Lambda result type.
     *
     * @return Whatever the lambda returns.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError If the thread was interrupted.
     */
    public static <T, U> U handleServiceAndServer(T value,
                                                  ThrowingFunction<? super T, ? extends U, ? extends Exception> mapper,
                                                  String error)
    throws ServiceException, OMEROServerError {
        return of(value, mapper, error)
                .rethrow(DSOutOfServiceException.class, ServiceException::new)
                .rethrow(ServerError.class, OMEROServerError::new)
                .get();
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
     * @param type   The exception class.
     * @param mapper Lambda to convert the caught exception.
     * @param <E>    The type of the exception.
     * @param <F>    The type of the exception thrown.
     *
     * @return The same ExceptionHandler.
     *
     * @throws F A converted Exception.
     */
    public <E extends Throwable, F extends Throwable> ExceptionHandler<T>
    rethrow(Class<E> type, ExceptionWrapper<? super E, ? extends F> mapper)
    throws F {
        if (type.isInstance(exception))
            throw mapper.apply(error, type.cast(exception));
        return this;
    }


    /**
     * Returns the contained object.
     *
     * @return See above.
     */
    public T get() {
        if (exception != null) doThrow(exception);
        return value;
    }


    @Override
    public String toString() {
        return "ExceptionHandler{" +
               "exception=" + exception +
               ", value=" + value +
               ", error='" + error + "'" +
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
