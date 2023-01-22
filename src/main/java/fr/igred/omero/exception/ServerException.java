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


/** Reports an error occurred on the server. */
public class ServerException extends ServerError {

    private static final long serialVersionUID = 3769544644500634998L;


    /**
     * Constructs a new exception with the specified cause and detailed message.
     *
     * @param details Short explanation of the problem.
     * @param cause   The exception that caused this one to be risen.
     */
    public ServerException(String details, Throwable cause) {
        super("", "", details, cause);
    }


    /**
     * Constructs a new exception with the specified cause and detailed message.
     *
     * @param cause The exception that caused this one to be risen.
     */
    public ServerException(Throwable cause) {
        this("A server error occurred", cause);
    }

}
