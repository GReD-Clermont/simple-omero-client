package fr.igred.omero.exception;


import omero.gateway.exception.DSAccessException;


/** Reports an error occurred while trying to pull out data from the server. */
public class AccessException extends DSAccessException {

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message Short explanation of the problem.
     * @param cause   The exception that caused this one to be risen.
     */
    public AccessException(String message, Throwable cause) {
        super(message, cause);
    }

}
