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


import omero.gateway.model.EllipseData;

import java.awt.geom.Ellipse2D;


/**
 * Interface to handle Ellipse shapes on OMERO.
 */
public interface Ellipse extends Shape {

    /**
     * Returns an {@link EllipseData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    EllipseData asDataObject();


    /**
     * Converts the shape to an {@link java.awt.Shape}.
     *
     * @return The converted AWT Shape.
     */
    @Override
    default java.awt.Shape toAWTShape() {
        return new Ellipse2D.Double(getX() - getRadiusX(),
                                    getY() - getRadiusY(),
                                    2 * getRadiusX(),
                                    2 * getRadiusY());
    }


    /**
     * Returns the x-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    double getX();


    /**
     * Sets the x-coordinate of the center of the ellipse.
     *
     * @param x See above.
     */
    void setX(double x);


    /**
     * Returns the y-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    double getY();


    /**
     * Sets the y-coordinate of the center of the ellipse.
     *
     * @param y See above.
     */
    void setY(double y);


    /**
     * Returns the radius along the X-axis.
     *
     * @return See above.
     */
    double getRadiusX();


    /**
     * Sets the radius along the X-axis.
     *
     * @param x the value to set.
     */
    void setRadiusX(double x);


    /**
     * Returns the radius along the Y-axis.
     *
     * @return See above.
     */
    double getRadiusY();


    /**
     * Sets the radius along the Y-axis.
     *
     * @param y The value to set.
     */
    void setRadiusY(double y);


    /**
     * Sets the coordinates of the EllipseData shape.
     *
     * @param x       The x-coordinate of the center of the ellipse.
     * @param y       The y-coordinate of the center of the ellipse.
     * @param radiusX The radius along the X-axis.
     * @param radiusY The radius along the Y-axis.
     */
    default void setCoordinates(double x, double y, double radiusX, double radiusY) {
        setX(x);
        setY(y);
        setRadiusX(radiusX);
        setRadiusY(radiusY);
    }


    /**
     * Gets the coordinates of the MaskData shape.
     *
     * @return Array of coordinates containing {X,Y,RadiusX,RadiusY}.
     */
    default double[] getCoordinates() {
        double[] coordinates = new double[4];
        coordinates[0] = getX();
        coordinates[1] = getY();
        coordinates[2] = getRadiusX();
        coordinates[3] = getRadiusY();
        return coordinates;
    }


    /**
     * Sets the coordinates of the EllipseData shape.
     *
     * @param coordinates Array of coordinates containing {X,Y,RadiusX,RadiusY}.
     */
    default void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            String error = "EllipseData cannot set null coordinates.";
            throw new IllegalArgumentException(error);
        } else if (coordinates.length == 4) {
            setX(coordinates[0]);
            setY(coordinates[1]);
            setRadiusX(coordinates[2]);
            setRadiusY(coordinates[3]);
        } else {
            String error = "4 coordinates required for EllipseData.";
            throw new IllegalArgumentException(error);
        }
    }

}
