/*
 *  Copyright (C) 2020-2024 GReD
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


import omero.AuthenticationException;
import omero.ResourceError;
import omero.SecurityViolation;
import omero.ServerError;
import omero.SessionException;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;

import java.util.Objects;

import static java.lang.String.format;


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
     * @param value     RemoteObject to process.
     * @param exception Caught exception.
     */
    protected ExceptionHandler(T value, Exception exception) {
        this.value     = value;
        this.exception = exception;
    }


    /**
     * Returns {@code true} if the exception is an {@link AuthenticationException}.
     *
     * @param t The Exception.
     *
     * @return See above.
     */
    private static boolean isAuthenticationException(Throwable t) {
        return t != null
               && AuthenticationException.class.isAssignableFrom(t.getClass());
    }


    /**
     * Returns {@code true} if the exception is an {@link SecurityViolation}.
     *
     * @param t The Exception.
     *
     * @return See above.
     */
    private static boolean isSecurityViolation(Throwable t) {
        return t instanceof SecurityViolation
               || t != null && t.getCause() instanceof SecurityViolation;
    }


    /**
     * Returns {@code true} if the exception is a {@link ServerError}, or a {@link DSOutOfServiceException} and the
     * cause is either:
     * <ul><li>a {@link SecurityViolation}</li>
     * <li>a {@link SessionException}</li>
     * <li>an {@link AuthenticationException}</li>
     * <li>a {@link ResourceError}</li></ul>
     *
     * @param t The Exception
     *
     * @return See above.
     */
    private static boolean shouldBeHandled(Throwable t) {
        boolean toHandle = false;
        if (t instanceof ServerError) {
            toHandle = true;
        } else if (t instanceof DSOutOfServiceException) {
            Throwable cause = t.getCause();
            toHandle = isSecurityViolation(cause)
                       || cause instanceof SessionException
                       || isAuthenticationException(cause)
                       || cause instanceof ResourceError;
        }
        return toHandle;
    }


    /**
     * Creates an ExceptionHandler from an object and a function.
     *
     * @param <I>    Input argument type.
     * @param <R>    Returned object type.
     * @param input  RemoteObject to process.
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
     * @param input    RemoteObject to process.
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
     * Calls the provided function on the given input and return the result or handles any OMERO exception and rethrows
     * the appropriate exception with the specified message.
     *
     * @param <I>     Input argument type.
     * @param <R>     Returned object type.
     * @param input   RemoteObject to process.
     * @param mapper  Lambda to apply on object.
     * @param message The message, if an exception is thrown.
     *
     * @return The function output.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public static <I, R> R call(I input,
                                OMEROFunction<? super I, ? extends R> mapper,
                                String message)
    throws AccessException, ServiceException {
        return of(input, mapper).handleOMEROException(message).get();
    }


    /**
     * Checks the cause of an exception on OMERO and throws:
     * <ul>
     *     <li>
     *         {@link ServiceException} if the cause was:
     *         <ul>
     *             <li>{@link SessionException}</li>
     *             <li>{@link AuthenticationException}</li>
     *             <li>{@link ResourceError}</li>
     *         </ul>
     *     </li>
     *     <li>
     *         {@link AccessException} if the cause was:
     *         <ul>
     *             <li>{@link SecurityViolation}</li>
     *             <Li>Anything else</li>
     *         </ul>
     *     </li>
     * </ul>
     * <p>See {@link omero.gateway.facility.Facility#handleException}</p>
     *
     * @param throwable The exception.
     * @param message   Error message.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    @SuppressWarnings("JavadocReference")
    public static void handleOMEROException(Throwable throwable, String message)
    throws ServiceException, AccessException {
        Throwable cause = throwable.getCause();
        if (isSecurityViolation(cause)) {
            String s = format("For security reasons, cannot access data. %n");
            throw new AccessException(s + message, cause);
        } else if (cause instanceof SessionException) {
            String s = format("Session is not valid. %n");
            throw new ServiceException(s + message, cause);
        } else if (isAuthenticationException(cause)) {
            String s = format("Cannot initialize the session. %n");
            throw new ServiceException(s + message, cause);
        } else if (cause instanceof ResourceError) {
            String s = format("Fatal error. Please contact the administrator. %n");
            throw new ServiceException(s + message, throwable);
        }
        String s = format("Cannot access data. %n");
        throw new AccessException(s + message, throwable);
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
    public <E extends Throwable> ExceptionHandler<T> rethrow(Class<E> type)
    throws E {
        if (type.isInstance(exception)) {
            throw type.cast(exception);
        }
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
        if (type.isInstance(exception)) {
            throw mapper.apply(message, type.cast(exception));
        }
        return this;
    }


    /**
     * Checks if a ServerError or a DSOutOfService was thrown and handles the exception according to the cause.
     * <p>See {@link #handleOMEROException(Throwable, String)}.</p>
     *
     * @param msg Error message.
     *
     * @return The same ExceptionHandler.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public ExceptionHandler<T> handleServerAndService(String msg)
    throws ServiceException, AccessException {
        if (shouldBeHandled(exception)) {
            handleOMEROException(this.exception, msg);
        } else if (exception instanceof DSOutOfServiceException) {
            rethrow(DSOutOfServiceException.class, ServiceException::new, msg);
        }
        return this;
    }


    /**
     * Throws:
     * <ul>
     *     <li>{@link AccessException} if {@link DSAccessException} was caught</li>
     *     <li>{@link ServiceException} if {@link DSOutOfServiceException} was caught</li>
     *     <li>The appropriate exception if {@link ServerError} was caught (see {@link  #handleOMEROException})</li>
     * </ul>
     *
     * @param msg Error message.
     *
     * @return The same ExceptionHandler.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public ExceptionHandler<T> handleOMEROException(String msg)
    throws ServiceException, AccessException {
        return this.rethrow(DSAccessException.class, AccessException::new, msg)
                   .handleServerAndService(msg);
    }


    /**
     * Rethrows exception if one was caught (to not swallow it).
     */
    public void rethrow() {
        if (exception != null) {
            doThrow(exception);
        }
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
     * Interface for a function that can throw OMERO exceptions.
     *
     * @param <T> The input type.
     * @param <R> The output type.
     */
    @FunctionalInterface
    public interface OMEROFunction<T, R> extends ThrowingFunction<T, R, Exception> {

        @Override
        R apply(T t) throws DSOutOfServiceException, DSAccessException, ServerError;

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
     * @param <T> The input exception type.
     * @param <E> The wrapped exception type.
     */
    @FunctionalInterface
    public interface ExceptionWrapper<T, E extends Throwable> {

        E apply(String message, T t);

    }

}
