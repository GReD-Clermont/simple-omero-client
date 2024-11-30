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

package fr.igred.omero.roi;


import omero.gateway.model.PolylineData;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;


/**
 * Interface to handle Polyline shapes on OMERO.
 */
public interface Polyline extends Shape {

    /**
     * Returns an {@link PolylineData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    PolylineData asDataObject();


    /**
     * Converts the shape to an {@link java.awt.Shape}.
     *
     * @return The converted AWT Shape.
     */
    @Override
    default java.awt.Shape toAWTShape() {
        Path2D polyline = new Path2D.Double();

        List<Point2D.Double> points = getPoints();
        if (!points.isEmpty()) {
            polyline.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i < points.size(); i++) {
                polyline.lineTo(points.get(i).x, points.get(i).y);
            }
        }
        return polyline;
    }


    /**
     * Returns the points in the Polyline.
     *
     * @return See above.
     */
    List<Point2D.Double> getPoints();


    /**
     * Sets the points in the polyline.
     *
     * @param points The points to set.
     */
    void setPoints(List<Point2D.Double> points);

}
