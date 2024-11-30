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


import omero.gateway.model.MapAnnotationData;

import java.util.Collection;
import java.util.List;
import java.util.Map;



/**
 * Interface to handle Map Annotations on OMERO.
 */
public interface MapAnnotation extends Annotation {

    /**
     * The name space used to identify MapAnnotations created be the user
     */
    String NS_USER_CREATED = MapAnnotationData.NS_CLIENT_CREATED;


    /**
     * Returns a {@link MapAnnotationData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    MapAnnotationData asDataObject();


    /**
     * Gets the List of Key-Value pairs contained in the map annotation.
     *
     * @return MapAnnotationData content.
     */
    List<Map.Entry<String, String>> getContent();


    /**
     * Sets the content of the map annotation.
     *
     * @param pairs Collection of Key-Value pairs.
     */
    void setContent(Collection<? extends Map.Entry<String, String>> pairs);


    /**
     * Gets the List of Key-Value pairs contained in the map annotation as a map.
     *
     * @return See above.
     */
    Map<String, List<String>> getContentAsMap();


    /**
     * Gets the content of the map annotation as a string.
     *
     * @return See above.
     */
    String getContentAsString();

}
