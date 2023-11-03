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


import fr.igred.omero.util.Wrapper;
import omero.gateway.model.AnnotationData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/** List of AnnotationWrapper objects */
public class AnnotationList extends ArrayList<AnnotationWrapper<?>> {


    private static final long serialVersionUID = 8792604507462788823L;


    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public AnnotationList() {
    }


    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     *
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    public AnnotationList(int initialCapacity) {
        super(initialCapacity);
    }


    /**
     * Gets a list of elements from this list whose class is specified.
     *
     * @param clazz Class of the wanted elements.
     * @param <T>   Subclass of AnnotationWrapper.
     *
     * @return See above.
     */
    public <T extends AnnotationWrapper<?>> List<T> getElementsOf(Class<? extends T> clazz) {
        return stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }


    /**
     * Wraps the specified AnnotationData object and add it to the end of this list.
     *
     * @param shape element to be wrapped and appended to this list
     *
     * @return {@code true} (as specified by {@link ArrayList#add(Object)})
     */
    public boolean add(AnnotationData shape) {
        boolean added;

        try {
            AnnotationWrapper<? extends AnnotationData> wrapper = Wrapper.wrap(shape);
            added = add(wrapper);
        } catch (IllegalArgumentException e) {
            added = false;
        }

        return added;
    }

}
