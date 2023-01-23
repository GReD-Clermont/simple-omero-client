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


import omero.gateway.model.PolygonData;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;


public interface Polygon extends Shape<PolygonData> {

    /**
     * Gets the text on the ShapeData.
     *
     * @return the text
     */
    @Override
    default String getText() {
        return asDataObject().getText();
    }


    /**
     * Sets the text on the ShapeData.
     *
     * @param text the text
     */
    @Override
    default void setText(String text) {
        asDataObject().setText(text);
    }


    /**
     * Converts the shape to an {@link java.awt.Shape}.
     *
     * @return The converted AWT Shape.
     */
    @Override
    default java.awt.Shape toAWTShape() {
        Path2D polygon = new Path2D.Double();

        List<Point2D.Double> points = getPoints();
        if (!points.isEmpty()) {
            polygon.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i < points.size(); i++) {
                polygon.lineTo(points.get(i).x, points.get(i).y);
            }
            polygon.closePath();
        }
        return polygon;
    }


    /**
     * Returns the points in the Polygon.
     *
     * @return See above.
     */
    default List<Point2D.Double> getPoints() {
        return asDataObject().getPoints();
    }


    /**
     * Sets the points in the polyline.
     *
     * @param points The points to set.
     */
    default void setPoints(List<Point2D.Double> points) {
        asDataObject().setPoints(points);
    }


    /**
     * Returns the points in the polygon.
     *
     * @return See above.
     */
    default List<Integer> getMaskPoints() {
        return asDataObject().getMaskPoints();
    }

}
