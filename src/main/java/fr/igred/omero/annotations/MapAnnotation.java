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


import omero.gateway.model.MapAnnotationData;

import java.util.List;
import java.util.Map;

import static java.util.Map.Entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;


/**
 * Interface to handle Map Annotations on OMERO.
 */
public interface MapAnnotation extends Annotation<MapAnnotationData> {

    /**
     * Gets the List of Key-Value pairs contained in the map annotation.
     *
     * @return See above.
     */
    List<Entry<String, String>> getContent();


    /**
     * Sets the content of the map annotation.
     *
     * @param pairs List of Key-Value pairs.
     */
    void setContent(List<? extends Entry<String, String>> pairs);


    /**
     * Gets the List of Key-Value pairs contained in the map annotation as a map.
     *
     * @return See above.
     */
    default Map<String, List<String>> getContentAsMap() {
        return getContent().stream().collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));
    }

}