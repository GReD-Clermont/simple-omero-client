/*
 *  Copyright (C) 2020 GReD
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

package fr.igred.omero;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.exception.ServerError;
import fr.igred.omero.metadata.ROIContainer;
import fr.igred.omero.metadata.annotation.MapAnnotationContainer;
import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.sort.SortTagAnnotationContainer;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import loci.common.DataTools;
import loci.formats.FormatTools;
import omero.api.RawFileStorePrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.FolderData;
import omero.gateway.model.ImageData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.gateway.model.TagAnnotationData;
import omero.model.*;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;


/**
 * Class containing an ImageData.
 * <p> Implements function using the ImageData contained
 */
public class ImageContainer {

    /** ImageData contained */
    final ImageData image;


    /**
     * Constructor of the class ImageContainer
     *
     * @param image The image contained in the ImageContainer.
     */
    public ImageContainer(ImageData image) {
        this.image = image;
    }


    /**
     * Gets the ImageData id
     *
     * @return id.
     */
    public Long getId() {
        return image.getId();
    }


    /**
     * Gets the ImageData name
     *
     * @return name.
     */
    public String getName() {
        return image.getName();
    }


    /**
     * Gets the ImageData description
     *
     * @return description.
     */
    public String getDescription() {
        return image.getDescription();
    }


    /**
     * Gets the ImageData creation date
     *
     * @return creation date.
     */
    public Timestamp getCreated() {
        return image.getCreated();
    }


    /**
     * Gets the ImageData acquisition date
     *
     * @return acquisition date.
     */
    public Timestamp getAcquisitionDate() {
        return image.getAcquisitionDate();
    }


    /**
     * @return ImageData contained.
     */
    public ImageData getImage() {
        return image;
    }


    /**
     * Adds a tag to the image in OMERO. The tag is created from the name and description in parameters.
     *
     * @param client      The user.
     * @param name        Name of the tag.
     * @param description Description of the tag.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ImageAnnotationLink addTag(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotationData tagData = new TagAnnotationData(name);
        tagData.setTagDescription(description);

        return addTag(client, tagData);
    }


    /**
     * Adds a tag to the image in OMERO. The tag to be added is already created.
     *
     * @param client The user.
     * @param tag    TagAnnotationContainer containing the tag to be added.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ImageAnnotationLink addTag(Client client, TagAnnotationContainer tag)
    throws ServiceException, AccessException, ExecutionException {
        return addTag(client, tag.getTag());
    }


    /**
     * Private function. Adds a tag to the image in OMERO.
     *
     * @param client  The user.
     * @param tagData Tag to be added.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    private ImageAnnotationLink addTag(Client client, TagAnnotationData tagData)
    throws ServiceException, AccessException, ExecutionException {
        ImageAnnotationLink newLink;
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(tagData.asAnnotation());
        link.setParent(image.asImage());

        try {
            newLink = (ImageAnnotationLink) client.getDm().saveAndReturnObject(client.getCtx(), link);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        return newLink;
    }


    /**
     * Adds a tag to the image in OMERO. The tag id is used.
     *
     * @param client The user.
     * @param id     Id of the tag.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ImageAnnotationLink addTag(Client client, Long id)
    throws ServiceException, AccessException, ExecutionException {
        ImageAnnotationLink newLink;
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(new TagAnnotationI(id, false));
        link.setParent(image.asImage());

        try {
            newLink = (ImageAnnotationLink) client.getDm().saveAndReturnObject(client.getCtx(), link);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        return newLink;
    }


    /**
     * Adds multiple tag to the image in OMERO.
     *
     * @param client The user.
     * @param tags   Table of TagAnnotationContainer to add.
     *
     * @return The objects saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public Collection<ImageAnnotationLink> addTags(Client client, TagAnnotationContainer... tags)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ImageAnnotationLink> links = new ArrayList<>();
        for (TagAnnotationContainer tag : tags) {
            ImageAnnotationLink link = addTag(client, tag.getTag());
            links.add(link);
        }

        return links;
    }


    /**
     * Adds multiple tag to the image in OMERO. The tags id is used
     *
     * @param client The user.
     * @param ids    Table of tag id to add.
     *
     * @return The objects saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public Collection<ImageAnnotationLink> addTags(Client client, Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ImageAnnotationLink> links = new ArrayList<>();
        for (Long id : ids) {
            ImageAnnotationLink link = addTag(client, id);
            links.add(link);
        }

        return links;
    }


    /**
     * Gets all tag linked to an image in OMERO
     *
     * @param client The user.
     *
     * @return List of TagAnnotationContainer each containing a tag linked to the image.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<TagAnnotationContainer> getTags(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<Long> userIds = new ArrayList<>();
        userIds.add(client.getId());

        List<Class<? extends AnnotationData>> types = new ArrayList<>();
        types.add(TagAnnotationData.class);

        List<AnnotationData> annotations;
        try {
            annotations = client.getMetadata().getAnnotations(client.getCtx(), image, types, userIds);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        List<TagAnnotationContainer> tags = new ArrayList<>();

        if (annotations != null) {
            for (AnnotationData annotation : annotations) {
                TagAnnotationData tagAnnotation = (TagAnnotationData) annotation;

                tags.add(new TagAnnotationContainer(tagAnnotation));
            }
        }

        tags.sort(new SortTagAnnotationContainer());
        return tags;
    }


    /**
     * Gets the List of NamedValue (Key-Value pair) associated to an image.
     *
     * @param client The user.
     *
     * @return Collection of NamedValue.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<NamedValue> getKeyValuePairs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<Long> userIds = new ArrayList<>();
        userIds.add(client.getId());

        List<Class<? extends AnnotationData>> types = new ArrayList<>();
        types.add(MapAnnotationData.class);

        List<AnnotationData> annotations;
        try {
            annotations = client.getMetadata().getAnnotations(client.getCtx(), image, types, userIds);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        List<NamedValue> keyValuePairs = new ArrayList<>();

        if (annotations != null) {
            for (AnnotationData annotation : annotations) {
                MapAnnotationData mapAnnotation = (MapAnnotationData) annotation;

                @SuppressWarnings("unchecked")
                List<NamedValue> list = (List<NamedValue>) mapAnnotation.getContent();

                keyValuePairs.addAll(list);
            }
        }

        return keyValuePairs;
    }


    /**
     * Gets the value from a Key-Value pair associated to the image
     *
     * @param client The user.
     * @param key    Key researched.
     *
     * @return Value associated to the key.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException Key not found.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public String getValue(Client client, String key)
    throws ServiceException, AccessException, NoSuchElementException, ExecutionException {
        Collection<NamedValue> keyValuePairs = getKeyValuePairs(client);

        for (NamedValue namedValue : keyValuePairs) {
            if (namedValue.name.equals(key)) {
                return namedValue.value;
            }
        }

        throw new NoSuchElementException("Key value pair " + key + " not found");
    }


    /**
     * Adds a List of Key-Value pair to the image The list is contained in the MapAnnotationContainer
     *
     * @param client The user.
     * @param data   MapAnnotationContainer containing a list of NamedValue.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addMapAnnotation(Client client, MapAnnotationContainer data)
    throws ServiceException, AccessException, ExecutionException {
        try {
            client.getDm().attachAnnotation(client.getCtx(),
                                            data.getMapAnnotation(),
                                            new ImageData(new ImageI(image.getId(), false)));
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }
    }


    /**
     * Adds a single Key-Value pair to the image.
     *
     * @param client The user.
     * @param key    Name of the key.
     * @param value  Value associated to the key.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<NamedValue> result = new ArrayList<>();
        result.add(new NamedValue(key, value));

        MapAnnotationData data = new MapAnnotationData();
        data.setContent(result);
        try {
            client.getDm().attachAnnotation(client.getCtx(), data, new ImageData(new ImageI(image.getId(), false)));
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }
    }


    /**
     * Links a ROI to the image in OMERO !!! DO NOT USE IT IF A SHAPE WAS DELETED !!!
     *
     * @param client The user.
     * @param roi    ROI to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void saveROI(Client client, ROIContainer roi)
    throws ServiceException, AccessException, ExecutionException {
        try {
            ROIData roiData = client.getRoiFacility()
                                    .saveROIs(client.getCtx(),
                                              image.getId(),
                                              Collections.singletonList(roi.getROI()))
                                    .iterator().next();
            roi.setData(roiData);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }
    }


    /**
     * Gets all ROIs linked to the image in OMERO
     *
     * @param client The user.
     *
     * @return List of ROIs linked to the image.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ROIContainer> getROIs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIResult> roiResults;
        try {
            roiResults = client.getRoiFacility().loadROIs(client.getCtx(), image.getId());
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }
        ROIResult r = roiResults.iterator().next();

        Collection<ROIData> rois = r.getROIs();

        List<ROIContainer> roiContainers = new ArrayList<>(rois.size());

        for (ROIData roi : rois) {
            ROIContainer temp = new ROIContainer(roi);
            roiContainers.add(temp);
        }

        return roiContainers;
    }


    /**
     * Gets the list of Folder linked to the image Associate the folder to the image
     *
     * @param client The user.
     *
     * @return List of FolderContainer containing the folder.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<FolderContainer> getFolders(Client client)
    throws ServiceException, AccessException, ExecutionException {
        ROIFacility roiFacility = client.getRoiFacility();

        Collection<FolderData> folders;
        try {
            folders = roiFacility.getROIFolders(client.getCtx(), this.image.getId());
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        List<FolderContainer> roiFolders = new ArrayList<>(folders.size());
        for (FolderData folder : folders) {
            FolderContainer roiFolder = new FolderContainer(folder);
            roiFolder.setImage(this.image.getId());

            roiFolders.add(roiFolder);
        }

        return roiFolders;
    }


    /**
     * Gets the folder with the specified id on OMERO.
     *
     * @param client   The user.
     * @param folderId Id of the folder.
     *
     * @return The folder if it exist.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerError      Server error.
     */
    public FolderContainer getFolder(Client client, Long folderId) throws ServiceException, ServerError {
        List<IObject> os = client.findByQuery("select f " +
                                              "from Folder as f " +
                                              "where f.id = " +
                                              folderId);

        FolderContainer folderContainer = new FolderContainer((Folder) os.get(0));
        folderContainer.setImage(this.image.getId());

        return folderContainer;
    }


    /**
     * Gets the PixelContainer of the image
     *
     * @return Contains the PixelsData associated with the image.
     */
    public PixelContainer getPixels() {
        return new PixelContainer(image.getDefaultPixels());
    }


    /**
     * Generates the ImagePlus from the ij library corresponding to the image from OMERO WARNING : you need to include
     * the ij library to use this function
     *
     * @param client The user.
     *
     * @return ImagePlus generated from the current image.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ImagePlus toImagePlus(Client client) throws AccessException, ExecutionException {
        return this.toImagePlus(client, null, null, null, null, null);
    }


    /**
     * Gets the imagePlus generated from the image from OMERO corresponding to the bound
     *
     * @param client The user.
     * @param xBound Array containing the X bound from which the pixels should be retrieved.
     * @param yBound Array containing the Y bound from which the pixels should be retrieved.
     * @param cBound Array containing the C bound from which the pixels should be retrieved.
     * @param zBound Array containing the Z bound from which the pixels should be retrieved.
     * @param tBound Array containing the T bound from which the pixels should be retrieved.
     *
     * @return an ImagePlus from the ij library.
     *
     * @throws AccessException    If an error occurs while retrieving the plane data from the pixels source.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ImagePlus toImagePlus(Client client, int[] xBound, int[] yBound, int[] cBound, int[] zBound, int[] tBound)
    throws AccessException, ExecutionException {
        PixelContainer pixels = this.getPixels();

        int sizeT, sizeZ, sizeC, sizeX, sizeY;
        int tStart, zStart, cStart, xStart, yStart;
        int tEnd, zEnd, cEnd, xEnd, yEnd;

        if (tBound != null) {
            tStart = Math.max(0, tBound[0]);
            tEnd = Math.min(pixels.getSizeT() - 1, tBound[1]);
        } else {
            tStart = 0;
            tEnd = pixels.getSizeT() - 1;
        }
        sizeT = tEnd - tStart + 1;

        if (zBound != null) {
            zStart = Math.max(0, zBound[0]);
            zEnd = Math.min(pixels.getSizeZ() - 1, zBound[1]);
        } else {
            zStart = 0;
            zEnd = pixels.getSizeZ() - 1;
        }
        sizeZ = zEnd - zStart + 1;

        if (cBound != null) {
            cStart = Math.max(0, cBound[0]);
            cEnd = Math.min(pixels.getSizeC() - 1, cBound[1]);
        } else {
            cStart = 0;
            cEnd = pixels.getSizeC() - 1;
        }
        sizeC = cEnd - cStart + 1;

        if (xBound != null) {
            xStart = Math.max(0, xBound[0]);
            xEnd = Math.min(pixels.getSizeX() - 1, xBound[1]);
        } else {
            xStart = 0;
            xEnd = pixels.getSizeX() - 1;
        }
        sizeX = xEnd - xStart + 1;

        if (yBound != null) {
            yStart = Math.max(0, yBound[0]);
            yEnd = Math.min(pixels.getSizeY() - 1, yBound[1]);
        } else {
            yStart = 0;
            yEnd = pixels.getSizeY() - 1;
        }
        sizeY = yEnd - yStart + 1;

        Length spacingX = pixels.getPixelSizeX();
        Length spacingY = pixels.getPixelSizeY();
        Length spacingZ = pixels.getPixelSizeZ();

        String pixelType   = pixels.getPixelType();
        int    pixels_type = FormatTools.pixelTypeFromString(pixelType);
        int    bpp         = FormatTools.getBytesPerPixel(pixels_type);

        ImagePlus imp = IJ.createHyperStack(image.getName(), sizeX, sizeY, sizeC, sizeZ, sizeT, bpp * 8);

        Calibration cal = imp.getCalibration();

        if (spacingX != null) {
            cal.setXUnit(spacingX.getUnit().name());
            cal.pixelWidth = spacingX.getValue();
        }
        if (spacingY != null) {
            cal.setYUnit(spacingY.getUnit().name());
            cal.pixelHeight = spacingY.getValue();
        }
        if (spacingZ != null) {
            cal.setZUnit(spacingZ.getUnit().name());
            cal.pixelDepth = spacingZ.getValue();
        }

        imp.setCalibration(cal);

        boolean is_float  = FormatTools.isFloatingPoint(pixels_type);
        boolean is_little = false;

        ImageStack stack = imp.getImageStack();

        double min = imp.getProcessor().getMin();
        double max = 0;

        for (int t = tStart; t <= tEnd; t++) {
            int[] tBoundTemp = {t, t};
            for (int z = zStart; z <= zEnd; z++) {
                int[] zBoundTemp = {z, z};
                for (int c = cStart; c <= cEnd; c++) {
                    int[] cBoundTemp = {c, c};
                    int   n          = imp.getStackIndex(c - cStart + 1, z - zStart + 1, t - tStart + 1);

                    byte[] tiles = pixels.getRawPixels(client,
                                                       xBound,
                                                       yBound,
                                                       cBoundTemp,
                                                       zBoundTemp,
                                                       tBoundTemp,
                                                       bpp)[0][0][0];

                    stack.setPixels(DataTools.makeDataArray(tiles, bpp, is_float, is_little), n);
                    ImageProcessor ip = stack.getProcessor(n);
                    ip.resetMinAndMax();

                    max = Math.max(ip.getMax(), max);
                    min = Math.min(ip.getMin(), min);

                    stack.setProcessor(ip, n);
                }
            }
        }

        imp.setStack(stack);
        imp.setOpenAsHyperStack(true);
        imp.setDisplayMode(IJ.COMPOSITE);

        imp.getProcessor().setMinAndMax(min, max);

        return imp;
    }


    /**
     * Gets the name of the channel
     *
     * @param client The user.
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
        List<ChannelData> channels;
        try {
            channels = client.getMetadata().getChannelData(client.getCtx(), this.image.getId());
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        return channels.get(index).getChannelLabeling();
    }


    /**
     * Links a file to the Dataset
     *
     * @param client The user.
     * @param file   File to add.
     *
     * @return File created in OMERO.
     *
     * @throws ServiceException      Cannot connect to OMERO.
     * @throws AccessException       Cannot access data on server.
     * @throws ExecutionException    A Facility can't be retrieved or instantiated.
     * @throws ServerError           Server error.
     * @throws FileNotFoundException The file could not be found.
     * @throws IOException           If an I/O error occurs.
     */
    public ImageAnnotationLink addFile(Client client, File file) throws
                                                                 ServiceException,
                                                                 AccessException,
                                                                 ExecutionException,
                                                                 ServerError,
                                                                 FileNotFoundException,
                                                                 IOException {
        int INC = 262144;

        ImageAnnotationLink newLink;
        RawFileStorePrx     rawFileStore;

        String name         = file.getName();
        String absolutePath = file.getAbsolutePath();
        String path         = absolutePath.substring(0, absolutePath.length() - name.length());

        OriginalFile originalFile = new OriginalFileI();
        originalFile.setName(omero.rtypes.rstring(name));
        originalFile.setPath(omero.rtypes.rstring(path));
        originalFile.setSize(omero.rtypes.rlong(file.length()));

        final ChecksumAlgorithm checksumAlgorithm = new ChecksumAlgorithmI();
        checksumAlgorithm.setValue(omero.rtypes.rstring(ChecksumAlgorithmSHA1160.value));
        originalFile.setHasher(checksumAlgorithm);
        originalFile.setMimetype(omero.rtypes.rstring(FilenameUtils.getExtension(file.getName())));

        try {
            originalFile = (OriginalFile) client.getDm().saveAndReturnObject(client.getCtx(), originalFile);
            rawFileStore = client.getGateway().getRawFileService(client.getCtx());
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        long       pos = 0;
        int        rLen;
        byte[]     buf = new byte[INC];
        ByteBuffer bBuf;
        try (FileInputStream stream = new FileInputStream(file)) {
            rawFileStore.setFileId(originalFile.getId().getValue());
            while ((rLen = stream.read(buf)) > 0) {
                rawFileStore.write(buf, pos, rLen);
                pos += rLen;
                bBuf = ByteBuffer.wrap(buf);
                bBuf.limit(rLen);
            }
            originalFile = rawFileStore.save();
            rawFileStore.close();
        } catch (omero.ServerError se) {
            throw new ServerError("Server error", se);
        }

        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(originalFile);
        fa.setDescription(omero.rtypes.rstring(""));
        fa.setNs(omero.rtypes.rstring(file.getName()));

        try {
            fa = (FileAnnotation) client.getDm().saveAndReturnObject(client.getCtx(), fa);

            ImageAnnotationLink link = new ImageAnnotationLinkI();
            link.setChild(fa);
            link.setParent(image.asImage());
            newLink = (ImageAnnotationLink) client.getDm().saveAndReturnObject(client.getCtx(), link);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }
        return newLink;
    }

}
