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

package fr.igred.omero.core;


/** Class representing a resolution of an image. */
public class ResolutionLevel {

    /** Level of the resolution. */
    private final int level;

    /** Size in X of the resolution. */
    private final int sizeX;

    /** Size in Y of the resolution. */
    private final int sizeY;


    /**
     * Constructor of the ResolutionLevel class.
     *
     * @param level Level of the resolution.
     * @param sizeX Size in X of the resolution.
     * @param sizeY Size in Y of the resolution.
     */
    ResolutionLevel(int level, int sizeX, int sizeY) {
        this.level = level;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }


    /**
     * Returns the level of the resolution.
     *
     * @return See above.
     */
    public int getLevel() {
        return level;
    }


    /**
     * Returns the size in X of the resolution.
     *
     * @return See above.
     */
    public int getSizeX() {
        return sizeX;
    }


    /**
     * Returns the size in Y of the resolution.
     *
     * @return See above.
     */
    public int getSizeY() {
        return sizeY;
    }

}
