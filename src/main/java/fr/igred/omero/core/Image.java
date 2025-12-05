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


import fr.igred.omero.RepositoryObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.containers.Folder;
import fr.igred.omero.containers.Project;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.screen.Plate;
import fr.igred.omero.screen.PlateAcquisition;
import fr.igred.omero.screen.Screen;
import fr.igred.omero.screen.Well;
import fr.igred.omero.screen.WellSample;
import fr.igred.omero.util.Bounds;
import ij.ImagePlus;
import omero.RLong;
import omero.ServerError;
import omero.api.RenderingEnginePrx;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.TransferFacility;
import omero.gateway.model.ImageData;
import omero.model.IObject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static fr.igred.omero.RemoteObject.distinct;
import static fr.igred.omero.RemoteObject.flatten;
import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.logging.Level.WARNING;


/**
 * Interface to handle Images on OMERO.
 */
public interface Image extends RepositoryObject {

    /** Annotation link name for this type of object */
    String ANNOTATION_LINK = "ImageAnnotationLink";

    /** Default IJ property to store image ID. */
    String IJ_ID_PROPERTY = "IMAGE_ID";


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
     * Returns the format of the image.
     *
     * @return See above.
     */
    String getFormat();


    /**
     * Returns the series.
     *
     * @return See above.
     */
    int getSeries();


    /**
     * Retrieves the projects containing this image
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Project> getProjects(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<Dataset>       datasets = getDatasets(browser);
        Collection<Project> projects = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            projects.addAll(dataset.getProjects(browser));
        }
        return distinct(projects);
    }


    /**
     * Retrieves the datasets containing this image
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Dataset> getDatasets(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        String query = "select link.parent from DatasetImageLink as link" +
                       " where link.child=" + getId();
        List<IObject> os = browser.findByQuery(query);

        return browser.getDatasets(os.stream()
                                     .map(IObject::getId)
                                     .map(RLong::getValue)
                                     .distinct()
                                     .toArray(Long[]::new));
    }


    /**
     * Retrieves the well samples containing this image.
     *
     * @return See above
     */
    List<WellSample> getWellSamples();


    /**
     * Retrieves the well samples containing this image and updates them from OMERO.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<WellSample> getWellSamples(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        reload(browser);
        List<WellSample> samples = getWellSamples();
        for (WellSample sample : samples) {
            sample.reload(browser);
        }
        return samples;
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
    default List<Well> getWells(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        List<WellSample> wellSamples = getWellSamples(browser);

        Collection<Well> wells = new ArrayList<>(wellSamples.size());
        for (WellSample ws : wellSamples) {
            wells.add(ws.getWell(browser));
        }
        return distinct(wells);
    }


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
    default List<PlateAcquisition> getPlateAcquisitions(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        List<WellSample> wellSamples = getWellSamples(browser);

        Collection<PlateAcquisition> acqs = new ArrayList<>(wellSamples.size());
        for (WellSample ws : wellSamples) {
            acqs.add(ws.getPlateAcquisition());
        }
        return distinct(acqs);
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
    default List<Plate> getPlates(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        return distinct(getWells(browser).stream()
                                         .map(Well::getPlate)
                                         .collect(Collectors.toList()));
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
     */
    default List<Screen> getScreens(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
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
     * @throws AccessException  Cannot access data.
     */
    default boolean isOrphaned(Browser browser)
    throws ServiceException, AccessException {
        String dsQuery = "select link.parent from DatasetImageLink link" +
                         " where link.child=" + getId();
        String wsQuery = "select ws from WellSample ws where image=" + getId();

        boolean noDataset    = browser.findByQuery(dsQuery).isEmpty();
        boolean noWellSample = browser.findByQuery(wsQuery).isEmpty();
        return noDataset && noWellSample;
    }


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
     */
    default List<Image> getFilesetImages(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        List<Image> related = new ArrayList<>(0);
        if (asDataObject().isFSImage()) {
            long   fsId  = this.asDataObject().getFilesetId();
            String query = "select i from Image i where fileset=" + fsId;

            List<IObject> objects = browser.findByQuery(query);

            Long[] ids = objects.stream()
                                .map(IObject::getId)
                                .map(RLong::getValue)
                                .sorted()
                                .toArray(Long[]::new);
            related = browser.getImages(ids);
        }
        return related;
    }


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
    List<ROI> saveROIs(DataManager dm, Collection<? extends ROI> rois)
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
     * Gets the list of folders linked to the ROIs in this image.
     *
     * @param dm The data manager.
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
     * Gets the list of folders linked to this image.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Folder> getFolders(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        String template = "select link.parent from FolderImageLink as link" +
                          " where link.child.id=%d";
        String query = String.format(template, getId());
        Long[] ids = browser.findByQuery(query)
                            .stream()
                            .map(o -> o.getId().getValue())
                            .toArray(Long[]::new);
        return browser.getFolders(ids);
    }


    /**
     * Gets the Pixels for this image.
     *
     * @return See above.
     */
    PixelsWrapper getPixels();


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
        return toImagePlus(client, null, null, null, null, null);
    }


    /**
     * Gets the ImagePlus from the image within the specified boundaries.
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
     * Gets the ImagePlus from the image, but only inside the ROI.
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
        return toImagePlus(client, roi.getBounds());
    }


    /**
     * Gets the ImagePlus from the image within the specified boundaries.
     *
     * @param client The client handling the connection.
     * @param bounds The bounds.
     *
     * @return an ImagePlus from the ij library.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default ImagePlus toImagePlus(Client client, Bounds bounds)
    throws ServiceException, AccessException, ExecutionException {
        int[] x = {bounds.getStart().getX(), bounds.getEnd().getX()};
        int[] y = {bounds.getStart().getY(), bounds.getEnd().getY()};
        int[] c = {bounds.getStart().getC(), bounds.getEnd().getC()};
        int[] z = {bounds.getStart().getZ(), bounds.getEnd().getZ()};
        int[] t = {bounds.getStart().getT(), bounds.getEnd().getT()};

        return toImagePlus(client, x, y, c, z, t);
    }


    /**
     * Gets the image channels.
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
     * Gets the name of the channel.
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
    default Color getChannelColor(Client client, int index)
    throws ServiceException, AccessException, ExecutionException {
        long  pixelsId = getPixels().getId();
        Color color    = getChannelImportedColor(client, index);
        try {
            RenderingEnginePrx re = client.getGateway()
                                          .getRenderingService(client.getCtx(),
                                                               pixelsId);
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
                  .log(WARNING, "Error while retrieving current color", e);
        }
        return color;
    }


    /**
     * Retrieves the image thumbnail of the specified size.
     * <p>If the image is not square, the size will be the longest side.
     *
     * @param conn The connection handler.
     * @param size The thumbnail size.
     *
     * @return The thumbnail as a {@link BufferedImage}.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot read thumbnail from store.
     */
    BufferedImage getThumbnail(ConnectionHandler conn, int size)
    throws ServiceException, AccessException, IOException;


    /**
     * Returns the original file paths where the image was imported from.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<String> getOriginalPaths(Browser browser)
    throws ExecutionException, AccessException, ServiceException;


    /**
     * Returns the file paths of the image in the managed repository.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<String> getManagedRepositoriesPaths(Browser browser)
    throws ExecutionException, AccessException, ServiceException;


    /**
     * Downloads the original files from the server.
     *
     * @param conn The connection handler.
     * @param path Path to the file.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<File> download(ConnectionHandler conn, String path)
    throws ServiceException, AccessException, ExecutionException {
        return call(conn.getGateway().getFacility(TransferFacility.class),
                    t -> t.downloadImage(conn.getCtx(), path, getId()),
                    "Could not download image " + getId());
    }

}
