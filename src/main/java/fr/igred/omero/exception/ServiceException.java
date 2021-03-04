package fr.igred.omero.exception;


import omero.gateway.exception.ConnectionStatus;
import omero.gateway.exception.DSOutOfServiceException;

/**
 * Reports an error occurred while trying to access the OMERO service.
 * Such an error can posted in the following case:
 * <i>broken connection</i>, <i>expired session</i> or <i>not logged in</i>.
 */
public class ServiceException extends DSOutOfServiceException {

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message          Short explanation of the problem.
     * @param cause            The exception that caused this one to be risen.
     * @param connectionStatus The status of the connection to the server.
     */
    public ServiceException(String message, Throwable cause, ConnectionStatus connectionStatus) {
        super(message, cause, connectionStatus);
    }

}
