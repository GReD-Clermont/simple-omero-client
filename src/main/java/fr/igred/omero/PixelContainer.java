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
    static public final int        maxDist = 5000;
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
     * @throws DataSourceException If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException  A Facility can't be retrieved or instantiated.
     */
    public double[][][][][] getAllPixels(Client client) throws DataSourceException, ExecutionException {
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
     * @throws DataSourceException If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException  A Facility can't be retrieved or instantiated.
     */
    public double[][][][][] getAllPixels(Client client,
                                         int[] xBound,
                                         int[] yBound,
                                         int[] cBound,
                                         int[] zBound,
                                         int[] tBound)
    throws DataSourceException, ExecutionException {

        int sizeT, sizeZ, sizeC, sizeX, sizeY;
        int tStart, zStart, cStart, xStart, yStart;
        int tEnd, zEnd, cEnd, xEnd, yEnd;

        if (tBound != null) {
            tStart = Math.max(0, tBound[0]);
            tEnd = Math.min(pixels.getSizeT() - 1, tBound[1]);
        } else {
            tStart = 0;
            tEnd = pixels.getSizeT() - 1;
        }
        sizeT = tEnd - tStart + 1;

        if (zBound != null) {
            zStart = Math.max(0, zBound[0]);
            zEnd = Math.min(pixels.getSizeZ() - 1, zBound[1]);
        } else {
            zStart = 0;
            zEnd = pixels.getSizeZ() - 1;
        }
        sizeZ = zEnd - zStart + 1;

        if (cBound != null) {
            cStart = Math.max(0, cBound[0]);
            cEnd = Math.min(pixels.getSizeC() - 1, cBound[1]);
        } else {
            cStart = 0;
            cEnd = pixels.getSizeC() - 1;
        }
        sizeC = cEnd - cStart + 1;

        if (xBound != null) {
            xStart = Math.max(0, xBound[0]);
            xEnd = Math.min(pixels.getSizeX() - 1, xBound[1]);
        } else {
            xStart = 0;
            xEnd = pixels.getSizeX() - 1;
        }
        sizeX = xEnd - xStart + 1;

        if (yBound != null) {
            yStart = Math.max(0, yBound[0]);
            yEnd = Math.min(pixels.getSizeY() - 1, yBound[1]);
        } else {
            yStart = 0;
            yEnd = pixels.getSizeY() - 1;
        }
        sizeY = yEnd - yStart + 1;

        double[][][][][] tab = new double[sizeT][sizeZ][sizeC][sizeY][sizeX];

        Plane2D p;

        for (int z = zStart; z <= zEnd; z++) {
            for (int t = tStart; t <= tEnd; t++) {
                for (int c = cStart; c <= cEnd; c++) {
                    for (int x = xStart; x <= xEnd; x += maxDist) {
                        int width = x + maxDist <= xEnd ? maxDist : xEnd - x + 1;
                        for (int y = yStart; y <= yEnd; y += maxDist) {
                            int height = y + maxDist <= yEnd ? maxDist : yEnd - y + 1;

                            p = client.getRdf().getTile(client.getCtx(), pixels, z, t, c, x, y, width, height);

                            copy(tab, p, x - xStart, y - yStart, c - cStart, z - zStart, t - tStart, width, height);
                        }
                    }
                }
            }
        }

        return tab;
    }


    /**
     * Copies the value from the plane at the corresponding position in the array
     *
     * @param tab    Array containing the results.
     * @param p      Plane2D containing the voxels value.
     * @param x      X start.
     * @param y      Y start.
     * @param c      Value of the c axis.
     * @param z      Value of the z axis.
     * @param t      Value of the t axis.
     * @param width  Width of the plane.
     * @param height Height of the plane.
     */
    private void copy(double[][][][][] tab, Plane2D p, int x, int y, int c, int z, int t, int width, int height) {
        for (int iteX = 0; iteX < width; iteX++) {
            for (int iteY = 0; iteY < height; iteY++) {
                tab[t][z][c][iteY + y][iteX + x] = p.getPixelValue(iteX, iteY);
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
     * @throws DataSourceException If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException  A Facility can't be retrieved or instantiated.
     */
    public byte[][][][] getRawPixels(Client client, int bpp) throws DataSourceException, ExecutionException {
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
     * @throws DataSourceException If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException  A Facility can't be retrieved or instantiated.
     */
    public byte[][][][] getRawPixels(Client client,
                                     int[] xBound,
                                     int[] yBound,
                                     int[] cBound,
                                     int[] zBound,
                                     int[] tBound,
                                     int bpp)
    throws DataSourceException, ExecutionException {

        int sizeT, sizeZ, sizeC, sizeX, sizeY;
        int tStart, zStart, cStart, xStart, yStart;
        int tEnd, zEnd, cEnd, xEnd, yEnd;

        if (tBound != null) {
            tStart = Math.max(0, tBound[0]);
            tEnd = Math.min(pixels.getSizeT() - 1, tBound[1]);
        } else {
            tStart = 0;
            tEnd = pixels.getSizeT() - 1;
        }
        sizeT = tEnd - tStart + 1;

        if (zBound != null) {
            zStart = Math.max(0, zBound[0]);
            zEnd = Math.min(pixels.getSizeZ() - 1, zBound[1]);
        } else {
            zStart = 0;
            zEnd = pixels.getSizeZ() - 1;
        }
        sizeZ = zEnd - zStart + 1;

        if (cBound != null) {
            cStart = Math.max(0, cBound[0]);
            cEnd = Math.min(pixels.getSizeC() - 1, cBound[1]);
        } else {
            cStart = 0;
            cEnd = pixels.getSizeC() - 1;
        }
        sizeC = cEnd - cStart + 1;

        if (xBound != null) {
            xStart = Math.max(0, xBound[0]);
            xEnd = Math.min(pixels.getSizeX() - 1, xBound[1]);
        } else {
            xStart = 0;
            xEnd = pixels.getSizeX() - 1;
        }
        sizeX = xEnd - xStart + 1;

        if (yBound != null) {
            yStart = Math.max(0, yBound[0]);
            yEnd = Math.min(pixels.getSizeY() - 1, yBound[1]);
        } else {
            yStart = 0;
            yEnd = pixels.getSizeY() - 1;
        }
        sizeY = yEnd - yStart + 1;

        byte[][][][] bytes = new byte[sizeT][sizeZ][sizeC][sizeX * sizeY * bpp];

        Plane2D p;

        for (int z = zStart; z <= zEnd; z++) {
            for (int t = tStart; t <= tEnd; t++) {
                for (int c = cStart; c <= cEnd; c++) {
                    for (int x = xStart; x <= xEnd; x += maxDist) {
                        int width = x + maxDist <= xEnd ? maxDist : xEnd - x + 1;
                        for (int y = yStart; y <= yEnd; y += maxDist) {
                            int height = y + maxDist <= yEnd ? maxDist : yEnd - y + 1;

                            p = client.getRdf().getTile(client.getCtx(), pixels, z, t, c, x, y, width, height);

                            copy(bytes, p, x - xStart, y - yStart, c - cStart, z - zStart, t - tStart, width, height,
                                 sizeX, bpp);
                        }
                    }
                }
            }
        }

        return bytes;
    }


    /**
     * Copies the value from the plane at the corresponding position in the array
     *
     * @param bytes     Array containing the results.
     * @param p         Plane2D containing the voxels value.
     * @param x         X start.
     * @param y         Y start.
     * @param c         Value of the c axis.
     * @param z         Value of the z axis.
     * @param t         Value of the t axis.
     * @param width     Width of the plane.
     * @param height    Height of the plane.
     * @param trueWidth Width of the image.
     * @param bpp       Bytes per pixels of the image.
     */
    private void copy(byte[][][][] bytes,
                      Plane2D p,
                      int x,
                      int y,
                      int c,
                      int z,
                      int t,
                      int width,
                      int height,
                      int trueWidth,
                      int bpp) {

        for (int iteX = 0; iteX < width; iteX++)
            for (int iteY = 0; iteY < height; iteY++)
                for (int i = 0; i < bpp; i++)
                    bytes[t][z][c][((iteY + y) * trueWidth + iteX + x) * bpp + i] =
                            p.getRawValue((iteX + iteY * width) * bpp + i);
    }

}
