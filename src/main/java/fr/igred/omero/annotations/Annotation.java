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


import fr.igred.omero.Client;
import fr.igred.omero.ContainerLinked;
import fr.igred.omero.HCSLinked;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.Folder;
import omero.gateway.model.AnnotationData;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Annotations on OMERO.
 */
public interface Annotation extends ContainerLinked, HCSLinked {

    /**
     * Returns an {@link AnnotationData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    AnnotationData asDataObject();


    /**
     * Retrieves the annotation namespace.
     *
     * @return See above.
     */
    String getNameSpace();


    /**
     * Sets the annotation namespace.
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
     * Gets the annotation description.
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
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError Server error.
     */
    int countAnnotationLinks(Client client) throws ServiceException, OMEROServerError;


    /**
     * Gets all folders with this annotation from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends Folder> getFolders(Client client)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException;

}
