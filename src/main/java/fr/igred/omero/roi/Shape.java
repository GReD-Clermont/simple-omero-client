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


import fr.igred.omero.RemoteObject;
import ij.gui.Roi;
import ome.model.units.BigResult;
import omero.gateway.model.DataObject;
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
 * Generic interface to handle data related to a ShapeData (or a subclass) object (expected to be wrapped by
 * instances).
 *
 * @param <T> Subclass of {@link DataObject}
 */
public interface Shape<T extends ShapeData> extends RemoteObject<T> {

    /**
     * Gets the channel.
     *
     * @return the channel. -1 if the shape applies to all channels of the image.
     */
    default int getC() {
        return asDataObject().getC();
    }


    /**
     * Sets the channel.
     *
     * @param c the channel. Pass -1 to remove z value, i.e. shape applies to all channels of the image.
     */
    default void setC(int c) {
        asDataObject().setC(c);
    }


    /**
     * Gets the z-section.
     *
     * @return the z-section. -1 if the shape applies to all z-sections of the image.
     */
    default int getZ() {
        return asDataObject().getZ();
    }


    /**
     * Sets the z-section.
     *
     * @param z the z-section. Pass -1 to remove z value, i.e. shape applies to all z-sections of the image.
     */
    default void setZ(int z) {
        asDataObject().setZ(z);
    }


    /**
     * Sets the time-point.
     *
     * @return the time-point. -1 if the shape applies to all time-points of the image.
     */
    default int getT() {
        return asDataObject().getT();
    }


    /**
     * Sets the time-point.
     *
     * @param t the time-point. Pass -1 to remove t value, i.e. shape applies to all time-points of the image.
     */
    default void setT(int t) {
        asDataObject().setT(t);
    }


    /**
     * Sets the channel, z-section and time-point at once.
     *
     * @param c the channel. Pass -1 to remove z value, i.e. shape applies to all channels of the image.
     * @param z the z-section. Pass -1 to remove z value, i.e. shape applies to all z-sections of the image.
     * @param t the time-point. Pass -1 to remove t value, i.e. shape applies to all time-points of the image.
     */
    default void setCZT(int c, int z, int t) {
        setC(c);
        setZ(z);
        setT(t);
    }


    /**
     * Returns the C,Z,T positions as a comma-delimited String.
     *
     * @return See above.
     */
    default String getCZT() {
        return String.format("%d,%d,%d", getC(), getZ(), getT());
    }


    /**
     * Gets ShapeData font size.
     *
     * @return The font size (in typography points)
     */
    default double getFontSize() {
        double fontSize = Double.NaN;
        try {
            fontSize = asDataObject().getShapeSettings().getFontSize(UnitsLength.POINT).getValue();
        } catch (BigResult bigResult) {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Error while getting font size from ShapeData.", bigResult);
        }
        return fontSize;
    }


    /**
     * Sets ShapeData font size.
     *
     * @param value The font size (in typography points)
     */
    default void setFontSize(double value) {
        LengthI size = new LengthI(value, UnitsLength.POINT);
        asDataObject().getShapeSettings().setFontSize(size);
    }


    /**
     * Gets the ShapeData stroke color.
     *
     * @return The stroke color
     */
    default Color getStroke() {
        return asDataObject().getShapeSettings().getStroke();
    }


    /**
     * Sets ShapeData stroke color.
     *
     * @param strokeColour The stroke color
     */
    default void setStroke(Color strokeColour) {
        asDataObject().getShapeSettings().setStroke(strokeColour);
    }


    /**
     * Gets ShapeData fill color.
     *
     * @return The fill color
     */
    default Color getFill() {
        return asDataObject().getShapeSettings().getFill();
    }


    /**
     * Sets the ShapeData fill color.
     *
     * @param fillColour The fill color
     */
    default void setFill(Color fillColour) {
        asDataObject().getShapeSettings().setFill(fillColour);
    }


    /**
     * Gets the text on the ShapeData.
     *
     * @return the text
     */
    String getText();


    /**
     * Sets the text on the ShapeData.
     *
     * @param text the text
     */
    void setText(String text);


    /**
     * Converts the shape to an {@link java.awt.Shape}.
     *
     * @return The converted AWT Shape.
     */
    java.awt.Shape toAWTShape();


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
    default void setTransform(double a00, double a10, double a01, double a11, double a02, double a12) {
        AffineTransform transform = new AffineTransformI();
        transform.setA00(omero.rtypes.rdouble(a00));
        transform.setA10(omero.rtypes.rdouble(a10));
        transform.setA01(omero.rtypes.rdouble(a01));
        transform.setA11(omero.rtypes.rdouble(a11));
        transform.setA02(omero.rtypes.rdouble(a02));
        transform.setA12(omero.rtypes.rdouble(a12));
        asDataObject().setTransform(transform);
    }


    /**
     * Sets the transform from a {@link java.awt.geom.AffineTransform}.
     *
     * @param transform A Java AffineTransform.
     */
    default void setTransform(java.awt.geom.AffineTransform transform) {
        double[] a = new double[6];
        transform.getMatrix(a);
        setTransform(a[0], a[1], a[2], a[3], a[4], a[5]);
    }


    /**
     * Converts {@link AffineTransform} to {@link java.awt.geom.AffineTransform}.
     *
     * @return The converted affine transform.
     */
    default java.awt.geom.AffineTransform toAWTTransform() {
        if (asDataObject().getTransform() == null) return new java.awt.geom.AffineTransform();
        else {
            double a00 = asDataObject().getTransform().getA00().getValue();
            double a10 = asDataObject().getTransform().getA10().getValue();
            double a01 = asDataObject().getTransform().getA01().getValue();
            double a11 = asDataObject().getTransform().getA11().getValue();
            double a02 = asDataObject().getTransform().getA02().getValue();
            double a12 = asDataObject().getTransform().getA12().getValue();
            return new java.awt.geom.AffineTransform(a00, a10, a01, a11, a02, a12);
        }
    }


    /**
     * Returns a new {@link java.awt.Shape} defined by the geometry of the specified Shape after it has been transformed
     * by the transform.
     *
     * @return A new transformed {@link java.awt.Shape}.
     */
    default java.awt.Shape createTransformedAWTShape() {
        if (toAWTTransform().getType() == java.awt.geom.AffineTransform.TYPE_IDENTITY) return toAWTShape();
        else return toAWTTransform().createTransformedShape(toAWTShape());
    }


    /**
     * Returns a new {@link RectangleWrapper} corresponding to the bounding box of the shape, once the related
     * {@link AffineTransform} has been applied.
     *
     * @return The bounding box.
     */
    @SuppressWarnings("ClassReferencesSubclass")
    default Rectangle getBoundingBox() {
        Rectangle2D rectangle = createTransformedAWTShape().getBounds2D();

        double x      = rectangle.getX();
        double y      = rectangle.getY();
        double width  = rectangle.getWidth();
        double height = rectangle.getHeight();

        Rectangle boundingBox = new RectangleWrapper(x, y, width, height);
        boundingBox.setCZT(getC(), getZ(), getT());
        boundingBox.setText(getText() + " (Bounding Box)");
        boundingBox.setStroke(getStroke());
        boundingBox.setFill(getFill());
        boundingBox.setFontSize(getFontSize());
        return boundingBox;
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    default Roi toImageJ() {
        Roi roi = new ij.gui.ShapeRoi(createTransformedAWTShape()).trySimplify();
        roi.setName(getText());
        roi.setStrokeColor(getStroke());
        roi.setFillColor(getFill());
        int c = Math.max(0, getC() + 1);
        int z = Math.max(0, getZ() + 1);
        int t = Math.max(0, getT() + 1);
        roi.setPosition(c, z, t);
        return roi;
    }

}
