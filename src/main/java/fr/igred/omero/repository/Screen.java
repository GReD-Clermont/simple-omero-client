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

package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.GatewayWrapper;
import fr.igred.omero.HCSLinked;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.ScreenData;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Screens on OMERO.
 */
public interface Screen extends RepositoryObject, HCSLinked {

    /**
     * Returns an {@link ScreenData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    ScreenData asDataObject();


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
     * Reloads and returns this screen as a singleton list.
     *
     * @param client The client handling the connection.(unused).
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default List<Screen> getScreens(Client client)
    throws ServiceException, AccessException, ExecutionException {
        refresh(client);
        return Collections.singletonList(this);
    }


    /**
     * Reloads this screen and returns the updated list of plates linked to this screen.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default List<? extends Plate> getPlates(Client client)
    throws ServiceException, AccessException, ExecutionException {
        refresh(client);
        return getPlates();
    }


    /**
     * Returns the plates contained in this screen.
     *
     * @return See above.
     */
    List<? extends Plate> getPlates();


    /**
     * Returns the plates contained in this screen, with the specified name.
     *
     * @param name The expected plates name.
     *
     * @return See above.
     */
    List<? extends Plate> getPlates(String name);


    /**
     * Returns the plate acquisitions linked to this object, either directly, or through parents/children.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    List<? extends PlateAcquisition> getPlateAcquisitions(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Retrieves the wells linked to this object, either directly, or through parents/children.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    List<? extends Well> getWells(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Retrieves the images contained in this screen.
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    List<? extends Image> getImages(Client client)
    throws ServiceException, AccessException, ExecutionException;


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
     * Reloads the screen from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void refresh(GatewayWrapper client)
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
     * @throws OMEROServerError    Server error.
     * @throws IOException        Cannot read file.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    boolean importImages(GatewayWrapper client, String... paths)
    throws ServiceException, OMEROServerError, AccessException, IOException, ExecutionException;


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
     * @throws OMEROServerError    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Long> importImage(GatewayWrapper client, String path)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException;

}
