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

package fr.igred.omero;


import fr.igred.omero.client.Browser;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;

import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Repository Objects on OMERO.
 */
public interface RepositoryObject extends Annotatable {

    /**
     * Gets the object name.
     *
     * @return See above.
     */
    String getName();


    /**
     * Gets the object description
     *
     * @return See above.
     */
    String getDescription();


    /**
     * Reloads the object from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void reload(Browser browser)
    throws ServiceException, AccessException, ExecutionException;

}
