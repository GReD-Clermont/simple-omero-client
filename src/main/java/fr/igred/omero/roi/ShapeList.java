/*
 *  Copyright (C) 2020-2021 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.

 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero.roi;


import omero.gateway.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/** List of GenericShapeWrapper objects */
public class ShapeList extends ArrayList<GenericShapeWrapper<?>> {


    /**
     * Gets a list of elements from this list whose class is specified.
     *
     * @param clazz Class of the wanted elements.
     * @param <T>   Subclass of GenericShapeWrapper.
     *
     * @return List of elements of
     */
    public <T extends GenericShapeWrapper<?>> List<T> getElementsOf(Class<T> clazz) {
        return stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }


    /**
     * Wraps the specified ShapeData object and add it to the end of this list.
     *
     * @param shape element to be wrapped and appended to this list
     *
     * @return {@code true} (as specified by {@link ArrayList#add(Object)})
     */
    public boolean add(ShapeData shape) {
        if (shape instanceof PointData) {
            return super.add(new PointWrapper((PointData) shape));
        } else if (shape instanceof TextData) {
            return super.add(new TextWrapper((TextData) shape));
        } else if (shape instanceof RectangleData) {
            return super.add(new RectangleWrapper((RectangleData) shape));
        } else if (shape instanceof MaskData) {
            return super.add(new MaskWrapper((MaskData) shape));
        } else if (shape instanceof EllipseData) {
            return super.add(new EllipseWrapper((EllipseData) shape));
        } else if (shape instanceof LineData) {
            return super.add(new LineWrapper((LineData) shape));
        } else if (shape instanceof PolylineData) {
            return super.add(new PolylineWrapper((PolylineData) shape));
        } else if (shape instanceof PolygonData) {
            return super.add(new PolygonWrapper((PolygonData) shape));
        } else {
            return super.add(new ShapeWrapper(shape));
        }
    }

}
