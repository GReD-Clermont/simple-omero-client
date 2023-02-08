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


import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.RepositoryObjectWrapper;
import omero.gateway.model.ScreenData;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;
import static fr.igred.omero.util.Wrapper.wrap;


/**
 * Class containing a ScreenData object.
 * <p> Wraps function calls to the ScreenData contained.
 */
public class ScreenWrapper extends RepositoryObjectWrapper<ScreenData> implements Screen {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "ScreenAnnotationLink";


    /**
     * Constructor of the ProjectWrapper class. Creates a new project and saves it to OMERO.
     *
     * @param dm          The data manager.
     * @param name        Project name.
     * @param description Project description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ScreenWrapper(DataManager dm, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        super(new ScreenData());
        data.setName(name);
        data.setDescription(description);
        super.saveAndUpdate(dm);
    }


    /**
     * Constructor of the class ScreenWrapper.
     *
     * @param screen The ScreenData contained in the ScreenWrapper.
     */
    public ScreenWrapper(ScreenData screen) {
        super(screen);
    }


    /**
     * Returns the type of annotation link for this object
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Gets the screen name.
     *
     * @return See above.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the screen.
     *
     * @param name The name of the screen. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    @Override
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Gets the screen description
     *
     * @return See above.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the screen.
     *
     * @param description The description of the screen.
     */
    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Returns the plates contained in this screen.
     *
     * @return See above.
     */
    @Override
    public List<Plate> getPlates() {
        return wrap(data.getPlates(), PlateWrapper::new);
    }


    /**
     * Returns the plates contained in this screen, with the specified name.
     *
     * @param name The expected plates name.
     *
     * @return See above.
     */
    @Override
    public List<Plate> getPlates(String name) {
        List<Plate> plates = getPlates();
        plates.removeIf(plate -> !plate.getName().equals(name));
        return plates;
    }


    /**
     * Returns the description of the protocol.
     *
     * @return See above.
     */
    @Override
    public String getProtocolDescription() {
        return data.getProtocolDescription();
    }


    /**
     * Sets the description of the protocol.
     *
     * @param value The value to set.
     */
    @Override
    public void setProtocolDescription(String value) {
        data.setProtocolDescription(value);
    }


    /**
     * Returns the identifier of the protocol.
     *
     * @return See above.
     */
    @Override
    public String getProtocolIdentifier() {
        return data.getProtocolIdentifier();
    }


    /**
     * Sets the identifier of the protocol.
     *
     * @param value The value to set.
     */
    @Override
    public void setProtocolIdentifier(String value) {
        data.setProtocolIdentifier(value);
    }


    /**
     * Returns the description of the reagent set.
     *
     * @return See above.
     */
    @Override
    public String getReagentSetDescription() {
        return data.getReagentSetDescripion();
    }


    /**
     * Sets the identifier of the reagent.
     *
     * @param value The value to set.
     */
    @Override
    public void setReagentSetDescription(String value) {
        data.setReagentSetDescripion(value);
    }


    /**
     * Returns the identifier of the Reagent set.
     *
     * @return See above.
     */
    @Override
    public String getReagentSetIdentifier() {
        return data.getReagentSetIdentifier();
    }


    /**
     * Sets the identifier of the reagent.
     *
     * @param value The value to set.
     */
    @Override
    public void setReagentSetIdentifier(String value) {
        data.setReagentSetIdentifier(value);
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
    @Override
    public void refresh(Browser client) throws ServiceException, AccessException, ExecutionException {
        String message = String.format("Cannot refresh %s", this);
        data = handleServiceAndAccess(client.getBrowseFacility(),
                                      bf -> bf.getScreens(client.getCtx(),
                                                          Collections.singletonList(this.getId()))
                                              .iterator()
                                              .next(),
                                      message);
    }


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
    @Override
    public boolean importImages(Client client, String... paths)
    throws ServiceException, ServerException, AccessException, IOException, ExecutionException {
        boolean success = importImages(client, data, paths);
        refresh(client);
        return success;
    }


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
    @Override
    public List<Long> importImage(Client client, String path)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        List<Long> ids = importImage(client, data, path);
        refresh(client);
        return ids;
    }

}