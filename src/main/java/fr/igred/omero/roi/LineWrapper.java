/*
 *  Copyright (C) 2020-2022 GReD
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


import ij.gui.Arrow;
import ij.gui.Line;
import ij.gui.Roi;
import omero.gateway.model.LineData;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;


/**
 * Class containing an LineData.
 * <p> Wraps function calls to the LineData contained.
 */
public class LineWrapper extends GenericShapeWrapper<LineData> {

    /** String to use arrows as markers */
    public static final String ARROW = "Arrow";


    /**
     * Constructor of the LineWrapper class using a LineData.
     *
     * @param shape the shape
     */
    public LineWrapper(LineData shape) {
        super(shape);
    }


    /**
     * Constructor of the RectangleWrapper class using a new empty LineData.
     */
    public LineWrapper() {
        this(new LineData());
    }


    /**
     * Constructor of the LineWrapper class using an ImageJ Line ROI.
     *
     * @param line An ImageJ Line ROI.
     */
    public LineWrapper(Line line) {
        this(line.x1d, line.y1d, line.x2d, line.y2d);
        data.setText(line.getName());

        if (line instanceof Arrow) {
            data.getShapeSettings().setMarkerEnd(ARROW);
            if (((Arrow) line).getDoubleHeaded()) {
                data.getShapeSettings().setMarkerStart(ARROW);
            }
        }
        super.copy(line);
    }


    /**
     * Constructor of the RectangleWrapper class using a new LineData.
     *
     * @param x1 x1-coordinate of the shape.
     * @param y1 y1-coordinate of the shape.
     * @param x2 x2-coordinate of the shape.
     * @param y2 y2-coordinate of the shape.
     */
    public LineWrapper(double x1, double y1, double x2, double y2) {
        this(new LineData(x1, y1, x2, y2));
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
        return new Line2D.Double(getX1(), getY1(), getX2(), getY2());
    }


    /**
     * Returns the x-coordinate of the starting point of an untransformed line.
     *
     * @return See above.
     */
    public double getX1() {
        return data.getX1();
    }


    /**
     * Set the x-coordinate of the starting point of an untransformed line.
     *
     * @param x1 See above.
     */
    public void setX1(double x1) {
        data.setX1(x1);
    }


    /**
     * Returns the x-coordinate of the end point of an untransformed line.
     *
     * @return See above.
     */
    public double getX2() {
        return data.getX2();
    }


    /**
     * Set the x-coordinate of the end point of an untransformed line.
     *
     * @param x2 See above.
     */
    public void setX2(double x2) {
        data.setX2(x2);
    }


    /**
     * Returns the y-coordinate of the starting point of an untransformed line.
     *
     * @return See above.
     */
    public double getY1() {
        return data.getY1();
    }


    /**
     * Set the y-coordinate of the starting point of an untransformed line.
     *
     * @param y1 See above.
     */
    public void setY1(double y1) {
        data.setY1(y1);
    }


    /**
     * Returns the y-coordinate of the end point of an untransformed line.
     *
     * @return See above.
     */
    public double getY2() {
        return data.getY2();
    }


    /**
     * Set the y-coordinate of the end point of an untransformed line.
     *
     * @param y2 See above.
     */
    public void setY2(double y2) {
        data.setY2(y2);
    }


    /**
     * Sets the coordinates of the LineData shape.
     *
     * @param x1 x-coordinate of the starting point of an untransformed line.
     * @param y1 y-coordinate of the starting point of an untransformed line.
     * @param x2 x-coordinate of the end point of an untransformed line.
     * @param y2 y-coordinate of the end point of an untransformed line.
     */
    public void setCoordinates(double x1, double y1, double x2, double y2) {
        setX1(x1);
        setY1(y1);
        setX2(x2);
        setY2(y2);
    }


    /**
     * Gets the coordinates of the LineData shape.
     *
     * @return Array of coordinates containing {X1,Y1,X2,Y2}.
     */
    public double[] getCoordinates() {
        double[] coordinates = new double[4];
        coordinates[0] = getX1();
        coordinates[1] = getY1();
        coordinates[2] = getX2();
        coordinates[3] = getY2();
        return coordinates;
    }


    /**
     * Sets the coordinates of the LineData shape.
     *
     * @param coordinates Array of coordinates containing {X1,Y1,X2,Y2}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("LineData cannot set null coordinates.");
        } else if (coordinates.length == 4) {
            data.setX1(coordinates[0]);
            data.setY1(coordinates[1]);
            data.setX2(coordinates[2]);
            data.setY2(coordinates[3]);
        } else {
            throw new IllegalArgumentException("4 coordinates required for LineData.");
        }
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    @Override
    public Roi toImageJ() {
        PointWrapper    p1        = new PointWrapper(getX1(), getY1());
        PointWrapper    p2        = new PointWrapper(getX2(), getY2());
        AffineTransform transform = toAWTTransform();
        if (transform != null) {
            p1.setTransform(transform);
            p2.setTransform(transform);
        }

        java.awt.geom.Rectangle2D shape1 = p1.createTransformedAWTShape().getBounds2D();
        java.awt.geom.Rectangle2D shape2 = p2.createTransformedAWTShape().getBounds2D();

        String start = asShapeData().getShapeSettings().getMarkerStart();
        String end   = asShapeData().getShapeSettings().getMarkerEnd();

        double x1 = shape1.getX();
        double x2 = shape2.getX();
        double y1 = shape1.getY();
        double y2 = shape2.getY();

        byte arrowStart = (byte) (start.equals(ARROW) ? 1 : 0);
        byte arrowEnd   = (byte) (end.equals(ARROW) ? 1 : 0);
        byte arrow      = (byte) (arrowStart + 2 * arrowEnd);

        Roi roi;
        switch (arrow) {
            case 3:
                roi = new Arrow(x1, y1, x2, y2);
                ((Arrow) roi).setDoubleHeaded(true);
                break;
            case 2:
                roi = new Arrow(x1, y1, x2, y2);
                break;
            case 1:
                roi = new Arrow(x2, y2, x1, y1);
                break;
            default:
                roi = new Line(x1, y1, x2, y2);
        }
        roi.setStrokeColor(getStroke());
        roi.setFillColor(getFill());
        int c = Math.max(0, getC() + 1);
        int z = Math.max(0, getZ() + 1);
        int t = Math.max(0, getT() + 1);
        roi.setPosition(c, z, t);
        return roi;
    }

}
