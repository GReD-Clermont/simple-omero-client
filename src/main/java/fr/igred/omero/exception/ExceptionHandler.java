/*
 *  Copyright (C) 2020-2021 GReD
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


/**
 * Class with methods to handle OMERO exceptions
 */
public class ExceptionHandler {

    private ExceptionHandler() {
    }


    /**
     * Helper method to convert DSOutOfServiceException to ServiceException.
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    private static void handleServiceException(Throwable t, String message)
    throws ServiceException {
        if (t instanceof DSOutOfServiceException) {
            throw new ServiceException(message, t, ((DSOutOfServiceException) t).getConnectionStatus());
        }
    }


    /**
     * Helper method to convert ServerError to OMEROServerError.
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws OMEROServerError Server error.
     */
    private static void handleServerError(Throwable t, String message)
    throws OMEROServerError {
        if (t instanceof ServerError) {
            throw new OMEROServerError(message, t);
        }
    }


    /**
     * Helper method to convert DSAccessException to AccessException.
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws AccessException Cannot access data.
     */
    private static void handleAccessException(Throwable t, String message)
    throws AccessException {
        if (t instanceof DSAccessException) {
            throw new AccessException(message, t);
        }
    }


    /**
     * Helper method to convert an exception from:
     * <ul><li>DSOutOfServiceException to ServiceException</li>
     * <li>ServerError to OMEROServerError</li></ul>
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError Server error.
     */
    public static void handleServiceOrServer(Throwable t, String message)
    throws ServiceException, OMEROServerError {
        handleServiceException(t, message);
        handleServerError(t, message);
    }


    /**
     * Helper method to convert an exception from:
     * <ul><li>DSOutOfServiceException to ServiceException</li>
     * <li>DSAccessException to AccessException</li></ul>
     *
     * @param t       The Exception
     * @param message Short explanation of the problem.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public static void handleServiceOrAccess(Throwable t, String message)
    throws ServiceException, AccessException {
        handleServiceException(t, message);
        handleAccessException(t, message);
    }


    /**
     * Helper method to convert an exception from:
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
    public static void handleException(Throwable t, String message)
    throws ServiceException, AccessException, OMEROServerError {
        handleAccessException(t, message);
        handleServerError(t, message);
        handleServiceException(t, message);
    }

}
