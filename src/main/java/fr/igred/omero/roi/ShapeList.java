/*
 *  Copyright (C) 2020-2022 GReD
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


import omero.gateway.model.EllipseData;
import omero.gateway.model.LineData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PointData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.TextData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


/** List of GenericShapeWrapper objects */
public class ShapeList extends ArrayList<GenericShapeWrapper<?>> {


    private static final long serialVersionUID = 9076633148525603098L;


    /**
     * Tries to convert a ShapeData object to a GenericShapeWrapper object.
     *
     * @param object The shape.
     * @param klass  The suspected class of the shape.
     * @param mapper The method used to wrap the object.
     * @param <T>    The type of ShapeData.
     * @param <U>    The type of GenericObjectWrapper.
     *
     * @return A GenericObjectWrapper.
     */
    private static <T extends ShapeData, U extends GenericShapeWrapper<T>>
    U tryConvert(ShapeData object, Class<? extends T> klass, Function<? super T, U> mapper) {
        if (klass.isInstance(object)) return mapper.apply(klass.cast(object));
        else return null;
    }


    /**
     * Gets a list of elements from this list whose class is specified.
     *
     * @param clazz Class of the wanted elements.
     * @param <T>   Subclass of GenericShapeWrapper.
     *
     * @return See above.
     */
    public <T extends GenericShapeWrapper<?>> List<T> getElementsOf(Class<? extends T> clazz) {
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
        boolean added = false;

        GenericShapeWrapper<? extends ShapeData> wrapper = tryConvert(shape, PointData.class, PointWrapper::new);
        if (wrapper == null) wrapper = tryConvert(shape, TextData.class, TextWrapper::new);
        if (wrapper == null) wrapper = tryConvert(shape, RectangleData.class, RectangleWrapper::new);
        if (wrapper == null) wrapper = tryConvert(shape, MaskData.class, MaskWrapper::new);
        if (wrapper == null) wrapper = tryConvert(shape, EllipseData.class, EllipseWrapper::new);
        if (wrapper == null) wrapper = tryConvert(shape, LineData.class, LineWrapper::new);
        if (wrapper == null) wrapper = tryConvert(shape, PolylineData.class, PolylineWrapper::new);
        if (wrapper == null) wrapper = tryConvert(shape, PolygonData.class, PolygonWrapper::new);

        if (wrapper != null) added = add(wrapper);

        return added;
    }

}
