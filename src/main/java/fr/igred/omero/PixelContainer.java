/*
 *  Copyright (C) 2020 GReD
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

package fr.igred.omero;


import fr.igred.omero.exception.AccessException;
import omero.gateway.exception.DataSourceException;
import omero.gateway.model.PixelsData;
import omero.gateway.rnd.Plane2D;
import omero.model.Length;

import java.util.concurrent.ExecutionException;


/**
 * Class containing a PixelData
 * <p> Implements function using the PixelData contained
 */
public class PixelContainer {

    /** Size of tiles when retrieving pixels */
    public static final int        MAX_DIST = 5000;
    /** PixelData contained */
    final               PixelsData pixels;


    /**
     * Constructor of the PixelContainer class
     *
     * @param pixels PixelData to be contained.
     */
    public PixelContainer(PixelsData pixels) {
        this.pixels = pixels;
    }


    /**
     * Gets the size of a single image pixel on the X axis.
     *
     * @return Size of a pixel on the X axis.
     */
    public Length getPixelSizeX() {
        return pixels.asPixels().getPhysicalSizeX();
    }


    /**
     * Gets the size of a single image pixel on the Y axis.
     *
     * @return Size of a pixel on the Y axis.
     */
    public Length getPixelSizeY() {
        return pixels.asPixels().getPhysicalSizeY();
    }


    /**
     * Gets the size of a single image pixel on the Z axis.
     *
     * @return Size of a pixel on the Z axis.
     */
    public Length getPixelSizeZ() {
        return pixels.asPixels().getPhysicalSizeZ();
    }


    public String getPixelType() {
        return pixels.getPixelType();
    }


    /**
     * Gets the size of the image on the X axis
     *
     * @return Size of the image on the X axis.
     */
    public int getSizeX() {
        return pixels.getSizeX();
    }


    /**
     * Gets the size of the image on the Y axis
     *
     * @return Size of the image on the Y axis.
     */
    public int getSizeY() {
        return pixels.getSizeY();
    }


    /**
     * Gets the size of the image on the Z axis
     *
     * @return Size of the image on the Z axis.
     */
    public int getSizeZ() {
        return pixels.getSizeZ();
    }


    /**
     * Gets the size of the image on the C axis
     *
     * @return Size of the image on the C axis.
     */
    public int getSizeC() {
        return pixels.getSizeC();
    }


    /**
     * Gets the size of the image on the T axis
     *
     * @return Size of the image on the T axis.
     */
    public int getSizeT() {
        return pixels.getSizeT();
    }


    /**
     * Returns an array containing the value for each voxels
     *
     * @param client The user.
     *
     * @return Array containing the value for each voxels of the image.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public double[][][][][] getAllPixels(Client client) throws AccessException, ExecutionException {
        return getAllPixels(client, null, null, null, null, null);
    }


    /**
     * Returns an array containing the value for each voxels corresponding to the bounds
     *
     * @param client The user.
     * @param xBound Array containing the X bound from which the pixels should be retrieved.
     * @param yBound Array containing the Y bound from which the pixels should be retrieved.
     * @param cBound Array containing the C bound from which the pixels should be retrieved.
     * @param zBound Array containing the Z bound from which the pixels should be retrieved.
     * @param tBound Array containing the T bound from which the pixels should be retrieved.
     *
     * @return Array containing the value for each voxels of the image.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public double[][][][][] getAllPixels(Client client,
                                         int[] xBound,
                                         int[] yBound,
                                         int[] cBound,
                                         int[] zBound,
                                         int[] tBound)
    throws AccessException, ExecutionException {
        int[] xLimits = checkBounds(xBound, pixels.getSizeX());
        int[] yLimits = checkBounds(yBound, pixels.getSizeY());
        int[] cLimits = checkBounds(cBound, pixels.getSizeC());
        int[] zLimits = checkBounds(zBound, pixels.getSizeZ());
        int[] tLimits = checkBounds(tBound, pixels.getSizeT());

        int sizeX = xLimits[1] - xLimits[0] + 1;
        int sizeY = yLimits[1] - yLimits[0] + 1;
        int sizeC = cLimits[1] - cLimits[0] + 1;
        int sizeZ = zLimits[1] - zLimits[0] + 1;
        int sizeT = tLimits[1] - tLimits[0] + 1;

        int xStart = xLimits[0];
        int yStart = yLimits[0];

        double[][][][][] tab = new double[sizeT][sizeZ][sizeC][][];

        for (int t = 0; t < sizeT; t++) {
            int posT = t + tLimits[0];
            for (int z = 0; z < sizeZ; z++) {
                int posZ = z + zLimits[0];
                for (int c = 0; c < sizeC; c++) {
                    int         posC = c + cLimits[0];
                    Coordinates pos  = new Coordinates(xStart, yStart, posC, posZ, posT);
                    tab[t][z][c] = getTile(client, pos, sizeX, sizeY);
                }
            }
        }

        return tab;
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     *
     * @param client The user.
     * @param start  Start position of the tile.
     * @param width  Width of the tile.
     * @param height Height of the tile.
     *
     * @return 2D array containing tile pixel values (as double).
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    double[][] getTile(Client client, Coordinates start, int width, int height)
    throws AccessException, ExecutionException {
        double[][] tile = new double[height][width];
        Plane2D    p;
        for (int x = start.x; x < start.x + width; x += MAX_DIST) {
            int posX  = x - start.x;
            int sizeX = Math.min(MAX_DIST, width - posX);

            for (int y = start.y; y < start.y + height; y += MAX_DIST) {
                int posY  = y - start.y;
                int sizeY = Math.min(MAX_DIST, height - posY);

                try {
                    p = client.getRdf().getTile(client.getCtx(), pixels, start.z, start.t, start.c, x, y, sizeX, sizeY);
                } catch (DataSourceException dse) {
                    throw new AccessException("Cannot read tile", dse);
                }

                Coordinates pos = new Coordinates(posX, posY, start.c, start.z, start.t);
                copy(tile, p, pos, sizeX, sizeY);
            }
        }
        return tile;
    }


    /**
     * Copies the value from the plane at the corresponding position in the 2D array
     *
     * @param tab    2D array containing the results.
     * @param p      Plane2D containing the voxels value.
     * @param start  Start position of the tile.
     * @param width  Width of the plane.
     * @param height Height of the plane.
     */
    private void copy(double[][] tab, Plane2D p, Coordinates start, int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tab[start.y + y][start.x + x] = p.getPixelValue(x, y);
            }
        }
    }


    /**
     * Returns an array containing the raw values for each voxels for each planes
     *
     * @param client The user.
     * @param bpp    Bytes per pixels of the image.
     *
     * @return a table of bytes containing the pixel values
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public byte[][][][] getRawPixels(Client client, int bpp) throws AccessException, ExecutionException {
        return getRawPixels(client, null, null, null, null, null, bpp);
    }


    /**
     * Returns an array containing the raw values for each voxels for each planes corresponding to the bounds
     *
     * @param client The user.
     * @param xBound Array containing the X bound from which the pixels should be retrieved.
     * @param yBound Array containing the Y bound from which the pixels should be retrieved.
     * @param cBound Array containing the C bound from which the pixels should be retrieved.
     * @param zBound Array containing the Z bound from which the pixels should be retrieved.
     * @param tBound Array containing the T bound from which the pixels should be retrieved.
     * @param bpp    Bytes per pixels of the image.
     *
     * @return a table of bytes containing the pixel values
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public byte[][][][] getRawPixels(Client client,
                                     int[] xBound,
                                     int[] yBound,
                                     int[] cBound,
                                     int[] zBound,
                                     int[] tBound,
                                     int bpp)
    throws ExecutionException, AccessException {
        int[] xLimits = checkBounds(xBound, pixels.getSizeX());
        int[] yLimits = checkBounds(yBound, pixels.getSizeY());
        int[] cLimits = checkBounds(cBound, pixels.getSizeC());
        int[] zLimits = checkBounds(zBound, pixels.getSizeZ());
        int[] tLimits = checkBounds(tBound, pixels.getSizeT());

        int sizeX = xLimits[1] - xLimits[0] + 1;
        int sizeY = yLimits[1] - yLimits[0] + 1;
        int sizeC = cLimits[1] - cLimits[0] + 1;
        int sizeZ = zLimits[1] - zLimits[0] + 1;
        int sizeT = tLimits[1] - tLimits[0] + 1;

        int xStart = xLimits[0];
        int yStart = yLimits[0];

        byte[][][][] bytes = new byte[sizeT][sizeZ][sizeC][];

        for (int t = 0; t < sizeT; t++) {
            int posT = t + tLimits[0];
            for (int z = 0; z < sizeZ; z++) {
                int posZ = z + zLimits[0];
                for (int c = 0; c < sizeC; c++) {
                    int         posC = c + cLimits[0];
                    Coordinates pos  = new Coordinates(xStart, yStart, posC, posZ, posT);
                    bytes[t][z][c] = getRawTile(client, pos, sizeX, sizeY, bpp);
                }
            }
        }

        return bytes;
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     *
     * @param client The user.
     * @param start  Start position of the tile.
     * @param width  Width of the tile.
     * @param height Height of the tile.
     * @param bpp    Bytes per pixels of the image.
     *
     * @return Array of bytes containing the pixel values.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    byte[] getRawTile(Client client, Coordinates start, int width, int height, int bpp)
    throws AccessException, ExecutionException {
        byte[]  tile = new byte[height * width * bpp];
        Plane2D p;
        for (int x = start.x; x < start.x + width; x += MAX_DIST) {
            int posX  = x - start.x;
            int sizeX = Math.min(MAX_DIST, width - posX);

            for (int y = start.y; y < start.y + height; y += MAX_DIST) {
                int posY  = y - start.y;
                int sizeY = Math.min(MAX_DIST, height - posY);

                try {
                    p = client.getRdf().getTile(client.getCtx(), pixels, start.z, start.t, start.c, x, y, sizeX, sizeY);
                } catch (DataSourceException dse) {
                    throw new AccessException("Cannot read tile", dse);
                }

                Coordinates pos = new Coordinates(posX, posY, start.c, start.z, start.t);
                copy(tile, p, pos, sizeX, sizeY, width, bpp);
            }
        }
        return tile;
    }


    /**
     * Copies the value from the plane at the corresponding position in the array
     *
     * @param bytes     Array containing the results.
     * @param p         Plane2D containing the voxels value.
     * @param start     Starting pixel coordinates.
     * @param width     Width of the plane.
     * @param height    Height of the plane.
     * @param trueWidth Width of the image.
     * @param bpp       Bytes per pixels of the image.
     */
    private void copy(byte[] bytes, Plane2D p, Coordinates start, int width, int height, int trueWidth, int bpp) {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                for (int i = 0; i < bpp; i++)
                    bytes[((y + start.y) * trueWidth + x + start.x) * bpp + i] =
                            p.getRawValue((x + y * width) * bpp + i);
    }


    /**
     * Checks bounds
     *
     * @param bounds    Array containing the specified bounds for 1 coordinate.
     * @param imageSize Size of the image (in the corresponding dimension).
     *
     * @return New array with valid bounds.
     */
    int[] checkBounds(int[] bounds, int imageSize) {
        int[] newBounds = {0, imageSize - 1};
        if (bounds != null && bounds.length > 1) {
            newBounds[0] = Math.max(newBounds[0], bounds[0]);
            newBounds[1] = Math.min(newBounds[1], bounds[1]);
        }
        return newBounds;
    }


    /** Class containing 5D pixel coordinates */
    static class Coordinates {

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
        Coordinates(int x, int y, int c, int z, int t) {
            this.x = x;
            this.y = y;
            this.c = c;
            this.z = z;
            this.t = t;
        }

    }

}
