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


import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.Bounds;
import fr.igred.omero.util.Coordinates;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import loci.formats.FormatTools;
import ome.units.unit.Unit;
import omero.api.ResolutionDescription;
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

import static fr.igred.omero.core.PlaneInfo.getMinPosition;
import static fr.igred.omero.exception.ExceptionHandler.call;
import static loci.common.DataTools.makeDataArray;
import static ome.formats.model.UnitsFactory.convertLength;


/**
 * Class containing a PixelData object.
 * <p> Wraps function calls to the PixelData contained.
 */
public class PixelsWrapper extends ObjectWrapper<PixelsData> implements Pixels {

    /** Size of tiles when retrieving pixels */
    public static final int MAX_DIST = 5000;

    /** Planes info (needs to be loaded) */
    private List<PlaneInfo> planesInfo = new ArrayList<>(0);

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
     * Retrieves the resolution descriptions.
     *
     * @param conn The connection handler.
     *
     * @return See above.
     *
     * @throws AccessException If an error occurs while retrieving the resolution descriptions.
     */
    private List<ResolutionDescription> getResolutionDescriptions(ConnectionHandler conn)
    throws AccessException {
        return ExceptionHandler.of(rawDataFacility,
                                   rf -> rf.getResolutionDescriptions(conn.getCtx(), data))
                               .rethrow(DataSourceException.class,
                                        AccessException::new,
                                        "Cannot get resolution descriptions")
                               .get();
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
    @Override
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
    @Override
    public List<PlaneInfo> getPlanesInfo() {
        return Collections.unmodifiableList(planesInfo);
    }


    /**
     * Gets the pixel type.
     *
     * @return the pixel type.
     */
    @Override
    public String getPixelType() {
        return data.getPixelType();
    }


    /**
     * Gets the size of a single image pixel on the X axis.
     *
     * @return Size of a pixel on the X axis.
     */
    @Override
    public Length getPixelSizeX() {
        return data.asPixels().getPhysicalSizeX();
    }


    /**
     * Gets the size of a single image pixel on the Y axis.
     *
     * @return Size of a pixel on the Y axis.
     */
    @Override
    public Length getPixelSizeY() {
        return data.asPixels().getPhysicalSizeY();
    }


    /**
     * Gets the size of a single image pixel on the Z axis.
     *
     * @return Size of a pixel on the Z axis.
     */
    @Override
    public Length getPixelSizeZ() {
        return data.asPixels().getPhysicalSizeZ();
    }


    /**
     * Gets the time increment between time points.
     *
     * @return Time increment between time points.
     */
    @Override
    public Time getTimeIncrement() {
        return data.asPixels().getTimeIncrement();
    }


    /**
     * Computes the mean time interval from the planes deltaTs.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    @Override
    public Time getMeanTimeInterval() {
        return PlaneInfo.computeMeanTimeInterval(planesInfo, getSizeT());
    }


    /**
     * Computes the mean exposure time for a given channel from the planes exposureTime.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
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
     * Retrieves the X stage position, using the same unit as {@link #getPixelSizeX()} if possible.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    @Override
    public Length getPositionX() {
        Length x = getMinPosition(planesInfo, PlaneInfo::getPositionX);

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
    @Override
    public Length getPositionY() {
        Length y = getMinPosition(planesInfo, PlaneInfo::getPositionY);

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
    @Override
    public Length getPositionZ() {
        Length z = getMinPosition(planesInfo, PlaneInfo::getPositionZ);

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
     * Retrieves the available resolution levels for this image.
     *
     * @param conn The connection handler.
     *
     * @return See above.
     *
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws AccessException    If an error occurs while retrieving the resolution descriptions.
     */
    @Override
    public List<ResolutionLevel> getResolutionLevels(ConnectionHandler conn)
    throws ExecutionException, AccessException {
        boolean rdf = createRawDataFacility(conn);
        try {
            List<ResolutionDescription> desc = getResolutionDescriptions(conn);

            List<ResolutionLevel> res = new ArrayList<>(desc.size());
            for (int i = 0; i < desc.size(); i++) {
                ResolutionDescription d = desc.get(i);
                res.add(new ResolutionLevel(i, d.sizeX, d.sizeY));
            }
            return res;
        } finally {
            if (rdf) {
                destroyRawDataFacility();
            }
        }
    }


    /**
     * Gets the size of the image on the X axis
     *
     * @return Size of the image on the X axis.
     */
    @Override
    public int getSizeX() {
        return data.getSizeX();
    }


    /**
     * Gets the size of the image on the Y axis
     *
     * @return Size of the image on the Y axis.
     */
    @Override
    public int getSizeY() {
        return data.getSizeY();
    }


    /**
     * Gets the size of the image on the Z axis
     *
     * @return Size of the image on the Z axis.
     */
    @Override
    public int getSizeZ() {
        return data.getSizeZ();
    }


    /**
     * Gets the size of the image on the C axis
     *
     * @return Size of the image on the C axis.
     */
    @Override
    public int getSizeC() {
        return data.getSizeC();
    }


    /**
     * Gets the size of the image on the T axis
     *
     * @return Size of the image on the T axis.
     */
    @Override
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
     * Returns the image size at the specified resolution level.
     *
     * @param conn     The connection handler.
     * @param resLevel The resolution level.
     *
     * @return See above.
     *
     * @throws AccessException    If an error occurs while retrieving the resolution descriptions.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    Coordinates getSize(ConnectionHandler conn, int resLevel)
    throws AccessException, ExecutionException {
        int sx = getSizeX();
        int sy = getSizeY();
        if (resLevel >= 0) {
            List<ResolutionLevel> resLevels = getResolutionLevels(conn);
            if (resLevel < resLevels.size()) {
                ResolutionLevel res = resLevels.get(resLevel);
                sx = res.getSizeX();
                sy = res.getSizeY();
            }
        }
        return new Coordinates(sx, sy, getSizeC(), getSizeZ(), getSizeT());
    }


    /**
     * Checks that the resolution level is valid.
     *
     * @param conn     The connection handler.
     * @param resLevel The resolution level.
     *
     * @return The resolution level if it is valid, -1 otherwise.
     *
     * @throws AccessException    If an error occurs while retrieving the resolution descriptions.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    int checkResolutionLevel(ConnectionHandler conn, int resLevel)
    throws AccessException, ExecutionException {
        int level = -1;
        if (resLevel >= 0) {
            List<ResolutionLevel> resLevels = getResolutionLevels(conn);
            if (resLevel < resLevels.size()) {
                level = resLevel;
            }
        }
        return level;
    }


    /**
     * Returns an array containing the value for each voxel corresponding to the bounds
     *
     * @param conn   The connection handler.
     * @param bounds The bounds from which the pixels should be retrieved.
     *
     * @return Array containing the value for each voxel of the image.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public double[][][][][] getAllPixels(ConnectionHandler conn, Bounds bounds, int resLevel)
    throws AccessException, ExecutionException {
        boolean rdf = createRawDataFacility(conn);

        int         lvl     = checkResolutionLevel(conn, resLevel);
        Coordinates imgSize = getSize(conn, lvl);
        Bounds      checked = bounds.checkBounds(imgSize);

        Coordinates start = checked.getStart();
        Coordinates size  = checked.getSize();

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

        try {
            double[][][][][] tab = new double[sizeT][sizeZ][sizeC][][];
            for (int t = 0, posT = startT; t < sizeT; t++, posT++) {
                for (int z = 0, posZ = startZ; z < sizeZ; z++, posZ++) {
                    for (int c = 0, posC = startC; c < sizeC; c++, posC++) {
                        Coordinates pos = new Coordinates(x0, y0, posC, posZ, posT);
                        tab[t][z][c] = getTile(conn, pos, sx, sy, lvl);
                    }
                }
            }
            return tab;
        } finally {
            if (rdf) {
                destroyRawDataFacility();
            }
        }
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     *
     * @param conn   The connection handler.
     * @param start  Start position of the tile.
     * @param width  Width of the tile.
     * @param height Height of the tile.
     * @param resLvl The resolution level to retrieve the pixels from.
     *
     * @return 2D array containing tile pixel values (as double).
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    double[][] getTile(ConnectionHandler conn, Coordinates start, int width, int height, int resLvl)
    throws AccessException, ExecutionException {
        boolean rdf = createRawDataFacility(conn);
        try {
            return ExceptionHandler.of(conn.getCtx(),
                                       cx -> getTileUnchecked(cx, start, width, height, resLvl))
                                   .rethrow(DataSourceException.class,
                                            AccessException::new,
                                            "Cannot read tile")
                                   .get();
        } finally {
            if (rdf) {
                destroyRawDataFacility();
            }
        }
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     * <p>The {@link #rawDataFacility} has to be created first.</p>
     *
     * @param ctx    The {@link SecurityContext}.
     * @param start  Start position of the tile.
     * @param width  Width of the tile.
     * @param height Height of the tile.
     * @param resLvl The resolution level to retrieve the pixels from.
     *
     * @return 2D array containing tile pixel values (as double).
     *
     * @throws DataSourceException If an error occurs while retrieving the plane data from the pixels source.
     */
    private double[][] getTileUnchecked(SecurityContext ctx,
                                        Coordinates start,
                                        int width,
                                        int height,
                                        int resLvl)
    throws DataSourceException {
        double[][] tile = new double[height][width];

        int c = start.getC();
        int z = start.getZ();
        int t = start.getT();

        for (int relX = 0, x = start.getX(); relX < width; relX += MAX_DIST, x += MAX_DIST) {
            int w = Math.min(MAX_DIST, width - relX);
            for (int relY = 0, y = start.getY(); relY < height; relY += MAX_DIST, y += MAX_DIST) {
                int         h   = Math.min(MAX_DIST, height - relY);
                Plane2D     p   = rawDataFacility.getTile(ctx, data, z, t, c, x, y, w, h, resLvl);
                Coordinates pos = new Coordinates(relX, relY, c, z, t);
                copy(tile, p, pos, w, h);
            }
        }
        return tile;
    }


    /**
     * Returns an array containing the raw values for each voxel for each plane corresponding to the bounds
     *
     * @param conn     The connection handler.
     * @param bounds   The bounds from which the pixels should be retrieved.
     * @param bpp      Bytes per pixels of the image.
     * @param resLevel The resolution level to retrieve the pixels from.
     *
     * @return a table of bytes containing the pixel values
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public byte[][][][] getRawPixels(ConnectionHandler conn, Bounds bounds, int bpp, int resLevel)
    throws ExecutionException, AccessException {
        boolean rdf = createRawDataFacility(conn);

        int         lvl     = checkResolutionLevel(conn, resLevel);
        Coordinates imgSize = getSize(conn, lvl);
        Bounds      checked = bounds.checkBounds(imgSize);

        Coordinates start = checked.getStart();
        Coordinates size  = checked.getSize();

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

        try {
            byte[][][][] bytes = new byte[sizeT][sizeZ][sizeC][];
            for (int t = 0, posT = startT; t < sizeT; t++, posT++) {
                for (int z = 0, posZ = startZ; z < sizeZ; z++, posZ++) {
                    for (int c = 0, posC = startC; c < sizeC; c++, posC++) {
                        Coordinates pos = new Coordinates(x0, y0, posC, posZ, posT);
                        bytes[t][z][c] = getRawTile(conn, pos, sx, sy, bpp, lvl);
                    }
                }
            }
            return bytes;
        } finally {
            if (rdf) {
                destroyRawDataFacility();
            }
        }
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     *
     * @param conn     The connection handler.
     * @param start    Start position of the tile.
     * @param width    Width of the tile.
     * @param height   Height of the tile.
     * @param bpp      Bytes per pixels of the image.
     * @param resLevel The resolution level.
     *
     * @return Array of bytes containing the pixel values.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    byte[] getRawTile(ConnectionHandler conn,
                      Coordinates start,
                      int width,
                      int height,
                      int bpp,
                      int resLevel)
    throws AccessException, ExecutionException {
        boolean rdf = createRawDataFacility(conn);
        try {
            return ExceptionHandler.of(conn.getCtx(),
                                       cx -> getRawTileUnchecked(cx,
                                                                 start,
                                                                 width,
                                                                 height,
                                                                 bpp,
                                                                 resLevel))
                                   .rethrow(DataSourceException.class,
                                            AccessException::new,
                                            "Cannot read raw tile")
                                   .get();
        } finally {
            if (rdf) {
                destroyRawDataFacility();
            }
        }
    }


    /**
     * Gets the tile at the specified position, with the defined width and height.
     * <p>The {@link #rawDataFacility} has to be created first.</p>
     *
     * @param ctx      The {@link SecurityContext}.
     * @param start    Start position of the tile.
     * @param width    Width of the tile.
     * @param height   Height of the tile.
     * @param bpp      Bytes per pixels of the image.
     * @param resLevel Resolution level
     *
     * @return Array of bytes containing the pixel values.
     *
     * @throws DataSourceException If an error occurs while retrieving the plane data from the pixels source.
     */
    private byte[] getRawTileUnchecked(SecurityContext ctx,
                                       Coordinates start,
                                       int width,
                                       int height,
                                       int bpp,
                                       int resLevel)
    throws DataSourceException {
        byte[] tile = new byte[height * width * bpp];

        int c = start.getC();
        int z = start.getZ();
        int t = start.getT();

        for (int relX = 0, x = start.getX(); relX < width; relX += MAX_DIST, x += MAX_DIST) {
            int w = Math.min(MAX_DIST, width - relX);
            for (int relY = 0, y = start.getY(); relY < height; relY += MAX_DIST, y += MAX_DIST) {
                int         h   = Math.min(MAX_DIST, height - relY);
                Plane2D     p   = rawDataFacility.getTile(ctx, data, z, t, c, x, y, w, h, resLevel);
                Coordinates pos = new Coordinates(relX, relY, c, z, t);
                copy(tile, p, pos, w, h, width, bpp);
            }
        }
        return tile;
    }


    /**
     * Creates an ImagePlus within the specified boundaries, at the given resolution level.
     *
     * @param client   The client handling the connection.
     * @param limits   The boundaries.
     * @param resLevel The resolution level to retrieve.
     *
     * @return An ImagePlus from the IJ library.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public ImagePlus toImagePlus(Client client, Bounds limits, int resLevel)
    throws AccessException, ExecutionException, ServiceException {
        loadPlanesInfo(client);

        boolean rdf = createRawDataFacility(client);

        int lvl = checkResolutionLevel(client, resLevel);

        Coordinates size   = getSize(client, lvl);
        Bounds      bounds = limits.checkBounds(size);

        double xFactor = (double) size.getX() / getSizeX();
        double yFactor = (double) size.getY() / getSizeY();

        int x0 = bounds.getStart().getX();
        int y0 = bounds.getStart().getY();
        int c0 = bounds.getStart().getC();
        int z0 = bounds.getStart().getZ();
        int t0 = bounds.getStart().getT();

        int sx = bounds.getSize().getX();
        int sy = bounds.getSize().getY();
        int nc = bounds.getSize().getC();
        int nz = bounds.getSize().getZ();
        int nt = bounds.getSize().getT();

        int pixelType = FormatTools.pixelTypeFromString(data.getPixelType());
        int bpp       = FormatTools.getBytesPerPixel(pixelType);

        String name = String.valueOf(getId());
        if (data.getImage() != null) {
            name = data.getImage().getName();
        }

        ImagePlus imp = IJ.createHyperStack(name, sx, sy, nc, nz, nt, bpp * 8);

        Calibration calibration = imp.getCalibration();
        setCalibration(calibration, xFactor, yFactor);
        calibration.xOrigin -= x0;
        calibration.yOrigin -= y0;
        calibration.zOrigin -= z0;
        imp.setCalibration(calibration);

        boolean isFloat = FormatTools.isFloatingPoint(pixelType);

        ImageStack stack = imp.getImageStack();

        double min = imp.getProcessor().getMin();
        double max = 0;

        int progressTotal = imp.getStackSize();
        IJ.showProgress(0, progressTotal);
        try {
            for (int t = 0; t < nt; t++) {
                int posT = t + t0;
                for (int z = 0; z < nz; z++) {
                    int posZ = z + z0;
                    for (int c = 0; c < nc; c++) {
                        int posC = c + c0;

                        Coordinates pos = new Coordinates(x0, y0, posC, posZ, posT);

                        byte[] tiles = getRawTile(client, pos, sx, sy, bpp, lvl);

                        int n = imp.getStackIndex(c + 1, z + 1, t + 1);
                        stack.setPixels(makeDataArray(tiles, bpp, isFloat, false), n);
                        ImageProcessor ip = stack.getProcessor(n);
                        ip.resetMinAndMax();

                        max = Math.max(ip.getMax(), max);
                        min = Math.min(ip.getMin(), min);

                        stack.setProcessor(ip, n);
                        IJ.showProgress(n, progressTotal);
                    }
                }
            }
        } finally {
            IJ.showProgress(progressTotal, progressTotal);
            if (rdf) {
                destroyRawDataFacility();
            }
        }

        imp.setStack(stack);
        imp.setOpenAsHyperStack(true);
        imp.setDisplayMode(IJ.COMPOSITE);

        imp.getProcessor().setMinAndMax(min, max);
        imp.setPosition(1);
        if (IJ.getVersion().compareTo("1.53a") >= 0) {
            imp.setProp("IMAGE_POS_X", x0);
            imp.setProp("IMAGE_POS_Y", y0);
            imp.setProp("IMAGE_POS_C", c0);
            imp.setProp("IMAGE_POS_Z", z0);
            imp.setProp("IMAGE_POS_T", t0);
        }
        return imp;
    }


    /**
     * Sets the calibration. Planes information has to be loaded first.
     *
     * @param calibration The ImageJ calibration.
     * @param xFactor     The factor to apply to X spacing.
     * @param yFactor     The factor to apply to Y spacing.
     */
    private void setCalibration(Calibration calibration, double xFactor, double yFactor) {
        Length positionX = getPositionX();
        Length positionY = getPositionY();
        Length positionZ = getPositionZ();
        Length spacingX  = getPixelSizeX();
        Length spacingY  = getPixelSizeY();
        Length spacingZ  = getPixelSizeZ();
        Time   stepT     = getTimeIncrement();

        if (stepT == null) {
            stepT = getMeanTimeInterval();
        }

        calibration.setXUnit(positionX.getSymbol());
        calibration.setYUnit(positionY.getSymbol());
        calibration.setZUnit(positionZ.getSymbol());
        calibration.xOrigin = -positionX.getValue();
        calibration.yOrigin = -positionY.getValue();
        calibration.zOrigin = -positionZ.getValue();
        if (spacingX != null) {
            calibration.setXUnit(spacingX.getSymbol());
            calibration.pixelWidth = xFactor * spacingX.getValue();
            // positionX and spacingX should use the same unit
            calibration.xOrigin /= calibration.pixelWidth;
        }
        if (spacingY != null) {
            calibration.setYUnit(spacingY.getSymbol());
            calibration.pixelHeight = yFactor * spacingY.getValue();
            // positionY and spacingY should use the same unit
            calibration.yOrigin /= calibration.pixelHeight;
        }
        if (spacingZ != null) {
            calibration.setZUnit(spacingZ.getSymbol());
            calibration.pixelDepth = spacingZ.getValue();
            // positionZ and spacingZ should use the same unit
            calibration.zOrigin /= calibration.pixelDepth;
        }
        if (!Double.isNaN(stepT.getValue())) {
            calibration.setTimeUnit(stepT.getSymbol());
            calibration.frameInterval = stepT.getValue();
        }
    }

}
