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

package fr.igred.omero.screen;


import fr.igred.omero.HCSLinked;
import fr.igred.omero.RepositoryObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.ScreenData;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Screens on OMERO.
 */
public interface Screen extends RepositoryObject<ScreenData>, HCSLinked<ScreenData> {


    /**
     * Sets the name of the screen.
     *
     * @param name The name of the screen. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Sets the description of the screen.
     *
     * @param description The description of the screen.
     */
    void setDescription(String description);


    /**
     * Returns the plates contained in this screen.
     *
     * @return See above.
     */
    List<Plate> getPlates();


    /**
     * Returns the plates contained in this screen, with the specified name.
     *
     * @param name The expected plates name.
     *
     * @return See above.
     */
    List<Plate> getPlates(String name);


    /**
     * Returns the description of the protocol.
     *
     * @return See above.
     */
    String getProtocolDescription();


    /**
     * Sets the description of the protocol.
     *
     * @param value The value to set.
     */
    void setProtocolDescription(String value);


    /**
     * Returns the identifier of the protocol.
     *
     * @return See above.
     */
    String getProtocolIdentifier();


    /**
     * Sets the identifier of the protocol.
     *
     * @param value The value to set.
     */
    void setProtocolIdentifier(String value);


    /**
     * Returns the description of the reagent set.
     *
     * @return See above.
     */
    String getReagentSetDescription();


    /**
     * Sets the identifier of the reagent.
     *
     * @param value The value to set.
     */
    void setReagentSetDescription(String value);


    /**
     * Returns the identifier of the Reagent set.
     *
     * @return See above.
     */
    String getReagentSetIdentifier();


    /**
     * Sets the identifier of the reagent.
     *
     * @param value The value to set.
     */
    void setReagentSetIdentifier(String value);


    /**
     * Refreshes the wrapped screen.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void refresh(Browser client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Imports all images candidates in the paths to the screen in OMERO.
     *
     * @param client The client handling the connection.
     * @param paths  Paths to the image files on the computer.
     *
     * @return If the import did not exit because of an error.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws IOException        Cannot read file.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    boolean importImages(Client client, String... paths)
    throws ServiceException, ServerException, AccessException, IOException, ExecutionException;


    /**
     * Imports one image file to the screen in OMERO.
     *
     * @param client The client handling the connection.
     * @param path   Path to the image file on the computer.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Long> importImage(Client client, String path)
    throws ServiceException, AccessException, ServerException, ExecutionException;

}
