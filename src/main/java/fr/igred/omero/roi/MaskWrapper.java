/*
 *  Copyright (C) 2020-2024 GReD
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


import ij.gui.ImageRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import omero.gateway.model.MaskData;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;


/**
 * Class containing an MaskData.
 * <p> Wraps function calls to the MaskData contained.
 */
public class MaskWrapper extends ShapeWrapper<MaskData> implements Mask {


    private static final double MAX_UINT8 = 255.0;


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
     * Constructor of the MaskWrapper class using an ImageJ ImageRoi.
     *
     * @param imageRoi An ImageJ ImageRoi.
     */
    public MaskWrapper(ImageRoi imageRoi) {
        this();
        data.setX(imageRoi.getXBase());
        data.setY(imageRoi.getYBase());
        data.setWidth(imageRoi.getFloatWidth());
        data.setHeight(imageRoi.getFloatHeight());

        ImageProcessor ip = imageRoi.getProcessor();
        ip.flipVertical();
        data.setMask(ip.getIntArray());
        ip.flipVertical();

        Color lut = new Color(ip.getCurrentColorModel()
                                .getRGB((int) ip.getMax()));
        int r = lut.getRed();
        int g = lut.getGreen();
        int b = lut.getBlue();
        int a = (int) (imageRoi.getOpacity() * MAX_UINT8);

        data.setText(imageRoi.getName());
        super.copyFromIJRoi(imageRoi);
        data.getShapeSettings().setFill(new Color(r, g, b, a));

        // Always 0 as long as ImageRoi::getAngle() method is not updated
        double          angle     = StrictMath.toRadians(-imageRoi.getAngle());
        AffineTransform transform = new AffineTransform();
        transform.rotate(angle, imageRoi.getXBase(), imageRoi.getYBase());
        super.setTransform(transform);
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
     * Returns the x-coordinate of the top-left corner of the mask.
     *
     * @return See above.
     */
    @Override
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate top-left corner of an untransformed mask.
     *
     * @param x See above.
     */
    @Override
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y-coordinate of the top-left corner of the mask.
     *
     * @return See above.
     */
    @Override
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate top-left corner of an untransformed mask.
     *
     * @param y See above.
     */
    @Override
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Returns the width of the mask.
     *
     * @return See above.
     */
    @Override
    public double getWidth() {
        return data.getWidth();
    }


    /**
     * Sets the width of an untransformed mask.
     *
     * @param width See above.
     */
    @Override
    public void setWidth(double width) {
        data.setWidth(width);
    }


    /**
     * Returns the height of the mask.
     *
     * @return See above.
     */
    @Override
    public double getHeight() {
        return data.getHeight();
    }


    /**
     * Sets the height of an untransformed mask.
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
     * Returns the mask image.
     *
     * @return See above.
     */
    @Override
    public BufferedImage getMaskAsBufferedImage() {
        return data.getMaskAsBufferedImage();
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
            int      x      = (int) getX();
            int      y      = (int) getY();
            ImageRoi imgRoi = new ImageRoi(x, y, getMaskAsBufferedImage());
            imgRoi.setZeroTransparent(true);
            imgRoi.setOpacity(getFill().getAlpha() / MAX_UINT8);
            roi = imgRoi;
        } else {
            Point p1 = new PointWrapper(getX(), getY() + getHeight() / 2);
            Point p2 = new PointWrapper(getX() + getWidth(), getY() + getHeight() / 2);
            p1.setTransform(transform);
            p2.setTransform(transform);

            Rectangle2D shape1 = p1.createTransformedAWTShape().getBounds2D();
            Rectangle2D shape2 = p2.createTransformedAWTShape().getBounds2D();

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
