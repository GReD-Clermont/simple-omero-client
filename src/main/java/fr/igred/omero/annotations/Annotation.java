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


import fr.igred.omero.ContainerLinked;
import fr.igred.omero.HCSLinked;
import fr.igred.omero.client.Browser;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.AnnotationData;

import java.sql.Timestamp;


/**
 * Interface to handle Annotations on OMERO.
 *
 * @param <T> Subclass of {@link AnnotationData}
 */
public interface Annotation<T extends AnnotationData> extends ContainerLinked<T>, HCSLinked<T> {

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
    default int countAnnotationLinks(Browser browser) throws ServiceException, ServerException {
        return browser.findByQuery("select link.parent from ome.model.IAnnotationLink link " +
                                   "where link.child.id=" + getId()).size();
    }

}
