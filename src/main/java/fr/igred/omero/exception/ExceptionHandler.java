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


import omero.AuthenticationException;
import omero.ResourceError;
import omero.SecurityViolation;
import omero.ServerError;
import omero.SessionException;
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
        this.value     = value;
        this.exception = exception;
    }


    /**
     * @deprecated Helper method to convert DSOutOfServiceException to ServiceException.
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Deprecated
    private static void handleServiceException(Throwable t, String message)
    throws ServiceException {
        if (t instanceof Exception) {
            new ExceptionHandler<>(null, (Exception) t)
                    .rethrow(DSOutOfServiceException.class, ServiceException::new, message);
        }
    }


    /**
     * @deprecated Helper method to convert ServerError to OMEROServerError.
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws OMEROServerError Server error.
     */
    @Deprecated
    private static void handleServerError(Throwable t, String message)
    throws OMEROServerError {
        if (t instanceof ServerError) {
            new ExceptionHandler<>(null, (Exception) t).rethrow(ServerError.class, OMEROServerError::new, message);
        }
    }


    /**
     * @deprecated Helper method to convert DSAccessException to AccessException.
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws AccessException Cannot access data.
     */
    @Deprecated
    private static void handleAccessException(Throwable t, String message)
    throws AccessException {
        if (t instanceof DSAccessException) {
            new ExceptionHandler<>(null, (Exception) t).rethrow(DSAccessException.class, AccessException::new, message);
        }
    }


    /**
     * @deprecated Helper method to convert an exception from:
     * <ul><li>DSOutOfServiceException to ServiceException</li>
     * <li>ServerError to OMEROServerError</li></ul>
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError Server error.
     */
    @Deprecated
    public static void handleServiceOrServer(Throwable t, String message)
    throws ServiceException, OMEROServerError {
        handleServiceException(t, message);
        handleServerError(t, message);
    }


    /**
     * @deprecated Helper method to convert an exception from:
     * <ul><li>DSOutOfServiceException to ServiceException</li>
     * <li>DSAccessException to AccessException</li></ul>
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    @Deprecated
    public static void handleServiceOrAccess(Throwable t, String message)
    throws ServiceException, AccessException {
        handleServiceException(t, message);
        handleAccessException(t, message);
    }


    /**
     * @deprecated Helper method to convert an exception from:
     * <ul><li>DSAccessException to AccessException</li>
     * <li>DSOutOfServiceException to ServiceException</li>
     * <li>ServerError to OMEROServerError</li></ul>
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws AccessException  Cannot access data.
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Deprecated
    public static void handleException(Throwable t, String message)
    throws ServiceException, AccessException, OMEROServerError {
        handleAccessException(t, message);
        handleServerError(t, message);
        handleServiceException(t, message);
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
        if (cause instanceof SecurityViolation) {
            String s = String.format("For security reasons, cannot access data. %n");
            throw new AccessException(s + message, cause);
        } else if (cause instanceof SessionException ||
                   (cause != null && AuthenticationException.class.isAssignableFrom(cause.getClass()))) {
            String s = String.format("Session is not valid or not properly initialized. %n");
            throw new ServiceException(s + message, cause);
        } else if (cause instanceof ResourceError) {
            String s = String.format("Fatal error. Please contact the administrator. %n");
            throw new ServiceException(s + message, throwable);
        }
        String s = String.format("Cannot access data. %n");
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
    public <E extends Throwable> ExceptionHandler<T> rethrow(Class<E> type) throws E {
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
     * @param message Error message.
     *
     * @return The same ExceptionHandler.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public ExceptionHandler<T> handleServerAndService(String message)
    throws ServiceException, AccessException {
        if (exception instanceof ServerError || exception instanceof DSOutOfServiceException) {
            handleOMEROException(this.exception, message);
        }
        return this;
    }


    /**
     * @deprecated Throws:
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
    @Deprecated
    public ExceptionHandler<T> handleServiceOrServer(String message)
    throws ServiceException, OMEROServerError {
        return this.rethrow(DSOutOfServiceException.class, ServiceException::new, message)
                   .rethrow(ServerError.class, OMEROServerError::new, message);
    }


    /**
     * @deprecated Throws:
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
    @Deprecated
    public ExceptionHandler<T> handleServiceOrAccess(String message)
    throws ServiceException, AccessException {
        return this.handleOMEROException(message);
    }


    /**
     * @deprecated Throws:
     * <ul>
     *     <li>{@link AccessException} if {@link DSAccessException} was caught</li>
     *     <li>{@link ServiceException} if {@link DSOutOfServiceException} was caught</li>
     *     <li>The appropriate exception if {@link ServerError} was caught (see {@link  #handleOMEROException})</li>
     * </ul>
     *
     * @param message Error message.
     *
     * @return The same ExceptionHandler.
     *
     * @throws AccessException  Cannot access data.
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Deprecated
    public ExceptionHandler<T> handleException(String message)
    throws ServiceException, AccessException, OMEROServerError {
        return this.handleOMEROException(message);
    }


    /**
     * Throws:
     * <ul>
     *     <li>{@link AccessException} if {@link DSAccessException} was caught</li>
     *     <li>{@link ServiceException} if {@link DSOutOfServiceException} was caught</li>
     *     <li>The appropriate exception if {@link ServerError} was caught (see {@link  #handleOMEROException})</li>
     * </ul>
     *
     * @param message Error message.
     *
     * @return The same ExceptionHandler.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public ExceptionHandler<T> handleOMEROException(String message)
    throws ServiceException, AccessException {
        return this.rethrow(DSOutOfServiceException.class, ServiceException::new, message)
                   .rethrow(DSAccessException.class, AccessException::new, message)
                   .handleServerAndService(message);
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
