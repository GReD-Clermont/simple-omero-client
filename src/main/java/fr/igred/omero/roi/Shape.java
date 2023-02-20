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


import fr.igred.omero.RemoteObject;
import ij.gui.Roi;
import omero.gateway.model.ShapeData;
import omero.model.AffineTransform;

import java.awt.Color;


/**
 * Generic interface to handle Shape objects.
 *
 * @param <T> Subclass of {@link ShapeData}
 */
public interface Shape<T extends ShapeData> extends RemoteObject<T> {

    /**
     * Gets the channel.
     *
     * @return the channel. -1 if the shape applies to all channels of the image.
     */
    int getC();


    /**
     * Sets the channel.
     *
     * @param c the channel. Pass -1 to remove z value, i.e. shape applies to all channels of the image.
     */
    void setC(int c);


    /**
     * Gets the z-section.
     *
     * @return the z-section. -1 if the shape applies to all z-sections of the image.
     */
    int getZ();


    /**
     * Sets the z-section.
     *
     * @param z the z-section. Pass -1 to remove z value, i.e. shape applies to all z-sections of the image.
     */
    void setZ(int z);


    /**
     * Sets the time-point.
     *
     * @return the time-point. -1 if the shape applies to all time-points of the image.
     */
    int getT();


    /**
     * Sets the time-point.
     *
     * @param t the time-point. Pass -1 to remove t value, i.e. shape applies to all time-points of the image.
     */
    void setT(int t);


    /**
     * Sets the channel, z-section and time-point at once.
     *
     * @param c the channel. Pass -1 to remove z value, i.e. shape applies to all channels of the image.
     * @param z the z-section. Pass -1 to remove z value, i.e. shape applies to all z-sections of the image.
     * @param t the time-point. Pass -1 to remove t value, i.e. shape applies to all time-points of the image.
     */
    void setCZT(int c, int z, int t);


    /**
     * Returns the C,Z,T positions as a comma-delimited String.
     *
     * @return See above.
     */
    String getCZT();


    /**
     * Gets ShapeData font size.
     *
     * @return The font size (in typography points)
     */
    double getFontSize();


    /**
     * Sets ShapeData font size.
     *
     * @param value The font size (in typography points)
     */
    void setFontSize(double value);


    /**
     * Gets the ShapeData stroke color.
     *
     * @return The stroke color
     */
    Color getStroke();


    /**
     * Sets ShapeData stroke color.
     *
     * @param strokeColour The stroke color
     */
    void setStroke(Color strokeColour);


    /**
     * Gets ShapeData fill color.
     *
     * @return The fill color
     */
    Color getFill();


    /**
     * Sets the ShapeData fill color.
     *
     * @param fillColour The fill color
     */
    void setFill(Color fillColour);


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
    void setTransform(double a00, double a10, double a01, double a11, double a02, double a12);


    /**
     * Sets the transform from a {@link java.awt.geom.AffineTransform}.
     *
     * @param transform A Java AffineTransform.
     */
    void setTransform(java.awt.geom.AffineTransform transform);


    /**
     * Converts {@link omero.model.AffineTransform} to {@link java.awt.geom.AffineTransform}.
     *
     * @return The converted affine transform.
     */
    java.awt.geom.AffineTransform toAWTTransform();


    /**
     * Returns a new {@link java.awt.Shape} defined by the geometry of the specified Shape after it has been transformed
     * by the transform.
     *
     * @return A new transformed {@link java.awt.Shape}.
     */
    java.awt.Shape createTransformedAWTShape();


    /**
     * Returns a new {@link RectangleWrapper} corresponding to the bounding box of the shape, once the related
     * {@link AffineTransform} has been applied.
     *
     * @return The bounding box.
     */
    @SuppressWarnings("ClassReferencesSubclass")
    Rectangle getBoundingBox();


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    Roi toImageJ();

}
