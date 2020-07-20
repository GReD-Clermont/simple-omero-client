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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FilenameUtils;

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
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.DataSourceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.gateway.model.TagAnnotationData;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.Length;
import omero.model.NamedValue;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.TagAnnotationI;
import omero.model.enums.ChecksumAlgorithmSHA1160;

/**
 * Class containing an ImageData.
 * Implements function using the ImageData contained
 */
public class ImageContainer {
    ///ImageData contained
    ImageData image;
    
    /**
     * Return the ImageData id
     * 
     * @return id
     */
    public Long getId()
    {   
        return image.getId();
    }

    /**
     * Return the ImageData name
     * 
     * @return name
     */
    public String getName()
    {
        return image.getName();
    }

    /**
     * Return the ImageData description
     * 
     * @return description
     */
    public String getDescription()
    {
        return image.getDescription();
    }

    /**
     * Return the ImageData creation date
     * 
     * @return creation date
     */
    public Timestamp getCreated()
    {
        return image.getCreated();
    }

    /**
     * Return the ImageData acquisition date
     * 
     * @return acquisition date
     */
    public Timestamp getAcquisitionDate()
    {
        return image.getAcquisitionDate();
    }

    /**
     * @return ImageData contained 
     */
    public ImageData getImage()
    {
        return image;
    }
    




    /**
     * Add a tag to the image in OMERO.
     * The tag is created from the name and description in parameters.
     * 
     * @param client      The user
     * @param name        Name of the tag
     * @param description Description of the tag
     * 
     * @return The object saved in OMERO.
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public IObject addTag(Client client, 
                          String name, 
                          String description)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        TagAnnotationData tagData = new TagAnnotationData(name);
        tagData.setTagDescription(description);

        IObject r = addTag(client, tagData);

        return r;
    }

    /**
     * Add a tag to the image in OMERO.
     * The tag to be added is already created.
     * 
     * @param client The user
     * @param tag    TagAnnotationContainer containing the tag to be added
     * 
     * @return The object saved in OMERO.
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public IObject addTag(Client                  client, 
                          TagAnnotationContainer  tag)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        IObject r = addTag(client, tag.getTag());

        return r;
    }

    /**
     * Private function.
     * Add a tag to the image in OMERO.
     * 
     * @param client  The user
     * @param tagData Tag to be added
     * 
     * @return The object saved in OMERO. 
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    private IObject addTag(Client            client, 
                           TagAnnotationData tagData)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(tagData.asAnnotation());
        link.setParent(image.asImage());

        IObject r = client.getDm().saveAndReturnObject(client.getCtx(), link);

        return r;
    }

    /**
     * Add a tag to the image in OMERO.
     * The tag id is used.
     * 
     * @param client The user
     * @param id     Id of the tag
     * 
     * @return The object saved in OMERO. 
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public IObject addTag(Client client, 
                          Long   id)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(new TagAnnotationI(id, false));
        link.setParent(image.asImage());
        IObject r = client.getDm().saveAndReturnObject(client.getCtx(), link);

        return r;
    }

    /**
     * Add multiple tag to the image in OMERO.
     * 
     * @param client The user
     * @param tags   Table of TagAnnotationContainer to add 
     * 
     * @return The objects saved in OMERO
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public Collection<IObject> addTags(Client                    client, 
                                       TagAnnotationContainer... tags)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<IObject> objects = new ArrayList<IObject>();
        for(TagAnnotationContainer tag : tags)
        {
            IObject r = addTag(client, tag.getTag());
            objects.add(r);
        }
        
        return objects;
    }

    /**
     * Add multiple tag to the image in OMERO.
     * The tags id is used
     * 
     * @param client The user
     * @param ids    Table of tag id to add
     * 
     * @return The objects saved in OMERO
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public Collection<IObject> addTags(Client  client, 
                                       Long... ids)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<IObject> objects = new ArrayList<IObject>();
        for(Long id : ids) {
            IObject r = addTag(client, id);
            objects.add(r);
        }
        
        return objects;
    }

    /**
     * Get all tag linked to an image in OMERO
     * 
     * @param client The user
     * 
     * @return List of TagAnnotationContainer each containing a tag linked to the image
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public List<TagAnnotationContainer> getTags(Client client)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        List<Long> userIds = new ArrayList<Long>();
        userIds.add(client.getId());

        List<Class<? extends AnnotationData>> types = new ArrayList<Class<? extends AnnotationData>>();
        types.add(TagAnnotationData.class);

        List<AnnotationData> annotations = client.getMetadata().getAnnotations(client.getCtx(), image, types, userIds);

        List<TagAnnotationContainer> tags = new ArrayList<TagAnnotationContainer>();

        if(annotations != null) {
            for (AnnotationData annotation : annotations) {
                TagAnnotationData tagAnnotation = (TagAnnotationData) annotation;
                
                tags.add(new TagAnnotationContainer(tagAnnotation));
            }
        }

        Collections.sort(tags, new SortTagAnnotationContainer());
        return tags;
    }

    /**
     * Get all File linked to an image in OMERO
     * 
     * @param client The user
     * 
     * @return List of File 
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public List<File> getFiles(Client client)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        List<Long> userIds = new ArrayList<Long>();
        userIds.add(client.getId());

        List<Class<? extends AnnotationData>> types = new ArrayList<Class<? extends AnnotationData>>();
        types.add(FileAnnotationData.class);

        List<AnnotationData> annotations = client.getMetadata().getAnnotations(client.getCtx(), image, types, userIds);

        List<File> files = new ArrayList<File>(annotations.size());

        if(annotations != null) {
            for (AnnotationData annotation : annotations) {
                FileAnnotationData file = (FileAnnotationData) annotation;
                files.add(file.getAttachedFile());
            }
        }

        return files;
    }

    /**
     * Get the List of NamedValue (Key-Value pair) associated to an image.
     * 
     * @param client The user
     * 
     * @return Collection of NamedValue
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public List<NamedValue> getKeyValuePairs(Client client)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        List<Long> userIds = new ArrayList<Long>();
        userIds.add(client.getId());

        List<Class<? extends AnnotationData>> types = new ArrayList<Class<? extends AnnotationData>>();
        types.add(MapAnnotationData.class);

        List<AnnotationData> annotations = client.getMetadata().getAnnotations(client.getCtx(), image, types, userIds);

        List<NamedValue> keyValuePairs = new ArrayList<NamedValue>();

        if(annotations != null) {
            for (AnnotationData annotation : annotations)  {    
                MapAnnotationData mapAnnotation = (MapAnnotationData) annotation;

                List<NamedValue> list = (List<NamedValue>) mapAnnotation.getContent();

                for (NamedValue namedValue : list)
                    keyValuePairs.add(namedValue);
            }
        }

        return keyValuePairs;
    }

    /**
     * Get the value from a Key-Value pair associated to the image
     * 
     * @param client The user
     * @param key    Key researched
     * 
     * @return Value associated to the key
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws NoSuchElementException  Key not found
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public String getValue(Client client, 
                           String key)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            NoSuchElementException,
            ExecutionException
    {
        Collection<NamedValue> keyValuePairs = getKeyValuePairs(client);

        for (NamedValue namedValue : keyValuePairs) {
            if(namedValue.name.equals(key)) {
                return namedValue.value;
            }
        }

        throw new NoSuchElementException("Key value pair " + key + " not found");
    }




    /**
     * Add a List of Key-Value pair to the image
     * The list is contained in the MapAnnotationContainer
     * 
     * @param client The user
     * @param data   MapAnnotationContainer containing a list of NamedValue
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public void addMapAnnotation(Client                 client, 
                                 MapAnnotationContainer data)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        client.getDm().attachAnnotation(client.getCtx(), data.getMapAnnotation(), new ImageData(new ImageI(image.getId(), false)));
    }

    /**
     * Add a single Key-Value pair to the image.
     * 
     * @param client The user
     * @param key    Name of the key
     * @param value  Value associated to the key
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public void addPairKeyValue(Client client, 
                                String key,
                                String value)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        List<NamedValue> result = new ArrayList<NamedValue>();
        result.add(new NamedValue(key, value));

        MapAnnotationData data = new MapAnnotationData();
        data.setContent(result);

        client.getDm().attachAnnotation(client.getCtx(), data, new ImageData(new ImageI(image.getId(), false)));
    }




    /**
     * Link a ROI to the image in OMERO
     * !!! DO NOT USE IT IF A SHAPE WAS DELETED !!!
     *  
     * @param client The user
     * @param roi    ROI to be added
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public void saveROI(Client       client, 
                        ROIContainer roi)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        ROIData roiData = client.getRoiFacility().saveROIs(client.getCtx(), image.getId(), Arrays.asList(roi.getROI())).iterator().next();

        roi.setData(roiData);
    }

    /**
     * Get all ROIs linked to the image in OMERO 
     * 
     * @param client The user
     * 
     * @return List of ROIs linked to the image
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public List<ROIContainer> getROIs(Client client)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        List<ROIResult> roiresults = client.getRoiFacility().loadROIs(client.getCtx(), image.getId());
        ROIResult r = roiresults.iterator().next();

        Collection<ROIData> rois = r.getROIs();

        List<ROIContainer> roiContainers = new ArrayList<ROIContainer>(rois.size());

        for(ROIData roi : rois) 
        {
            ROIContainer temp = new ROIContainer(roi);
                
            roiContainers.add(temp);
        }

        return roiContainers;
    }




    /**
     * Get the PixelContainer of the image
     * 
     * @return Contains the PixelsData associated with the image
     */
    public PixelContainer getPixels()
    {
        return new PixelContainer(image.getDefaultPixels());
    }




    /**
     * Generate the ImagePlus from the ij library corresponding to the image from OMERO
     * WARNING : you need to include the ij library to use this function
     * 
     * @param client The user
     * 
     * @return ImagePlus generated from the current image
     * 
     * @throws DataSourceException
     * @throws ExecutionException
     */
    public ImagePlus toImagePlus(Client client)
        throws
            DataSourceException,
            ExecutionException
    {
        ImagePlus imp = this.toImagePlus(client, null, null, null, null, null);

        return imp;
    }

    /**
     * Return the imagePlus generated from the image from OMERO corresponding to the bound
     * 
     * @param client The user
     * @param xBound Array containing the X bound from which the pixels should be retrieved 
     * @param yBound Array containing the Y bound from which the pixels should be retrieved 
     * @param cBound Array containing the C bound from which the pixels should be retrieved 
     * @param zBound Array containing the Z bound from which the pixels should be retrieved 
     * @param tBound Array containing the T bound from which the pixels should be retrieved 
     *  
     * @return an ImagePlus from the ij library
     * 
     * @throws DataSourceException 
     * @throws ExecutionException  A Facility can't be retrieved or instancied
     */
    public ImagePlus toImagePlus(Client client,
                                 int    xBound[],
                                 int    yBound[],
                                 int    cBound[],
                                 int    zBound[],
                                 int    tBound[])
        throws
            DataSourceException,
            ExecutionException
    {
        PixelContainer pixels = this.getPixels();

        int sizeT, sizeZ, sizeC, sizeX, sizeY;
        int tStart, zStart, cStart, xStart, yStart;
        int tEnd, zEnd, cEnd, xEnd, yEnd;

        if(tBound != null) {
            tStart = tBound[0];
            tEnd   = tBound[1] + 1;
        }
        else {
            tStart = 0;
            tEnd   = pixels.getSizeT();
        }
        sizeT = tEnd - tStart;

        if(zBound != null) {
            zStart = zBound[0];
            zEnd   = zBound[1] + 1;
        }
        else {
            zStart = 0;
            zEnd   = pixels.getSizeZ();
        }
        sizeZ = zEnd - zStart;

        if(cBound != null) {
            cStart = cBound[0];
            cEnd   = cBound[1] + 1;
        }
        else {
            cStart = 0;
            cEnd   = pixels.getSizeC();
        }
        sizeC = cEnd - cStart;

        if(xBound != null) {
            xStart = xBound[0];
            xEnd   = xBound[1] + 1;
        }
        else {
            xStart = 0;
            xEnd = pixels.getSizeX();
        }
        sizeX = xEnd - xStart;

        if(yBound != null) {
            yStart = yBound[0];
            yEnd   = yBound[1] + 1;
        }
        else {
            yStart = 0;
            yEnd   = pixels.getSizeY();
        }
        sizeY = yEnd - yStart;

        Length spacingX = pixels.getPixelSizeX();
        Length spacingY = pixels.getPixelSizeY();
        Length spacingZ = pixels.getPixelSizeZ();

        String pixtype = pixels.getPixelType();
        int pixels_type = FormatTools.pixelTypeFromString(pixtype);
        int bpp = FormatTools.getBytesPerPixel(pixels_type);

        ImagePlus imp = IJ.createHyperStack("tmp", sizeX, sizeY, sizeC, sizeZ, sizeT, bpp*8);

        Calibration cal   = imp.getCalibration();
        
        if (spacingX != null) {
            cal.setXUnit(spacingX.getUnit().name());
            cal.pixelWidth  = spacingX.getValue();
        }
        if (spacingY != null) {
            cal.setYUnit(spacingY.getUnit().name());
            cal.pixelHeight = spacingY.getValue();
        }
        if (spacingZ != null) {
            cal.setZUnit(spacingZ.getUnit().name());
            cal.pixelDepth  = spacingZ.getValue();
        }

        imp.setCalibration(cal);

        boolean is_float = FormatTools.isFloatingPoint(pixels_type);
        boolean is_little = false;

        ImageStack stack = imp.getImageStack();

        double min = imp.getProcessor().getMin();
        double max = 0;

        for(int t = 0; t < sizeT; t++) {
            int tBoundTemp[] = {t, t};
            for(int z = 0; z < sizeZ; z++) {
                int zBoundTemp[] = {z, z};
                for(int c = 0; c < sizeC; c++) {
                    int cBoundTemp[] = {c, c};
                    int n = imp.getStackIndex(c+1, z+1, t+1);

                    byte tiles[] = pixels.getRawPixels(client, xBound, yBound, cBoundTemp, zBoundTemp, tBoundTemp, bpp)[0][0][0];

                    stack.setPixels(DataTools.makeDataArray(tiles, bpp, is_float, is_little), n);
                    ImageProcessor ip = stack.getProcessor(n);
                    ip.resetMinAndMax();

                    max = ip.getMax() > max ? ip.getMax() : max;
                    min = ip.getMin() < min ? ip.getMin() : min;

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
     * Return the name of the channel 
     * 
     * @param client The user
     * @param index  Channel number
     * 
     * @return name of the channel
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public String getChannelName(Client client, 
                                 int index) 
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        List<ChannelData> channels = client.getMetadata().getChannelData(client.getCtx(), this.image.getId());
        
        return channels.get(index).getChannelLabeling();
    }



    

    /**
     * Link a file to the Dataset
     * 
     * @param client The user
     * @param file   File to add
     * 
     * @return File created in OMERO
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     * @throws ServerError              
     * @throws FileNotFoundException
     * @throws IOException
     */
    public IObject addFile(Client client, 
                        File file) 
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            ServerError,
            FileNotFoundException,
            IOException
    {
        int INC = 262144;

        String name = file.getName();
        String absolutePath = file.getAbsolutePath();
        String path = absolutePath.substring(0,
                absolutePath.length()-name.length());

        OriginalFile originalFile = new OriginalFileI();
        originalFile.setName(omero.rtypes.rstring(name));
        originalFile.setPath(omero.rtypes.rstring(path));
        originalFile.setSize(omero.rtypes.rlong(file.length()));
        final ChecksumAlgorithm checksumAlgorithm = new ChecksumAlgorithmI();
        checksumAlgorithm.setValue(omero.rtypes.rstring(ChecksumAlgorithmSHA1160.value));
        originalFile.setHasher(checksumAlgorithm);
        originalFile.setMimetype(omero.rtypes.rstring(FilenameUtils.getExtension(file.getName())));
        originalFile = (OriginalFile) client.getDm().saveAndReturnObject(client.getCtx(), originalFile);

        RawFileStorePrx rawFileStore = client.getGateway().getRawFileService(client.getCtx());

        long pos = 0;
        int rlen;
        byte[] buf = new byte[INC];
        ByteBuffer bbuf;
        try {
            FileInputStream stream = new FileInputStream(file);
            rawFileStore.setFileId(originalFile.getId().getValue());
            while ((rlen = stream.read(buf)) > 0) {
                rawFileStore.write(buf, pos, rlen);
                pos += rlen;
                bbuf = ByteBuffer.wrap(buf);
                bbuf.limit(rlen);
            }
            originalFile = rawFileStore.save();
            stream.close();
        } finally {
            rawFileStore.close();
        }

        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(originalFile);
        fa.setDescription(omero.rtypes.rstring(""));
        fa.setNs(omero.rtypes.rstring(file.getName())); 

        fa = (FileAnnotation) client.getDm().saveAndReturnObject(client.getCtx(), fa);

        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild(fa);
        link.setParent(image.asImage());
        return client.getDm().saveAndReturnObject(client.getCtx(), link);                      
    }



    /**
     * Constructor of the class ImageContainer
     * 
     * @param image The image contained in the ImageContainer
     */
    public ImageContainer(ImageData image)
    {
        this.image = image;
    }
}