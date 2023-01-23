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

package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.RemoteObjectWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.PlaneInfo;
import fr.igred.omero.meta.PlaneInfoWrapper;
import fr.igred.omero.util.Bounds;
import fr.igred.omero.util.Coordinates;
import ome.units.UNITS;
import ome.units.unit.Unit;
import omero.gateway.exception.DataSourceException;
import omero.gateway.facility.RawDataFacility;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlaneInfoData;
import omero.gateway.rnd.Plane2D;
import omero.model.Length;
import omero.model.Time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;
import static ome.formats.model.UnitsFactory.convertLength;


/**
 * Class containing a PixelData object.
 * <p> Wraps function calls to the PixelData contained.
 */
public class PixelsWrapper extends RemoteObjectWrapper<PixelsData> implements Pixels {

    /** Size of tiles when retrieving pixels */
    private static final int MAX_DIST = 5000;

    /** Planes info (needs to be loaded) */
    private List<PlaneInfo> planesInfo = new ArrayList<>(0);

    /** Raw Data Facility to retrieve pixels */
    private RawDataFacility rawDataFacility;


    /**
     * Constructor of the Pixels class
     *
     * @param dataObject PixelData to be contained.
     */
    public PixelsWrapper(PixelsData dataObject) {
        super(dataObject);
        rawDataFacility = null;
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
    private static void copy(double[][] tab, Plane2D p, Coordinates start, int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tab[start.getY() + y][start.getX() + x] = p.getPixelValue(x, y);
            }
        }
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
    private static void copy(byte[] bytes, Plane2D p, Coordinates start, int width, int height, int trueWidth,
                             int bpp) {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                for (int i = 0; i < bpp; i++)
                    bytes[((y + start.getY()) * trueWidth + x + start.getX()) * bpp + i] =
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
    private static int[] checkBounds(int[] bounds, int imageSize) {
        int[] newBounds = {0, imageSize - 1};
        if (bounds != null && bounds.length > 1) {
            newBounds[0] = Math.max(newBounds[0], bounds[0]);
            newBounds[1] = Math.min(newBounds[1], bounds[1]);
        }
        return newBounds;
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
        boolean rdf = createRawDataFacility(client);
        Plane2D p;

        double[][] tile = new double[height][width];
        for (int relX = 0, x = start.getX(); relX < width; relX += MAX_DIST, x += MAX_DIST) {
            int sizeX = Math.min(MAX_DIST, width - relX);
            for (int relY = 0, y = start.getY(); relY < height; relY += MAX_DIST, y += MAX_DIST) {
                int sizeY = Math.min(MAX_DIST, height - relY);
                try {
                    p = rawDataFacility.getTile(client.getCtx(), data, start.getZ(), start.getT(), start.getC(),
                                                x, y, sizeX, sizeY);
                } catch (DataSourceException dse) {
                    throw new AccessException("Cannot read tile", dse);
                }
                Coordinates pos = new Coordinates(relX, relY, start.getC(), start.getZ(), start.getT());
                copy(tile, p, pos, sizeX, sizeY);
            }
        }
        if (rdf) {
            destroyRawDataFacility();
        }
        return tile;
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
        boolean rdf = createRawDataFacility(client);
        Plane2D p;

        byte[] tile = new byte[height * width * bpp];
        for (int relX = 0, x = start.getX(); relX < width; relX += MAX_DIST, x += MAX_DIST) {
            int sizeX = Math.min(MAX_DIST, width - relX);
            for (int relY = 0, y = start.getY(); relY < height; relY += MAX_DIST, y += MAX_DIST) {
                int sizeY = Math.min(MAX_DIST, height - relY);
                try {
                    p = rawDataFacility.getTile(client.getCtx(), data, start.getZ(), start.getT(), start.getC(),
                                                x, y, sizeX, sizeY);
                } catch (DataSourceException dse) {
                    throw new AccessException("Cannot read raw tile", dse);
                }
                Coordinates pos = new Coordinates(relX, relY, start.getC(), start.getZ(), start.getT());
                copy(tile, p, pos, sizeX, sizeY, width, bpp);
            }
        }
        if (rdf) {
            destroyRawDataFacility();
        }
        return tile;
    }


    /**
     * Checks all bounds
     *
     * @param xBounds Array containing the X bounds from which the pixels should be retrieved.
     * @param yBounds Array containing the Y bounds from which the pixels should be retrieved.
     * @param cBounds Array containing the C bounds from which the pixels should be retrieved.
     * @param zBounds Array containing the Z bounds from which the pixels should be retrieved.
     * @param tBounds Array containing the T bounds from which the pixels should be retrieved.
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


    /**
     * Loads the planes information.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void loadPlanesInfo(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<PlaneInfoData> planes = handleServiceAndAccess(client.getMetadata(),
                                                            m -> m.getPlaneInfos(client.getCtx(), data),
                                                            "Cannot retrieve planes info.");
        planesInfo = wrap(planes, PlaneInfoWrapper::new);
    }


    /**
     * Retrieves the planes information (which need to be {@link #loadPlanesInfo(Client) loaded} first).
     *
     * @return See above.
     */
    @Override
    public List<PlaneInfo> getPlanesInfo() {
        return Collections.unmodifiableList(planesInfo);
    }


    /**
     * Computes the mean time interval from the planes deltaTs.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @return See above.
     */
    @Override
    public Time getMeanTimeInterval() {
        return PlaneInfo.computeMeanTimeInterval(planesInfo, getSizeT());
    }


    /**
     * Computes the mean exposure time for a given channel from the planes exposureTime.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @param channel The channel index.
     *
     * @return See above.
     */
    @Override
    public Time getMeanExposureTime(int channel) {
        return PlaneInfo.computeMeanExposureTime(planesInfo, channel);
    }


    /**
     * Retrieves the X stage position.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @return See above.
     */
    @Override
    public Length getPositionX() {
        ome.units.quantity.Length       pixSizeX = convertLength(getPixelSizeX());
        Unit<ome.units.quantity.Length> unit     = pixSizeX == null ? UNITS.MICROMETER : pixSizeX.unit();
        return PlaneInfo.getMinPosition(planesInfo, PlaneInfo::getPositionX, unit);
    }


    /**
     * Retrieves the Y stage position.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @return See above.
     */
    @Override
    public Length getPositionY() {
        ome.units.quantity.Length       pixSizeY = convertLength(getPixelSizeY());
        Unit<ome.units.quantity.Length> unit     = pixSizeY == null ? UNITS.MICROMETER : pixSizeY.unit();
        return PlaneInfo.getMinPosition(planesInfo, PlaneInfo::getPositionY, unit);
    }


    /**
     * Retrieves the Z stage position.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @return See above.
     */
    @Override
    public Length getPositionZ() {
        ome.units.quantity.Length       pixSizeZ = convertLength(getPixelSizeZ());
        Unit<ome.units.quantity.Length> unit     = pixSizeZ == null ? UNITS.MICROMETER : pixSizeZ.unit();
        return PlaneInfo.getMinPosition(planesInfo, PlaneInfo::getPositionZ, unit);
    }


    /**
     * Returns an array containing the value for each voxel corresponding to the bounds
     *
     * @param client  The client handling the connection.
     * @param xBounds Array containing the X bounds from which the pixels should be retrieved.
     * @param yBounds Array containing the Y bounds from which the pixels should be retrieved.
     * @param cBounds Array containing the C bounds from which the pixels should be retrieved.
     * @param zBounds Array containing the Z bounds from which the pixels should be retrieved.
     * @param tBounds Array containing the T bounds from which the pixels should be retrieved.
     *
     * @return Array containing the value for each voxel of the image.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public double[][][][][] getAllPixels(Client client,
                                         int[] xBounds,
                                         int[] yBounds,
                                         int[] cBounds,
                                         int[] zBounds,
                                         int[] tBounds)
    throws AccessException, ExecutionException {
        boolean rdf = createRawDataFacility(client);
        Bounds  lim = getBounds(xBounds, yBounds, cBounds, zBounds, tBounds);

        Coordinates start = lim.getStart();
        Coordinates size  = lim.getSize();

        double[][][][][] tab = new double[size.getT()][size.getZ()][size.getC()][][];

        for (int t = 0, posT = start.getT(); t < size.getT(); t++, posT++) {
            for (int z = 0, posZ = start.getZ(); z < size.getZ(); z++, posZ++) {
                for (int c = 0, posC = start.getC(); c < size.getC(); c++, posC++) {
                    Coordinates pos = new Coordinates(start.getX(), start.getY(), posC, posZ, posT);
                    tab[t][z][c] = getTile(client, pos, size.getX(), size.getY());
                }
            }
        }

        if (rdf) {
            destroyRawDataFacility();
        }
        return tab;
    }


    /**
     * Returns an array containing the raw values for each voxel for each plane corresponding to the bounds
     *
     * @param client  The client handling the connection.
     * @param xBounds Array containing the X bounds from which the pixels should be retrieved.
     * @param yBounds Array containing the Y bounds from which the pixels should be retrieved.
     * @param cBounds Array containing the C bounds from which the pixels should be retrieved.
     * @param zBounds Array containing the Z bounds from which the pixels should be retrieved.
     * @param tBounds Array containing the T bounds from which the pixels should be retrieved.
     * @param bpp     Bytes per pixels of the image.
     *
     * @return a table of bytes containing the pixel values
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public byte[][][][] getRawPixels(Client client,
                                     int[] xBounds,
                                     int[] yBounds,
                                     int[] cBounds,
                                     int[] zBounds,
                                     int[] tBounds,
                                     int bpp)
    throws ExecutionException, AccessException {
        boolean rdf = createRawDataFacility(client);
        Bounds  lim = getBounds(xBounds, yBounds, cBounds, zBounds, tBounds);

        Coordinates start = lim.getStart();
        Coordinates size  = lim.getSize();

        byte[][][][] bytes = new byte[size.getT()][size.getZ()][size.getC()][];

        for (int t = 0, posT = start.getT(); t < size.getT(); t++, posT++) {
            for (int z = 0, posZ = start.getZ(); z < size.getZ(); z++, posZ++) {
                for (int c = 0, posC = start.getC(); c < size.getC(); c++, posC++) {
                    Coordinates pos = new Coordinates(start.getX(), start.getY(), posC, posZ, posT);
                    bytes[t][z][c] = getRawTile(client, pos, size.getX(), size.getY(), bpp);
                }
            }
        }
        if (rdf) {
            destroyRawDataFacility();
        }
        return bytes;
    }

}
