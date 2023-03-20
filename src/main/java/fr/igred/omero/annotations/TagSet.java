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


import fr.igred.omero.RemoteObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.TagAnnotationData;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.TagAnnotationI;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle tag sets on OMERO.
 */
public interface TagSet extends RemoteObject {


    /**
     * Returns a {@link TagAnnotationData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    TagAnnotationData asDataObject();


    /**
     * Gets the tag set name.
     *
     * @return See above.
     */
    String getName();


    /**
     * Sets the tag name.
     *
     * @param name The tag set name. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Gets the tag set description.
     *
     * @return See above.
     */
    String getDescription();


    /**
     * Sets the tag set description.
     *
     * @param description The tag set description. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the description is {@code null}.
     */
    void setDescription(String description);


    /**
     * Returns the list of tags related to this tag set.
     *
     * @return See above.
     */
    List<TagAnnotation> getTags();


    /**
     * Reloads the tag set and returns the corresponding list of tags.
     *
     * @param browser The data browser.
     *
     * @return See above.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<TagAnnotation> getTags(Browser browser)
    throws ExecutionException, AccessException, ServiceException {
        reload(browser);
        return getTags();
    }


    /**
     * Links a tag to this tag set.
     *
     * @param dm  The data manager.
     * @param tag The tag.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void link(DataManager dm, TagAnnotation tag)
    throws AccessException, ServiceException, ExecutionException {
        AnnotationAnnotationLink link = new AnnotationAnnotationLinkI();
        link.setParent(new TagAnnotationI(getId(), false));
        link.setChild(tag.asDataObject().asAnnotation());
        dm.save(link);
    }


    /**
     * Links multiple tags to this tag set.
     *
     * @param dm   The data manager.
     * @param tags The tags.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void link(DataManager dm, TagAnnotation... tags)
    throws AccessException, ServiceException, ExecutionException {
        for (TagAnnotation tag : tags) {
            link(dm, tag);
        }
    }


    /**
     * Reloads the tag set from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void reload(Browser browser)
    throws ServiceException, AccessException, ExecutionException;

}
