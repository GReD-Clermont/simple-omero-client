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
import omero.gateway.model.PointData;

import java.util.Collection;
import java.util.LinkedList;


/**
 * Class containing an PointData.
 * <p> Wraps function calls to the PointData contained.
 */
public class PointWrapper extends ShapeWrapper<PointData> implements Point {


    /**
     * Constructor of the Point class using a PointData.
     *
     * @param dataObject the shape
     */
    public PointWrapper(PointData dataObject) {
        super(dataObject);
    }


    /**
     * Constructor of the Point class using a new empty PointData.
     */
    public PointWrapper() {
        this(new PointData());
    }


    /**
     * Constructor of the Point class using a new empty ShapeData.
     *
     * @param x x-coordinate of the shape.
     * @param y y-coordinate of the shape.
     */
    public PointWrapper(double x, double y) {
        this(new PointData(x, y));
    }


    /**
     * Creates a list of {@link Point} from an ImageJ Roi.
     *
     * @param ijRoi The ImageJ Roi.
     *
     * @return The corresponding list of {@link Point}.
     */
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static Collection<Point> fromIJ(Roi ijRoi) {
        float[] x = ijRoi.getFloatPolygon().xpoints;
        float[] y = ijRoi.getFloatPolygon().ypoints;

        Collection<Point> points = new LinkedList<>();
        for (int i = 0; i < x.length; i++) {
            PointWrapper p = new PointWrapper(x[i], y[i]);
            p.setText(ijRoi.getName());
            p.copy(ijRoi);
            points.add(p);
        }

        return points;
    }


}
