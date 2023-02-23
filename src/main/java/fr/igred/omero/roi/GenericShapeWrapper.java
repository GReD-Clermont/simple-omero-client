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


import fr.igred.omero.GenericObjectWrapper;
import ij.gui.Line;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ome.model.units.BigResult;
import omero.gateway.model.ShapeData;
import omero.model.AffineTransform;
import omero.model.AffineTransformI;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Generic class containing a ShapeData (or a subclass) object.
 *
 * @param <T> Subclass of {@link ShapeData}
 */
public abstract class GenericShapeWrapper<T extends ShapeData> extends GenericObjectWrapper<T> {

    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);


    /**
     * Constructor of the GenericShapeWrapper class using a ShapeData.
     *
     * @param s The shape.
     */
    protected GenericShapeWrapper(T s) {
        super(s);
    }


    /**
     * Converts an IJ roi to a list of shapes.
     *
     * @param ijRoi An ImageJ ROI.
     *
     * @return A list of ShapeWrappers.
     */
    static ShapeList fromImageJ(ij.gui.Roi ijRoi) {
        ShapeList list = new ShapeList();
        int       type = ijRoi.getType();
        switch (type) {
            case Roi.FREEROI:
            case Roi.TRACED_ROI:
            case Roi.POLYGON:
                list.add(new PolygonWrapper(ijRoi));
                break;
            case Roi.FREELINE:
            case Roi.ANGLE:
            case Roi.POLYLINE:
                list.add(new PolylineWrapper(ijRoi));
                break;
            case Roi.LINE:
                list.add(new LineWrapper((Line) ijRoi));
                break;
            case Roi.OVAL:
                list.add(new EllipseWrapper(ijRoi));
                break;
            case Roi.POINT:
                float[] x = ijRoi.getFloatPolygon().xpoints;
                float[] y = ijRoi.getFloatPolygon().ypoints;

                Collection<PointWrapper> points = new LinkedList<>();
                for (int i = 0; i < x.length; i++) {
                    points.add(new PointWrapper(x[i], y[i]));
                }
                points.forEach(p -> p.setText(ijRoi.getName()));
                points.forEach(p -> p.copyFromIJRoi(ijRoi));
                list.addAll(points);
                break;
            case Roi.COMPOSITE:
                List<ij.gui.Roi> rois = Arrays.asList(((ShapeRoi) ijRoi).getRois());
                rois.forEach(r -> r.setName(ijRoi.getName()));
                rois.forEach(r -> r.setPosition(ijRoi.getCPosition(),
                                                ijRoi.getZPosition(),
                                                ijRoi.getTPosition()));
                rois.stream().map(GenericShapeWrapper::fromImageJ).forEach(list::addAll);
                break;
            default:
                if (ijRoi instanceof TextRoi)
                    list.add(new TextWrapper((TextRoi) ijRoi));
                else
                    list.add(new RectangleWrapper(ijRoi));
                break;
        }
        return list;
    }


    /**
     * Copies details from an ImageJ ROI (position, stroke color, stroke width).
     *
     * @param ijRoi An ImageJ Roi.
     */
    protected void copyFromIJRoi(ij.gui.Roi ijRoi) {
        data.setC(Math.max(-1, ijRoi.getCPosition() - 1));
        data.setZ(Math.max(-1, ijRoi.getZPosition() - 1));
        data.setT(Math.max(-1, ijRoi.getTPosition() - 1));
        LengthI size          = new LengthI(ijRoi.getStrokeWidth(), UnitsLength.POINT);
        Color   defaultStroke = Optional.ofNullable(Roi.getColor()).orElse(Color.YELLOW);
        Color   defaultFill   = Optional.ofNullable(Roi.getDefaultFillColor()).orElse(TRANSPARENT);
        Color   stroke        = Optional.ofNullable(ijRoi.getStrokeColor()).orElse(defaultStroke);
        Color   fill          = Optional.ofNullable(ijRoi.getFillColor()).orElse(defaultFill);
        data.getShapeSettings().setStrokeWidth(size);
        data.getShapeSettings().setStroke(stroke);
        data.getShapeSettings().setFill(fill);
    }


    /**
     * Copies details to an ImageJ ROI (name, position, stroke color, fill color, stroke width).
     *
     * @param ijRoi An ImageJ Roi.
     */
    protected void copyToIJRoi(ij.gui.Roi ijRoi) {
        ijRoi.setName(getText());
        ijRoi.setStrokeColor(getStroke());
        Color fill = getFill();
        if (!TRANSPARENT.equals(fill)) ijRoi.setFillColor(getFill());
        int c = Math.max(0, getC() + 1);
        int z = Math.max(0, getZ() + 1);
        int t = Math.max(0, getT() + 1);
        ijRoi.setPosition(c, z, t);
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
     * @param c the channel. Pass -1 to remove z value, i.e. shape applies to all channels of the image.
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
     * @param z the z-section. Pass -1 to remove z value, i.e. shape applies to all z-sections of the image.
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
     * @param t the time-point. Pass -1 to remove t value, i.e. shape applies to all time-points of the image.
     */
    public void setT(int t) {
        this.data.setT(t);
    }


    /**
     * Sets the channel, z-section and time-point at once.
     *
     * @param c the channel. Pass -1 to remove z value, i.e. shape applies to all channels of the image.
     * @param z the z-section. Pass -1 to remove z value, i.e. shape applies to all z-sections of the image.
     * @param t the time-point. Pass -1 to remove t value, i.e. shape applies to all time-points of the image.
     */
    public void setCZT(int c, int z, int t) {
        setC(c);
        setZ(z);
        setT(t);
    }


    /**
     * Returns the C,Z,T positions as a comma-delimited String.
     *
     * @return See above.
     */
    String getCZT() {
        return String.format("%d,%d,%d", getC(), getZ(), getT());
    }


    /**
     * Gets ShapeData font size.
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
     * Sets ShapeData font size.
     *
     * @param value The font size (in typography points)
     */
    public void setFontSize(double value) {
        LengthI size = new LengthI(value, UnitsLength.POINT);
        data.getShapeSettings().setFontSize(size);
    }


    /**
     * Gets the ShapeData stroke color.
     *
     * @return The stroke color
     */
    public Color getStroke() {
        return data.getShapeSettings().getStroke();
    }


    /**
     * Sets ShapeData stroke color.
     *
     * @param strokeColour The stroke color
     */
    public void setStroke(Color strokeColour) {
        data.getShapeSettings().setStroke(strokeColour);
    }


    /**
     * Gets ShapeData fill color.
     *
     * @return The fill color
     */
    public Color getFill() {
        return data.getShapeSettings().getFill();
    }


    /**
     * Sets the ShapeData fill color.
     *
     * @param fillColour The fill color
     */
    public void setFill(Color fillColour) {
        data.getShapeSettings().setFill(fillColour);
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
        if (data.getTransform() == null) return new java.awt.geom.AffineTransform();
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
        boundingBox.setFill(getFill());
        boundingBox.setFontSize(getFontSize());
        return boundingBox;
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    public Roi toImageJ() {
        Roi roi = new ij.gui.ShapeRoi(createTransformedAWTShape()).trySimplify();
        copyToIJRoi(roi);
        return roi;
    }

}
