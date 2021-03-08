package fr.igred.omero.exception;


/** Reports an error occurred on the server. */
public class ServerError extends omero.ServerError {

    /**
     * Constructs a new exception with the specified cause and detailed message.
     *
     * @param details Short explanation of the problem.
     * @param cause   The exception that caused this one to be risen.
     */
    public ServerError(String details, Throwable cause) {
        super(cause);
        message = details;
    }


}
