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


    /**
     * Constructs a new exception with a general message and the cause.
     *
     * @param cause The exception that caused this one to be risen.
     */
    public AccessException(Throwable cause) {
        super("Cannot access data", cause);
    }

}
