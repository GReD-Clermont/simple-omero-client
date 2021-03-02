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

package fr.igred.omero.metadata.annotation;


import fr.igred.omero.Client;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DataObject;
import omero.gateway.model.TagAnnotationData;

import java.util.concurrent.ExecutionException;


/**
 * Class containing a TagAnnotationData
 * <p> Implements function using the TagAnnotationData contained.
 */
public class TagAnnotationContainer {

    /** TagAnnotationContainer contained */
    private final TagAnnotationData tag;


    /**
     * Constructor of the TagAnnotationContainer class.
     *
     * @param tag Tag to be contained.
     */
    public TagAnnotationContainer(TagAnnotationData tag) {
        this.tag = tag;
        this.tag.setNameSpace(tag.getContentAsString());
    }


    /**
     * Constructor of the TagAnnotationContainer class. Creates the tag and save it in OMERO.
     *
     * @param client      The user.
     * @param name        Tag name.
     * @param description Tag description.
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO.
     * @throws DSAccessException       Cannot access data.
     * @throws ExecutionException      A Facility can't be retrieved or instantiated.
     */
    public TagAnnotationContainer(Client client, String name, String description)
    throws DSOutOfServiceException, DSAccessException, ExecutionException {
        this.tag = new TagAnnotationData(name, description);
        DataObject o = client.getDm().saveAndReturnObject(client.getCtx(), tag);
        this.tag.setId(o.getId());
    }


    /**
     * Gets the name of the TagData.
     *
     * @return TagData name.
     */
    public String getName() {
        return tag.getNameSpace();
    }


    /**
     * Gets the description of the TagData.
     *
     * @return TagData description.
     */
    public String getDescription() {
        return tag.getDescription();
    }


    /**
     * Gets the TagData id.
     *
     * @return TagData id.
     */
    public Long getId() {
        return tag.getId();
    }


    /**
     * Gets the TagAnnotationData contained.
     *
     * @return the {@link TagAnnotationData} contained.
     */
    public TagAnnotationData getTag() {
        return tag;
    }

}