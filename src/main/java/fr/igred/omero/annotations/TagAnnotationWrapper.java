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


import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.TagAnnotationData;

import java.util.concurrent.ExecutionException;


/**
 * Class containing a TagAnnotationData object.
 * <p> Wraps function calls to the TagAnnotationData contained.
 */
public class TagAnnotationWrapper extends AnnotationWrapper<TagAnnotationData> implements TagAnnotation {

    /**
     * Constructor of the TagAnnotationWrapper class.
     *
     * @param tag TagAnnotationData to wrap.
     */
    public TagAnnotationWrapper(TagAnnotationData tag) {
        super(tag);
    }


    /**
     * Constructor of the TagAnnotationWrapper class. Creates the tag and save it in OMERO.
     *
     * @param dm          The data manager.
     * @param name        Annotation name.
     * @param description Tag description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public TagAnnotationWrapper(DataManager dm, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        super(new TagAnnotationData(name, description));
        super.saveAndUpdate(dm);
    }


    /**
     * Gets the name of the TagData.
     *
     * @return TagData name.
     */
    @Override
    public String getName() {
        return data.getTagValue();
    }


    /**
     * Sets the name of the TagData.
     *
     * @param name The name of the TagData. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    @Override
    public void setName(String name) {
        data.setTagValue(name);
    }

}