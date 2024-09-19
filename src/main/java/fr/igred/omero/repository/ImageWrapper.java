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

package fr.igred.omero.repository;


import fr.igred.omero.Browser;
import fr.igred.omero.Client;
import fr.igred.omero.GenericObjectWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.PixelsWrapper.Bounds;
import fr.igred.omero.repository.PixelsWrapper.Coordinates;
import fr.igred.omero.roi.ROIWrapper;
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;
import loci.formats.FormatTools;
import omero.RLong;
import omero.ServerError;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.TransferFacility;
import omero.gateway.model.ChannelData;
import omero.gateway.model.FolderData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.gateway.model.WellSampleData;
import omero.model.Folder;
import omero.model.IObject;
import omero.model.Length;
import omero.model.Time;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.Comparator.comparing;
import static java.util.logging.Level.WARNING;
import static loci.common.DataTools.makeDataArray;
import static omero.rtypes.rint;


/**
 * Class containing an ImageData.
 * <p> Wraps function calls to the ImageData contained.
 */
public class ImageWrapper extends GenericRepositoryObjectWrapper<ImageData> {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "ImageAnnotationLink";

    /** Default IJ property to store image ID. */
    public static final String IJ_ID_PROPERTY = "IMAGE_ID";


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
    private static void setCalibration(PixelsWrapper pixels, Calibration calibration) {
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
     * @param client The client handling the connection.
     * @param size   The thumbnail size.
     *
     * @return The thumbnail pixels as a byte array.
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO.
     * @throws ServerError             Server error.
     */
    private byte[] getThumbnailBytes(Client client, int size)
    throws DSOutOfServiceException, ServerError {
        PixelsWrapper pixels = getPixels();

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
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * @return See above.
     *
     * @deprecated Returns the ImageData contained. Use {@link #asDataObject()} instead.
     */
    @Deprecated
    public ImageData asImageData() {
        return data;
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
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Gets the ImageData acquisition date
     *
     * @return acquisition date.
     */
    public Timestamp getAcquisitionDate() {
        return data.getAcquisitionDate();
    }


    /**
     * Returns the format of the image.
     *
     * @return See above.
     */
    public String getFormat() {
        return data.getFormat();
    }


    /**
     * Returns the series.
     *
     * @return See above.
     */
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
     * Retrieves the projects containing this image
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws OMEROServerError   Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ProjectWrapper> getProjects(Client client)
    throws OMEROServerError, ServiceException, AccessException, ExecutionException {
        List<DatasetWrapper>       datasets = getDatasets(client);
        Collection<ProjectWrapper> projects = new ArrayList<>(datasets.size());
        for (DatasetWrapper dataset : datasets) {
            projects.addAll(dataset.getProjects(client));
        }
        return distinct(projects);
    }


    /**
     * Retrieves the datasets containing this image
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws OMEROServerError   Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<DatasetWrapper> getDatasets(Client client)
    throws OMEROServerError, ServiceException, AccessException, ExecutionException {
        String query = "select link.parent from DatasetImageLink as link" +
                       " where link.child=" + getId();
        List<IObject> os = client.findByQuery(query);

        return client.getDatasets(os.stream()
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
    public List<WellSampleWrapper> getWellSamples() {
        return data.asImage()
                   .copyWellSamples()
                   .stream()
                   .map(WellSampleData::new)
                   .map(WellSampleWrapper::new)
                   .collect(Collectors.toList());
    }


    /**
     * Retrieves the well samples containing this image and updates them from OMERO.
     *
     * @param client The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<WellSampleWrapper> getWellSamples(Client client)
    throws AccessException, ServiceException, ExecutionException {
        reload(client);
        List<WellSampleWrapper> samples = getWellSamples();
        for (WellSampleWrapper sample : samples) {
            sample.reload(client);
        }
        return samples;
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
    public List<WellWrapper> getWells(Client client)
    throws AccessException, ServiceException, ExecutionException {
        List<WellSampleWrapper> wellSamples = getWellSamples(client);

        Collection<WellWrapper> wells = new ArrayList<>(wellSamples.size());
        for (WellSampleWrapper ws : wellSamples) {
            wells.add(ws.getWell(client));
        }
        return distinct(wells);
    }


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
    public List<PlateAcquisitionWrapper> getPlateAcquisitions(Client client)
    throws AccessException, ServiceException, ExecutionException {
        List<WellSampleWrapper> wellSamples = getWellSamples(client);

        Collection<PlateAcquisitionWrapper> acqs = new ArrayList<>(wellSamples.size());
        for (WellSampleWrapper ws : wellSamples) {
            acqs.add(ws.getPlateAcquisition());
        }
        return distinct(acqs);
    }


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
    public List<PlateWrapper> getPlates(Client client)
    throws AccessException, ServiceException, ExecutionException {
        return distinct(getWells(client).stream()
                                        .map(WellWrapper::getPlate)
                                        .collect(Collectors.toList()));
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
     * @throws OMEROServerError   Server error.
     */
    public List<ScreenWrapper> getScreens(Client client)
    throws AccessException, ServiceException, ExecutionException, OMEROServerError {
        List<PlateWrapper> plates = getPlates(client);

        Collection<List<ScreenWrapper>> screens = new ArrayList<>(plates.size());
        for (PlateWrapper plate : plates) {
            screens.add(plate.getScreens(client));
        }
        return flatten(screens);
    }


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
    public boolean isOrphaned(Client client)
    throws ServiceException, OMEROServerError {
        String dsQuery = "select link.parent from DatasetImageLink link" +
                         " where link.child=" + getId();
        String wsQuery = "select ws from WellSample ws where image=" + getId();

        boolean noDataset    = client.findByQuery(dsQuery).isEmpty();
        boolean noWellSample = client.findByQuery(wsQuery).isEmpty();
        return noDataset && noWellSample;
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
     * @throws OMEROServerError   Server error.
     */
    public List<ImageWrapper> getFilesetImages(Client client)
    throws AccessException, ServiceException, ExecutionException, OMEROServerError {
        List<ImageWrapper> related = new ArrayList<>(0);
        if (data.isFSImage()) {
            long   fsId  = this.asDataObject().getFilesetId();
            String query = "select i from Image i where fileset=" + fsId;

            List<IObject> objects = client.findByQuery(query);

            Long[] ids = objects.stream()
                                .map(IObject::getId)
                                .map(RLong::getValue)
                                .sorted()
                                .toArray(Long[]::new);
            related = client.getImages(ids);
        }
        return related;
    }


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
    public List<ROIWrapper> saveROIs(Client client, Collection<? extends ROIWrapper> rois)
    throws ServiceException, AccessException, ExecutionException {
        rois.forEach(r -> r.setImage(this));
        List<ROIData> roisData = rois.stream()
                                     .map(GenericObjectWrapper::asDataObject)
                                     .collect(Collectors.toList());
        Collection<ROIData> results = call(client.getRoiFacility(),
                                           rf -> rf.saveROIs(client.getCtx(),
                                                             data.getId(),
                                                             roisData),
                                           "Cannot link ROI to " + this);
        return wrap(results, ROIWrapper::new);
    }


    /**
     * Links ROIs to the image in OMERO.
     * <p> DO NOT USE IT IF A SHAPE WAS DELETED !!!
     *
     * @param client The data manager.
     * @param rois   ROIs to be added.
     *
     * @return The updated list of ROIs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ROIWrapper> saveROIs(Client client, ROIWrapper... rois)
    throws ServiceException, AccessException, ExecutionException {
        return saveROIs(client, Arrays.asList(rois));
    }


    /**
     * @param client The client handling the connection.
     * @param roi    ROI to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Links a ROI to the image in OMERO
     * <p> DO NOT USE IT IF A SHAPE WAS DELETED !!!
     */
    @Deprecated
    public void saveROI(Client client, ROIWrapper roi)
    throws ServiceException, AccessException, ExecutionException {
        roi.setData(saveROIs(client, roi).iterator().next().asDataObject());
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
    public List<ROIWrapper> getROIs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIResult> roiResults = call(client.getRoiFacility(),
                                          rf -> rf.loadROIs(client.getCtx(),
                                                            data.getId()),
                                          "Cannot get ROIs from " + this);

        List<ROIWrapper> roiWrappers = roiResults.stream()
                                                 .map(ROIResult::getROIs)
                                                 .flatMap(Collection::stream)
                                                 .map(ROIWrapper::new)
                                                 .sorted(comparing(GenericObjectWrapper::getId))
                                                 .collect(Collectors.toList());

        return distinct(roiWrappers);
    }


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
    public List<FolderWrapper> getROIFolders(Client client)
    throws ServiceException, AccessException, ExecutionException {
        Collection<FolderData> folders = call(client.getRoiFacility(),
                                              rf -> rf.getROIFolders(client.getCtx(),
                                                                     data.getId()),
                                              "Cannot get folders for " + this);

        return wrap(folders, FolderWrapper::new);
    }


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
    public List<FolderWrapper> getFolders(Client client)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError {
        String template = "select link.parent from FolderImageLink as link" +
                          " where link.child.id=%d";
        String query = String.format(template, getId());
        Long[] ids = client.findByQuery(query)
                           .stream()
                           .map(o -> o.getId().getValue())
                           .toArray(Long[]::new);
        return client.loadFolders(ids);
    }


    /**
     * @param client   The client handling the connection.
     * @param folderId ID of the folder.
     *
     * @return The folder if it exists.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws OMEROServerError       Server error.
     * @throws NoSuchElementException Folder does not exist.
     * @deprecated Gets the folder with the specified id on OMERO.
     */
    @Deprecated
    public FolderWrapper getFolder(Client client, Long folderId)
    throws ServiceException, OMEROServerError {
        List<IObject> os = client.findByQuery("select f " +
                                              "from Folder as f " +
                                              "where f.id = " +
                                              folderId);

        FolderWrapper folderWrapper = new FolderWrapper((Folder) os.iterator().next());
        folderWrapper.setImage(data.getId());

        return folderWrapper;
    }


    /**
     * Gets the Pixels for this image.
     *
     * @return See above.
     */
    public PixelsWrapper getPixels() {
        return new PixelsWrapper(data.getDefaultPixels());
    }


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
    public ImagePlus toImagePlus(Client client)
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
    public ImagePlus toImagePlus(Client client,
                                 int[] xBounds,
                                 int[] yBounds,
                                 int[] cBounds,
                                 int[] zBounds,
                                 int[] tBounds)
    throws ServiceException, AccessException, ExecutionException {
        PixelsWrapper pixels = this.getPixels();
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
    public ImagePlus toImagePlus(Client client, ROIWrapper roi)
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
    public ImagePlus toImagePlus(Client client, Bounds bounds)
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
     * @param client The client handling the connection.
     *
     * @return the channels.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ChannelWrapper> getChannels(Client client)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get the channel name for " + this;
        List<ChannelData> channels = call(client.getMetadata(),
                                          m -> m.getChannelData(client.getCtx(),
                                                                getId()),
                                          error);
        return channels.stream()
                       .sorted(comparing(ChannelData::getIndex))
                       .map(ChannelWrapper::new)
                       .collect(Collectors.toList());
    }


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
    public String getChannelName(Client client, int index)
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
    public Color getChannelImportedColor(Client client, int index)
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
    public Color getChannelColor(Client client, int index)
    throws ServiceException, AccessException, ExecutionException {
        long  pixelsId = data.getDefaultPixels().getId();
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
     * @param client The client handling the connection.
     * @param size   The thumbnail size.
     *
     * @return The thumbnail as a {@link BufferedImage}.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError Server error.
     * @throws IOException      Cannot read thumbnail from store.
     */
    public BufferedImage getThumbnail(Client client, int size)
    throws ServiceException, OMEROServerError, IOException {
        BufferedImage thumbnail = null;

        byte[] arr = ExceptionHandler.of(client,
                                         c -> getThumbnailBytes(c, size))
                                     .handleServiceOrServer("Error retrieving thumbnail.")
                                     .get();
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
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<String> getOriginalPaths(Client client)
    throws ExecutionException, AccessException, ServiceException {
        String error = "Cannot get managed repositories paths for " + this;
        return call(client.getMetadata(),
                    m -> m.getOriginalPaths(client.getCtx(), data),
                    error);
    }


    /**
     * Returns the file paths of the image in the managed repository.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<String> getManagedRepositoriesPaths(Client client)
    throws ExecutionException, AccessException, ServiceException {
        String error = "Cannot get managed repositories paths for " + this;
        return call(client.getMetadata(),
                    m -> m.getManagedRepositoriesPaths(client.getCtx(), data),
                    error);
    }


    /**
     * Downloads the original files from the server.
     *
     * @param client The client handling the connection.
     * @param path   Path to the file.
     *
     * @return See above.
     *
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<File> download(Client client, String path)
    throws OMEROServerError, ServiceException, AccessException {
        List<File> files = new ArrayList<>(0);
        try {
            files = call(client.getGateway().getFacility(TransferFacility.class),
                         t -> t.downloadImage(client.getCtx(), path, getId()),
                         "Could not download image " + getId());
        } catch (ExecutionException e) {
            // IGNORE FOR API COMPATIBILITY
        }
        return files;
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
