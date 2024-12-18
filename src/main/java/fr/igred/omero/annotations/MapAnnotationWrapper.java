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
import omero.model.NamedValue;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;


/**
 * Class containing a MapAnnotationData, a MapAnnotationData contains a list of NamedValue(Key-Value pair).
 * <p> Wraps function calls to the MapAnnotationData contained.
 */
public class MapAnnotationWrapper extends AnnotationWrapper<MapAnnotationData> {

    /**
     * The name space used to identify MapAnnotations created be the user
     */
    public static final String NS_USER_CREATED = MapAnnotationData.NS_CLIENT_CREATED;


    /**
     * Constructor of the MapAnnotationWrapper class.
     *
     * @param data MapAnnotationData to wrap.
     */
    public MapAnnotationWrapper(MapAnnotationData data) {
        super(data);
    }


    /**
     * Constructor of the MapAnnotationWrapper class. Sets the content of the map annotation.
     *
     * @param pairs Collection of Key-Value pairs.
     */
    public MapAnnotationWrapper(Collection<? extends Entry<String, String>> pairs) {
        super(new MapAnnotationData());
        List<NamedValue> nv = pairs.stream()
                                   .map(e -> new NamedValue(e.getKey(), e.getValue()))
                                   .collect(toList());
        data.setContent(nv);
    }


    /**
     * Constructor of the MapAnnotationWrapper class. Sets the content to a single key-value pair.
     * <p>Initializes the namespace to {@link #NS_USER_CREATED}.</p>
     *
     * @param key   The key.
     * @param value The value.
     */
    public MapAnnotationWrapper(String key, String value) {
        this(Collections.singletonList(new AbstractMap.SimpleEntry<>(key, value)));
        data.setNameSpace(NS_USER_CREATED);
    }


    /**
     * Constructor of the MapAnnotationWrapper class.
     */
    public MapAnnotationWrapper() {
        super(new MapAnnotationData());
    }


    /**
     * Converts a {@link NamedValue} to a {@link Entry}.
     *
     * @param namedValue The {@link NamedValue}.
     *
     * @return See above.
     */
    private static Entry<String, String> toMapEntry(NamedValue namedValue) {
        return new AbstractMap.SimpleEntry<>(namedValue.name, namedValue.value);
    }


    /**
     * Converts a {@link Entry} to a {@link NamedValue}.
     *
     * @param entry The {@link Entry}.
     *
     * @return See above.
     */
    private static NamedValue toNamedValue(Entry<String, String> entry) {
        return new NamedValue(entry.getKey(), entry.getValue());
    }


    /**
     * Gets the List of Key-Value pairs contained in the map annotation.
     *
     * @return MapAnnotationData content.
     */
    @SuppressWarnings("unchecked")
    public List<Entry<String, String>> getContent() {
        return ((Collection<NamedValue>) data.getContent()).stream()
                                                           .map(MapAnnotationWrapper::toMapEntry)
                                                           .collect(toList());
    }


    /**
     * Sets the content of the map annotation.
     *
     * @param pairs Collection of Key-Value pairs.
     */
    public void setContent(Collection<? extends Entry<String, String>> pairs) {
        List<NamedValue> nv = pairs.stream()
                                   .map(MapAnnotationWrapper::toNamedValue)
                                   .collect(toList());
        data.setContent(nv);
    }


    /**
     * Gets the List of Key-Value pairs contained in the map annotation as a map.
     *
     * @return See above.
     */
    public Map<String, List<String>> getContentAsMap() {
        return this.getContent()
                   .stream()
                   .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));
    }


    /**
     * Gets the content of the map annotation as a string.
     *
     * @return See above.
     */
    public String getContentAsString() {
        return data.getContentAsString();
    }

}