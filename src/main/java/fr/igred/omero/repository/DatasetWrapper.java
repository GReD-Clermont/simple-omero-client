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

package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.annotations.TableWrapper;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.cli.ErrorHandler;
import ome.formats.importer.cli.LoggingImportMonitor;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.TagAnnotationData;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.NamedValue;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.TagAnnotationI;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;


/**
 * Class containing a DatasetData.
 * <p> Implements function using the DatasetData contained
 */
public class DatasetWrapper extends ObjectWrapper<DatasetData> {


    /**
     * Constructor of the DatasetWrapper class
     *
     * @param name        Name of the dataset.
     * @param description Description of the dataset.
     */
    public DatasetWrapper(String name, String description) {
        super(new DatasetData());
        this.data.setName(name);
        this.data.setDescription(description);
    }


    /**
     * Constructor of the DatasetWrapper class
     *
     * @param dataset Dataset to be contained.
     */
    public DatasetWrapper(DatasetData dataset) {
        super(dataset);
    }


    /**
     * Gets the DatasetData name
     *
     * @return DatasetData name.
     */
    public String getName() {
        return data.getName();
    }


    /**
     * Gets the DatasetData description
     *
     * @return DatasetData description.
     */
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * @return the DatasetData contained.
     */
    public DatasetData getDataset() {
        return data;
    }


    /**
     * Adds a tag to the dataset in OMERO. Create the tag.
     *
     * @param client      The user.
     * @param name        Tag Name.
     * @param description Tag description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTag(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotationData tagData = new TagAnnotationData(name);
        tagData.setTagDescription(description);

        addTag(client, tagData);
    }


    /**
     * Adds a tag to the dataset in OMERO.
     *
     * @param client The user.
     * @param tag    Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTag(Client client, TagAnnotationWrapper tag)
    throws ServiceException, AccessException, ExecutionException {
        addTag(client, tag.getTag());
    }


    /**
     * Private function. Adds a tag to the dataset in OMERO.
     *
     * @param client  The user.
     * @param tagData Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    private void addTag(Client client, TagAnnotationData tagData)
    throws ServiceException, AccessException, ExecutionException {
        DatasetAnnotationLink link = new DatasetAnnotationLinkI();
        link.setChild(tagData.asAnnotation());
        link.setParent(new DatasetI(data.getId(), false));

        client.save(link);
    }


    /**
     * Adds multiple tag to the dataset in OMERO.
     *
     * @param client The user.
     * @param id     Id in OMERO of tag to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTag(Client client, Long id)
    throws ServiceException, AccessException, ExecutionException {
        DatasetAnnotationLink link = new DatasetAnnotationLinkI();
        link.setChild(new TagAnnotationI(id, false));
        link.setParent(new DatasetI(data.getId(), false));

        client.save(link);
    }


    /**
     * Adds multiple tag to the dataset in OMERO.
     *
     * @param client The user.
     * @param tags   Array of TagAnnotationWrapper to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTags(Client client, TagAnnotationWrapper... tags)
    throws ServiceException, AccessException, ExecutionException {
        for (TagAnnotationWrapper tag : tags) {
            addTag(client, tag.getTag());
        }
    }


    /**
     * Adds multiple tag to the dataset in OMERO. The tags id is used
     *
     * @param client The user.
     * @param ids    Array of tag id in OMERO to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTags(Client client, Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        for (Long id : ids) {
            addTag(client, id);
        }
    }


    /**
     * Gets all tag linked to a dataset in OMERO
     *
     * @param client The user.
     *
     * @return Collection of TagAnnotationWrapper each containing a tag linked to the dataset.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<TagAnnotationWrapper> getTags(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<TagAnnotationWrapper> tags = new ArrayList<>();

        List<Class<? extends AnnotationData>> types = new ArrayList<>();
        types.add(TagAnnotationData.class);

        List<AnnotationData> annotations = null;
        try {
            annotations = client.getMetadata().getAnnotations(client.getCtx(), data, types, null);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get tags for dataset ID: " + getId());
        }

        if (annotations != null) {
            for (AnnotationData annotation : annotations) {
                TagAnnotationData tagAnnotation = (TagAnnotationData) annotation;

                tags.add(new TagAnnotationWrapper(tagAnnotation));
            }
        }

        tags.sort(new SortById<>());
        return tags;
    }


    /**
     * Transforms a collection of ImageData in a list of ImageWrapper sorted by the ImageData id.
     *
     * @param images ImageData Collection.
     *
     * @return ImageWrapper list sorted.
     */
    private List<ImageWrapper> toImageWrappers(Collection<ImageData> images) {
        List<ImageWrapper> imageWrappers = new ArrayList<>();

        for (ImageData image : images)
            imageWrappers.add(new ImageWrapper(image));

        imageWrappers.sort(new SortById<>());

        return imageWrappers;
    }


    /**
     * Gets all images in the dataset available from OMERO.
     *
     * @param client The user.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageWrapper> getImages(Client client) throws ServiceException, AccessException {
        Collection<ImageData> images = new ArrayList<>();
        try {
            images = client.getBrowseFacility()
                           .getImagesForDatasets(client.getCtx(),
                                                 Collections.singletonList(data.getId()));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get images from dataset ID: " + getId());
        }

        return toImageWrappers(images);
    }


    /**
     * Gets all images in the dataset with a certain from OMERO.
     *
     * @param client The user.
     * @param name   Name searched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageWrapper> getImages(Client client, String name)
    throws ServiceException, AccessException {
        List<ImageWrapper> images = getImages(client);
        images.removeIf(image -> !image.getName().equals(name));
        return images;
    }


    /**
     * Gets all images in the dataset with a certain motif in their name from OMERO.
     *
     * @param client The user.
     * @param motif  Motif searched in an image name.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageWrapper> getImagesLike(Client client, String motif)
    throws ServiceException, AccessException {
        List<ImageWrapper> images = getImages(client);
        final String       regexp = ".*" + motif + ".*";
        images.removeIf(image -> !image.getName().matches(regexp));
        return images;
    }


    /**
     * Gets all images in the dataset tagged with a specified tag from OMERO.
     *
     * @param client The user.
     * @param tag    TagAnnotationWrapper containing the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws OMEROServerError Server error.
     */
    public List<ImageWrapper> getImagesTagged(Client client, TagAnnotationWrapper tag)
    throws ServiceException, AccessException, OMEROServerError {
        List<ImageWrapper> selected = new ArrayList<>();
        List<IObject> os = client.findByQuery("select link.parent " +
                                              "from ImageAnnotationLink link " +
                                              "where link.child = " +
                                              tag.getId() +
                                              " and link.parent in " +
                                              "(select link2.child " +
                                              "from DatasetImageLink link2 " +
                                              "where link2.parent = " +
                                              data.getId() + ")");

        for (IObject o : os) {
            selected.add(client.getImage(o.getId().getValue()));
        }

        return selected;
    }


    /**
     * Gets all images in the dataset tagged with a specified tag from OMERO.
     *
     * @param client The user.
     * @param tagId  Id of the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws OMEROServerError Server error.
     */
    public List<ImageWrapper> getImagesTagged(Client client, Long tagId)
    throws ServiceException, AccessException, OMEROServerError {
        List<ImageWrapper> selected = new ArrayList<>();
        List<IObject> os = client.findByQuery("select link.parent " +
                                              "from ImageAnnotationLink link " +
                                              "where link.child = " +
                                              tagId +
                                              " and link.parent in " +
                                              "(select link2.child " +
                                              "from DatasetImageLink link2 " +
                                              "where link2.parent = " +
                                              data.getId() + ")");

        for (IObject o : os) {
            selected.add(client.getImage(o.getId().getValue()));
        }

        return selected;
    }


    /**
     * Gets all images in the dataset with a certain key
     *
     * @param client The user.
     * @param key    Name of the key researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesKey(Client client, String key)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> selected = new ArrayList<>();
        Collection<ImageData> images   = new ArrayList<>();
        try {
            images = client.getBrowseFacility()
                           .getImagesForDatasets(client.getCtx(),
                                                 Collections.singletonList(data.getId()));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get images with key \"" + key + "\" from dataset ID: " + getId());
        }

        for (ImageData image : images) {
            ImageWrapper imageWrapper = new ImageWrapper(image);

            Collection<NamedValue> pairsKeyValue = imageWrapper.getKeyValuePairs(client);

            for (NamedValue pairKeyValue : pairsKeyValue) {
                if (pairKeyValue.name.equals(key)) {
                    selected.add(image);
                    break;
                }
            }
        }

        return toImageWrappers(selected);
    }


    /**
     * Gets all images in the dataset with a certain key value pair from OMERO
     *
     * @param client The user.
     * @param key    Name of the key researched.
     * @param value  Value associated with the key.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> selected = new ArrayList<>();
        Collection<ImageData> images   = new ArrayList<>();
        try {
            images = client.getBrowseFacility()
                           .getImagesForDatasets(client.getCtx(),
                                                 Collections.singletonList(data.getId()));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get images with k/v pair from dataset ID: " + getId());
        }

        for (ImageData image : images) {
            ImageWrapper imageWrapper = new ImageWrapper(image);

            Collection<NamedValue> pairsKeyValue = imageWrapper.getKeyValuePairs(client);

            for (NamedValue pairKeyValue : pairsKeyValue) {
                if (pairKeyValue.name.equals(key) && pairKeyValue.value.equals(value)) {
                    selected.add(image);
                    break;
                }
            }
        }

        return toImageWrappers(selected);
    }


    /**
     * Adds a list of image to the dataset in OMERO.
     *
     * @param client The user.
     * @param images Image to add to the dataset.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addImages(Client client, List<ImageWrapper> images)
    throws ServiceException, AccessException, ExecutionException {
        for (ImageWrapper image : images) {
            addImage(client, image);
        }
    }


    /**
     * Adds a single image to the dataset in OMERO
     *
     * @param client The user.
     * @param image  Image to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addImage(Client client, ImageWrapper image)
    throws ServiceException, AccessException, ExecutionException {
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild(image.getImage().asImage());
        link.setParent(new DatasetI(data.getId(), false));

        client.save(link);
    }


    /**
     * Imports all images candidates in the paths to the dataset in OMERO.
     *
     * @param client The user.
     * @param paths  Paths to the image on your computer.
     *
     * @throws Exception        OMEROMetadataStoreClient creation failed.
     * @throws OMEROServerError Server error.
     */
    public void importImages(Client client, String... paths) throws Exception {
        ImportConfig config = client.getConfig();
        config.target.set("Dataset:" + data.getId());
        OMEROMetadataStoreClient store;

        store = config.createStore();
        try {
            store.logVersionInfo(config.getIniVersionNumber());
        } catch (ServerError se) {
            throw new OMEROServerError(se);
        }
        OMEROWrapper  reader  = new OMEROWrapper(config);
        ImportLibrary library = new ImportLibrary(store, reader);

        ErrorHandler handler = new ErrorHandler(config);
        library.addObserver(new LoggingImportMonitor());

        ImportCandidates candidates = new ImportCandidates(reader, paths, handler);
        reader.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.ALL));
        library.importCandidates(config, candidates);

        store.logout();
    }


    /**
     * Links a file to the Dataset
     *
     * @param client The user.
     * @param file   File to add.
     *
     * @return ID of the file created in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data on server.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError   Server error.
     * @throws IOException        If an I/O error occurs.
     */
    public long addFile(Client client, File file) throws
                                                  ServiceException,
                                                  AccessException,
                                                  ExecutionException,
                                                  OMEROServerError,
                                                  IOException {
        final int INC = 262144;

        DatasetAnnotationLink newLink;
        RawFileStorePrx       rawFileStore;

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
        originalFile = (OriginalFile) client.save(originalFile);

        try {
            rawFileStore = client.getGateway().getRawFileService(client.getCtx());
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException(oos, oos.getConnectionStatus());
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
        } catch (ServerError se) {
            throw new OMEROServerError(se);
        }

        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(originalFile);
        fa.setDescription(omero.rtypes.rstring(""));
        fa.setNs(omero.rtypes.rstring(file.getName()));

        fa = (FileAnnotation) client.save(fa);

        DatasetAnnotationLink link = new DatasetAnnotationLinkI();
        link.setChild(fa);
        link.setParent(data.asDataset());
        newLink = (DatasetAnnotationLink) client.save(link);

        return newLink.getChild().getId().getValue();
    }


    /**
     * Gets all table linked to the dataset in OMERO.
     *
     * @param client The user.
     *
     * @return List of TableWrapper containing the tables information.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<TableWrapper> getTables(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<TableWrapper> tablesWrapper = new ArrayList<>();

        Collection<FileAnnotationData> tables = new ArrayList<>();
        try {
            tables = client.getTablesFacility().getAvailableTables(client.getCtx(), data);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get tables from dataset ID: " + getId());
        }

        for (FileAnnotationData table : tables) {
            TableWrapper tableWrapper = getTable(client, table.getFileID());
            tableWrapper.setId(table.getId());
            tablesWrapper.add(tableWrapper);
        }

        return tablesWrapper;
    }

}