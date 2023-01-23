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


import omero.gateway.model.EllipseData;


/**
 * Class containing an EllipseData.
 * <p> Wraps function calls to the EllipseData contained.
 */
public class EllipseWrapper extends ShapeWrapper<EllipseData> implements Ellipse {


    /**
     * Constructor of the Ellipse class using a EllipseData.
     *
     * @param dataObject the shape
     */
    public EllipseWrapper(EllipseData dataObject) {
        super(dataObject);
    }


    /**
     * Constructor of the Ellipse class using a new empty EllipseData.
     */
    public EllipseWrapper() {
        this(new EllipseData());
    }


    /**
     * Constructor of the Ellipse class using bounds from an ImageJ ROI.
     *
     * @param ijRoi An ImageJ ROI.
     */
    public EllipseWrapper(ij.gui.Roi ijRoi) {
        this(ijRoi.getBounds().getX() + ijRoi.getBounds().getWidth() / 2,
             ijRoi.getBounds().getY() + ijRoi.getBounds().getHeight() / 2,
             ijRoi.getBounds().getWidth() / 2,
             ijRoi.getBounds().getHeight() / 2);
        data.setText(ijRoi.getName());
        super.copy(ijRoi);
    }


    /**
     * Constructor of the Ellipse class using a new EllipseData.
     *
     * @param x       The x-coordinate of the center of the ellipse.
     * @param y       The y-coordinate of the center of the ellipse.
     * @param radiusX The radius along the X-axis.
     * @param radiusY The radius along the Y-axis.
     */
    public EllipseWrapper(double x, double y, double radiusX, double radiusY) {
        this(new EllipseData(x, y, radiusX, radiusY));
    }


}
