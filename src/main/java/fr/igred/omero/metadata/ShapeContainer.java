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

package fr.igred.omero.metadata;


import ome.model.units.BigResult;
import omero.gateway.model.ShapeData;
import omero.gateway.model.PointData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.EllipseData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.MaskData;
import omero.gateway.model.TextData;
import omero.gateway.model.LineData;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;


/**
 * Class containing a ShapeData
 * <p> Implements functions using the ShapeData contained
 */
public class ShapeContainer {

    /** Set if shape is PointData. */
    public final static String POINT = "Point";

    /** Set if shape is LineData. */
    public final static String LINE = "Line";

    /** Set if shape is PolylineData. */
    public final static String POLYLINE = "Polyline";

    /** Set if shape is RectangleData. */
    public final static String RECTANGLE = "Rectangle";

    /** Set if shape is PolygonData. */
    public final static String POLYGON = "Polygon";

    /** Set if shape is EllipseData. */
    public final static String ELLIPSE = "Ellipse";

    /** Set if shape is MaskData. */
    public final static String MASK = "Mask";

    /** Set if shape is TextData. */
    public final static String TEXT = "Text";

    /** Set if shape is ShapeData. */
    public final static String OTHER = "Other";

    /** Shape contained in the ShapeContainer. */
    private final ShapeData shape;


    /**
     * Constructor of the ShapeContainer class using a ShapeData.
     *
     * @param shape the shape
     */
    public ShapeContainer(ShapeData shape) {
        this.shape = shape;
    }


    /**
     * Constructor of the ShapeContainer class creating a new ShapeData.
     * <p> Default shape is a RectangleData.
     *
     * @param shapeType the shape type
     */
    public ShapeContainer(String shapeType) {
        switch (shapeType) {
            case POINT:
                this.shape = new PointData();
                break;
            case LINE:
                this.shape = new LineData();
                break;
            case POLYLINE:
                this.shape = new PolylineData();
                break;
            case POLYGON:
                this.shape = new PolygonData();
                break;
            case ELLIPSE:
                this.shape = new EllipseData();
                break;
            case MASK:
                this.shape = new MaskData();
                break;
            case TEXT:
                this.shape = new TextData();
                break;
            default:
                this.shape = new RectangleData();
                break;
        }
    }


    /**
     * Gets the type of ShapeData contained.
     *
     * @return the ShapeData type.
     */
    public String getShapeType() {
        if (shape != null) {
            if (PointData.class.equals(shape.getClass())) {
                return POINT;
            } else if (RectangleData.class.equals(shape.getClass())) {
                return RECTANGLE;
            } else if (LineData.class.equals(shape.getClass())) {
                return LINE;
            } else if (EllipseData.class.equals(shape.getClass())) {
                return ELLIPSE;
            } else if (PolygonData.class.equals(shape.getClass())) {
                return POLYGON;
            } else if (PolylineData.class.equals(shape.getClass())) {
                return POLYLINE;
            } else if (MaskData.class.equals(shape.getClass())) {
                return MASK;
            } else if (TextData.class.equals(shape.getClass())) {
                return TEXT;
            }
        }
        return OTHER;
    }


    /**
     * Gets the ShapeData object contained.
     *
     * @return the shape.
     */
    public ShapeData getShape() {
        return shape;
    }


    /**
     * Gets the channel.
     *
     * @return the channel. -1 if the shape applies to all channels of the image.
     */
    public int getC() {
        return this.shape.getC();
    }


    /**
     * Sets the channel.
     *
     * @param c the channel. Pass -1 to remove z value, i. e. shape applies to all channels of the image.
     */
    public void setC(int c) {
        this.shape.setC(c);
    }


    /**
     * Gets the z-section.
     *
     * @return the z-section. -1 if the shape applies to all z-sections of the image.
     */
    public int getZ() {
        return this.shape.getZ();
    }


    /**
     * Sets the z-section.
     *
     * @param z the z-section. Pass -1 to remove z value, i. e. shape applies to all z-sections of the image.
     */
    public void setZ(int z) {
        this.shape.setZ(z);
    }


    /**
     * Sets the time-point.
     *
     * @return the time-point. -1 if the shape applies to all time-points of the image.
     */
    public int getT() {
        return this.shape.getT();
    }


    /**
     * Sets the time-point.
     *
     * @param t the time-point. Pass -1 to remove t value, i. e. shape applies to all time-points of the image.
     */
    public void setT(int t) {
        this.shape.setT(t);
    }


    /**
     * Gets the text on the ShapeData.
     *
     * @return the text
     */
    public String getText() {
        String shapeType = getShapeType();
        String text      = null;
        switch (shapeType) {
            case POINT:
                text = ((PointData) this.shape).getText();
                break;
            case LINE:
                text = ((LineData) this.shape).getText();
                break;
            case POLYLINE:
                text = ((PolylineData) this.shape).getText();
                break;
            case RECTANGLE:
                text = ((RectangleData) this.shape).getText();
                break;
            case POLYGON:
                text = ((PolygonData) this.shape).getText();
                break;
            case ELLIPSE:
                text = ((EllipseData) this.shape).getText();
                break;
            case MASK:
                text = ((MaskData) this.shape).getText();
                break;
            case TEXT:
                text = ((TextData) this.shape).getText();
                break;
            default:
                System.err.println("Cannot get text on this type of ShapeData.");
                break;
        }
        return text;
    }


    /**
     * Sets the text on the ShapeData.
     *
     * @param text the text
     */
    public void setText(String text) {
        String shapeType = getShapeType();
        switch (shapeType) {
            case POINT:
                ((PointData) this.shape).setText(text);
                break;
            case LINE:
                ((LineData) this.shape).setText(text);
                break;
            case POLYLINE:
                ((PolylineData) this.shape).setText(text);
                break;
            case RECTANGLE:
                ((RectangleData) this.shape).setText(text);
                break;
            case POLYGON:
                ((PolygonData) this.shape).setText(text);
                break;
            case ELLIPSE:
                ((EllipseData) this.shape).setText(text);
                break;
            case MASK:
                ((MaskData) this.shape).setText(text);
                break;
            case TEXT:
                ((TextData) this.shape).setText(text);
                break;
            default:
                System.err.println("Cannot set text on this type of ShapeData.");
                break;
        }
    }


    /**
     * Gets the mask of a MaskData shape.
     *
     * @return The mask image (int[width][height]).
     */
    public int[][] getMask() {
        int[][] mask = null;
        if (getShapeType().equals(MASK)) {
            mask = ((MaskData) shape).getMaskAsBinaryArray();
        } else {
            System.err.println("ShapeData is not a MaskData object.");
        }
        return mask;
    }


    /**
     * Sets the mask of a MaskData shape.
     *
     * @param mask The binary mask (int[width][height]).
     */
    public void setMask(int[][] mask) {
        if (getShapeType().equals(MASK)) {
            ((MaskData) shape).setMask(mask);
        } else {
            System.err.println("ShapeData is not a MaskData object.");
        }
    }


    /**
     * Sets the coordinates of a PointData or TextData shape.
     *
     * @param x x-coordinate of the PointData shape.
     * @param y y-coordinate of the PointData shape.
     */
    public void setPointCoordinates(double x, double y) {
        String shapeType = getShapeType();
        if (shapeType.equals(POINT) || shapeType.equals(TEXT)) {
            double[] coordinates = {x, y};
            setCoordinates(coordinates);
        } else {
            System.err.println("ShapeData is neither a PointData nor a TextData object.");
        }
    }


    /**
     * Sets the coordinates of a RectangleData or MaskData shape.
     *
     * @param x      x-coordinate of the RectangleData shape.
     * @param y      y-coordinate of the RectangleData shape.
     * @param width  width of the RectangleData shape.
     * @param height height of the RectangleData shape.
     */
    public void setRectangleCoordinates(double x, double y, double width, double height) {
        String shapeType = getShapeType();
        if (shapeType.equals(RECTANGLE) || shapeType.equals(MASK)) {
            double[] coordinates = {x, y, width, height};
            setCoordinates(coordinates);
        } else {
            System.err.println("ShapeData is neither a RectangleData nor a MaskData object.");
        }
    }


    /**
     * Sets the coordinates of a LineData shape.
     *
     * @param x1 x-coordinate of the starting point of the LineData shape.
     * @param y1 y-coordinate of the starting point of the LineData shape.
     * @param x2 x-coordinate of the end point of the LineData shape.
     * @param y2 y-coordinate of the end point of the LineData shape.
     */
    public void setLineCoordinates(double x1, double y1, double x2, double y2) {
        if (getShapeType().equals(LINE)) {
            double[] coordinates = {x1, y1, x2, y2};
            setCoordinates(coordinates);
        } else {
            System.err.println("ShapeData is not a LineData object.");
        }
    }


    /**
     * Sets the coordinates of an EllipseData shape.
     *
     * @param x       x-coordinate of the EllipseData shape.
     * @param y       y-coordinate of the EllipseData shape.
     * @param radiusX the radius along the X-axis of the EllipseData shape.
     * @param radiusY the radius along the Y-axis of the EllipseData shape.
     */
    public void setEllipseCoordinates(double x, double y, double radiusX, double radiusY) {
        if (getShapeType().equals(ELLIPSE)) {
            double[] coordinates = {x, y, radiusX, radiusY};
            setCoordinates(coordinates);
        } else {
            System.err.println("ShapeData is not an EllipseData object.");
        }
    }


    /**
     * Returns the points of a PolylineData or PolygonData shape.
     *
     * @return the points.
     */
    public List<Point2D.Double> getPoints() {
        List<Point2D.Double> points    = null;
        String               shapeType = getShapeType();
        if (shapeType.equals(POLYLINE)) {
            points = ((PolylineData) shape).getPoints();
        } else if (shapeType.equals(POLYGON)) {
            points = ((PolygonData) shape).getPoints();
        } else {
            System.err.println("ShapeData is neither a PolylineData nor a PolygonData object.");
        }
        return points;
    }


    /**
     * Sets the points of a PolylineData or PolygonData shape.
     *
     * @param points The points to set.
     */
    public void setPoints(List<Point2D.Double> points) {
        String shapeType = getShapeType();
        if (shapeType.equals(POLYLINE)) {
            ((PolylineData) shape).setPoints(points);
        } else if (shapeType.equals(POLYGON)) {
            ((PolygonData) shape).setPoints(points);
        } else {
            System.err.println("ShapeData is neither a PolylineData nor a PolygonData object.");
        }
    }


    /**
     * Gets the coordinates of either:
     * <ul>
     *     <li>a PointData object</li>
     *     <li>a TextData object</li>
     *     <li>a RectangleData object</li>
     *     <li>a MaskData object</li>
     *     <li>an EllipseData object</li>
     *     <li>a LineData object</li>
     * </ul>
     *
     * @return Array of coordinates depending on the object type:
     * <ul>
     *     <li>PointData: {X,Y}</li>
     *     <li>TextData: {X,Y}</li>
     *     <li>RectangleData: {X,Y,Width,Height}</li>
     *     <li>MaskData: {X,Y,Width,Height}</li>
     *     <li>EllipseData: {X,Y,RadiusX,RadiusY}</li>
     *     <li>LineData: {X1,Y1,X2,Y2}</li>
     *     <li>Other: null</li>
     * </ul>
     */
    public double[] getCoordinates() {
        String   shapeType   = getShapeType();
        double[] coordinates = null;
        switch (shapeType) {
            case POINT:
                coordinates = new double[2];
                coordinates[0] = ((PointData) shape).getX();
                coordinates[1] = ((PointData) shape).getY();
                break;
            case TEXT:
                coordinates = new double[2];
                coordinates[0] = ((TextData) shape).getX();
                coordinates[1] = ((TextData) shape).getY();
                break;
            case RECTANGLE:
                coordinates = new double[4];
                coordinates[0] = ((RectangleData) shape).getX();
                coordinates[1] = ((RectangleData) shape).getY();
                coordinates[2] = ((RectangleData) shape).getWidth();
                coordinates[3] = ((RectangleData) shape).getHeight();
                break;
            case MASK:
                coordinates = new double[4];
                coordinates[0] = ((MaskData) shape).getX();
                coordinates[1] = ((MaskData) shape).getY();
                coordinates[2] = ((MaskData) shape).getWidth();
                coordinates[3] = ((MaskData) shape).getHeight();
                break;
            case ELLIPSE:
                coordinates = new double[4];
                coordinates[0] = ((EllipseData) shape).getX();
                coordinates[1] = ((EllipseData) shape).getY();
                coordinates[2] = ((EllipseData) shape).getRadiusX();
                coordinates[3] = ((EllipseData) shape).getRadiusY();
                break;
            case LINE:
                coordinates = new double[4];
                coordinates[0] = ((LineData) shape).getX1();
                coordinates[1] = ((LineData) shape).getY1();
                coordinates[2] = ((LineData) shape).getX2();
                coordinates[3] = ((LineData) shape).getY2();
                break;
            default:
                System.err.println("ShapeData does not have coordinates.");
                break;
        }
        return coordinates;
    }


    /**
     * Sets the coordinates of either:
     * <ul>
     *     <li>a PointData object</li>
     *     <li>a TextData object</li>
     *     <li>a RectangleData object</li>
     *     <li>a MaskData object</li>
     *     <li>an EllipseData object</li>
     *     <li>a LineData object</li>
     * </ul>
     *
     * @param coordinates Array of coordinates depending on the object type:
     *                    <ul>
     *                        <li>PointData: {X,Y}</li>
     *                        <li>TextData: {X,Y}</li>
     *                        <li>RectangleData: {X,Y,Width,Height}</li>
     *                        <li>MaskData: {X,Y,Width,Height}</li>
     *                        <li>EllipseData: {X,Y,RadiusX,RadiusY}</li>
     *                        <li>LineData: {X1,Y1,X2,Y2}</li>
     *                        <li>Other: null</li>
     *                    </ul>
     */
    public void setCoordinates(double[] coordinates) {
        String shapeType = getShapeType();
        if (coordinates != null) {
            switch (shapeType) {
                case POINT:
                    if (coordinates.length == 2) {
                        ((PointData) shape).setX(coordinates[0]);
                        ((PointData) shape).setY(coordinates[1]);
                    } else {
                        System.err.println("2 coordinates required for PointData.");
                    }
                    break;
                case TEXT:
                    if (coordinates.length == 2) {
                        ((TextData) shape).setX(coordinates[0]);
                        ((TextData) shape).setY(coordinates[1]);
                    } else {
                        System.err.println("2 coordinates required for TextData.");
                    }
                    break;
                case RECTANGLE:
                    if (coordinates.length == 4) {
                        ((RectangleData) shape).setX(coordinates[0]);
                        ((RectangleData) shape).setY(coordinates[1]);
                        ((RectangleData) shape).setWidth(coordinates[2]);
                        ((RectangleData) shape).setHeight(coordinates[3]);
                    } else {
                        System.err.println("4 coordinates required for RectangleData.");
                    }
                    break;
                case MASK:
                    if (coordinates.length == 4) {
                        ((MaskData) shape).setX(coordinates[0]);
                        ((MaskData) shape).setY(coordinates[1]);
                        ((MaskData) shape).setWidth(coordinates[2]);
                        ((MaskData) shape).setHeight(coordinates[3]);
                    } else {
                        System.err.println("4 coordinates required for MaskData.");
                    }
                    break;
                case ELLIPSE:
                    if (coordinates.length == 4) {
                        ((EllipseData) shape).setX(coordinates[0]);
                        ((EllipseData) shape).setY(coordinates[1]);
                        ((EllipseData) shape).setRadiusX(coordinates[2]);
                        ((EllipseData) shape).setRadiusY(coordinates[3]);
                    } else {
                        System.err.println("4 coordinates required for EllipseData.");
                    }
                    break;
                case LINE:
                    if (coordinates.length == 4) {
                        ((LineData) shape).setX1(coordinates[0]);
                        ((LineData) shape).setY1(coordinates[1]);
                        ((LineData) shape).setX2(coordinates[2]);
                        ((LineData) shape).setY2(coordinates[3]);
                    } else {
                        System.err.println("2 coordinates required for LineData.");
                    }
                    break;
                default:
                    System.err.println("ShapeData does not have coordinates.");
                    break;
            }
        } else {
            System.err.println("ShapeContainer cannot set null coordinates.");
        }
    }


    /**
     * Gets ShapeData font size
     *
     * @return The font size (in typography points)
     */
    public double getFontSize() {
        double fontSize = Double.NaN;
        try {
            fontSize = shape.getShapeSettings().getFontSize(UnitsLength.POINT).getValue();
        } catch (BigResult bigResult) {
            System.err.println("Error while getting font size from ShapeData.");
            bigResult.printStackTrace();
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
        shape.getShapeSettings().setFontSize(size);
    }


    /**
     * Sets ShapeData stroke color
     *
     * @return The stroke color
     */
    public Color getStroke() {
        return shape.getShapeSettings().getStroke();
    }


    /**
     * Sets ShapeData stroke color
     *
     * @param color The stroke color
     */
    public void setStroke(Color color) {
        shape.getShapeSettings().setStroke(color);
    }

}
