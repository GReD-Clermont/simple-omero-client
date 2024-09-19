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
public class MapAnnotationWrapper extends GenericAnnotationWrapper<MapAnnotationData> {

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
     * Constructor of the MapAnnotationWrapper class. Sets the content of the MapAnnotationData
     *
     * @param result List of NamedValue(Key-Value pair).
     *
     * @deprecated This constructor will be removed in a future version.
     * <p>Use {@link #MapAnnotationWrapper(Collection)} instead.
     */
    @Deprecated
    public MapAnnotationWrapper(List<NamedValue> result) {
        super(new MapAnnotationData());
        data.setContent(result);
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
     * Gets the List of NamedValue contained in the map annotation.
     *
     * @return MapAnnotationData content.
     */
    @SuppressWarnings("unchecked")
    public List<NamedValue> getContent() {
        return (List<NamedValue>) data.getContent();
    }


    /**
     * Sets the content of the map annotation.
     *
     * @param result List of NamedValue(Key-Value pair).
     *
     * @deprecated This method will be replaced by {@link #setContent(Collection)} in a future version.
     */
    @Deprecated
    public void setContent(List<NamedValue> result) {
        data.setContent(result);
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
     * Gets the List of Key-Value pairs contained in the map annotation.
     *
     * @return MapAnnotationData content.
     *
     * @deprecated This method will be renamed to {@link #getContent()} in a future version.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public List<Entry<String, String>> getContentAsEntryList() {
        return ((Collection<NamedValue>) data.getContent()).stream()
                                                           .map(MapAnnotationWrapper::toMapEntry)
                                                           .collect(toList());
    }


    /**
     * Gets the List of Key-Value pairs contained in the map annotation as a map.
     * <p>As keys may not be unique, the map contains values as a list.</p>
     *
     * @return See above.
     */
    public Map<String, List<String>> getContentAsMap() {
        return this.getContentAsEntryList()
                   .stream()
                   .collect(groupingBy(Entry::getKey,
                                       mapping(Entry::getValue, toList())));
    }


    /**
     * Gets the content of the map annotation as a string.
     *
     * @return See above.
     */
    public String getContentAsString() {
        return data.getContentAsString();
    }


    /**
     * @return the {@link MapAnnotationData} contained.
     *
     * @deprecated Gets the MapAnnotationData contained. Use {@link #asDataObject()} instead.
     */
    @Deprecated
    public MapAnnotationData asMapAnnotationData() {
        return data;
    }

}