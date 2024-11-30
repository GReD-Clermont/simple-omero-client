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


import fr.igred.omero.Annotatable;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ij.gui.Roi;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ShapeData;
import omero.model.AffineTransform;
import omero.model.ShapeAnnotationLink;
import omero.model.ShapeAnnotationLinkI;

import java.awt.Color;
import java.util.concurrent.ExecutionException;

import static java.awt.geom.AffineTransform.TYPE_IDENTITY;


/**
 * Generic interface to handle Shape objects.
 */
public interface Shape extends Annotatable {

    /** Annotation link name for this type of object */
    String ANNOTATION_LINK = "ShapeAnnotationLink";

    /** Default IJ property to store shape ID. */
    String IJ_ID_PROPERTY = "SHAPE_ID";


    /**
     * Returns a ShapeData corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    ShapeData asDataObject();


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
     * @param c the channel. Pass -1 to remove c value, i.e. shape applies to all channels of the image.
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
    default void setTransform(java.awt.geom.AffineTransform transform) {
        double[] a = new double[6];
        transform.getMatrix(a);
        setTransform(a[0], a[1], a[2], a[3], a[4], a[5]);
    }


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
    default java.awt.Shape createTransformedAWTShape() {
        if (toAWTTransform().getType() == TYPE_IDENTITY) {
            return toAWTShape();
        } else {
            return toAWTTransform().createTransformedShape(toAWTShape());
        }
    }


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


    /**
     * Attach an {@link AnnotationData} to this object.
     *
     * @param dm         The data manager.
     * @param annotation The {@link AnnotationData}.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default <A extends AnnotationData> void link(DataManager dm, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        ShapeAnnotationLink link = new ShapeAnnotationLinkI();
        link.setChild(annotation.asAnnotation());
        link.setParent((omero.model.Shape) asDataObject().asIObject());
        dm.save(link);
    }

}
