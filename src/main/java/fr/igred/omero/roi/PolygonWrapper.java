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


import ij.gui.Roi;
import omero.gateway.model.PolygonData;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;


/**
 * Class containing an PolygonData.
 * <p> Wraps function calls to the PolygonData contained.
 */
public class PolygonWrapper extends ShapeWrapper<PolygonData> implements Polygon {


    /**
     * Constructor of the Polygon class using a PolygonData.
     *
     * @param dataObject the shape.
     */
    public PolygonWrapper(PolygonData dataObject) {
        super(dataObject);
    }


    /**
     * Constructor of the Polygon class using a new empty LineData.
     */
    public PolygonWrapper() {
        this(new PolygonData());
    }


    /**
     * Constructor of the Polygon class using an ImageJ PolygonRoi.
     *
     * @param ijRoi An ImageJ ROI.
     */
    public PolygonWrapper(Roi ijRoi) {
        this();
        float[] x = ijRoi.getFloatPolygon().xpoints;
        float[] y = ijRoi.getFloatPolygon().ypoints;

        List<Point2D.Double> points = new LinkedList<>();
        IntStream.range(0, x.length).forEach(i -> points.add(new Point2D.Double(x[i], y[i])));

        data.setPoints(points);
        data.setText(ijRoi.getName());
        super.copy(ijRoi);
    }


    /**
     * Constructor of the Polygon class using a new LineData.
     *
     * @param points the points in the polyline.
     */
    public PolygonWrapper(List<Point2D.Double> points) {
        this(new PolygonData(points));
    }


}
