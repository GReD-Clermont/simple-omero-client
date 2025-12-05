/*
 *  Copyright (C) 2020-2025 GReD
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
 * Interface for rectangular shapes.
 */
public interface Rectangular {

    /**
     * Returns the x-coordinate of the rectangular shape.
     *
     * @return See above.
     */
    double getX();


    /**
     * Sets the x-coordinate of the rectangular shape.
     *
     * @param x See above.
     */
    void setX(double x);


    /**
     * Returns the y coordinate of the rectangular shape.
     *
     * @return See above.
     */
    double getY();


    /**
     * Sets the y-coordinate of the rectangular shape.
     *
     * @param y See above.
     */
    void setY(double y);


    /**
     * Returns the width of the rectangular shape.
     *
     * @return See above.
     */
    double getWidth();


    /**
     * Sets width of the rectangular shape.
     *
     * @param width See above.
     */
    void setWidth(double width);


    /**
     * Returns the height of the rectangular shape.
     *
     * @return See above.
     */
    double getHeight();


    /**
     * Sets the height of the rectangular shape.
     *
     * @param height See above.
     */
    void setHeight(double height);


    /**
     * Sets the coordinates of the rectangular shape.
     *
     * @param x      The x-coordinate of the top-left corner.
     * @param y      The y-coordinate of the top-left corner.
     * @param width  The width of the rectangular shape.
     * @param height The height of the rectangular shape.
     */
    default void setCoordinates(double x, double y, double width, double height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }


    /**
     * Gets the coordinates of the rectangular shape.
     *
     * @return Array of coordinates containing {X,Y,Width,Height}.
     */
    default double[] getCoordinates() {
        return new double[]{getX(), getY(), getWidth(), getHeight()};
    }


    /**
     * Sets the coordinates of the rectangular shape.
     *
     * @param coordinates Array of coordinates containing {X,Y,Width,Height}.
     */
    default void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            String msg = "Cannot set null coordinates for rectangular shape.";
            throw new IllegalArgumentException(msg);
        } else if (coordinates.length == 4) {
            setX(coordinates[0]);
            setY(coordinates[1]);
            setWidth(coordinates[2]);
            setHeight(coordinates[3]);
        } else {
            String msg = "4 coordinates required for rectangular shape.";
            throw new IllegalArgumentException(msg);
        }
    }

}
