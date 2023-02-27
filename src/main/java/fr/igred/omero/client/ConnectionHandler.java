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

package fr.igred.omero.client;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle the connection to an OMERO server through a {@link Gateway} for a given user and in a specific
 * {@link SecurityContext}.
 */
public interface ConnectionHandler {


    /**
     * Returns a ConnectionHandler associated with the given username.
     * <p> All actions realized with the returned ConnectionHandler will be considered as his.
     * <p> The user calling this function needs to have administrator rights.
     *
     * @param username The user name.
     *
     * @return The connection and context corresponding to the new user.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     * @throws NoSuchElementException The requested user does not exist.
     */
    ConnectionHandler sudo(String username)
    throws ServiceException, AccessException, ExecutionException;

}
