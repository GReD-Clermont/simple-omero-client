/*
 *  Copyright (C) 2020-2023 GReD
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

package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.ContainerLinked;
import fr.igred.omero.HCSLinked;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import ij.ImagePlus;
import omero.gateway.model.ImageData;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Images on OMERO.
 */
public interface Image extends RepositoryObject, ContainerLinked, HCSLinked {

    /**
     * Returns a {@link ImageData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    ImageData asDataObject();


    /**
     * Sets the name of the image.
     *
     * @param name The name of the image. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Sets the description of the image.
     *
     * @param description The description of the image.
     */
    void setDescription(String description);


    /**
     * Gets the ImageData acquisition date
     *
     * @return acquisition date.
     */
    Timestamp getAcquisitionDate();


    /**
     * Returns this image, updated from OMERO, as a singleton list.
     *
     * @param client The client handling the connection.(unused).
     *
     * @return See above
     */
    @Override
    default List<? extends Image> getImages(Client client)
    throws AccessException, ServiceException, ExecutionException {
        return Collections.singletonList(client.getImage(getId()));
    }


    /**
     * Retrieves the wells containing this image.
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    List<? extends Well> getWells(Client client)
    throws AccessException, ServiceException, ExecutionException;


    /**
     * Returns the plate acquisitions linked to this image.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    List<? extends PlateAcquisition> getPlateAcquisitions(Client client)
    throws AccessException, ServiceException, ExecutionException;


    /**
     * Retrieves the plates containing this image.
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends Plate> getPlates(Client client)
    throws AccessException, ServiceException, ExecutionException;


    /**
     * Checks if image is orphaned (not in a WellSample nor linked to a dataset).
     *
     * @param client The client handling the connection.
     *
     * @return {@code true} if the image is orphaned, {@code false} otherwise.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError Server error.
     */
    boolean isOrphaned(Client client)
    throws ServiceException, OMEROServerError;


    /**
     * Returns the list of images sharing the same fileset as the current image.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws AccessException    Cannot access data.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError   Server error.
     */
    List<? extends Image> getFilesetImages(Client client)
    throws AccessException, ServiceException, ExecutionException, OMEROServerError;


    /**
     * Links ROIs to the image in OMERO.
     * <p> DO NOT USE IT IF A SHAPE WAS DELETED !!!
     *
     * @param client The client handling the connection.
     * @param rois   ROIs to be added.
     *
     * @return The updated list of ROIs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends ROI> saveROIs(Client client, Collection<? extends ROI> rois)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Links ROIs to the image in OMERO.
     * <p> DO NOT USE IT IF A SHAPE WAS DELETED !!!
     *
     * @param client The client handling the connection.
     * @param rois   ROIs to be added.
     *
     * @return The updated list of ROIs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends ROI> saveROIs(Client client, ROI... rois)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all ROIs linked to the image in OMERO
     *
     * @param client The client handling the connection.
     *
     * @return List of ROIs linked to the image.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends ROI> getROIs(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the list of folders linked to the ROIs in this image.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends Folder> getROIFolders(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the list of folders linked to this image.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError   Server error.
     */
    List<? extends Folder> getFolders(Client client)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError;


    /**
     * Gets the Pixels for this image.
     *
     * @return See above.
     */
    Pixels getPixels();


    /**
     * Generates the ImagePlus from the ij library corresponding to the image from OMERO WARNING : you need to include
     * the ij library to use this function
     *
     * @param client The client handling the connection.
     *
     * @return ImagePlus generated from the current image.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    ImagePlus toImagePlus(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the imagePlus generated from the image from OMERO corresponding to the bound.
     *
     * @param client  The client handling the connection.
     * @param xBounds Array containing the X bounds from which the pixels should be retrieved.
     * @param yBounds Array containing the Y bounds from which the pixels should be retrieved.
     * @param cBounds Array containing the C bounds from which the pixels should be retrieved.
     * @param zBounds Array containing the Z bounds from which the pixels should be retrieved.
     * @param tBounds Array containing the T bounds from which the pixels should be retrieved.
     *
     * @return an ImagePlus from the ij library.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    ImagePlus toImagePlus(Client client, int[] xBounds, int[] yBounds, int[] cBounds, int[] zBounds, int[] tBounds)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the imagePlus from the image generated from the ROI.
     *
     * @param client The client handling the connection.
     * @param roi    The ROI.
     *
     * @return an ImagePlus from the ij library.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    ImagePlus toImagePlus(Client client, ROI roi)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the image channels.
     *
     * @param client The client handling the connection.
     *
     * @return the channels.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends Channel> getChannels(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the name of the channel.
     *
     * @param client The client handling the connection.
     * @param index  Channel number.
     *
     * @return name of the channel.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    String getChannelName(Client client, int index)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the original color of the channel
     *
     * @param client The client handling the connection.
     * @param index  Channel number.
     *
     * @return Original color of the channel.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    Color getChannelImportedColor(Client client, int index)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the current color of the channel
     *
     * @param client The client handling the connection.
     * @param index  Channel number.
     *
     * @return Color of the channel.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    Color getChannelColor(Client client, int index)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Retrieves the image thumbnail of the specified size.
     * <p>If the image is not square, the size will be the longest side.
     *
     * @param client The client handling the connection.
     * @param size   The thumbnail size.
     *
     * @return The thumbnail as a {@link BufferedImage}.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError Server error.
     * @throws IOException      Cannot read thumbnail from store.
     */
    BufferedImage getThumbnail(Client client, int size)
    throws ServiceException, OMEROServerError, IOException;


    /**
     * Downloads the original files from the server.
     *
     * @param client The client handling the connection.
     * @param path   Path to the file.
     *
     * @return See above.
     *
     * @throws OMEROServerError   Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<File> download(Client client, String path)
    throws OMEROServerError, ServiceException, AccessException, ExecutionException;

}
