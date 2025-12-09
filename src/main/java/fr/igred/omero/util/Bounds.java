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
     * Creates Bounds object from given bounds arrays, set values to -1 when array size is lesser than 2.
     * Bounds can then be checked against image size using {@link #checkBounds(Coordinates)}.
     *
     * @param xBounds   Array containing the X bounds from which the pixels should be retrieved.
     * @param yBounds   Array containing the Y bounds from which the pixels should be retrieved.
     * @param cBounds   Array containing the C bounds from which the pixels should be retrieved.
     * @param zBounds   Array containing the Z bounds from which the pixels should be retrieved.
     * @param tBounds   Array containing the T bounds from which the pixels should be retrieved.
     *
     * @return 5D bounds.
     */
    public static Bounds getBounds(int[] xBounds,
                                   int[] yBounds,
                                   int[] cBounds,
                                   int[] zBounds,
                                   int[] tBounds) {
        int[][] limits = new int[5][];
        limits[0] = checkBounds(xBounds);
        limits[1] = checkBounds(yBounds);
        limits[2] = checkBounds(cBounds);
        limits[3] = checkBounds(zBounds);
        limits[4] = checkBounds(tBounds);
        Coordinates start = new Coordinates(limits[0][0],
                                            limits[1][0],
                                            limits[2][0],
                                            limits[3][0],
                                            limits[4][0]);
        Coordinates end = new Coordinates(limits[0][1],
                                          limits[1][1],
                                          limits[2][1],
                                          limits[3][1],
                                          limits[4][1]);
        return new Bounds(start, end);
    }


    /**
     * Checks bounds.
     * <br>If, for a given axis, the lower bound is outside [0 - imageSize-1], set the value to 0.
     * <br>Conversely, if the higher bound is outside [0 - imageSize-1], the resulting value will be imageSize-1.
     *
     * @param imageSize Size of the image.
     *
     * @return Corrected bounds.
     */
    public Bounds checkBounds(Coordinates imageSize) {
        int[][] b = new int[5][];
        int[][] l = new int[5][];

        l[0] = new int[]{0, imageSize.getX() - 1};
        l[1] = new int[]{0, imageSize.getY() - 1};
        l[2] = new int[]{0, imageSize.getC() - 1};
        l[3] = new int[]{0, imageSize.getZ() - 1};
        l[4] = new int[]{0, imageSize.getT() - 1};

        b[0] = new int[]{start.getX(), start.getX() + size.getX() - 1};
        b[1] = new int[]{start.getY(), start.getY() + size.getY() - 1};
        b[2] = new int[]{start.getC(), start.getC() + size.getC() - 1};
        b[3] = new int[]{start.getZ(), start.getZ() + size.getZ() - 1};
        b[4] = new int[]{start.getT(), start.getT() + size.getT() - 1};

        for (int i = 0; i < 5; i++) {
            b[i][0] = b[i][0] >= l[i][0] && b[i][0] <= b[i][1] ? b[i][0] : l[i][0];
            b[i][1] = b[i][1] >= b[i][0] && b[i][1] <= l[i][1] ? b[i][1] : l[i][1];
        }
        return getBounds(b[0], b[1], b[2], b[3], b[4]);
    }


    /**
     * Checks if bound arrays are valid (not null and length >= 2).
     * <br>If not, sets both bounds to -1 (indicating full range).
     *
     * @param bounds    Array containing the specified bounds for 1 coordinate.
     *
     * @return New array with bounds.
     */
    private static int[] checkBounds(int[] bounds) {
        int[] b = {-1, -1};
        if (bounds != null && bounds.length > 1) {
            b[0] = bounds[0] >= 0 ? bounds[0] : b[0];
            b[1] = bounds[1] >= 0 ? bounds[1] : b[1];
        }
        return b;
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
