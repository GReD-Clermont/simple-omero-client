/*
 *  Copyright (C) 2020-2021 GReD
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

package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.GenericObjectWrapper;
import fr.igred.omero.exception.AccessException;
import omero.gateway.exception.DataSourceException;
import omero.gateway.facility.RawDataFacility;
import omero.gateway.model.PixelsData;
import omero.gateway.rnd.Plane2D;
import omero.model.Length;

import java.util.concurrent.ExecutionException;


/**
 * Class containing a PixelData
 * <p> Implements function using the PixelData contained
 */
public class PixelsWrapper extends GenericObjectWrapper<PixelsData> {

    /** Size of tiles when retrieving pixels */
    public static final int MAX_DIST = 5000;

    /** Raw Data Facility to retrieve pixels */
    private RawDataFacility rawDataFacility;


    /**
     * Constructor of the PixelsWrapper class
     *
     * @param pixels PixelData to be contained.
     */
    public PixelsWrapper(PixelsData pixels) {
        super(pixels);
    }


    /**
     * Gets the pixel type.
     *
     * @return the pixel type.
     */
    public String getPixelType() {
        return data.getPixelType();
    }


    /**
     * Gets the size of a single image pixel on the X axis.
     *
     * @return Size of a pixel on the X axis.
     */
    public Length getPixelSizeX() {
        return data.asPixels().getPhysicalSizeX();
    }


    /**
     * Gets the size of a single image pixel on the Y axis.
     *
     * @return Size of a pixel on the Y axis.
     */
    public Length getPixelSizeY() {
        return data.asPixels().getPhysicalSizeY();
    }


    /**
     * Gets the size of a single image pixel on the Z axis.
     *
     * @return Size of a pixel on the Z axis.
     */
    public Length getPixelSizeZ() {
        return data.asPixels().getPhysicalSizeZ();
    }


    /**
     * Gets the size of the image on the X axis
     *
     * @return Size of the image on the X axis.
     */
    public int getSizeX() {
        return data.getSizeX();
    }


    /**
     * Gets the size of the image on the Y axis
     *
     * @return Size of the image on the Y axis.
     */
    public int getSizeY() {
        return data.getSizeY();
    }


    /**
     * Gets the size of the image on the Z axis
     *
     * @return Size of the image on the Z axis.
     */
    public int getSizeZ() {
        return data.getSizeZ();
    }


    /**
     * Gets the size of the image on the C axis
     *
     * @return Size of the image on the C axis.
     */
    public int getSizeC() {
        return data.getSizeC();
    }


    /**
     * Gets the size of the image on the T axis
     *
     * @return Size of the image on the T axis.
     */
    public int getSizeT() {
        return data.getSizeT();
    }


    /**
     * Creates a {@link omero.gateway.facility.RawDataFacility} to retrieve the pixel values.
     *
     * @param client The client handling the connection.
     *
     * @return <ul><li>True if a new RawDataFacility was created</li>
     * <li>False otherwise</li></ul>
     *
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    boolean createRawDataFacility(Client client) throws ExecutionException {
        boolean created = false;
        if (rawDataFacility == null) {
            rawDataFacility = client.getGateway().getFacility(RawDataFacility.class);
            created = true;
        }
        return created;
    }


    /**
     * Destroy the {@link omero.gateway.facility.RawDataFacility}.
     */
    void destroyRawDataFacility() {
        rawDataFacility.close();
        rawDataFacility = null;
    }


    /**
     * Returns an array containing the value for each voxels
     *
     * @param client The client handling the connection.
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
     * @param client The client handling the connection.
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
        boolean createdRawDataFacility = createRawDataFacility(client);
        Bounds  lim                    = getBounds(xBound, yBound, cBound, zBound, tBound);

        double[][][][][] tab = new double[lim.size.t][lim.size.z][lim.size.c][][];

        for (int t = 0, posT = lim.start.t; t < lim.size.t; t++, posT++) {
            for (int z = 0, posZ = lim.start.z; z < lim.size.z; z++, posZ++) {
                for (int c = 0, posC = lim.start.c; c < lim.size.c; c++, posC++) {
                    Coordinates pos = new Coordinates(lim.start.x, lim.start.y, posC, posZ, posT);
                    tab[t][z][c] = getTile(client, pos, lim.size.x, lim.size.y);
                }
            }
        }

        if (createdRawDataFacility) {
            destroyRawDataFacility();
        }
        return tab;
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     *
     * @param client The client handling the connection.
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
        boolean createdRawDataFacility = createRawDataFacility(client);
        Plane2D p;

        double[][] tile = new double[height][width];
        for (int relX = 0, x = start.x; relX < width; relX += MAX_DIST, x += MAX_DIST) {
            int sizeX = Math.min(MAX_DIST, width - relX);
            for (int relY = 0, y = start.y; relY < height; relY += MAX_DIST, y += MAX_DIST) {
                int sizeY = Math.min(MAX_DIST, height - relY);
                try {
                    p = rawDataFacility.getTile(client.getCtx(), data, start.z, start.t, start.c, x, y, sizeX, sizeY);
                } catch (DataSourceException dse) {
                    throw new AccessException("Cannot read tile", dse);
                }
                Coordinates pos = new Coordinates(relX, relY, start.c, start.z, start.t);
                copy(tile, p, pos, sizeX, sizeY);
            }
        }
        if (createdRawDataFacility) {
            destroyRawDataFacility();
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
     * @param client The client handling the connection.
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
     * @param client The client handling the connection.
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
        boolean createdRawDataFacility = createRawDataFacility(client);

        Bounds lim = getBounds(xBound, yBound, cBound, zBound, tBound);

        byte[][][][] bytes = new byte[lim.size.t][lim.size.z][lim.size.c][];

        for (int t = 0, posT = lim.start.t; t < lim.size.t; t++, posT++) {
            for (int z = 0, posZ = lim.start.z; z < lim.size.z; z++, posZ++) {
                for (int c = 0, posC = lim.start.c; c < lim.size.c; c++, posC++) {
                    Coordinates pos = new Coordinates(lim.start.x, lim.start.y, posC, posZ, posT);
                    bytes[t][z][c] = getRawTile(client, pos, lim.size.x, lim.size.y, bpp);
                }
            }
        }
        if (createdRawDataFacility) {
            destroyRawDataFacility();
        }
        return bytes;
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     *
     * @param client The client handling the connection.
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
        boolean createdRawDataFacility = createRawDataFacility(client);
        Plane2D p;

        byte[] tile = new byte[height * width * bpp];
        for (int relX = 0, x = start.x; relX < width; relX += MAX_DIST, x += MAX_DIST) {
            int sizeX = Math.min(MAX_DIST, width - relX);
            for (int relY = 0, y = start.y; relY < height; relY += MAX_DIST, y += MAX_DIST) {
                int sizeY = Math.min(MAX_DIST, height - relY);
                try {
                    p = rawDataFacility.getTile(client.getCtx(), data, start.z, start.t, start.c, x, y, sizeX, sizeY);
                } catch (DataSourceException dse) {
                    throw new AccessException("Cannot read raw tile", dse);
                }
                Coordinates pos = new Coordinates(relX, relY, start.c, start.z, start.t);
                copy(tile, p, pos, sizeX, sizeY, width, bpp);
            }
        }
        if (createdRawDataFacility) {
            destroyRawDataFacility();
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
    private int[] checkBounds(int[] bounds, int imageSize) {
        int[] newBounds = {0, imageSize - 1};
        if (bounds != null && bounds.length > 1) {
            newBounds[0] = Math.max(newBounds[0], bounds[0]);
            newBounds[1] = Math.min(newBounds[1], bounds[1]);
        }
        return newBounds;
    }


    /**
     * Checks all bounds
     *
     * @param xBounds Array containing the X bound from which the pixels should be retrieved.
     * @param yBounds Array containing the Y bound from which the pixels should be retrieved.
     * @param cBounds Array containing the C bound from which the pixels should be retrieved.
     * @param zBounds Array containing the Z bound from which the pixels should be retrieved.
     * @param tBounds Array containing the T bound from which the pixels should be retrieved.
     *
     * @return 5D bounds.
     */
    Bounds getBounds(int[] xBounds, int[] yBounds, int[] cBounds, int[] zBounds, int[] tBounds) {
        int[][] limits = new int[5][2];
        limits[0] = checkBounds(xBounds, data.getSizeX());
        limits[1] = checkBounds(yBounds, data.getSizeY());
        limits[2] = checkBounds(cBounds, data.getSizeC());
        limits[3] = checkBounds(zBounds, data.getSizeZ());
        limits[4] = checkBounds(tBounds, data.getSizeT());
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


    /** Class containing 5D pixel coordinates */
    public static class Coordinates {

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

    }


    /** Class containing 5D bounds coordinates */
    public static class Bounds {

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
            this.size = new Coordinates(end.x - start.x + 1,
                                        end.y - start.y + 1,
                                        end.c - start.c + 1,
                                        end.z - start.z + 1,
                                        end.t - start.t + 1);
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
            return new Coordinates(start.x + size.x - 1,
                                   start.y + size.y - 1,
                                   start.c + size.c - 1,
                                   start.z + size.z - 1,
                                   start.t + size.t - 1);
        }


        /**
         * Gets size of bounds for each coordinate.
         *
         * @return Bounds size.
         */
        public Coordinates getSize() {
            return size;
        }

    }

}
