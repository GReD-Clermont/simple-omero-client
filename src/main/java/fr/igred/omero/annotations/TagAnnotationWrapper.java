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
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.TagAnnotationData;

import java.util.concurrent.ExecutionException;


/**
 * Class containing a TagAnnotationData object.
 * <p> Wraps function calls to the TagAnnotationData contained.
 */
public class TagAnnotationWrapper extends GenericAnnotationWrapper<TagAnnotationData> {

    /**
     * Constructor of the TagAnnotationWrapper class.
     *
     * @param tag TagAnnotationData to wrap.
     */
    public TagAnnotationWrapper(TagAnnotationData tag) {
        super(tag);
    }


    /**
     * Constructor of the TagAnnotationWrapper class. Creates the tag and saves it to OMERO.
     *
     * @param client      The client handling the connection.
     * @param name        Annotation name.
     * @param description Tag description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public TagAnnotationWrapper(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        super(new TagAnnotationData(name, description));
        super.saveAndUpdate(client);
    }


    /**
     * Gets the tag name.
     *
     * @return See above.
     */
    public String getName() {
        return data.getTagValue();
    }


    /**
     * Sets the tag name.
     *
     * @param name The tag name. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    public void setName(String name) {
        data.setTagValue(name);
    }


    /**
     * @return the {@link TagAnnotationData} contained.
     *
     * @deprecated Gets the TagAnnotationData contained. Use {@link #asDataObject()} instead.
     */
    @Deprecated
    public TagAnnotationData asTagAnnotationData() {
        return data;
    }

}