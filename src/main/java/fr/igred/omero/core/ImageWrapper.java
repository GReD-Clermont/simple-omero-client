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
import fr.igred.omero.RepositoryObjectWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.containers.Folder;
import fr.igred.omero.containers.FolderWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.screen.WellSample;
import fr.igred.omero.screen.WellSampleWrapper;
import fr.igred.omero.util.Bounds;
import fr.igred.omero.util.Coordinates;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;
import loci.formats.FormatTools;
import omero.ServerError;
import omero.api.ThumbnailStorePrx;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.TransferFacility;
import omero.gateway.model.ChannelData;
import omero.gateway.model.FolderData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.gateway.model.WellSampleData;
import omero.model.Length;
import omero.model.Time;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.RemoteObject.distinct;
import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.Comparator.comparing;
import static loci.common.DataTools.makeDataArray;
import static omero.rtypes.rint;


/**
 * Class containing an ImageData.
 * <p> Wraps function calls to the ImageData contained.
 */
public class ImageWrapper extends RepositoryObjectWrapper<ImageData> implements Image {


    /**
     * Constructor of the class ImageWrapper
     *
     * @param image The image to wrap in the ImageWrapper.
     */
    public ImageWrapper(ImageData image) {
        super(image);
    }


    /**
     * Sets the calibration. Planes information has to be loaded first.
     *
     * @param pixels      The pixels.
     * @param calibration The ImageJ calibration.
     */
    private static void setCalibration(Pixels pixels, Calibration calibration) {
        Length positionX = pixels.getPositionX();
        Length positionY = pixels.getPositionY();
        Length positionZ = pixels.getPositionZ();
        Length spacingX  = pixels.getPixelSizeX();
        Length spacingY  = pixels.getPixelSizeY();
        Length spacingZ  = pixels.getPixelSizeZ();
        Time   stepT     = pixels.getTimeIncrement();

        if (stepT == null) {
            stepT = pixels.getMeanTimeInterval();
        }

        calibration.setXUnit(positionX.getSymbol());
        calibration.setYUnit(positionY.getSymbol());
        calibration.setZUnit(positionZ.getSymbol());
        calibration.xOrigin = -positionX.getValue();
        calibration.yOrigin = -positionY.getValue();
        calibration.zOrigin = -positionZ.getValue();
        if (spacingX != null) {
            calibration.setXUnit(spacingX.getSymbol());
            calibration.pixelWidth = spacingX.getValue();
            // positionX and spacingX should use the same unit
            calibration.xOrigin /= calibration.pixelWidth;
        }
        if (spacingY != null) {
            calibration.setYUnit(spacingY.getSymbol());
            calibration.pixelHeight = spacingY.getValue();
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


    /**
     * Retrieves the image thumbnail of the specified size as a byte array.
     * <p>If the image is not square, the size will be the longest side.
     *
     * @param conn The connection handler.
     * @param size The thumbnail size.
     *
     * @return The thumbnail pixels as a byte array.
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO.
     * @throws ServerError             Server error.
     */
    private byte[] getThumbnailBytes(ConnectionHandler conn, int size)
    throws DSOutOfServiceException, ServerError {
        Pixels pixels = getPixels();

        int   sizeX  = pixels.getSizeX();
        int   sizeY  = pixels.getSizeY();
        float ratioX = (float) sizeX / size;
        float ratioY = (float) sizeY / size;
        float ratio  = Math.max(ratioX, ratioY);
        int   width  = (int) (sizeX / ratio);
        int   height = (int) (sizeY / ratio);

        ThumbnailStorePrx store = null;
        byte[]            array;
        try {
            store = conn.getGateway().getThumbnailService(conn.getCtx());
            store.setPixelsId(pixels.getId());
            array = store.getThumbnail(rint(width), rint(height));
        } finally {
            if (store != null) {
                store.close();
            }
        }
        return array;
    }


    /**
     * Gets the ImageData name
     *
     * @return name.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the image.
     *
     * @param name The name of the image. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    @Override
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Gets the ImageData description
     *
     * @return description.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the image.
     *
     * @param description The description of the image.
     */
    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Gets the ImageData acquisition date
     *
     * @return acquisition date.
     */
    @Override
    public Timestamp getAcquisitionDate() {
        return data.getAcquisitionDate();
    }


    /**
     * Returns the format of the image.
     *
     * @return See above.
     */
    @Override
    public String getFormat() {
        return data.getFormat();
    }


    /**
     * Returns the series.
     *
     * @return See above.
     */
    @Override
    public int getSeries() {
        return data.getSeries();
    }


    /**
     * Returns the type of annotation link for this object.
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Retrieves the well samples containing this image.
     *
     * @return See above
     */
    @Override
    public List<WellSample> getWellSamples() {
        return data.asImage()
                   .copyWellSamples()
                   .stream()
                   .map(WellSampleData::new)
                   .map(WellSampleWrapper::new)
                   .collect(Collectors.toList());
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
    @Override
    public List<ROI> saveROIs(DataManager dm, Collection<? extends ROI> rois)
    throws ServiceException, AccessException, ExecutionException {
        rois.forEach(r -> r.setImage(this));
        List<ROIData> roisData = rois.stream()
                                     .map(ROI::asDataObject)
                                     .collect(Collectors.toList());
        Collection<ROIData> results = call(dm.getRoiFacility(),
                                           rf -> rf.saveROIs(dm.getCtx(),
                                                             data.getId(),
                                                             roisData),
                                           "Cannot link ROI to " + this);
        return wrap(results, ROIWrapper::new);
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
    @Override
    public List<ROI> getROIs(DataManager dm)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIResult> roiResults = call(dm.getRoiFacility(),
                                          rf -> rf.loadROIs(dm.getCtx(),
                                                            data.getId()),
                                          "Cannot get ROIs from " + this);

        List<ROI> roiWrappers = roiResults.stream()
                                          .map(ROIResult::getROIs)
                                          .flatMap(Collection::stream)
                                          .map(ROIWrapper::new)
                                          .sorted(comparing(RemoteObject::getId))
                                          .collect(Collectors.toList());

        return distinct(roiWrappers);
    }


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
    @Override
    public List<Folder> getROIFolders(DataManager dm)
    throws ServiceException, AccessException, ExecutionException {
        Collection<FolderData> folders = call(dm.getRoiFacility(),
                                              rf -> rf.getROIFolders(dm.getCtx(),
                                                                     data.getId()),
                                              "Cannot get folders for " + this);

        return wrap(folders, FolderWrapper::new);
    }


    /**
     * Gets the Pixels for this image.
     *
     * @return See above.
     */
    @Override
    public PixelsWrapper getPixels() {
        return new PixelsWrapper(data.getDefaultPixels());
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
    @Override
    public ImagePlus toImagePlus(Client client,
                                 int[] xBounds,
                                 int[] yBounds,
                                 int[] cBounds,
                                 int[] zBounds,
                                 int[] tBounds)
    throws ServiceException, AccessException, ExecutionException {
        PixelsWrapper pixels = getPixels();
        pixels.loadPlanesInfo(client);

        boolean createdRDF = pixels.createRawDataFacility(client);

        Bounds bounds = pixels.getBounds(xBounds, yBounds, cBounds, zBounds, tBounds);

        int startX = bounds.getStart().getX();
        int startY = bounds.getStart().getY();
        int startC = bounds.getStart().getC();
        int startZ = bounds.getStart().getZ();
        int startT = bounds.getStart().getT();

        int sizeX = bounds.getSize().getX();
        int sizeY = bounds.getSize().getY();
        int sizeC = bounds.getSize().getC();
        int sizeZ = bounds.getSize().getZ();
        int sizeT = bounds.getSize().getT();

        int pixelType = FormatTools.pixelTypeFromString(pixels.getPixelType());
        int bpp       = FormatTools.getBytesPerPixel(pixelType);

        ImagePlus imp = IJ.createHyperStack(data.getName(), sizeX, sizeY, sizeC, sizeZ, sizeT, bpp * 8);

        Calibration calibration = imp.getCalibration();
        setCalibration(pixels, calibration);
        calibration.xOrigin -= startX;
        calibration.yOrigin -= startY;
        calibration.zOrigin -= startZ;
        imp.setCalibration(calibration);

        boolean isFloat = FormatTools.isFloatingPoint(pixelType);

        ImageStack stack = imp.getImageStack();

        double min = imp.getProcessor().getMin();
        double max = 0;

        int progressTotal = imp.getStackSize();
        IJ.showProgress(0, progressTotal);
        for (int t = 0; t < sizeT; t++) {
            int posT = t + startT;
            for (int z = 0; z < sizeZ; z++) {
                int posZ = z + startZ;
                for (int c = 0; c < sizeC; c++) {
                    int posC = c + startC;

                    Coordinates pos = new Coordinates(startX, startY, posC, posZ, posT);

                    byte[] tiles = pixels.getRawTile(client, pos, sizeX, sizeY, bpp);

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

        imp.setStack(stack);
        imp.setOpenAsHyperStack(true);
        imp.setDisplayMode(IJ.COMPOSITE);

        imp.getProcessor().setMinAndMax(min, max);

        LUT[] luts = imp.getLuts();
        for (int c = 0; c < sizeC; ++c) {
            luts[c] = LUT.createLutFromColor(getChannelColor(client, startC + c));
            imp.setC(c + 1);
            imp.setLut(luts[c]);
        }
        if (imp.isComposite()) {
            ((CompositeImage) imp).setLuts(luts);
        }
        if (createdRDF) {
            pixels.destroyRawDataFacility();
        }
        imp.setPosition(1);
        if (IJ.getVersion().compareTo("1.53a") >= 0) {
            imp.setProp(IJ_ID_PROPERTY, getId());
            imp.setProp("IMAGE_POS_X", startX);
            imp.setProp("IMAGE_POS_Y", startY);
            imp.setProp("IMAGE_POS_C", startC);
            imp.setProp("IMAGE_POS_Z", startZ);
            imp.setProp("IMAGE_POS_T", startT);
        }
        return imp;
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
    @Override
    public List<Channel> getChannels(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get the channel name for " + this;
        List<ChannelData> channels = call(browser.getMetadataFacility(),
                                          m -> m.getChannelData(browser.getCtx(),
                                                                getId()),
                                          error);
        return channels.stream()
                       .sorted(comparing(ChannelData::getIndex))
                       .map(ChannelWrapper::new)
                       .collect(Collectors.toList());
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
    @Override
    public BufferedImage getThumbnail(ConnectionHandler conn, int size)
    throws ServiceException, AccessException, IOException {
        BufferedImage thumbnail = null;

        byte[] arr = call(conn,
                          c -> getThumbnailBytes(c, size),
                          "Error retrieving thumbnail.");
        if (arr != null) {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(arr)) {
                //Create a buffered image to display
                thumbnail = ImageIO.read(stream);
            }
        }
        return thumbnail;
    }


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
    @Override
    public List<String> getOriginalPaths(Browser browser)
    throws ExecutionException, AccessException, ServiceException {
        String error = "Cannot get original paths for " + this;
        return call(browser.getMetadataFacility(),
                    m -> m.getOriginalPaths(browser.getCtx(), data),
                    error);
    }


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
    @Override
    public List<String> getManagedRepositoriesPaths(Browser browser)
    throws ExecutionException, AccessException, ServiceException {
        String error = "Cannot get managed repositories paths for " + this;
        return call(browser.getMetadataFacility(),
                    m -> m.getManagedRepositoriesPaths(browser.getCtx(), data),
                    error);
    }


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
    @Override
    public List<File> download(ConnectionHandler conn, String path)
    throws ServiceException, AccessException, ExecutionException {
        return call(conn.getGateway().getFacility(TransferFacility.class),
                    t -> t.downloadImage(conn.getCtx(), path, getId()),
                    "Could not download image " + getId());
    }


    /**
     * Reloads the image from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void reload(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        data = call(browser.getBrowseFacility(),
                    b -> b.getImage(browser.getCtx(), getId()),
                    "Can not reload " + this);
    }

}
