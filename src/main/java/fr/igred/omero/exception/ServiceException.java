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


import omero.gateway.exception.ConnectionStatus;
import omero.gateway.exception.DSOutOfServiceException;


/**
 * Reports an error occurred while trying to access the OMERO service. Such an error can occur in the following case:
 * <i>broken connection</i>, <i>expired session</i> or <i>not logged in</i>.
 */
public class ServiceException extends DSOutOfServiceException {

    private static final long serialVersionUID = -7235789233263889665L;


    /**
     * Constructs a new exception with the specified cause and a generic message.
     *
     * @param message          Short explanation of the problem.
     * @param cause            The exception that caused this one to be risen.
     * @param connectionStatus The status of the connection to the server.
     */
    public ServiceException(String message, Throwable cause, ConnectionStatus connectionStatus) {
        super(message, cause, connectionStatus);
    }


    /**
     * Constructs a new exception with the specified cause and a generic message.
     *
     * @param cause            The exception that caused this one to be risen.
     * @param connectionStatus The status of the connection to the server.
     */
    public ServiceException(Throwable cause, ConnectionStatus connectionStatus) {
        super("Cannot connect to OMERO", cause, connectionStatus);
    }


    /**
     * Creates a ServiceException from a {@link DSOutOfServiceException}.
     *
     * @param message Short explanation of the problem.
     * @param cause   The exception that caused this one to be raised.
     */
    public ServiceException(String message, DSOutOfServiceException cause) {
        this(String.format("%s%n%s", message, cause.getMessage()), cause.getCause(), cause.getConnectionStatus());
    }


    /**
     * Constructs a new exception with the specified cause and a generic message.
     *
     * @param message Short explanation of the problem.
     * @param cause   The exception that caused this one to be raised.
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
