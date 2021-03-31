/*
 *  Copyright (C) 2020 GReD
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


import omero.gateway.model.PolylineData;

import java.awt.geom.Point2D;
import java.util.List;


public class PolylineWrapper extends GenericShapeWrapper<PolylineData> {


    /**
     * Constructor of the PolylineWrapper class using a PolylineData.
     *
     * @param shape the shape.
     */
    public PolylineWrapper(PolylineData shape) {
        super(shape);
    }


    /**
     * Constructor of the RectangleWrapper class using a new empty LineData.
     */
    public PolylineWrapper() {
        this(new PolylineData());
    }


    /**
     * Constructor of the RectangleWrapper class using a new LineData.
     *
     * @param points the points in the polyline.
     */
    public PolylineWrapper(List<Point2D.Double> points) {
        this(new PolylineData(points));
    }


    /**
     * Returns the points in the Polyline.
     *
     * @return See above.
     */
    public List<Point2D.Double> getPoints() {
        return data.getPoints();
    }


    /**
     * Sets the points in the polyline.
     *
     * @param points The points to set.
     */
    public void setPoints(List<Point2D.Double> points) {
        data.setPoints(points);
    }

}
