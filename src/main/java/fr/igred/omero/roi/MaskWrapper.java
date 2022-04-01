/*
 *  Copyright (C) 2020-2021 GReD
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
import omero.gateway.model.MaskData;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;


public class MaskWrapper extends GenericShapeWrapper<MaskData> {


    /**
     * Constructor of the MaskWrapper class using a MaskData.
     *
     * @param shape the shape
     */
    public MaskWrapper(MaskData shape) {
        super(shape);
    }


    /**
     * Constructor of the MaskWrapper class using a new empty MaskData.
     */
    public MaskWrapper() {
        this(new MaskData());
    }


    /**
     * Constructor of the MaskWrapper class using a new MaskData.
     *
     * @param x      The x-coordinate of the top-left corner of the image.
     * @param y      The y-coordinate of the top-left corner of the image.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @param mask   The mask image.
     */
    public MaskWrapper(double x, double y, double width, double height, byte[] mask) {
        this(new MaskData(x, y, width, height, mask));
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
     * Returns the x-coordinate of the top-left corner of the mask.
     *
     * @return See above.
     */
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate top-left corner of an untransformed mask.
     *
     * @param x The value to set.
     */
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y-coordinate of the top-left corner of the mask.
     *
     * @return See above.
     */
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate top-left corner of an untransformed mask.
     *
     * @param y See above.
     */
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Returns the width of the mask.
     *
     * @return See above.
     */
    public double getWidth() {
        return data.getWidth();
    }


    /**
     * Sets the width of an untransformed mask.
     *
     * @param width See above.
     */
    public void setWidth(double width) {
        data.setWidth(width);
    }


    /**
     * Returns the height of the mask.
     *
     * @return See above.
     */
    public double getHeight() {
        return data.getHeight();
    }


    /**
     * Sets the height of an untransformed mask.
     *
     * @param height See above.
     */
    public void setHeight(double height) {
        data.setHeight(height);
    }


    /**
     * Returns the mask image.
     *
     * @return See above.
     */
    public int[][] getMaskAsBinaryArray() {
        return data.getMaskAsBinaryArray();
    }


    /**
     * Returns the mask as a byte array.
     *
     * @return See above.
     */
    public byte[] getMask() {
        return data.getMask();
    }


    /**
     * Sets the mask image.
     *
     * @param mask See above.
     */
    public void setMask(byte[] mask) {
        data.setMask(mask);
    }


    /**
     * Sets the mask
     *
     * @param mask The binary mask (int[width][height])
     */
    public void setMask(int[][] mask) {
        data.setMask(mask);
    }


    /**
     * Sets the mask
     *
     * @param mask The binary mask (boolean[width][height])
     */
    public void setMask(boolean[][] mask) {
        data.setMask(mask);
    }


    /**
     * Sets the coordinates of the MaskData shape.
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
     * Gets the coordinates of the MaskData shape.
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
     * Sets the coordinates of the MaskData shape.
     *
     * @param coordinates Array of coordinates containing {X,Y,Width,Height}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("MaskData cannot set null coordinates.");
        } else if (coordinates.length == 4) {
            data.setX(coordinates[0]);
            data.setY(coordinates[1]);
            data.setWidth(coordinates[2]);
            data.setHeight(coordinates[3]);
        } else {
            throw new IllegalArgumentException("4 coordinates required for MaskData.");
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
        if (transform == null) {
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
        roi.setStrokeColor(getStroke());
        int c = Math.max(0, getC() + 1);
        int z = Math.max(0, getZ() + 1);
        int t = Math.max(0, getT() + 1);
        roi.setPosition(c, z, t);
        return roi;
    }

}
