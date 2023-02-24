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


import ij.gui.Roi;
import omero.gateway.model.RectangleData;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;


/**
 * Class containing an RectangleData.
 * <p> Wraps function calls to the RectangleData contained.
 */
public class RectangleWrapper extends ShapeWrapper<RectangleData> {


    /**
     * Constructor of the RectangleWrapper class using a RectangleData.
     *
     * @param shape the shape
     */
    public RectangleWrapper(RectangleData shape) {
        super(shape);
    }


    /**
     * Constructor of the RectangleWrapper class using a new empty RectangleData.
     */
    public RectangleWrapper() {
        this(new RectangleData());
    }


    /**
     * Constructor of the RectangleWrapper class using bounds from an ImageJ ROI.
     *
     * @param ijRoi An ImageJ ROI.
     */
    public RectangleWrapper(ij.gui.Roi ijRoi) {
        this(ijRoi.getBounds().getX(),
             ijRoi.getBounds().getY(),
             ijRoi.getBounds().getWidth(),
             ijRoi.getBounds().getHeight());

        data.setText(ijRoi.getName());
        super.copyFromIJRoi(ijRoi);
    }


    /**
     * Constructor of the RectangleWrapper class using a new RectangleData.
     *
     * @param x      The x-coordinate of the top-left corner.
     * @param y      The y-coordinate of the top-left corner.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public RectangleWrapper(double x, double y, double width, double height) {
        this(new RectangleData(x, y, width, height));
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
        return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
    }


    /**
     * Returns the x-coordinate of the shape.
     *
     * @return See above.
     */
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate of the shape.
     *
     * @param x See above.
     */
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y coordinate of the shape.
     *
     * @return See above.
     */
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate of the shape.
     *
     * @param y See above.
     */
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Returns the width untransformed rectangle.
     *
     * @return See above.
     */
    public double getWidth() {
        return data.getWidth();
    }


    /**
     * Sets width of an untransformed rectangle.
     *
     * @param width See above.
     */
    public void setWidth(double width) {
        data.setWidth(width);
    }


    /**
     * Returns the height untransformed rectangle.
     *
     * @return See above.
     */
    public double getHeight() {
        return data.getHeight();
    }


    /**
     * Sets the height of an untransformed rectangle.
     *
     * @param height See above.
     */
    public void setHeight(double height) {
        data.setHeight(height);
    }


    /**
     * Sets the coordinates of the RectangleData shape.
     *
     * @param x      The x-coordinate of the top-left corner.
     * @param y      The y-coordinate of the top-left corner.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public void setCoordinates(double x, double y, double width, double height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }


    /**
     * Gets the coordinates of the RectangleData shape.
     *
     * @return Array of coordinates containing {X,Y,Width,Height}.
     */
    public double[] getCoordinates() {
        double[] coordinates = new double[4];
        coordinates[0] = getX();
        coordinates[1] = getY();
        coordinates[2] = getWidth();
        coordinates[3] = getHeight();
        return coordinates;
    }


    /**
     * Sets the coordinates of the RectangleData shape.
     *
     * @param coordinates Array of coordinates containing {X,Y,Width,Height}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("RectangleData cannot set null coordinates.");
        } else if (coordinates.length == 4) {
            data.setX(coordinates[0]);
            data.setY(coordinates[1]);
            data.setWidth(coordinates[2]);
            data.setHeight(coordinates[3]);
        } else {
            throw new IllegalArgumentException("4 coordinates required for RectangleData.");
        }
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    @Override
    public Roi toImageJ() {
        AffineTransform transform = toAWTTransform();

        Roi roi;
        if (transform.getType() == AffineTransform.TYPE_IDENTITY) {
            roi = new ij.gui.Roi(getX(), getY(), getWidth(), getHeight());
        } else {
            PointWrapper p1 = new PointWrapper(getX(), getY() + getHeight() / 2);
            PointWrapper p2 = new PointWrapper(getX() + getWidth(), getY() + getHeight() / 2);
            p1.setTransform(transform);
            p2.setTransform(transform);

            java.awt.geom.Rectangle2D shape1 = p1.createTransformedAWTShape().getBounds2D();
            java.awt.geom.Rectangle2D shape2 = p2.createTransformedAWTShape().getBounds2D();

            double x1 = shape1.getX();
            double y1 = shape1.getY();
            double x2 = shape2.getX();
            double y2 = shape2.getY();

            roi = new ij.gui.RotatedRectRoi(x1, y1, x2, y2, getWidth());
        }
        copyToIJRoi(roi);
        return roi;
    }

}
