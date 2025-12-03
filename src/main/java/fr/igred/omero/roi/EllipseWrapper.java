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


import ij.gui.EllipseRoi;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import omero.gateway.model.EllipseData;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;


/**
 * Class containing an EllipseData.
 * <p> Wraps function calls to the EllipseData contained.
 */
public class EllipseWrapper extends ShapeWrapper<EllipseData> implements Ellipse {


    /**
     * Constructor of the EllipseWrapper class using a EllipseData.
     *
     * @param ellipse The EllipseData to wrap.
     */
    public EllipseWrapper(EllipseData ellipse) {
        super(ellipse);
    }


    /**
     * Constructor of the EllipseWrapper class using a new empty EllipseData.
     */
    public EllipseWrapper() {
        this(new EllipseData());
    }


    /**
     * Constructor of the EllipseWrapper class using bounds from an ImageJ ROI.
     *
     * @param ijRoi An ImageJ ROI.
     */
    public EllipseWrapper(ij.gui.Roi ijRoi) {
        this(ijRoi.getBounds().getX() + ijRoi.getBounds().getWidth() / 2,
             ijRoi.getBounds().getY() + ijRoi.getBounds().getHeight() / 2,
             ijRoi.getBounds().getWidth() / 2,
             ijRoi.getBounds().getHeight() / 2);
        data.setText(ijRoi.getName());
        super.copyFromIJRoi(ijRoi);
    }


    /**
     * Constructor of the EllipseWrapper class using a new EllipseData.
     *
     * @param x       The x-coordinate of the center of the ellipse.
     * @param y       The y-coordinate of the center of the ellipse.
     * @param radiusX The radius along the X-axis.
     * @param radiusY The radius along the Y-axis.
     */
    public EllipseWrapper(double x, double y, double radiusX, double radiusY) {
        this(new EllipseData(x, y, radiusX, radiusY));
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
     * Returns the x-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    @Override
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate of the center of the ellipse.
     *
     * @param x See above.
     */
    @Override
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    @Override
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate of the center of the ellipse.
     *
     * @param y See above.
     */
    @Override
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Returns the radius along the X-axis.
     *
     * @return See above.
     */
    @Override
    public double getRadiusX() {
        return data.getRadiusX();
    }


    /**
     * Sets the radius along the X-axis.
     *
     * @param x the value to set.
     */
    @Override
    public void setRadiusX(double x) {
        data.setRadiusX(x);
    }


    /**
     * Returns the radius along the Y-axis.
     *
     * @return See above.
     */
    @Override
    public double getRadiusY() {
        return data.getRadiusY();
    }


    /**
     * Sets the radius along the Y-axis.
     *
     * @param y The value to set.
     */
    @Override
    public void setRadiusY(double y) {
        data.setRadiusY(y);
    }


    /**
     * Converts shape to ImageJ ROI.
     *
     * @return An ImageJ ROI.
     */
    @Override
    public Roi toImageJ() {
        java.awt.Shape awtShape = createTransformedAWTShape();

        Roi roi;
        if (awtShape instanceof RectangularShape) {
            double x = ((RectangularShape) awtShape).getX();
            double y = ((RectangularShape) awtShape).getY();
            double w = ((RectangularShape) awtShape).getWidth();
            double h = ((RectangularShape) awtShape).getHeight();
            roi = new OvalRoi(x, y, w, h);
        } else {
            double x  = getX();
            double y  = getY();
            double rx = getRadiusX();
            double ry = getRadiusY();
            double ratio;

            Shape p1;
            Shape p2;
            if (ry <= rx) {
                p1    = new PointWrapper(x - rx, y);
                p2    = new PointWrapper(x + rx, y);
                ratio = ry / rx;
            } else {
                p1    = new PointWrapper(x, y - ry);
                p2    = new PointWrapper(x, y + ry);
                ratio = rx / ry;
            }
            p1.setTransform(toAWTTransform());
            p2.setTransform(toAWTTransform());
            Rectangle2D shape1 = p1.createTransformedAWTShape().getBounds2D();
            Rectangle2D shape2 = p2.createTransformedAWTShape().getBounds2D();

            double x1 = shape1.getX();
            double y1 = shape1.getY();
            double x2 = shape2.getX();
            double y2 = shape2.getY();
            roi = new EllipseRoi(x1, y1, x2, y2, ratio);
        }
        copyToIJRoi(roi);
        return roi;
    }

}
