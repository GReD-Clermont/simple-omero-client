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


import ij.gui.TextRoi;
import omero.gateway.model.TextData;

import java.awt.geom.Path2D;


/**
 * Class containing an TextData.
 * <p> Wraps function calls to the TextData contained.
 */
public class Text extends Shape<TextData> {


    /**
     * Constructor of the Text class using a TextData.
     *
     * @param dataObject the shape
     */
    public Text(TextData dataObject) {
        super(dataObject);
    }


    /**
     * Constructor of the Text class using a new empty ShapeData.
     */
    public Text() {
        this(new TextData());
    }


    /**
     * Constructor of the Text class using an ImageJ TextRoi.
     *
     * @param text An ImageJ TextRoi.
     */
    public Text(TextRoi text) {
        this(text.getText(), text.getBounds().getX(), text.getBounds().getY());
        super.copy(text);
    }


    /**
     * Creates a new instance of the Text, sets the centre and major, minor axes.
     *
     * @param text Object text.
     * @param x    x-coordinate of the shape.
     * @param y    y-coordinate of the shape.
     */
    public Text(String text, double x, double y) {
        this(new TextData(text, x, y));
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
        Path2D point = new Path2D.Double();
        point.moveTo(getX(), getY());
        return point;
    }


    /**
     * Returns the x-coordinate of the shape.
     *
     * @return See above.
     */
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate of the shape.
     *
     * @param x See above.
     */
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y coordinate of the shape.
     *
     * @return See above.
     */
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate of the shape.
     *
     * @param y See above.
     */
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Sets the coordinates of the TextData shape.
     *
     * @param x x-coordinate of the TextData shape.
     * @param y y-coordinate of the TextData shape.
     */
    public void setCoordinates(double x, double y) {
        setX(x);
        setY(y);
    }


    /**
     * Gets the coordinates of the TextData shape.
     *
     * @return Array of coordinates containing {X,Y}.
     */
    public double[] getCoordinates() {
        double[] coordinates = new double[2];
        coordinates[0] = getX();
        coordinates[1] = getY();
        return coordinates;
    }


    /**
     * Sets the coordinates of the TextData object.
     *
     * @param coordinates Array of coordinates containing {X,Y}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("TextData cannot set null coordinates.");
        } else if (coordinates.length == 2) {
            data.setX(coordinates[0]);
            data.setY(coordinates[1]);
        } else {
            throw new IllegalArgumentException("2 coordinates required for TextData.");
        }
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    @Override
    public ij.gui.Roi toImageJ() {
        java.awt.Shape awtShape = createTransformedAWTShape();

        String text = getText();
        double x    = awtShape.getBounds2D().getX();
        double y    = awtShape.getBounds2D().getY();

        TextRoi roi = new TextRoi(x, y, text);
        roi.setStrokeColor(getStroke());
        roi.setFillColor(getFill());
        int c = Math.max(0, getC() + 1);
        int z = Math.max(0, getZ() + 1);
        int t = Math.max(0, getT() + 1);
        roi.setPosition(c, z, t);

        return roi;
    }

}
