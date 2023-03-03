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


/**
 * Interface for punctual shapes.
 */
public interface Punctual {

    /**
     * Returns the x-coordinate of the punctual shape.
     *
     * @return See above.
     */
    double getX();


    /**
     * Sets the x-coordinate of the punctual shape.
     *
     * @param x See above.
     */
    void setX(double x);


    /**
     * Returns the y coordinate of the punctual shape.
     *
     * @return See above.
     */
    double getY();


    /**
     * Sets the y-coordinate of the punctual shape.
     *
     * @param y See above.
     */
    void setY(double y);


    /**
     * Sets the coordinates the punctual shape.
     *
     * @param x x-coordinate of the PointData shape.
     * @param y y-coordinate of the PointData shape.
     */
    void setCoordinates(double x, double y);


    /**
     * Gets the coordinates of the punctual shape.
     *
     * @return Array of coordinates containing {X,Y}.
     */
    double[] getCoordinates();


    /**
     * Sets the coordinates of the punctual shape.
     *
     * @param coordinates Array of coordinates containing {X,Y}.
     */
    void setCoordinates(double[] coordinates);

}
