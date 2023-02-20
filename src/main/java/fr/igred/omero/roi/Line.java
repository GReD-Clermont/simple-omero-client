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


import omero.gateway.model.LineData;


/**
 * Interface to handle Line shapes on OMERO.
 */
public interface Line extends Shape<LineData> {

    /** String to use arrows as markers */
    String ARROW = "Arrow";


    /**
     * Returns the x-coordinate of the starting point of an untransformed line.
     *
     * @return See above.
     */
    double getX1();


    /**
     * Set the x-coordinate of the starting point of an untransformed line.
     *
     * @param x1 See above.
     */
    void setX1(double x1);


    /**
     * Returns the x-coordinate of the end point of an untransformed line.
     *
     * @return See above.
     */
    double getX2();


    /**
     * Set the x-coordinate of the end point of an untransformed line.
     *
     * @param x2 See above.
     */
    void setX2(double x2);


    /**
     * Returns the y-coordinate of the starting point of an untransformed line.
     *
     * @return See above.
     */
    double getY1();


    /**
     * Set the y-coordinate of the starting point of an untransformed line.
     *
     * @param y1 See above.
     */
    void setY1(double y1);


    /**
     * Returns the y-coordinate of the end point of an untransformed line.
     *
     * @return See above.
     */
    double getY2();


    /**
     * Set the y-coordinate of the end point of an untransformed line.
     *
     * @param y2 See above.
     */
    void setY2(double y2);


    /**
     * Sets the coordinates of the LineData shape.
     *
     * @param x1 x-coordinate of the starting point of an untransformed line.
     * @param y1 y-coordinate of the starting point of an untransformed line.
     * @param x2 x-coordinate of the end point of an untransformed line.
     * @param y2 y-coordinate of the end point of an untransformed line.
     */
    void setCoordinates(double x1, double y1, double x2, double y2);


    /**
     * Gets the coordinates of the LineData shape.
     *
     * @return Array of coordinates containing {X1,Y1,X2,Y2}.
     */
    double[] getCoordinates();


    /**
     * Sets the coordinates of the LineData shape.
     *
     * @param coordinates Array of coordinates containing {X1,Y1,X2,Y2}.
     */
    void setCoordinates(double[] coordinates);

}
