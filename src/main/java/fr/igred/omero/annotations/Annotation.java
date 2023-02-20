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

package fr.igred.omero.annotations;


import fr.igred.omero.Browser;
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


/**
 * Interface to handle Annotations on OMERO.
 *
 * @param <T> Subclass of {@link AnnotationData}
 */
public interface Annotation<T extends AnnotationData> extends RemoteObject<T> {

    /**
     * Retrieves the {@link AnnotationData} namespace of the underlying {@link AnnotationData} instance.
     *
     * @return See above.
     */
    String getNameSpace();


    /**
     * Sets the name space of the underlying {@link AnnotationData} instance.
     *
     * @param name The value to set.
     */
    void setNameSpace(String name);


    /**
     * Returns the time when the annotation was last modified.
     *
     * @return See above.
     */
    Timestamp getLastModified();


    /**
     * Retrieves the {@link AnnotationData#getDescription() description} of the underlying {@link AnnotationData}
     * instance.
     *
     * @return See above
     */
    String getDescription();


    /**
     * Sets the description of the underlying {@link AnnotationData} instance.
     *
     * @param description The description
     */
    void setDescription(String description);


    /**
     * Returns the number of annotations links for this object.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    int countAnnotationLinks(Browser browser) throws ServiceException, ServerException;


    /**
     * Gets all projects with this tag from OMERO.
     *
     * @param browser The data browser.
     *
     * @return The projects list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Project> getProjects(Browser browser)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all datasets with this tag from OMERO.
     *
     * @param browser The data browser.
     *
     * @return The datasets list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Dataset> getDatasets(Browser browser)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all images with this tag from OMERO.
     *
     * @param browser The data browser.
     *
     * @return The images list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImages(Browser browser)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all screens with this tag from OMERO.
     *
     * @param browser The data browser.
     *
     * @return The screens list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Screen> getScreens(Browser browser)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all plates with this tag from OMERO.
     *
     * @param browser The data browser.
     *
     * @return The plates list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Plate> getPlates(Browser browser)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all wells with this tag from OMERO.
     *
     * @param browser The data browser.
     *
     * @return The wells list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Well> getWells(Browser browser)
    throws ServiceException, AccessException, ServerException, ExecutionException;

}
