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

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;


/**
 * Class containing an PolygonData.
 * <p> Wraps function calls to the PolygonData contained.
 */
public class PolygonWrapper extends ShapeWrapper<PolygonData> {


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
     * Constructor of the PolygonWrapper class using an ImageJ PolygonRoi.
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
        super.copyFromIJRoi(ijRoi);
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
     * Gets the text on the ShapeData.
     *
     * @return the text
     */
    @Override
    public String getText() {
        return data.getText();
    }


    /**
     * Sets the text on the ShapeData.
     *
     * @param text the text
     */
    @Override
    public void setText(String text) {
        data.setText(text);
    }


    /**
     * Converts the shape to an {@link Shape}.
     *
     * @return The converted AWT Shape.
     */
    @Override
    public java.awt.Shape toAWTShape() {
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
