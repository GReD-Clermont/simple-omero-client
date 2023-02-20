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


import fr.igred.omero.ContainerLinked;
import fr.igred.omero.HCSLinked;
import fr.igred.omero.RepositoryObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.containers.Folder;
import fr.igred.omero.containers.Project;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.screen.Plate;
import fr.igred.omero.screen.PlateAcquisition;
import fr.igred.omero.screen.Screen;
import fr.igred.omero.screen.Well;
import fr.igred.omero.util.Bounds;
import ij.ImagePlus;
import omero.gateway.model.ImageData;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.RemoteObject.distinct;
import static fr.igred.omero.RemoteObject.flatten;


/**
 * Interface to handle Images on OMERO.
 */
public interface Image extends RepositoryObject<ImageData>, ContainerLinked<ImageData>, HCSLinked<ImageData> {

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
     * Retrieves the projects containing this image.
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
    @Override
    default List<Project> getProjects(Browser browser)
    throws ServerException, ServiceException, AccessException, ExecutionException {
        List<Dataset> datasets = getDatasets(browser);

        Collection<List<Project>> projects = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            projects.add(dataset.getProjects(browser));
        }
        return flatten(projects);
    }


    /**
     * Returns this image, updated from OMERO, as a singleton list.
     *
     * @param browser The data browser (unused).
     *
     * @return See above
     */
    @Override
    default List<Image> getImages(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        return Collections.singletonList(browser.getImage(getId()));
    }


    /**
     * Retrieves the wells containing this image.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    List<Well> getWells(Browser browser)
    throws AccessException, ServiceException, ExecutionException;


    /**
     * Returns the plate acquisitions linked to this image.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default List<PlateAcquisition> getPlateAcquisitions(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        List<Well> wells = getWells(browser);

        Collection<List<PlateAcquisition>> acqs = new ArrayList<>(wells.size());
        for (Well w : wells) {
            acqs.add(w.getPlateAcquisitions(browser));
        }
        return flatten(acqs);
    }


    /**
     * Retrieves the plates containing this image.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default List<Plate> getPlates(Browser browser) throws AccessException, ServiceException, ExecutionException {
        return distinct(getWells(browser).stream().map(Well::getPlate).collect(Collectors.toList()));
    }


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
    @Override
    default List<Screen> getScreens(Browser browser)
    throws AccessException, ServiceException, ExecutionException, ServerException {
        List<Plate> plates = getPlates(browser);

        Collection<List<Screen>> screens = new ArrayList<>(plates.size());
        for (Plate plate : plates) {
            screens.add(plate.getScreens(browser));
        }
        return flatten(screens);
    }


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
     * Gets the list of Folders linked to the ROIs in this image.
     *
     * @param dm The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Folder> getROIFolders(DataManager dm)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the list of Folders linked to this image.
     *
     * @param browser The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    List<Folder> getFolders(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException;


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
    default ImagePlus toImagePlus(Client client)
    throws ServiceException, AccessException, ExecutionException {
        return this.toImagePlus(client, null, null, null, null, null);
    }


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
    default ImagePlus toImagePlus(Client client, ROI roi)
    throws ServiceException, AccessException, ExecutionException {
        Bounds bounds = roi.getBounds();

        int[] x = {bounds.getStart().getX(), bounds.getEnd().getX()};
        int[] y = {bounds.getStart().getY(), bounds.getEnd().getY()};
        int[] c = {bounds.getStart().getC(), bounds.getEnd().getC()};
        int[] z = {bounds.getStart().getZ(), bounds.getEnd().getZ()};
        int[] t = {bounds.getStart().getT(), bounds.getEnd().getT()};

        return toImagePlus(client, x, y, c, z, t);
    }


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
    default String getChannelName(Browser browser, int index)
    throws ServiceException, AccessException, ExecutionException {
        return getChannels(browser).get(index).getChannelLabeling();
    }


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
    default Color getChannelImportedColor(Browser browser, int index)
    throws ServiceException, AccessException, ExecutionException {
        return getChannels(browser).get(index).getColor();
    }


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