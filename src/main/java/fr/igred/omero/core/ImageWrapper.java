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
import fr.igred.omero.RemoteObject;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.containers.Folder;
import fr.igred.omero.containers.FolderWrapper;
import fr.igred.omero.RepositoryObjectWrapper;
import fr.igred.omero.screen.Well;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.util.Bounds;
import fr.igred.omero.util.Coordinates;
import fr.igred.omero.roi.ROIWrapper;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;
import loci.common.DataTools;
import loci.formats.FormatTools;
import omero.RLong;
import omero.ServerError;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.ROIFacility;
import omero.gateway.facility.TransferFacility;
import omero.gateway.model.ChannelData;
import omero.gateway.model.FolderData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.model.IObject;
import omero.model.Length;
import omero.model.Time;
import omero.model.WellSample;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndServer;
import static fr.igred.omero.util.Wrapper.wrap;
import static omero.rtypes.rint;


/**
 * Class containing an ImageData.
 * <p> Wraps function calls to the ImageData contained.
 */
public class ImageWrapper extends RepositoryObjectWrapper<ImageData> implements Image {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "ImageAnnotationLink";


    /**
     * Constructor of the class ImageWrapper
     *
     * @param image The image contained in the ImageWrapper.
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
        calibration.xOrigin = positionX.getValue();
        calibration.yOrigin = positionY.getValue();
        calibration.zOrigin = positionZ.getValue();
        if (spacingX != null) {
            calibration.pixelWidth = spacingX.getValue();
        }
        if (spacingY != null) {
            calibration.pixelHeight = spacingY.getValue();
        }
        if (spacingZ != null) {
            calibration.pixelDepth = spacingZ.getValue();
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
     * @param client The client handling the connection.
     * @param size   The thumbnail size.
     *
     * @return The thumbnail pixels as a byte array.
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO.
     * @throws ServerError             Server error.
     */
    private byte[] getThumbnailBytes(ConnectionHandler client, int size) throws DSOutOfServiceException, ServerError {
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
            store = client.getGateway().getThumbnailService(client.getCtx());
            store.setPixelsId(pixels.getId());
            array = store.getThumbnail(rint(width), rint(height));
        } finally {
            if (store != null) store.close();
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
     * Returns the type of annotation link for this object
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


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
    @Override
    public List<Dataset> getDatasets(Browser browser)
    throws ServerException, ServiceException, AccessException, ExecutionException {
        Long[] ids = browser.findByQuery("select link.parent from DatasetImageLink as link " +
                                         "where link.child=" + getId())
                            .stream()
                            .map(IObject::getId)
                            .map(RLong::getValue)
                            .distinct()
                            .toArray(Long[]::new);

        return browser.getDatasets(ids);
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
    public List<Well> getWells(Browser browser) throws AccessException, ServiceException, ExecutionException {
        Long[] ids = data.asImage()
                         .copyWellSamples()
                         .stream()
                         .map(WellSample::getWell)
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .sorted().distinct()
                         .toArray(Long[]::new);
        return browser.getWells(ids);
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
    @Override
    public boolean isOrphaned(Browser browser) throws ServiceException, ServerException {
        boolean noDataset = browser.findByQuery("select link.parent from DatasetImageLink link " +
                                                "where link.child=" + getId()).isEmpty();
        boolean noWell = browser.findByQuery("select ws from WellSample ws where image=" + getId()).isEmpty();
        return noDataset && noWell;
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
     * @throws ServerException    Server error.
     */
    @Override
    public List<Image> getFilesetImages(Browser browser)
    throws AccessException, ServiceException, ExecutionException, ServerException {
        List<Image> related = new ArrayList<>(0);
        if (data.isFSImage()) {
            long fsId = this.asDataObject().getFilesetId();

            List<IObject> objects = browser.findByQuery("select i from Image i where fileset=" + fsId);

            Long[] ids = objects.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
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
    @Override
    public List<ROI> saveROIs(DataManager dm, List<? extends ROI> rois)
    throws ServiceException, AccessException, ExecutionException {
        rois.forEach(r -> r.setImage(this));
        List<ROIData> roisData = rois.stream().map(RemoteObject::asDataObject).collect(Collectors.toList());
        Collection<ROIData> results = handleServiceAndAccess(dm.getRoiFacility(),
                                                             rf -> rf.saveROIs(dm.getCtx(),
                                                                               this.data.getId(),
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
        List<ROIResult> roiResults = handleServiceAndAccess(dm.getRoiFacility(),
                                                            rf -> rf.loadROIs(dm.getCtx(), data.getId()),
                                                            "Cannot get ROIs from " + this);
        ROIResult r = roiResults.iterator().next();

        Collection<ROIData> rois = r.getROIs();

        List<ROI> roiWrappers = new ArrayList<>(rois.size());
        for (ROIData roi : rois) {
            ROI temp = new ROIWrapper(roi);
            roiWrappers.add(temp);
        }

        return roiWrappers;
    }


    /**
     * Gets the list of Folders linked to the ROIs in this image.
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
        ROIFacility roiFacility = dm.getRoiFacility();

        Collection<FolderData> folders = handleServiceAndAccess(roiFacility,
                                                                rf -> rf.getROIFolders(dm.getCtx(),
                                                                                       this.data.getId()),
                                                                "Cannot get folders for " + this);
        return wrap(folders, FolderWrapper::new);
    }


    /**
     * Gets the list of Folders linked to this image.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    @Override
    public List<Folder> getFolders(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        String query = String.format("select link.parent from FolderImageLink as link where link.child.id=%d", getId());
        Long[] ids   = browser.findByQuery(query)
                              .stream()
                              .map(o -> o.getId().getValue())
                              .sorted().distinct()
                              .toArray(Long[]::new);
        return browser.loadFolders(ids);
    }


    /**
     * Gets the PixelsWrapper of the image
     *
     * @return Contains the PixelsData associated with the image.
     */
    @Override
    public Pixels getPixels() {
        return new PixelsWrapper(data.getDefaultPixels());
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
    @Override
    public ImagePlus toImagePlus(Client client,
                                 int[] xBounds,
                                 int[] yBounds,
                                 int[] cBounds,
                                 int[] zBounds,
                                 int[] tBounds)
    throws ServiceException, AccessException, ExecutionException {
        PixelsWrapper pixels = new PixelsWrapper(data.getDefaultPixels());
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
                    stack.setPixels(DataTools.makeDataArray(tiles, bpp, isFloat, false), n);
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
        return imp;
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
    @Override
    public List<Channel> getChannels(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<ChannelData> channels = handleServiceAndAccess(browser.getMetadata(),
                                                            m -> m.getChannelData(browser.getCtx(), getId()),
                                                            "Cannot get the channel name for " + this);
        return channels.stream()
                       .sorted(Comparator.comparing(ChannelData::getIndex))
                       .map(ChannelWrapper::new)
                       .collect(Collectors.toList());
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
    @Override
    public Color getChannelColor(Client client, int index)
    throws ServiceException, AccessException, ExecutionException {
        long  pixelsId = data.getDefaultPixels().getId();
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
    @Override
    public BufferedImage getThumbnail(ConnectionHandler client, int size)
    throws ServiceException, ServerException, IOException {
        BufferedImage thumbnail = null;

        byte[] array = handleServiceAndServer(client, c -> getThumbnailBytes(c, size), "Error retrieving thumbnail.");
        if (array != null) {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(array)) {
                //Create a buffered image to display
                thumbnail = ImageIO.read(stream);
            }
        }
        return thumbnail;
    }


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
    @Override
    public List<File> download(Client client, String path)
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