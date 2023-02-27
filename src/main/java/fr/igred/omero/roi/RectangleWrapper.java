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
public class RectangleWrapper extends ShapeWrapper<RectangleData> implements Rectangle {


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
     * Returns the width untransformed rectangle.
     *
     * @return See above.
     */
    @Override
    public double getWidth() {
        return data.getWidth();
    }


    /**
     * Sets width of an untransformed rectangle.
     *
     * @param width See above.
     */
    @Override
    public void setWidth(double width) {
        data.setWidth(width);
    }


    /**
     * Returns the height untransformed rectangle.
     *
     * @return See above.
     */
    @Override
    public double getHeight() {
        return data.getHeight();
    }


    /**
     * Sets the height of an untransformed rectangle.
     *
     * @param height See above.
     */
    @Override
    public void setHeight(double height) {
        data.setHeight(height);
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
            Shape p1 = new PointWrapper(getX(), getY() + getHeight() / 2);
            Shape p2 = new PointWrapper(getX() + getWidth(), getY() + getHeight() / 2);
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
