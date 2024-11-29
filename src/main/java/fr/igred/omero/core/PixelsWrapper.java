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

package fr.igred.omero.core;


import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.Bounds;
import fr.igred.omero.util.Coordinates;
import ome.units.unit.Unit;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DataSourceException;
import omero.gateway.facility.RawDataFacility;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlaneInfoData;
import omero.gateway.rnd.Plane2D;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.core.PlaneInfoWrapper.getMinPosition;
import static fr.igred.omero.exception.ExceptionHandler.call;
import static ome.formats.model.UnitsFactory.convertLength;


/**
 * Class containing a PixelData object.
 * <p> Wraps function calls to the PixelData contained.
 */
public class PixelsWrapper extends ObjectWrapper<PixelsData> {

    /** Size of tiles when retrieving pixels */
    public static final int MAX_DIST = 5000;

    /** Planes info (needs to be loaded) */
    private List<PlaneInfoWrapper> planesInfo = new ArrayList<>(0);

    /** Raw Data Facility to retrieve pixels */
    private RawDataFacility rawDataFacility;


    /**
     * Constructor of the PixelsWrapper class
     *
     * @param pixels The PixelData to be wrap.
     */
    public PixelsWrapper(PixelsData pixels) {
        super(pixels);
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
        int startX = start.getX();
        int startY = start.getY();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tab[startY + y][startX + x] = p.getPixelValue(x, y);
            }
        }
    }


    /**
     * Copies the value from the plane at the corresponding position in the array
     *
     * @param bytes    Array containing the results.
     * @param p        Plane2D containing the voxels value.
     * @param start    Starting pixel coordinates.
     * @param width    Width of the plane.
     * @param height   Height of the plane.
     * @param imgWidth Width of the image.
     * @param bpp      Bytes per pixels of the image.
     */
    private static void copy(byte[] bytes, Plane2D p, Coordinates start, int width, int height, int imgWidth, int bpp) {
        int x0 = start.getX();
        int y0 = start.getY();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int i = 0; i < bpp; i++) {
                    bytes[((y + y0) * imgWidth + x + x0) * bpp + i] = p.getRawValue((x + y * width) * bpp + i);
                }
            }
        }
    }


    /**
     * Checks bounds.
     * <br>If the lower bound is outside [0 - imageSize-1], the resulting value will be 0.
     * <br>Conversely, if the higher bound is outside [0 - imageSize-1], the resulting value will be imageSize-1.
     *
     * @param bounds    Array containing the specified bounds for 1 coordinate.
     * @param imageSize Size of the image (in the corresponding dimension).
     *
     * @return New array with valid bounds.
     */
    private static int[] checkBounds(int[] bounds, int imageSize) {
        int[] b = {0, imageSize - 1};
        if (bounds != null && bounds.length > 1) {
            b[0] = bounds[0] >= b[0] && bounds[0] <= b[1] ? bounds[0] : b[0];
            b[1] = bounds[1] >= b[0] && bounds[1] <= b[1] ? bounds[1] : b[1];
        }
        return b;
    }


    /**
     * Loads the planes information.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void loadPlanesInfo(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<PlaneInfoData> planes = call(browser.getMetadataFacility(),
                                          m -> m.getPlaneInfos(browser.getCtx(),
                                                               data),
                                          "Cannot retrieve planes info.");
        planesInfo = wrap(planes, PlaneInfoWrapper::new);
    }


    /**
     * Retrieves the planes information (which need to be {@link #loadPlanesInfo(Browser) loaded} first).
     *
     * @return See above.
     */
    public List<PlaneInfoWrapper> getPlanesInfo() {
        return Collections.unmodifiableList(planesInfo);
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
     * Gets the time increment between time points.
     *
     * @return Time increment between time points.
     */
    public Time getTimeIncrement() {
        return data.asPixels().getTimeIncrement();
    }


    /**
     * Computes the mean time interval from the planes deltaTs.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    public Time getMeanTimeInterval() {
        return PlaneInfoWrapper.computeMeanTimeInterval(planesInfo, getSizeT());
    }


    /**
     * Computes the mean exposure time for a given channel from the planes exposureTime.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @param channel The channel index.
     *
     * @return See above.
     */
    public Time getMeanExposureTime(int channel) {
        return PlaneInfoWrapper.computeMeanExposureTime(planesInfo, channel);
    }


    /**
     * Retrieves the X stage position, using the same unit as {@link #getPixelSizeX()} if possible.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    public Length getPositionX() {
        Length x = getMinPosition(planesInfo, PlaneInfoWrapper::getPositionX);

        ome.units.quantity.Length pixSizeX = convertLength(getPixelSizeX());
        ome.units.quantity.Length posX     = convertLength(x);

        if (pixSizeX != null) {
            Unit<ome.units.quantity.Length> unit = pixSizeX.unit();
            if (posX.value(unit) != null) {
                x = new LengthI(posX.value(unit).doubleValue(), unit);
            }
        }

        return x;
    }


    /**
     * Retrieves the Y stage position, using the same unit as {@link #getPixelSizeY()} if possible.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    public Length getPositionY() {
        Length y = getMinPosition(planesInfo, PlaneInfoWrapper::getPositionY);

        ome.units.quantity.Length pixSizeY = convertLength(getPixelSizeY());
        ome.units.quantity.Length posY     = convertLength(y);

        if (pixSizeY != null) {
            Unit<ome.units.quantity.Length> unit = pixSizeY.unit();
            if (posY.value(unit) != null) {
                y = new LengthI(posY.value(unit).doubleValue(), unit);
            }
        }

        return y;
    }


    /**
     * Retrieves the Z stage position, using the same unit as {@link #getPixelSizeZ()} if possible.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    public Length getPositionZ() {
        Length z = getMinPosition(planesInfo, PlaneInfoWrapper::getPositionZ);

        ome.units.quantity.Length pixSizeZ = convertLength(getPixelSizeZ());
        ome.units.quantity.Length posZ     = convertLength(z);

        if (pixSizeZ != null) {
            Unit<ome.units.quantity.Length> unit = pixSizeZ.unit();
            if (posZ.value(unit) != null) {
                z = new LengthI(posZ.value(unit).doubleValue(), unit);
            }
        }

        return z;
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
     * @param conn The connection handler.
     *
     * @return <ul><li>True if a new RawDataFacility was created</li>
     * <li>False otherwise</li></ul>
     *
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    boolean createRawDataFacility(ConnectionHandler conn) throws ExecutionException {
        boolean created = false;
        if (rawDataFacility == null) {
            rawDataFacility = conn.getGateway()
                                  .getFacility(RawDataFacility.class);
            created         = true;
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
     * Returns an array containing the value for each voxel
     *
     * @param conn The connection handler.
     *
     * @return Array containing the value for each voxel of the image.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public double[][][][][] getAllPixels(ConnectionHandler conn)
    throws AccessException, ExecutionException {
        return getAllPixels(conn, null, null, null, null, null);
    }


    /**
     * Returns an array containing the value for each voxel corresponding to the bounds
     *
     * @param conn    The connection handler.
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
    public double[][][][][] getAllPixels(ConnectionHandler conn,
                                         int[] xBounds,
                                         int[] yBounds,
                                         int[] cBounds,
                                         int[] zBounds,
                                         int[] tBounds)
    throws AccessException, ExecutionException {
        boolean rdf = createRawDataFacility(conn);
        Bounds  lim = getBounds(xBounds, yBounds, cBounds, zBounds, tBounds);

        Coordinates start = lim.getStart();
        Coordinates size  = lim.getSize();

        int x0 = start.getX();
        int y0 = start.getY();
        int sx = size.getX();
        int sy = size.getY();

        int startC = start.getC();
        int startZ = start.getZ();
        int startT = start.getT();
        int sizeC  = size.getC();
        int sizeZ  = size.getZ();
        int sizeT  = size.getT();

        double[][][][][] tab = new double[sizeT][sizeZ][sizeC][][];

        for (int t = 0, posT = startT; t < sizeT; t++, posT++) {
            for (int z = 0, posZ = startZ; z < sizeZ; z++, posZ++) {
                for (int c = 0, posC = startC; c < sizeC; c++, posC++) {
                    Coordinates pos = new Coordinates(x0, y0, posC, posZ, posT);
                    tab[t][z][c] = getTile(conn, pos, sx, sy);
                }
            }
        }

        if (rdf) {
            destroyRawDataFacility();
        }
        return tab;
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     *
     * @param conn   The connection handler.
     * @param start  Start position of the tile.
     * @param width  Width of the tile.
     * @param height Height of the tile.
     *
     * @return 2D array containing tile pixel values (as double).
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    double[][] getTile(ConnectionHandler conn, Coordinates start, int width, int height)
    throws AccessException, ExecutionException {
        boolean rdf = createRawDataFacility(conn);
        double[][] tile = ExceptionHandler.of(this,
                                              t -> t.getTileUnchecked(conn.getCtx(), start, width, height))
                                          .rethrow(DataSourceException.class,
                                                   AccessException::new,
                                                   "Cannot read tile")
                                          .get();
        if (rdf) {
            destroyRawDataFacility();
        }
        return tile;
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     * <p>The {@link #rawDataFacility} has to be created first.</p>
     *
     * @param ctx    The {@link SecurityContext}.
     * @param start  Start position of the tile.
     * @param width  Width of the tile.
     * @param height Height of the tile.
     *
     * @return 2D array containing tile pixel values (as double).
     *
     * @throws DataSourceException If an error occurs while retrieving the plane data from the pixels source.
     */
    private double[][] getTileUnchecked(SecurityContext ctx, Coordinates start, int width, int height)
    throws DataSourceException {
        double[][] tile = new double[height][width];

        int c = start.getC();
        int z = start.getZ();
        int t = start.getT();

        for (int relX = 0, x = start.getX(); relX < width; relX += MAX_DIST, x += MAX_DIST) {
            int sizeX = Math.min(MAX_DIST, width - relX);
            for (int relY = 0, y = start.getY(); relY < height; relY += MAX_DIST, y += MAX_DIST) {
                int         sizeY = Math.min(MAX_DIST, height - relY);
                Plane2D     p     = rawDataFacility.getTile(ctx, data, z, t, c, x, y, sizeX, sizeY);
                Coordinates pos   = new Coordinates(relX, relY, c, z, t);
                copy(tile, p, pos, sizeX, sizeY);
            }
        }
        return tile;
    }


    /**
     * Returns an array containing the raw values for each voxel for each planes
     *
     * @param conn The connection handler.
     * @param bpp  Bytes per pixels of the image.
     *
     * @return a table of bytes containing the pixel values
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public byte[][][][] getRawPixels(ConnectionHandler conn, int bpp)
    throws AccessException, ExecutionException {
        return getRawPixels(conn, null, null, null, null, null, bpp);
    }


    /**
     * Returns an array containing the raw values for each voxel for each plane corresponding to the bounds
     *
     * @param conn    The connection handler.
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
    public byte[][][][] getRawPixels(ConnectionHandler conn,
                                     int[] xBounds,
                                     int[] yBounds,
                                     int[] cBounds,
                                     int[] zBounds,
                                     int[] tBounds,
                                     int bpp)
    throws ExecutionException, AccessException {
        boolean rdf = createRawDataFacility(conn);
        Bounds  lim = getBounds(xBounds, yBounds, cBounds, zBounds, tBounds);

        Coordinates start = lim.getStart();
        Coordinates size  = lim.getSize();

        int x0     = start.getX();
        int y0     = start.getY();
        int startC = start.getC();
        int startZ = start.getZ();
        int startT = start.getT();

        int sx    = size.getX();
        int sy    = size.getY();
        int sizeC = size.getC();
        int sizeZ = size.getZ();
        int sizeT = size.getT();

        byte[][][][] bytes = new byte[sizeT][sizeZ][sizeC][];

        for (int t = 0, posT = startT; t < sizeT; t++, posT++) {
            for (int z = 0, posZ = startZ; z < sizeZ; z++, posZ++) {
                for (int c = 0, posC = startC; c < sizeC; c++, posC++) {
                    Coordinates pos = new Coordinates(x0, y0, posC, posZ, posT);
                    bytes[t][z][c] = getRawTile(conn, pos, sx, sy, bpp);
                }
            }
        }
        if (rdf) {
            destroyRawDataFacility();
        }
        return bytes;
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     *
     * @param conn   The connection handler.
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
    byte[] getRawTile(ConnectionHandler conn, Coordinates start, int width, int height, int bpp)
    throws AccessException, ExecutionException {
        boolean rdf = createRawDataFacility(conn);
        byte[] tile = ExceptionHandler.of(this,
                                          t -> t.getRawTileUnchecked(conn.getCtx(), start, width, height, bpp))
                                      .rethrow(DataSourceException.class,
                                               AccessException::new,
                                               "Cannot read raw tile")
                                      .get();
        if (rdf) {
            destroyRawDataFacility();
        }
        return tile;
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     * <p>The {@link #rawDataFacility} has to be created first.</p>
     *
     * @param ctx    The {@link SecurityContext}.
     * @param start  Start position of the tile.
     * @param width  Width of the tile.
     * @param height Height of the tile.
     * @param bpp    Bytes per pixels of the image.
     *
     * @return Array of bytes containing the pixel values.
     *
     * @throws DataSourceException If an error occurs while retrieving the plane data from the pixels source.
     */
    private byte[] getRawTileUnchecked(SecurityContext ctx, Coordinates start, int width, int height, int bpp)
    throws DataSourceException {
        byte[] tile = new byte[height * width * bpp];

        int c = start.getC();
        int z = start.getZ();
        int t = start.getT();

        for (int relX = 0, x = start.getX(); relX < width; relX += MAX_DIST, x += MAX_DIST) {
            int sizeX = Math.min(MAX_DIST, width - relX);
            for (int relY = 0, y = start.getY(); relY < height; relY += MAX_DIST, y += MAX_DIST) {
                int         sizeY = Math.min(MAX_DIST, height - relY);
                Plane2D     p     = rawDataFacility.getTile(ctx, data, z, t, c, x, y, sizeX, sizeY);
                Coordinates pos   = new Coordinates(relX, relY, c, z, t);
                copy(tile, p, pos, sizeX, sizeY, width, bpp);
            }
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

}
