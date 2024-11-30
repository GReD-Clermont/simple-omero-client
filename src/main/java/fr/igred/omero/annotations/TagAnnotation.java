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


import omero.gateway.model.TagAnnotationData;


/**
 * Interface to handle Tag Annotations on OMERO.
 */
public interface TagAnnotation extends Annotation {

    /**
     * The name space used to indicate that the tag is used a tag set.
     */
    String NS_TAGSET = TagAnnotationData.INSIGHT_TAGSET_NS;


    /**
     * Returns a {@link TagAnnotationData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    TagAnnotationData asDataObject();


    /**
     * Gets the tag name.
     *
     * @return See above.
     */
    String getName();


    /**
     * Sets the tag name.
     *
     * @param name The tag name. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Returns whether this tag is a TagSet or not.
     *
     * @return {@code true} if this tag is a tag set, {@code false} otherwise.
     */
    default boolean isTagSet() {
        return NS_TAGSET.equals(getNameSpace());
    }


    /**
     * Converts this tag annotation to a tag set.
     *
     * @return See above.
     */
    TagSet toTagSet();

}
