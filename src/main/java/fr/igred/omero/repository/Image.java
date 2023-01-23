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
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.util.Bounds;
import ij.ImagePlus;
import omero.RLong;
import omero.ServerError;
import omero.api.RenderingEnginePrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.TransferFacility;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.model.IObject;
import omero.model.WellSample;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


public interface Image extends RepositoryObject<ImageData> {

    /**
     * Gets the ImageData name
     *
     * @return name.
     */
    @Override
    default String getName() {
        return asDataObject().getName();
    }


    /**
     * Sets the name of the image.
     *
     * @param name The name of the image. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    default void setName(String name) {
        asDataObject().setName(name);
    }


    /**
     * Gets the ImageData description
     *
     * @return description.
     */
    @Override
    default String getDescription() {
        return asDataObject().getDescription();
    }


    /**
     * Sets the description of the image.
     *
     * @param description The description of the image.
     */
    default void setDescription(String description) {
        asDataObject().setDescription(description);
    }


    /**
     * Gets the ImageData acquisition date
     *
     * @return acquisition date.
     */
    default Timestamp getAcquisitionDate() {
        return asDataObject().getAcquisitionDate();
    }


    /**
     * Retrieves the projects containing this image
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServerException    Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Project> getProjects(Client client)
    throws ServerException, ServiceException, AccessException, ExecutionException {
        List<Dataset>       datasets = getDatasets(client);
        Collection<Project> projects = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            projects.addAll(dataset.getProjects(client));
        }
        return RemoteObject.distinct(projects);
    }


    /**
     * Retrieves the datasets containing this image
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServerException    Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Dataset> getDatasets(Client client)
    throws ServerException, ServiceException, AccessException, ExecutionException {
        List<IObject> os = client.findByQuery("select link.parent from DatasetImageLink as link " +
                                              "where link.child=" + getId());

        return client.getDatasets(os.stream().map(IObject::getId).map(RLong::getValue).distinct().toArray(Long[]::new));
    }


    /**
     * Retrieves the wells containing this image
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Well> getWells(Client client) throws AccessException, ServiceException, ExecutionException {
        Long[] ids = this.asDataObject()
                         .asImage()
                         .copyWellSamples()
                         .stream()
                         .map(WellSample::getWell)
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .sorted().distinct()
                         .toArray(Long[]::new);
        return client.getWells(ids);
    }


    /**
     * Retrieves the plates containing this image
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Plate> getPlates(Client client) throws AccessException, ServiceException, ExecutionException {
        return RemoteObject.distinct(getWells(client).stream().map(Well::getPlate).collect(Collectors.toList()));
    }


    /**
     * Retrieves the screens containing this image
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    default List<Screen> getScreens(Client client)
    throws AccessException, ServiceException, ExecutionException, ServerException {
        List<Plate>        plates  = getPlates(client);
        Collection<Screen> screens = new ArrayList<>(plates.size());
        for (Plate plate : plates) {
            screens.addAll(plate.getScreens(client));
        }
        return RemoteObject.distinct(screens);
    }


    /**
     * Checks if image is orphaned (not in a WellSample nor linked to a dataset).
     *
     * @param client The client handling the connection.
     *
     * @return {@code true} if the image is orphaned, {@code false} otherwise.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    default boolean isOrphaned(Client client) throws ServiceException, ServerException {
        boolean noDataset = client.findByQuery("select link.parent from DatasetImageLink link " +
                                               "where link.child=" + getId()).isEmpty();
        boolean noWell = client.findByQuery("select ws from WellSample ws where image=" + getId()).isEmpty();
        return noDataset && noWell;
    }


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
     * @throws ServerException    Server error.
     */
    default List<Image> getFilesetImages(Client client)
    throws AccessException, ServiceException, ExecutionException, ServerException {
        List<Image> related = new ArrayList<>(0);
        if (asDataObject().isFSImage()) {
            long fsId = this.asDataObject().getFilesetId();

            List<IObject> objects = client.findByQuery("select i from Image i where fileset=" + fsId);

            Long[] ids = objects.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
            related = client.getImages(ids);
        }
        return related;
    }


    /**
     * Links a ROI to the image in OMERO
     * <p> DO NOT USE IT IF A SHAPE WAS DELETED !!!
     *
     * @param client The client handling the connection.
     * @param roi    ROI to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void saveROI(Client client, ROI roi)
    throws ServiceException, AccessException, ExecutionException {
        ROIData roiData = handleServiceAndAccess(client.getRoiFacility(),
                                                 rf -> rf.saveROIs(client.getCtx(),
                                                                   getId(),
                                                                   Collections.singletonList(roi.asDataObject())),
                                                 "Cannot link ROI to " + this).iterator().next();
        roi.replace(roiData);
    }


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
    List<ROI> getROIs(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the list of Folder linked to the image Associate the folder to the image
     *
     * @param client The client handling the connection.
     *
     * @return List of Folder containing the folder.
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
     * Gets the Pixels of the image
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
     * @param client The client handling the connection.
     *
     * @return the channels.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Channel> getChannels(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the name of the channel
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
    default String getChannelName(Client client, int index)
    throws ServiceException, AccessException, ExecutionException {
        return getChannels(client).get(index).getChannelLabeling();
    }


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
    default Color getChannelImportedColor(Client client, int index)
    throws ServiceException, AccessException, ExecutionException {
        return getChannels(client).get(index).getColor();
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
    default Color getChannelColor(Client client, int index)
    throws ServiceException, AccessException, ExecutionException {
        long  pixelsId = asDataObject().getDefaultPixels().getId();
        Color color    = getChannelImportedColor(client, index);
        try {
            RenderingEnginePrx re = client.getGateway().getRenderingService(client.getCtx(), pixelsId);
            re.lookupPixels(pixelsId);
            if (!(re.lookupRenderingDef(pixelsId))) {
                re.resetDefaultSettings(true);
                re.lookupRenderingDef(pixelsId);
            }
            re.load();
            int[] rgba = re.getRGBA(index);
            color = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
            re.close();
        } catch (DSOutOfServiceException | ServerError e) {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Error while retrieving current color", e);
        }
        return color;
    }


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
    BufferedImage getThumbnail(Client client, int size) throws ServiceException, ServerException, IOException;


    /**
     * Downloads the original files from the server.
     *
     * @param client The client handling the connection.
     * @param path   Path to the file.
     *
     * @return See above.
     *
     * @throws ServerException  Server error.
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    default List<File> download(Client client, String path)
    throws ServerException, ServiceException, AccessException, ExecutionException {
        TransferFacility transfer = client.getGateway().getFacility(TransferFacility.class);
        return ExceptionHandler.of(transfer,
                                   t -> t.downloadImage(client.getCtx(), path, getId()),
                                   "Could not download image " + getId())
                               .rethrow(DSOutOfServiceException.class, ServiceException::new)
                               .rethrow(ServerError.class, ServerException::new)
                               .rethrow(DSAccessException.class, AccessException::new)
                               .get();
    }

}
