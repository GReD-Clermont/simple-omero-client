/*
 *  Copyright (C) 2020-2023 GReD
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


/** List of Shape objects */
public class ShapeList extends ArrayList<Shape<?>> {


    private static final long serialVersionUID = 9076633148525603098L;


    /**
     * Tries to convert a ShapeData object to a Shape object.
     *
     * @param object The shape.
     * @param klass  The suspected class of the shape.
     * @param mapper The method used to wrap the object.
     * @param <T>    The type of ShapeData.
     * @param <U>    The type of RemoteObject.
     *
     * @return An RemoteObject.
     */
    private static <T extends ShapeData, U extends Shape<T>>
    U tryConvert(ShapeData object, Class<? extends T> klass, Function<? super T, U> mapper) {
        if (klass.isInstance(object)) return mapper.apply(klass.cast(object));
        else return null;
    }


    /**
     * Gets a list of elements from this list whose class is specified.
     *
     * @param clazz Class of the wanted elements.
     * @param <T>   Subclass of Shape.
     *
     * @return See above.
     */
    public <T extends Shape<?>> List<T> getElementsOf(Class<? extends T> clazz) {
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

        Shape<? extends ShapeData> wrapper = tryConvert(shape, PointData.class, Point::new);
        if (wrapper == null) wrapper = tryConvert(shape, TextData.class, Text::new);
        if (wrapper == null) wrapper = tryConvert(shape, RectangleData.class, Rectangle::new);
        if (wrapper == null) wrapper = tryConvert(shape, MaskData.class, Mask::new);
        if (wrapper == null) wrapper = tryConvert(shape, EllipseData.class, Ellipse::new);
        if (wrapper == null) wrapper = tryConvert(shape, LineData.class, Line::new);
        if (wrapper == null) wrapper = tryConvert(shape, PolylineData.class, Polyline::new);
        if (wrapper == null) wrapper = tryConvert(shape, PolygonData.class, Polygon::new);

        if (wrapper != null) added = add(wrapper);

        return added;
    }

}
