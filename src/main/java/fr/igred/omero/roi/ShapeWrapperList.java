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

package fr.igred.omero.roi;


import fr.igred.omero.WrapperList;
import omero.gateway.model.ShapeData;


/** ArrayList of Shape Objects implementing the ShapeList interface */
public class ShapeWrapperList extends WrapperList<ShapeData, Shape<?>> implements ShapeList {

    private static final long serialVersionUID = -602818154494998324L;


    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public ShapeWrapperList() {
    }


    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     *
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    public ShapeWrapperList(int initialCapacity) {
        super(initialCapacity);
    }

}
