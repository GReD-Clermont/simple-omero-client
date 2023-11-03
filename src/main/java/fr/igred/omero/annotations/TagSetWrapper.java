/*
 *  Copyright (C) 2020-2024 GReD
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


import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.TagAnnotationData;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.IObject;
import omero.model.TagAnnotationI;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Class containing a TagAnnotationData object with a namespace set to {@link #NS_TAGSET}.
 * <p> Wraps function calls to the TagAnnotationData contained.
 */
public class TagSetWrapper extends TagAnnotationWrapper {

    /**
     * Constructor of the TagSetWrapper class.
     *
     * @param tag TagAnnotationData to wrap.
     */
    public TagSetWrapper(TagAnnotationData tag) {
        super(tag);
        super.setNameSpace(NS_TAGSET);
    }


    /**
     * Constructor of the TagSetWrapper class. Creates the tag set and saves it to OMERO.
     *
     * @param dm          The data manager.
     * @param name        Tag name.
     * @param description Tag description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public TagSetWrapper(Client dm, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        this(new TagAnnotationData(name, description));
        super.saveAndUpdate(dm);
    }


    /**
     * Returns the list of tags related to this tag set.
     *
     * @return See above.
     */
    public List<TagAnnotationWrapper> getTags() {
        return wrap(data.getTags(), TagAnnotationWrapper::new);
    }


    /**
     * Reloads the tag set and returns the corresponding list of tags.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<TagAnnotationWrapper> getTags(Browser browser)
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
    public void link(Client dm, TagAnnotationWrapper tag)
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
    public void link(Client dm, TagAnnotationWrapper... tags)
    throws AccessException, ServiceException, ExecutionException {
        for (TagAnnotationWrapper tag : tags) {
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
     */
    public void reload(Browser browser)
    throws ServiceException, AccessException {
        String query = "select t from TagAnnotation as t " +
                       "left outer join fetch t.annotationLinks as l " +
                       "left outer join fetch l.child as a " +
                       "where t.id=" + getId();
        IObject o = browser.findByQuery(query).iterator().next();
        data = new TagAnnotationData((omero.model.TagAnnotation) o);
    }

}