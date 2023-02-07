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

package fr.igred.omero;


import fr.igred.omero.util.Wrapper;
import omero.gateway.model.DataObject;

import java.util.List;
import java.util.stream.Collectors;


/** Generic list of Remote Objects */
public interface OMEROList<T extends DataObject, U extends RemoteObject<? extends T>> extends List<U> {


    /**
     * Gets a list of elements from this list whose class is specified.
     *
     * @param clazz Class of the wanted elements.
     * @param <V>   Subclass of RemoteObject.
     *
     * @return See above.
     */
    default <V extends U> List<V> getElementsOf(Class<? extends V> clazz) {
        return stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }


    /**
     * Wraps the specified Remote Object and add it to the end of this list.
     *
     * @param object element to be wrapped and appended to this list
     *
     * @return {@code true} (as specified by {@link List#add(Object)})
     */
    default boolean add(T object) {
        boolean added = false;

        try {
            U wrapper = Wrapper.wrap(object);
            added = add(wrapper);
        } catch (IllegalArgumentException e) {
            // IGNORE
        }

        return added;
    }

}
