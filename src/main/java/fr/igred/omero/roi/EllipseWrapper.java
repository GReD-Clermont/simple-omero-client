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


import ij.gui.EllipseRoi;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import omero.gateway.model.EllipseData;

import java.awt.geom.Ellipse2D;


public class EllipseWrapper extends GenericShapeWrapper<EllipseData> {


    /**
     * Constructor of the EllipseWrapper class using a EllipseData.
     *
     * @param shape the shape
     */
    public EllipseWrapper(EllipseData shape) {
        super(shape);
    }


    /**
     * Constructor of the EllipseWrapper class using a new empty EllipseData.
     */
    public EllipseWrapper() {
        this(new EllipseData());
    }


    /**
     * Constructor of the EllipseWrapper class using a new EllipseData.
     *
     * @param x       The x-coordinate of the center of the ellipse.
     * @param y       The y-coordinate of the center of the ellipse.
     * @param radiusX The radius along the X-axis.
     * @param radiusY The radius along the Y-axis.
     */
    public EllipseWrapper(double x, double y, double radiusX, double radiusY) {
        this(new EllipseData(x, y, radiusX, radiusY));
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
        return new Ellipse2D.Double(getX() - getRadiusX(), getY() - getRadiusY(), 2 * getRadiusX(), 2 * getRadiusY());
    }


    /**
     * Returns the x-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate of the center of the ellipse.
     *
     * @param x See above.
     */
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate of the center of the ellipse.
     *
     * @param y See above.
     */
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Returns the radius along the X-axis.
     *
     * @return See above.
     */
    public double getRadiusX() {
        return data.getRadiusX();
    }


    /**
     * Sets the radius along the X-axis.
     *
     * @param x the value to set.
     */
    public void setRadiusX(double x) {
        data.setRadiusX(x);
    }


    /**
     * Returns the radius along the Y-axis.
     *
     * @return See above.
     */
    public double getRadiusY() {
        return data.getRadiusY();
    }


    /**
     * Sets the radius along the Y-axis.
     *
     * @param y The value to set.
     */
    public void setRadiusY(double y) {
        data.setRadiusY(y);
    }


    /**
     * Sets the coordinates of the EllipseData shape.
     *
     * @param x       The x-coordinate of the center of the ellipse.
     * @param y       The y-coordinate of the center of the ellipse.
     * @param radiusX The radius along the X-axis.
     * @param radiusY The radius along the Y-axis.
     */
    public void setCoordinates(double x, double y, double radiusX, double radiusY) {
        data.setX(x);
        data.setY(y);
        data.setRadiusX(radiusX);
        data.setRadiusY(radiusY);
    }


    /**
     * Gets the coordinates of the MaskData shape.
     *
     * @return Array of coordinates containing {X,Y,RadiusX,RadiusY}.
     */
    public double[] getCoordinates() {
        double[] coordinates = new double[4];
        coordinates[0] = getX();
        coordinates[1] = getY();
        coordinates[2] = getRadiusX();
        coordinates[3] = getRadiusY();
        return coordinates;
    }


    /**
     * Sets the coordinates of the EllipseData shape.
     *
     * @param coordinates Array of coordinates containing {X,Y,RadiusX,RadiusY}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("EllipseData cannot set null coordinates.");
        } else if (coordinates.length == 4) {
            data.setX(coordinates[0]);
            data.setY(coordinates[1]);
            data.setRadiusX(coordinates[2]);
            data.setRadiusY(coordinates[3]);
        } else {
            throw new IllegalArgumentException("4 coordinates required for EllipseData.");
        }
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    @Override
    public Roi toImageJ() {
        java.awt.Shape awtShape = createTransformedAWTShape();

        Roi roi;
        if (awtShape instanceof Ellipse2D) {
            double x = ((Ellipse2D) awtShape).getX();
            double y = ((Ellipse2D) awtShape).getY();
            double w = ((Ellipse2D) awtShape).getWidth();
            double h = ((Ellipse2D) awtShape).getHeight();
            roi = new OvalRoi(x, y, w, h);
        } else {
            java.awt.geom.Rectangle2D shape1;
            java.awt.geom.Rectangle2D shape2;

            double x  = getX();
            double y  = getY();
            double rx = getRadiusX();
            double ry = getRadiusY();
            double ratio;

            if (ry <= rx) {
                PointWrapper p1 = new PointWrapper(x - rx, y);
                PointWrapper p2 = new PointWrapper(x + rx, y);
                p1.setTransform(toAWTTransform());
                p2.setTransform(toAWTTransform());
                shape1 = p1.createTransformedAWTShape().getBounds2D();
                shape2 = p2.createTransformedAWTShape().getBounds2D();
                ratio = ry / rx;
            } else {
                PointWrapper p1 = new PointWrapper(x, y - rx);
                PointWrapper p2 = new PointWrapper(x, y + ry);
                p1.setTransform(toAWTTransform());
                p2.setTransform(toAWTTransform());
                shape1 = p1.createTransformedAWTShape().getBounds2D();
                shape2 = p2.createTransformedAWTShape().getBounds2D();
                ratio = rx / ry;
            }

            double x1 = shape1.getX();
            double y1 = shape1.getY();
            double x2 = shape2.getX();
            double y2 = shape2.getY();

            roi = new EllipseRoi(x1, y1, x2, y2, ratio);
        }
        roi.setStrokeColor(getStroke());
        int c = getC() >= 0 ? getC() + 1 : getC();
        int z = getZ() >= 0 ? getZ() + 1 : getZ();
        int t = getT() >= 0 ? getT() + 1 : getT();
        roi.setPosition(c, z, t);
        return roi;
    }

}
