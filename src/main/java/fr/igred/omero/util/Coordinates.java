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

package fr.igred.omero.util;


/** Class containing 5D pixel coordinates */
public class Coordinates {

    /** X coordinate */
    private final int x;
    /** Y coordinate */
    private final int y;
    /** C coordinate */
    private final int c;
    /** Z coordinate */
    private final int z;
    /** T coordinate */
    private final int t;


    /**
     * Coordinates constructor.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param c C coordinate.
     * @param z Z coordinate.
     * @param t T coordinate.
     */
    public Coordinates(int x, int y, int c, int z, int t) {
        this.x = x;
        this.y = y;
        this.c = c;
        this.z = z;
        this.t = t;
    }


    /**
     * Gets X coordinate.
     *
     * @return x coordinate.
     */
    public int getX() {
        return x;
    }


    /**
     * Gets Y coordinate.
     *
     * @return Y coordinate.
     */
    public int getY() {
        return y;
    }


    /**
     * Gets C coordinate.
     *
     * @return C coordinate.
     */
    public int getC() {
        return c;
    }


    /**
     * Gets Z coordinate.
     *
     * @return Z coordinate.
     */
    public int getZ() {
        return z;
    }


    /**
     * Gets T coordinate.
     *
     * @return T coordinate.
     */
    public int getT() {
        return t;
    }


    @Override
    public String toString() {
        return "Coordinates{" +
               "x=" + x +
               ", y=" + y +
               ", c=" + c +
               ", z=" + z +
               ", t=" + t +
               "}";
    }

}
