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


import omero.gateway.model.RectangleData;


/**
 * Class containing an RectangleData.
 * <p> Wraps function calls to the RectangleData contained.
 */
public class RectangleWrapper extends ShapeWrapper<RectangleData> implements Rectangle {


    /**
     * Constructor of the Rectangle class using a RectangleData.
     *
     * @param dataObject the shape
     */
    public RectangleWrapper(RectangleData dataObject) {
        super(dataObject);
    }


    /**
     * Constructor of the Rectangle class using a new empty RectangleData.
     */
    public RectangleWrapper() {
        this(new RectangleData());
    }


    /**
     * Constructor of the Rectangle class using bounds from an ImageJ ROI.
     *
     * @param ijRoi An ImageJ ROI.
     */
    public RectangleWrapper(ij.gui.Roi ijRoi) {
        this(ijRoi.getBounds().getX(),
             ijRoi.getBounds().getY(),
             ijRoi.getBounds().getWidth(),
             ijRoi.getBounds().getHeight());

        data.setText(ijRoi.getName());
        super.copy(ijRoi);
    }


    /**
     * Constructor of the Rectangle class using a new RectangleData.
     *
     * @param x      The x-coordinate of the top-left corner.
     * @param y      The y-coordinate of the top-left corner.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public RectangleWrapper(double x, double y, double width, double height) {
        this(new RectangleData(x, y, width, height));
    }


}
