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


import fr.igred.omero.GenericObjectWrapper;
import ij.gui.Roi;
import ome.model.units.BigResult;
import omero.gateway.model.ShapeData;
import omero.model.AffineTransform;
import omero.model.AffineTransformI;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Generic class containing a ShapeData (or a subclass) object.
 *
 * @param <T> Subclass of {@link ShapeData}
 */
public abstract class GenericShapeWrapper<T extends ShapeData> extends GenericObjectWrapper<T> {


    /**
     * Constructor of the GenericShapeWrapper class using a ShapeData.
     *
     * @param shape the shape
     */
    protected GenericShapeWrapper(T shape) {
        super(shape);
    }


    /**
     * Gets the ShapeData object contained.
     *
     * @return the shape.
     */
    public T asShapeData() {
        return data;
    }


    /**
     * Gets the channel.
     *
     * @return the channel. -1 if the shape applies to all channels of the image.
     */
    public int getC() {
        return this.data.getC();
    }


    /**
     * Sets the channel.
     *
     * @param c the channel. Pass -1 to remove z value, i. e. shape applies to all channels of the image.
     */
    public void setC(int c) {
        this.data.setC(c);
    }


    /**
     * Gets the z-section.
     *
     * @return the z-section. -1 if the shape applies to all z-sections of the image.
     */
    public int getZ() {
        return this.data.getZ();
    }


    /**
     * Sets the z-section.
     *
     * @param z the z-section. Pass -1 to remove z value, i. e. shape applies to all z-sections of the image.
     */
    public void setZ(int z) {
        this.data.setZ(z);
    }


    /**
     * Sets the time-point.
     *
     * @return the time-point. -1 if the shape applies to all time-points of the image.
     */
    public int getT() {
        return this.data.getT();
    }


    /**
     * Sets the time-point.
     *
     * @param t the time-point. Pass -1 to remove t value, i. e. shape applies to all time-points of the image.
     */
    public void setT(int t) {
        this.data.setT(t);
    }


    /**
     * Sets the channel, z-section and time-point at once.
     *
     * @param c the channel. Pass -1 to remove z value, i. e. shape applies to all channels of the image.
     * @param z the z-section. Pass -1 to remove z value, i. e. shape applies to all z-sections of the image.
     * @param t the time-point. Pass -1 to remove t value, i. e. shape applies to all time-points of the image.
     */
    public void setCZT(int c, int z, int t) {
        setC(c);
        setZ(z);
        setT(t);
    }


    /**
     * Gets ShapeData font size
     *
     * @return The font size (in typography points)
     */
    public double getFontSize() {
        double fontSize = Double.NaN;
        try {
            fontSize = data.getShapeSettings().getFontSize(UnitsLength.POINT).getValue();
        } catch (BigResult bigResult) {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Error while getting font size from ShapeData.", bigResult);
        }
        return fontSize;
    }


    /**
     * Sets ShapeData font size
     *
     * @param value The font size (in typography points)
     */
    public void setFontSize(double value) {
        LengthI size = new LengthI(value, UnitsLength.POINT);
        data.getShapeSettings().setFontSize(size);
    }


    /**
     * Sets ShapeData stroke color
     *
     * @return The stroke color
     */
    public Color getStroke() {
        return data.getShapeSettings().getStroke();
    }


    /**
     * Sets ShapeData stroke color
     *
     * @param color The stroke color
     */
    public void setStroke(Color color) {
        data.getShapeSettings().setStroke(color);
    }


    /**
     * Gets the text on the ShapeData.
     *
     * @return the text
     */
    public abstract String getText();


    /**
     * Sets the text on the ShapeData.
     *
     * @param text the text
     */
    public abstract void setText(String text);


    /**
     * Converts the shape to an {@link java.awt.Shape}.
     *
     * @return The converted AWT Shape.
     */
    public abstract java.awt.Shape toAWTShape();


    /**
     * Sets the transform to the matrix specified by the 6 double precision values.
     *
     * @param a00 the X coordinate scaling element of the 3x3 matrix
     * @param a10 the Y coordinate shearing element of the 3x3 matrix
     * @param a01 the X coordinate shearing element of the 3x3 matrix
     * @param a11 the Y coordinate scaling element of the 3x3 matrix
     * @param a02 the X coordinate translation element of the 3x3 matrix
     * @param a12 the Y coordinate translation element of the 3x3 matrix
     */
    public void setTransform(double a00, double a10, double a01, double a11, double a02, double a12) {
        AffineTransform transform = new AffineTransformI();
        transform.setA00(omero.rtypes.rdouble(a00));
        transform.setA10(omero.rtypes.rdouble(a10));
        transform.setA01(omero.rtypes.rdouble(a01));
        transform.setA11(omero.rtypes.rdouble(a11));
        transform.setA02(omero.rtypes.rdouble(a02));
        transform.setA12(omero.rtypes.rdouble(a12));
        data.setTransform(transform);
    }


    /**
     * Sets the transform from a {@link java.awt.geom.AffineTransform}.
     *
     * @param transform A Java AffineTransform.
     */
    public void setTransform(java.awt.geom.AffineTransform transform) {
        double[] a = new double[6];
        transform.getMatrix(a);
        setTransform(a[0], a[1], a[2], a[3], a[4], a[5]);
    }


    /**
     * Converts {@link omero.model.AffineTransform} to {@link java.awt.geom.AffineTransform}.
     *
     * @return The converted affine transform.
     */
    public java.awt.geom.AffineTransform toAWTTransform() {
        if (data.getTransform() == null) return null;
        else {
            double a00 = data.getTransform().getA00().getValue();
            double a10 = data.getTransform().getA10().getValue();
            double a01 = data.getTransform().getA01().getValue();
            double a11 = data.getTransform().getA11().getValue();
            double a02 = data.getTransform().getA02().getValue();
            double a12 = data.getTransform().getA12().getValue();
            return new java.awt.geom.AffineTransform(a00, a10, a01, a11, a02, a12);
        }
    }


    /**
     * Returns a new {@link java.awt.Shape} defined by the geometry of the specified Shape after it has been transformed
     * by the transform.
     *
     * @return A new transformed {@link java.awt.Shape}.
     */
    public java.awt.Shape createTransformedAWTShape() {
        if (toAWTTransform() == null) return toAWTShape();
        else return toAWTTransform().createTransformedShape(toAWTShape());
    }


    /**
     * Returns a new {@link RectangleWrapper} corresponding to the bounding box of the shape, once the related {@link
     * AffineTransform} has been applied.
     *
     * @return The bounding box.
     */
    public RectangleWrapper getBoundingBox() {
        Rectangle2D rectangle = createTransformedAWTShape().getBounds2D();

        double x      = rectangle.getX();
        double y      = rectangle.getY();
        double width  = rectangle.getWidth();
        double height = rectangle.getHeight();

        RectangleWrapper boundingBox = new RectangleWrapper(x, y, width, height);
        boundingBox.setCZT(getC(), getZ(), getT());
        boundingBox.setText(getText() + " (Bounding Box)");
        boundingBox.setStroke(getStroke());
        boundingBox.setFontSize(getFontSize());
        return boundingBox;
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    public Roi toImageJ() {
        ij.gui.ShapeRoi roi = new ij.gui.ShapeRoi(createTransformedAWTShape());
        roi.setStrokeColor(getStroke());
        int c = Math.max(0, getC() + 1);
        int z = Math.max(0, getZ() + 1);
        int t = Math.max(0, getT() + 1);
        roi.setPosition(c, z, t);
        return roi;
    }

}
