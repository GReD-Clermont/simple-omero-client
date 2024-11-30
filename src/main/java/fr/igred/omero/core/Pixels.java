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


import fr.igred.omero.RemoteObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.PixelsData;
import omero.model.Length;
import omero.model.Time;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Pixels on OMERO.
 */
public interface Pixels extends RemoteObject {

    /**
     * Returns a {@link PixelsData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    PixelsData asDataObject();


    /**
     * Loads the planes information.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void loadPlanesInfo(Browser browser)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Retrieves the planes information (which need to be {@link #loadPlanesInfo(Browser) loaded} first).
     *
     * @return See above.
     */
    List<PlaneInfo> getPlanesInfo();


    /**
     * Gets the pixel type.
     *
     * @return the pixel type.
     */
    String getPixelType();


    /**
     * Gets the size of a single image pixel on the X axis.
     *
     * @return Size of a pixel on the X axis.
     */
    Length getPixelSizeX();


    /**
     * Gets the size of a single image pixel on the Y axis.
     *
     * @return Size of a pixel on the Y axis.
     */
    Length getPixelSizeY();


    /**
     * Gets the size of a single image pixel on the Z axis.
     *
     * @return Size of a pixel on the Z axis.
     */
    Length getPixelSizeZ();


    /**
     * Gets the time increment between time points.
     *
     * @return Time increment between time points.
     */
    Time getTimeIncrement();


    /**
     * Computes the mean time interval from the planes deltaTs.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    Time getMeanTimeInterval();


    /**
     * Computes the mean exposure time for a given channel from the planes exposureTime.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @param channel The channel index.
     *
     * @return See above.
     */
    Time getMeanExposureTime(int channel);


    /**
     * Retrieves the X stage position, using the same unit as {@link #getPixelSizeX()} if possible.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    Length getPositionX();


    /**
     * Retrieves the Y stage position, using the same unit as {@link #getPixelSizeY()} if possible.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    Length getPositionY();


    /**
     * Retrieves the Z stage position, using the same unit as {@link #getPixelSizeZ()} if possible.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Browser) loaded} first.</p>
     *
     * @return See above.
     */
    Length getPositionZ();


    /**
     * Gets the size of the image on the X axis
     *
     * @return Size of the image on the X axis.
     */
    int getSizeX();


    /**
     * Gets the size of the image on the Y axis
     *
     * @return Size of the image on the Y axis.
     */
    int getSizeY();


    /**
     * Gets the size of the image on the Z axis
     *
     * @return Size of the image on the Z axis.
     */
    int getSizeZ();


    /**
     * Gets the size of the image on the C axis
     *
     * @return Size of the image on the C axis.
     */
    int getSizeC();


    /**
     * Gets the size of the image on the T axis
     *
     * @return Size of the image on the T axis.
     */
    int getSizeT();


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
    default double[][][][][] getAllPixels(ConnectionHandler conn)
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
    double[][][][][] getAllPixels(ConnectionHandler conn,
                                  int[] xBounds,
                                  int[] yBounds,
                                  int[] cBounds,
                                  int[] zBounds,
                                  int[] tBounds)
    throws AccessException, ExecutionException;


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
    default byte[][][][] getRawPixels(ConnectionHandler conn, int bpp)
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
    byte[][][][] getRawPixels(ConnectionHandler conn,
                              int[] xBounds,
                              int[] yBounds,
                              int[] cBounds,
                              int[] zBounds,
                              int[] tBounds,
                              int bpp)
    throws ExecutionException, AccessException;

}
