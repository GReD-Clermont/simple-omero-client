/*
 *  Copyright (C) 2020-2023 GReD
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


import ij.gui.PointRoi;
import omero.gateway.model.PointData;

import java.awt.geom.Path2D;


/**
 * Class containing an PointData.
 * <p> Wraps function calls to the PointData contained.
 */
public class PointWrapper extends ShapeWrapper<PointData> implements Point {


    /**
     * Constructor of the PointWrapper class using a PointData.
     *
     * @param shape the shape
     */
    public PointWrapper(PointData shape) {
        super(shape);
    }


    /**
     * Constructor of the PointWrapper class using a new empty PointData.
     */
    public PointWrapper() {
        this(new PointData());
    }


    /**
     * Constructor of the PointWrapper class using a new empty ShapeData.
     *
     * @param x x-coordinate of the shape.
     * @param y y-coordinate of the shape.
     */
    public PointWrapper(double x, double y) {
        this(new PointData(x, y));
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
     * Converts the shape to an {@link java.awt.Shape}.
     *
     * @return The converted AWT Shape.
     */
    @Override
    public java.awt.Shape toAWTShape() {
        Path2D point = new Path2D.Double();
        point.moveTo(getX(), getY());
        return point;
    }


    /**
     * Returns the x-coordinate of the shape.
     *
     * @return See above.
     */
    @Override
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate of the shape.
     *
     * @param x See above.
     */
    @Override
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y coordinate of the shape.
     *
     * @return See above.
     */
    @Override
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate of the shape.
     *
     * @param y See above.
     */
    @Override
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Sets the coordinates the PointData shape.
     *
     * @param x x-coordinate of the PointData shape.
     * @param y y-coordinate of the PointData shape.
     */
    @Override
    public void setCoordinates(double x, double y) {
        setX(x);
        setY(y);
    }


    /**
     * Gets the coordinates of the PointData shape.
     *
     * @return Array of coordinates containing {X,Y}.
     */
    @Override
    public double[] getCoordinates() {
        double[] coordinates = new double[2];
        coordinates[0] = getX();
        coordinates[1] = getY();
        return coordinates;
    }


    /**
     * Sets the coordinates of the PointData shape.
     *
     * @param coordinates Array of coordinates containing {X,Y}.
     */
    @Override
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("PointData cannot set null coordinates.");
        } else if (coordinates.length == 2) {
            data.setX(coordinates[0]);
            data.setY(coordinates[1]);
        } else {
            throw new IllegalArgumentException("2 coordinates required for PointData.");
        }
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    @Override
    public ij.gui.Roi toImageJ() {
        java.awt.Shape awtShape = createTransformedAWTShape();

        double x = awtShape.getBounds2D().getX();
        double y = awtShape.getBounds2D().getY();

        PointRoi roi = new PointRoi(x, y);
        copyToIJRoi(roi);

        return roi;
    }

}
