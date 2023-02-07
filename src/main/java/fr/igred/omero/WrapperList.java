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

import java.util.ArrayList;
import java.util.List;


/** ArrayList of Remote Objects implementing the OMEROList interface */
@SuppressWarnings("ClassExtendsConcreteCollection")
public class WrapperList<T extends DataObject, U extends RemoteObject<? extends T>>
        extends ArrayList<U>
        implements OMEROList<T, U> {

    private static final long serialVersionUID = -4150557399805439478L;


    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public WrapperList() {
    }


    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public WrapperList(int initialCapacity) {
        super(initialCapacity);
    }


    /**
     * Wraps the specified DataObject and adds it to the end of this list, if possible.
     *
     * @param object element to be wrapped and appended to this list
     *
     * @return {@code true} (as specified by {@link List#add(Object)})
     */
    @Override
    public boolean add(T object) {
        boolean added;

        try {
            U wrapper = Wrapper.wrap(object);
            added = add(wrapper);
        } catch (IllegalArgumentException e) {
            added = false;
        }

        return added;
    }

}
