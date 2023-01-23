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

package fr.igred.omero.repository;


import fr.igred.omero.GatewayWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.ScreenData;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;


public interface Screen extends RepositoryObject<ScreenData> {

    /**
     * Gets the screen name.
     *
     * @return See above.
     */
    @Override
    default String getName() {
        return asDataObject().getName();
    }


    /**
     * Sets the name of the screen.
     *
     * @param name The name of the screen. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    default void setName(String name) {
        asDataObject().setName(name);
    }


    /**
     * Gets the screen description
     *
     * @return See above.
     */
    @Override
    default String getDescription() {
        return asDataObject().getDescription();
    }


    /**
     * Sets the description of the screen.
     *
     * @param description The description of the screen.
     */
    default void setDescription(String description) {
        asDataObject().setDescription(description);
    }


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
    default List<Plate> getPlates(String name) {
        List<Plate> plates = getPlates();
        plates.removeIf(plate -> !plate.getName().equals(name));
        return plates;
    }


    /**
     * Returns the description of the protocol.
     *
     * @return See above.
     */
    default String getProtocolDescription() {
        return asDataObject().getProtocolDescription();
    }


    /**
     * Sets the description of the protocol.
     *
     * @param value The value to set.
     */
    default void setProtocolDescription(String value) {
        asDataObject().setProtocolDescription(value);
    }


    /**
     * Returns the identifier of the protocol.
     *
     * @return See above.
     */
    default String getProtocolIdentifier() {
        return asDataObject().getProtocolIdentifier();
    }


    /**
     * Sets the identifier of the protocol.
     *
     * @param value The value to set.
     */
    default void setProtocolIdentifier(String value) {
        asDataObject().setProtocolIdentifier(value);
    }


    /**
     * Returns the description of the reagent set.
     *
     * @return See above.
     */
    default String getReagentSetDescription() {
        return asDataObject().getReagentSetDescripion();
    }


    /**
     * Sets the identifier of the reagent.
     *
     * @param value The value to set.
     */
    default void setReagentSetDescription(String value) {
        asDataObject().setReagentSetDescripion(value);
    }


    /**
     * Returns the identifier of the Reagent set.
     *
     * @return See above.
     */
    default String getReagentSetIdentifier() {
        return asDataObject().getReagentSetIdentifier();
    }


    /**
     * Sets the identifier of the reagent.
     *
     * @param value The value to set.
     */
    default void setReagentSetIdentifier(String value) {
        asDataObject().setReagentSetIdentifier(value);
    }


    /**
     * Refreshes the wrapped screen.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void refresh(GatewayWrapper client) throws ServiceException, AccessException, ExecutionException;


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
    boolean importImages(GatewayWrapper client, String... paths)
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
    List<Long> importImage(GatewayWrapper client, String path)
    throws ServiceException, AccessException, ServerException, ExecutionException;

}
