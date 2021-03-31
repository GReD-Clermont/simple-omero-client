/*
 *  Copyright (C) 2020 GReD
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


import fr.igred.omero.GenericObjectWrapper;
import omero.gateway.model.AnnotationData;

import java.sql.Timestamp;


/**
 * Generic class containing an AnnotationData (or a subclass) object.
 *
 * @param <T> Subclass of {@link AnnotationData}
 */
public abstract class GenericAnnotationWrapper<T extends AnnotationData> extends GenericObjectWrapper<T> {


    /**
     * Constructor of the GenericAnnotationWrapper class.
     *
     * @param annotation Annotation to be contained.
     */
    protected GenericAnnotationWrapper(T annotation) {
        super(annotation);
    }


    /**
     * Retrieves the {@link AnnotationData} namespace of the underlying {@link AnnotationData} instance.
     *
     * @return See above.
     */
    public String getNameSpace() {
        return data.getNameSpace();
    }


    /**
     * Sets the name space of the underlying {@link AnnotationData} instance.
     *
     * @param name The value to set.
     */
    public void setNameSpace(String name) {
        data.setNameSpace(name);
    }


    /**
     * Returns the time when the annotation was last modified.
     *
     * @return See above.
     */
    public Timestamp getLastModified() {
        return data.getLastModified();
    }


    /**
     * Retrieves the {@link AnnotationData#getDescription() description} of the underlying {@link AnnotationData}
     * instance.
     *
     * @return See above
     */
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the underlying {@link AnnotationData} instance.
     *
     * @param description The description
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }

}
