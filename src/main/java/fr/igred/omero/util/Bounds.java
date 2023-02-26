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

package fr.igred.omero.util;


/** Class containing 5D bounds coordinates */
public class Bounds {

    /** Start coordinates */
    private final Coordinates start;
    /** Bounds size */
    private final Coordinates size;


    /**
     * Bounds constructor.
     *
     * @param start Start coordinates.
     * @param end   End coordinates.
     */
    public Bounds(Coordinates start, Coordinates end) {
        this.start = start;
        this.size  = new Coordinates(end.getX() - start.getX() + 1,
                                     end.getY() - start.getY() + 1,
                                     end.getC() - start.getC() + 1,
                                     end.getZ() - start.getZ() + 1,
                                     end.getT() - start.getT() + 1);
    }


    /**
     * Gets starting coordinates.
     *
     * @return Starting coordinates.
     */
    public Coordinates getStart() {
        return start;
    }


    /**
     * Gets size of bounds for each coordinate.
     *
     * @return Bounds size.
     */
    public Coordinates getEnd() {
        return new Coordinates(start.getX() + size.getX() - 1,
                               start.getY() + size.getY() - 1,
                               start.getC() + size.getC() - 1,
                               start.getZ() + size.getZ() - 1,
                               start.getT() + size.getT() - 1);
    }


    /**
     * Gets size of bounds for each coordinate.
     *
     * @return Bounds size.
     */
    public Coordinates getSize() {
        return size;
    }


    @Override
    public String toString() {
        return "Bounds{" +
               "start=" + start +
               ", size=" + size +
               "}";
    }

}
