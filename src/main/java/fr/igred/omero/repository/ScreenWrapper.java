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


import fr.igred.omero.Client;
import fr.igred.omero.GatewayWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.ScreenData;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


/**
 * Class containing a ScreenData object.
 * <p> Wraps function calls to the ScreenData contained.
 */
public class ScreenWrapper extends RepositoryObjectWrapper<ScreenData> implements Screen {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "ScreenAnnotationLink";


    /**
     * Constructor of the Project class. Creates a new project and saves it to OMERO.
     *
     * @param client      The client handling the connection.
     * @param name        Project name.
     * @param description Project description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ScreenWrapper(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        super(new ScreenData());
        data.setName(name);
        data.setDescription(description);
        super.saveAndUpdate(client);
    }


    /**
     * Constructor of the class Screen.
     *
     * @param dataObject The ScreenData contained in the Screen.
     */
    public ScreenWrapper(ScreenData dataObject) {
        super(dataObject);
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
     * Returns the plates contained in this screen.
     *
     * @return See above.
     */
    @Override
    public List<Plate> getPlates() {
        return wrap(asDataObject().getPlates(), PlateWrapper::new);
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
    public void refresh(GatewayWrapper client) throws ServiceException, AccessException, ExecutionException {
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
    public boolean importImages(GatewayWrapper client, String... paths)
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
    public List<Long> importImage(GatewayWrapper client, String path)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        List<Long> ids = importImage(client, data, path);
        refresh(client);
        return ids;
    }

}
