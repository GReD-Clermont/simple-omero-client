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


import ij.gui.TextRoi;
import omero.gateway.model.TextData;


/**
 * Class containing an TextData.
 * <p> Wraps function calls to the TextData contained.
 */
public class TextWrapper extends ShapeWrapper<TextData> implements Text {


    /**
     * Constructor of the TextWrapper class using a TextData.
     *
     * @param text The TextData to wrap.
     */
    public TextWrapper(TextData text) {
        super(text);
    }


    /**
     * Constructor of the TextWrapper class using a new empty ShapeData.
     */
    public TextWrapper() {
        this(new TextData());
    }


    /**
     * Constructor of the TextWrapper class using an ImageJ TextRoi.
     *
     * @param text An ImageJ TextRoi.
     */
    public TextWrapper(TextRoi text) {
        this(text.getText(), text.getBounds().getX(), text.getBounds().getY());
        super.copyFromIJRoi(text);
    }


    /**
     * Creates a new instance of the TextWrapper, sets the centre and major, minor axes.
     *
     * @param text Object text.
     * @param x    x-coordinate of the shape.
     * @param y    y-coordinate of the shape.
     */
    public TextWrapper(String text, double x, double y) {
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
     * Returns the x-coordinate of the shape.
     *
     * @return See above.
     */
    @Override
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate of the shape.
     *
     * @param x See above.
     */
    @Override
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y coordinate of the shape.
     *
     * @return See above.
     */
    @Override
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate of the shape.
     *
     * @param y See above.
     */
    @Override
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    @Override
    public ij.gui.Roi toImageJ() {
        java.awt.Shape awtShape = createTransformedAWTShape();

        double x = awtShape.getBounds2D().getX();
        double y = awtShape.getBounds2D().getY();

        TextRoi roi = new TextRoi(x, y, getText());
        copyToIJRoi(roi);

        return roi;
    }

}
