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

package fr.igred.omero.annotations;


import fr.igred.omero.Client;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.Dataset;
import fr.igred.omero.repository.Image;
import fr.igred.omero.repository.Plate;
import fr.igred.omero.repository.Project;
import fr.igred.omero.repository.Screen;
import fr.igred.omero.repository.Well;
import omero.gateway.model.AnnotationData;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutionException;


public interface Annotation<T extends AnnotationData> extends RemoteObject<T> {

    /**
     * Retrieves the {@link AnnotationData} namespace of the underlying {@link AnnotationData} instance.
     *
     * @return See above.
     */
    default String getNameSpace() {
        return asDataObject().getNameSpace();
    }


    /**
     * Sets the name space of the underlying {@link AnnotationData} instance.
     *
     * @param name The value to set.
     */
    default void setNameSpace(String name) {
        asDataObject().setNameSpace(name);
    }


    /**
     * Returns the time when the annotation was last modified.
     *
     * @return See above.
     */
    default Timestamp getLastModified() {
        return asDataObject().getLastModified();
    }


    /**
     * Retrieves the {@link AnnotationData#getDescription() description} of the underlying {@link AnnotationData}
     * instance.
     *
     * @return See above
     */
    default String getDescription() {
        return asDataObject().getDescription();
    }


    /**
     * Sets the description of the underlying {@link AnnotationData} instance.
     *
     * @param description The description
     */
    default void setDescription(String description) {
        asDataObject().setDescription(description);
    }


    /**
     * Returns the number of annotations links for this object.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    default int countAnnotationLinks(Client client) throws ServiceException, ServerException {
        return client.findByQuery("select link.parent from ome.model.IAnnotationLink link " +
                                  "where link.child.id=" + getId()).size();
    }


    /**
     * Gets all projects with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Project list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Project> getProjects(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all datasets with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Dataset list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Dataset> getDatasets(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all images with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImages(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all screens with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Screen list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Screen> getScreens(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all plates with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Plate list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Plate> getPlates(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all wells with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Well list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Well> getWells(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException;

}
