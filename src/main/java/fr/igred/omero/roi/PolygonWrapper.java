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


import omero.gateway.model.PolygonData;

import java.awt.geom.Point2D;
import java.util.List;


public class PolygonWrapper extends GenericShapeWrapper<PolygonData> {


    /**
     * Constructor of the PolygonWrapper class using a PolygonData.
     *
     * @param shape the shape.
     */
    public PolygonWrapper(PolygonData shape) {
        super(shape);
    }


    /**
     * Constructor of the PolygonWrapper class using a new empty LineData.
     */
    public PolygonWrapper() {
        this(new PolygonData());
    }


    /**
     * Constructor of the PolygonWrapper class using a new LineData.
     *
     * @param points the points in the polyline.
     */
    public PolygonWrapper(List<Point2D.Double> points) {
        this(new PolygonData(points));
    }


    /**
     * Returns the points in the Polygon.
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


    /**
     * Returns the points in the polygon.
     *
     * @return See above.
     */
    public List<Integer> getMaskPoints() {
        return data.getMaskPoints();
    }

}
