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

package fr.igred.omero.core;


import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.containers.Folder;
import fr.igred.omero.screen.Plate;
import fr.igred.omero.containers.Project;
import fr.igred.omero.RepositoryObject;
import fr.igred.omero.screen.Screen;
import fr.igred.omero.screen.Well;
import fr.igred.omero.roi.ROI;
import ij.ImagePlus;
import omero.gateway.model.ImageData;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Images on OMERO.
 */
public interface Image extends RepositoryObject<ImageData> {

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
     * Retrieves the projects containing this image
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServerException    Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Project> getProjects(Browser browser)
    throws ServerException, ServiceException, AccessException, ExecutionException;


    /**
     * Retrieves the datasets containing this image
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServerException    Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Dataset> getDatasets(Browser browser)
    throws ServerException, ServiceException, AccessException, ExecutionException;


    /**
     * Retrieves the wells containing this image
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Well> getWells(Browser browser) throws AccessException, ServiceException, ExecutionException;


    /**
     * Retrieves the plates containing this image
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Plate> getPlates(Browser browser) throws AccessException, ServiceException, ExecutionException;


    /**
     * Retrieves the screens containing this image
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    List<Screen> getScreens(Browser browser)
    throws AccessException, ServiceException, ExecutionException, ServerException;


    /**
     * Checks if image is orphaned (not in a WellSample nor linked to a dataset).
     *
     * @param browser The data browser.
     *
     * @return {@code true} if the image is orphaned, {@code false} otherwise.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    boolean isOrphaned(Browser browser) throws ServiceException, ServerException;


    /**
     * Returns the list of images sharing the same fileset as the current image.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws AccessException    Cannot access data.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    List<Image> getFilesetImages(Browser browser)
    throws AccessException, ServiceException, ExecutionException, ServerException;


    /**
     * Links ROIs to the image in OMERO.
     * <p> DO NOT USE IT IF A SHAPE WAS DELETED !!!
     *
     * @param dm   The data manager.
     * @param rois List of ROIs to be added.
     *
     * @return The updated list of ROIs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<ROI> saveROIs(DataManager dm, List<? extends ROI> rois)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Links ROIs to the image in OMERO.
     * <p> DO NOT USE IT IF A SHAPE WAS DELETED !!!
     *
     * @param dm   The data manager.
     * @param rois ROIs to be added.
     *
     * @return The updated list of ROIs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ROI> saveROIs(DataManager dm, ROI... rois)
    throws ServiceException, AccessException, ExecutionException {
        return saveROIs(dm, Arrays.asList(rois));
    }


    /**
     * Gets all ROIs linked to the image in OMERO
     *
     * @param dm The data manager.
     *
     * @return List of ROIs linked to the image.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<ROI> getROIs(DataManager dm)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the list of Folder linked to the image Associate the folder to the image
     *
     * @param client The client handling the connection.
     *
     * @return List of FolderWrapper containing the folder.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Folder> getFolders(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the folder with the specified id on OMERO.
     *
     * @param client   The client handling the connection.
     * @param folderId ID of the folder.
     *
     * @return The folder if it exists.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws ServerException        Server error.
     * @throws NoSuchElementException Folder does not exist.
     */
    Folder getFolder(Client client, Long folderId) throws ServiceException, ServerException;


    /**
     * Gets the PixelsWrapper of the image
     *
     * @return Contains the PixelsData associated with the image.
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
    ImagePlus toImagePlus(Client client,
                          int[] xBounds,
                          int[] yBounds,
                          int[] cBounds,
                          int[] zBounds,
                          int[] tBounds)
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
     * Gets the image channels
     *
     * @param browser The data browser.
     *
     * @return the channels.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Channel> getChannels(Browser browser)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the name of the channel
     *
     * @param browser The data browser.
     * @param index   Channel number.
     *
     * @return name of the channel.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    String getChannelName(Browser browser, int index)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the original color of the channel
     *
     * @param browser The data browser.
     * @param index   Channel number.
     *
     * @return Original color of the channel.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    Color getChannelImportedColor(Browser browser, int index)
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
     * @throws ServerException  Server error.
     * @throws IOException      Cannot read thumbnail from store.
     */
    BufferedImage getThumbnail(ConnectionHandler client, int size)
    throws ServiceException, ServerException, IOException;


    /**
     * Downloads the original files from the server.
     *
     * @param client The client handling the connection.
     * @param path   Path to the file.
     *
     * @return See above.
     *
     * @throws ServerException    Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<File> download(Client client, String path)
    throws ServerException, ServiceException, AccessException, ExecutionException;

}
