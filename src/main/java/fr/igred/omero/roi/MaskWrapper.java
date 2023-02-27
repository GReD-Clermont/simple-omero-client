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
import omero.gateway.model.MaskData;

import java.awt.geom.AffineTransform;


/**
 * Class containing an MaskData.
 * <p> Wraps function calls to the MaskData contained.
 */
public class MaskWrapper extends ShapeWrapper<MaskData> implements Mask {


    /**
     * Constructor of the MaskWrapper class using a MaskData.
     *
     * @param mask The MaskData to wrap.
     */
    public MaskWrapper(MaskData mask) {
        super(mask);
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
     * Returns the x-coordinate of the rectangular shape.
     *
     * @return See above.
     */
    @Override
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate of the rectangular shape.
     *
     * @param x See above.
     */
    @Override
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y coordinate of the rectangular shape.
     *
     * @return See above.
     */
    @Override
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate of the rectangular shape.
     *
     * @param y See above.
     */
    @Override
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Returns the width of the rectangular shape.
     *
     * @return See above.
     */
    @Override
    public double getWidth() {
        return data.getWidth();
    }


    /**
     * Sets width of the rectangular shape.
     *
     * @param width See above.
     */
    @Override
    public void setWidth(double width) {
        data.setWidth(width);
    }


    /**
     * Returns the height of the rectangular shape.
     *
     * @return See above.
     */
    @Override
    public double getHeight() {
        return data.getHeight();
    }


    /**
     * Sets the height of the rectangular shape.
     *
     * @param height See above.
     */
    @Override
    public void setHeight(double height) {
        data.setHeight(height);
    }


    /**
     * Returns the mask image.
     *
     * @return See above.
     */
    @Override
    public int[][] getMaskAsBinaryArray() {
        return data.getMaskAsBinaryArray();
    }


    /**
     * Returns the mask as a byte array.
     *
     * @return See above.
     */
    @Override
    public byte[] getMask() {
        return data.getMask();
    }


    /**
     * Sets the mask image.
     *
     * @param mask See above.
     */
    @Override
    public void setMask(byte[] mask) {
        data.setMask(mask);
    }


    /**
     * Sets the mask
     *
     * @param mask The binary mask (int[width][height])
     */
    @Override
    public void setMask(int[][] mask) {
        data.setMask(mask);
    }


    /**
     * Sets the mask
     *
     * @param mask The binary mask (boolean[width][height])
     */
    @Override
    public void setMask(boolean[][] mask) {
        data.setMask(mask);
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
