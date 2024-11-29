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


import fr.igred.omero.AnnotatableWrapper;
import fr.igred.omero.client.ClientImpl;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Line;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ome.model.units.BigResult;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ShapeData;
import omero.model.AffineTransform;
import omero.model.AffineTransformI;
import omero.model.LengthI;
import omero.model.ShapeAnnotationLink;
import omero.model.ShapeAnnotationLinkI;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static java.awt.Color.YELLOW;
import static java.awt.Font.BOLD;
import static java.awt.Font.ITALIC;
import static java.awt.geom.AffineTransform.TYPE_IDENTITY;
import static java.util.logging.Level.WARNING;
import static omero.gateway.model.ShapeSettingsData.FONT_BOLD;
import static omero.gateway.model.ShapeSettingsData.FONT_BOLD_ITALIC;
import static omero.gateway.model.ShapeSettingsData.FONT_ITALIC;
import static omero.gateway.model.ShapeSettingsData.FONT_REGULAR;
import static omero.model.enums.UnitsLength.POINT;


/**
 * Generic class containing a ShapeData (or a subclass) object.
 *
 * @param <T> Subclass of {@link ShapeData}
 */
public abstract class ShapeWrapper<T extends ShapeData> extends AnnotatableWrapper<T> {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "ShapeAnnotationLink";

    /** Default IJ property to store shape ID. */
    public static final String IJ_ID_PROPERTY = "SHAPE_ID";

    /** Transparent color */
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);


    /**
     * Constructor of the ShapeWrapper class using a ShapeData.
     *
     * @param s The shape.
     */
    protected ShapeWrapper(T s) {
        super(s);
    }


    /**
     * Converts an IJ roi to a list of shapes.
     *
     * @param ijRoi An ImageJ ROI.
     *
     * @return A list of shapes.
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
                rois.stream().map(ShapeWrapper::fromImageJ).forEach(list::addAll);
                break;
            default:
                if (ijRoi instanceof TextRoi) {
                    list.add(new TextWrapper((TextRoi) ijRoi));
                } else if (ijRoi instanceof ImageRoi) {
                    list.add(new MaskWrapper((ImageRoi) ijRoi));
                } else {
                    list.add(new RectangleWrapper(ijRoi));
                }
                break;
        }
        return list;
    }


    /**
     * Extracts the angle from an AffineTransform.
     *
     * @param transform The AffineTransform.
     *
     * @return See above.
     */
    private static double extractAngle(java.awt.geom.AffineTransform transform) {
        Point2D p1  = new Point2D.Double(0, 0);
        Point2D p2  = new Point2D.Double(1, 0);
        Point2D tp1 = transform.transform(p1, null);
        Point2D tp2 = transform.transform(p2, null);
        double  dx  = tp2.getX() - tp1.getX();
        double  dy  = tp2.getY() - tp1.getY();
        return StrictMath.atan2(dy, dx);
    }


    /**
     * Copies details from an ImageJ ROI (position, stroke color, stroke width).
     *
     * @param ijRoi An ImageJ Roi.
     */
    protected void copyFromIJRoi(ij.gui.Roi ijRoi) {
        LengthI size = new LengthI(ijRoi.getStrokeWidth(), POINT);
        Color defaultStroke = Optional.ofNullable(Roi.getColor())
                                      .orElse(YELLOW);
        Color defaultFill = Optional.ofNullable(Roi.getDefaultFillColor())
                                    .orElse(TRANSPARENT);
        Color stroke = Optional.ofNullable(ijRoi.getStrokeColor())
                               .orElse(defaultStroke);
        Color fill = Optional.ofNullable(ijRoi.getFillColor())
                             .orElse(defaultFill);
        data.getShapeSettings().setStrokeWidth(size);
        data.getShapeSettings().setStroke(stroke);
        data.getShapeSettings().setFill(fill);

        // Set the plane
        int c = ijRoi.getCPosition();
        int z = ijRoi.getZPosition();
        int t = ijRoi.getTPosition();

        // Adjust coordinates if ROI does not have hyperstack positions
        if (!ijRoi.hasHyperStackPosition()) {
            ImagePlus imp = ijRoi.getImage();
            if (imp == null) {
                imp = ij.WindowManager.getImage(ijRoi.getImageID());
            }
            if (imp != null) {
                int stackSize = imp.getStackSize();
                int imageC    = imp.getNChannels();
                int imageZ    = imp.getNSlices();
                int imageT    = imp.getNFrames();
                int pos       = ijRoi.getPosition();

                // Reset values
                c = 1;
                z = 1;
                t = 1;

                if (stackSize == imageZ) {
                    z = pos;
                } else if (stackSize == imageC) {
                    c = pos;
                } else if (stackSize == imageT) {
                    t = pos;
                }
            }
        }

        data.setC(Math.max(-1, c - 1));
        data.setZ(Math.max(-1, z - 1));
        data.setT(Math.max(-1, t - 1));

        if (ijRoi instanceof TextRoi) {
            copyFromIJTextRoi((TextRoi) ijRoi);
        }
    }


    /**
     * Copies details from an ImageJ TextRoi (angle, font).
     *
     * @param text An ImageJ TextRoi.
     */
    private void copyFromIJTextRoi(ij.gui.TextRoi text) {
        Font font = text.getCurrentFont();
        setFontSize(font.getSize());

        int style = font.getStyle();

        String fontStyle;
        if (style == BOLD) {
            fontStyle = FONT_BOLD;
        } else if (style == ITALIC) {
            fontStyle = FONT_ITALIC;
        } else if (style == BOLD + ITALIC) {
            fontStyle = FONT_BOLD_ITALIC;
        } else {
            fontStyle = FONT_REGULAR;
        }
        data.getShapeSettings().setFontStyle(fontStyle);
        data.getShapeSettings().setFontFamily(font.getFamily());

        // Angle is negative and in degrees in IJ
        double angle = StrictMath.toRadians(-text.getAngle());
        double x     = text.getBounds().getX();
        double y     = text.getBounds().getY();

        java.awt.geom.AffineTransform at = new java.awt.geom.AffineTransform();
        at.rotate(angle, x, y);
        setTransform(at);
    }


    /**
     * Copies details to an ImageJ ROI (name, position, stroke color, fill color, stroke width).
     * <p>Also sets the {@code SHAPE_ID} property to the shape ID.</p>
     *
     * @param ijRoi An ImageJ Roi.
     */
    protected void copyToIJRoi(ij.gui.Roi ijRoi) {
        ijRoi.setName(getText());
        ijRoi.setStrokeColor(getStroke());
        Color fill = getFill();
        if (!TRANSPARENT.equals(fill)) {
            ijRoi.setFillColor(fill);
        }
        int c = Math.max(0, getC() + 1);
        int z = Math.max(0, getZ() + 1);
        int t = Math.max(0, getT() + 1);
        ijRoi.setPosition(c, z, t);
        if (ijRoi instanceof TextRoi) {
            copyToIJTextRoi((TextRoi) ijRoi);
        }
        ijRoi.setProperty(IJ_ID_PROPERTY, String.valueOf(data.getId()));
    }


    /**
     * Copies details to an ImageJ TextRoi (angle, font).
     *
     * @param text An ImageJ TextRoi.
     */
    private void copyToIJTextRoi(ij.gui.TextRoi text) {
        // Set negative angle in degrees for IJ
        double angle = -StrictMath.toDegrees(extractAngle(toAWTTransform()));
        text.setAngle(angle);

        String fontFamily = data.getShapeSettings().getFontFamily();
        String fontStyle  = data.getShapeSettings().getFontStyle();
        int    style      = Font.PLAIN;
        if (FONT_BOLD.equals(fontStyle)) {
            style = BOLD;
        } else if (FONT_ITALIC.equals(fontStyle)) {
            style = ITALIC;
        } else if (FONT_BOLD_ITALIC.equals(fontStyle)) {
            style = BOLD + ITALIC;
        }
        text.setFont(new Font(fontFamily, style, (int) getFontSize()));
    }


    /**
     * Gets the channel.
     *
     * @return the channel. -1 if the shape applies to all channels of the image.
     */
    public int getC() {
        return data.getC();
    }


    /**
     * Sets the channel.
     *
     * @param c the channel. Pass -1 to remove z value, i.e. shape applies to all channels of the image.
     */
    public void setC(int c) {
        data.setC(c);
    }


    /**
     * Gets the z-section.
     *
     * @return the z-section. -1 if the shape applies to all z-sections of the image.
     */
    public int getZ() {
        return data.getZ();
    }


    /**
     * Sets the z-section.
     *
     * @param z the z-section. Pass -1 to remove z value, i.e. shape applies to all z-sections of the image.
     */
    public void setZ(int z) {
        data.setZ(z);
    }


    /**
     * Sets the time-point.
     *
     * @return the time-point. -1 if the shape applies to all time-points of the image.
     */
    public int getT() {
        return data.getT();
    }


    /**
     * Sets the time-point.
     *
     * @param t the time-point. Pass -1 to remove t value, i.e. shape applies to all time-points of the image.
     */
    public void setT(int t) {
        data.setT(t);
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
            fontSize = data.getShapeSettings().getFontSize(POINT).getValue();
        } catch (BigResult bigResult) {
            String msg = "Error while getting font size from ShapeData.";
            Logger.getLogger(getClass().getName()).log(WARNING, msg, bigResult);
        }
        return fontSize;
    }


    /**
     * Sets ShapeData font size.
     *
     * @param value The font size (in typography points)
     */
    public void setFontSize(double value) {
        LengthI size = new LengthI(value, POINT);
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
        if (data.getTransform() == null) {
            return new java.awt.geom.AffineTransform();
        } else {
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
        Roi roi = new ShapeRoi(createTransformedAWTShape()).trySimplify();
        copyToIJRoi(roi);
        return roi;
    }


    /**
     * Returns the type of annotation link for this object.
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Attach an {@link AnnotationData} to this object.
     *
     * @param client     The client handling the connection.
     * @param annotation The {@link AnnotationData}.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public <A extends AnnotationData> void link(ClientImpl client, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        ShapeAnnotationLink link = new ShapeAnnotationLinkI();
        link.setChild(annotation.asAnnotation());
        link.setParent((omero.model.Shape) data.asIObject());
        client.save(link);
    }

}
