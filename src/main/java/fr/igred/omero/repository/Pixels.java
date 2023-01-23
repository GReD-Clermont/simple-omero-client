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
import fr.igred.omero.RemoteObject;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.PlaneInfo;
import omero.gateway.model.PixelsData;
import omero.model.Length;
import omero.model.Time;

import java.util.List;
import java.util.concurrent.ExecutionException;


public interface Pixels extends RemoteObject<PixelsData> {


    /**
     * Loads the planes information.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void loadPlanesInfo(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Retrieves the planes information (which need to be {@link #loadPlanesInfo(Client) loaded} first).
     *
     * @return See above.
     */
    List<PlaneInfo> getPlanesInfo();


    /**
     * Gets the pixel type.
     *
     * @return the pixel type.
     */
    default String getPixelType() {
        return asDataObject().getPixelType();
    }


    /**
     * Gets the size of a single image pixel on the X axis.
     *
     * @return Size of a pixel on the X axis.
     */
    default Length getPixelSizeX() {
        return asDataObject().asPixels().getPhysicalSizeX();
    }


    /**
     * Gets the size of a single image pixel on the Y axis.
     *
     * @return Size of a pixel on the Y axis.
     */
    default Length getPixelSizeY() {
        return asDataObject().asPixels().getPhysicalSizeY();
    }


    /**
     * Gets the size of a single image pixel on the Z axis.
     *
     * @return Size of a pixel on the Z axis.
     */
    default Length getPixelSizeZ() {
        return asDataObject().asPixels().getPhysicalSizeZ();
    }


    /**
     * Gets the time increment between time points.
     *
     * @return Time increment between time points.
     */
    default Time getTimeIncrement() {
        return asDataObject().asPixels().getTimeIncrement();
    }


    /**
     * Computes the mean time interval from the planes deltaTs.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @return See above.
     */
    Time getMeanTimeInterval();


    /**
     * Computes the mean exposure time for a given channel from the planes exposureTime.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @param channel The channel index.
     *
     * @return See above.
     */
    Time getMeanExposureTime(int channel);


    /**
     * Retrieves the X stage position.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @return See above.
     */
    Length getPositionX();


    /**
     * Retrieves the Y stage position.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @return See above.
     */
    Length getPositionY();


    /**
     * Retrieves the Z stage position.
     * <p>Planes information needs to be {@link #loadPlanesInfo(Client) loaded} first.</p>
     *
     * @return See above.
     */
    Length getPositionZ();


    /**
     * Gets the size of the image on the X axis
     *
     * @return Size of the image on the X axis.
     */
    default int getSizeX() {
        return asDataObject().getSizeX();
    }


    /**
     * Gets the size of the image on the Y axis
     *
     * @return Size of the image on the Y axis.
     */
    default int getSizeY() {
        return asDataObject().getSizeY();
    }


    /**
     * Gets the size of the image on the Z axis
     *
     * @return Size of the image on the Z axis.
     */
    default int getSizeZ() {
        return asDataObject().getSizeZ();
    }


    /**
     * Gets the size of the image on the C axis
     *
     * @return Size of the image on the C axis.
     */
    default int getSizeC() {
        return asDataObject().getSizeC();
    }


    /**
     * Gets the size of the image on the T axis
     *
     * @return Size of the image on the T axis.
     */
    default int getSizeT() {
        return asDataObject().getSizeT();
    }


    /**
     * Returns an array containing the value for each voxel
     *
     * @param client The client handling the connection.
     *
     * @return Array containing the value for each voxel of the image.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default double[][][][][] getAllPixels(Client client) throws AccessException, ExecutionException {
        return getAllPixels(client, null, null, null, null, null);
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
    double[][][][][] getAllPixels(Client client,
                                  int[] xBounds,
                                  int[] yBounds,
                                  int[] cBounds,
                                  int[] zBounds,
                                  int[] tBounds)
    throws AccessException, ExecutionException;


    /**
     * Returns an array containing the raw values for each voxel for each planes
     *
     * @param client The client handling the connection.
     * @param bpp    Bytes per pixels of the image.
     *
     * @return a table of bytes containing the pixel values
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default byte[][][][] getRawPixels(Client client, int bpp) throws AccessException, ExecutionException {
        return getRawPixels(client, null, null, null, null, null, bpp);
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
    byte[][][][] getRawPixels(Client client,
                              int[] xBounds,
                              int[] yBounds,
                              int[] cBounds,
                              int[] zBounds,
                              int[] tBounds,
                              int bpp)
    throws ExecutionException, AccessException;

}
