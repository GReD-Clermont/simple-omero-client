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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FilenameUtils;

import fr.igred.omero.Client;
import fr.igred.omero.ImageContainer;
import fr.igred.omero.sort.SortImageContainer;
import fr.igred.omero.sort.SortTagAnnotationContainer;
import fr.igred.omero.metadata.TableContainer;
import fr.igred.omero.metadata.annotation.TagAnnotationContainer;

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
import omero.gateway.model.TableData;
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

/**
 * Class containing a DatasetData.
 * Implements function using the DatasetData contained
 */
public class DatasetContainer {

    ///DatasetData contained
    private final DatasetData dataset;

    /**
     * Gets the DatasetData id
     *
     * @return DatasetData id
     */
    public Long getId()
    {
        return dataset.getId();
    }

    /**
     * Gets the DatasetData name
     *
     * @return DatasetData name
     */
    public String getName()
    {
        return dataset.getName();
    }

    /**
     * Gets the DatasetData description
     *
     * @return DatasetData description
     */
    public String getDescription()
    {
        return dataset.getDescription();
    }

    /**
     * @return the DatasetData contained
     */
    public DatasetData getDataset()
    {
        return dataset;
    }





    /**
     * Adds a tag to the dataset in OMERO.
     * Create the tag.
     *
     * @param client      The user
     * @param name        Tag Name
     * @param description Tag description
     *
     * @return The object saved in OMERO.
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
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

        return addTag(client, tagData);
    }

    /**
     * Adds a tag to the dataset in OMERO.
     *
     * @param client The user
     * @param tag    Tag to be added
     *
     * @return The object saved in OMERO.
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public IObject addTag(Client                  client,
                          TagAnnotationContainer  tag)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        return addTag(client, tag.getTag());
    }

    /**
     * Private function.
     * Adds a tag to the dataset in OMERO.
     *
     * @param client  The user
     * @param tagData Tag to be added
     *
     * @return The object saved in OMERO.
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    private IObject addTag(Client              client,
                           TagAnnotationData   tagData)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        DatasetAnnotationLink link = new DatasetAnnotationLinkI();
        link.setChild(tagData.asAnnotation());
        link.setParent(new DatasetI(dataset.getId(), false));

        return client.getDm().saveAndReturnObject(client.getCtx(), link);
    }

    /**
     * Adds multiple tag to the dataset in OMERO.
     *
     * @param client The user
     * @param id     Id in OMERO of tag to add
     *
     * @return The objects saved in OMERO
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public IObject addTag(Client client,
                          Long   id)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        DatasetAnnotationLink link = new DatasetAnnotationLinkI();
        link.setChild(new TagAnnotationI(id, false));
        link.setParent(new DatasetI(dataset.getId(), false));

        return client.getDm().saveAndReturnObject(client.getCtx(), link);
    }

    /**
     * Adds multiple tag to the dataset in OMERO.
     *
     * @param client The user
     * @param tags   Array of TagAnnotationContainer to add
     *
     * @return The objects saved in OMERO
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public Collection<IObject> addTags(Client                    client,
                                       TagAnnotationContainer... tags)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<IObject> objects = new ArrayList<>();
        for(TagAnnotationContainer tag : tags) {
            IObject r = addTag(client, tag.getTag());
            objects.add(r);
        }

        return objects;
    }

    /**
     * Adds multiple tag to the dataset in OMERO.
     * The tags id is used
     *
     * @param client The user
     * @param ids    Array of tag Id in OMERO to add
     *
     * @return The objects saved in OMERO
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public Collection<IObject> addTags(Client  client,
                                       Long... ids)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<IObject> objects = new ArrayList<>();
        for(Long id : ids) {
            IObject r = addTag(client, id);
            objects.add(r);
        }

        return objects;
    }

    /**
     * Gets all tag linked to a dataset in OMERO
     *
     * @param client The user
     *
     * @return Collection of TagAnnotationContainer each containing a tag linked to the dataset
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public List<TagAnnotationContainer> getTags(Client client)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        List<Class<? extends AnnotationData>> types = new ArrayList<>();
        types.add(TagAnnotationData.class);

        List<AnnotationData> annotations = client.getMetadata().getAnnotations(client.getCtx(), dataset, types, null);

        List<TagAnnotationContainer> tags = new ArrayList<>();

        if(annotations != null) {
            for (AnnotationData annotation : annotations) {
                TagAnnotationData tagAnnotation = (TagAnnotationData) annotation;

                tags.add(new TagAnnotationContainer(tagAnnotation));
            }
        }

        tags.sort(new SortTagAnnotationContainer());
        return tags;
    }





    /**
     * Transforms a collection of ImageData in a list of ImageContainer sorted by the ImageData id.
     *
     * @param images ImageData Collection
     *
     * @return ImageContainer list sorted
     */
    private List<ImageContainer> toImagesContainer(Collection<ImageData> images)
    {
        List<ImageContainer> imagesContainer = new ArrayList<>(images.size());

        for(ImageData image : images)
            imagesContainer.add(new ImageContainer(image));

        imagesContainer.sort(new SortImageContainer());

        return imagesContainer;
    }

    /**
     * Gets all images in the dataset available from OMERO.
     *
     * @param client The user
     *
     * @return ImageContainer list
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public List<ImageContainer> getImages(Client client)
        throws
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<ImageData> images = client.getBrowseFacility().getImagesForDatasets(client.getCtx(), Collections.singletonList(dataset.getId()));

        return toImagesContainer(images);
    }

    /**
     * Gets all images in the dataset with a certain from OMERO.
     *
     * @param client The user
     * @param name   Name searched
     *
     * @return ImageContainer list
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public List<ImageContainer> getImages(Client client,
                                          String name)
        throws
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<ImageData> images = client.getBrowseFacility().getImagesForDatasets(client.getCtx(), Collections.singletonList(dataset.getId()));
        Collection<ImageData> selected = new ArrayList<>(images.size());

        for(ImageData image : images) {
            if(image.getName().equals(name))
                selected.add(image);
        }

        return toImagesContainer(selected);
    }

    /**
     * Gets all images in the dataset with a certain motif in their name from OMERO.
     *
     * @param client The user
     * @param motif  Motif searched in an Image name
     *
     * @return ImageContainer list
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public List<ImageContainer> getImagesLike(Client client,
                                              String motif)
        throws
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<ImageData> images = client.getBrowseFacility().getImagesForDatasets(client.getCtx(), Collections.singletonList(dataset.getId()));

        motif = ".*" + motif + ".*";

        Collection<ImageData> selected = new ArrayList<>(images.size());

        for(ImageData image : images) {
            if(image.getName().matches(motif))
                selected.add(image);
        }

        return toImagesContainer(selected);
    }

    /**
     * Gets all images in the dataset tagged with a specified tag from OMERO.
     *
     * @param client The user
     * @param tag    TagAnnotationContainer containing the tag researched
     *
     * @return ImageContainer list
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public List<ImageContainer> getImagesTagged(Client                 client,
                                                TagAnnotationContainer tag)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ServerError
    {
        List<IObject> os = client.getQueryService().findAllByQuery("select link.parent " +
                                                                   "from ImageAnnotationLink link " +
                                                                   "where link.child = " +
                                                                   tag.getId() +
                                                                   " and link.parent in (select link2.child from DatasetImageLink link2 where link2.parent = " +
                                                                   dataset.getId() + ")", null);

        Collection<ImageData> selected = new ArrayList<>();

        for(IObject o : os) {
            ImageData image = client.getBrowseFacility().getImage(client.getCtx(), o.getId().getValue());
            selected.add(image);
        }

        return toImagesContainer(selected);
    }

    /**
     * Gets all images in the dataset tagged with a specified tag from OMERO.
     *
     * @param client The user
     * @param tagId  Id of the tag researched
     *
     * @return ImageContainer list
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public List<ImageContainer> getImagesTagged(Client client,
                                                Long   tagId)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ServerError
    {
        List<IObject> os = client.getQueryService().findAllByQuery("select link.parent " +
                                                                   "from ImageAnnotationLink link " +
                                                                   "where link.child = " +
                                                                   tagId +
                                                                   " and link.parent in (select link2.child from DatasetImageLink link2 where link2.parent = " +
                                                                   dataset.getId() + ")", null);

        Collection<ImageData> selected = new ArrayList<>();

        for(IObject o : os) {
            ImageData image = client.getBrowseFacility().getImage(client.getCtx(), o.getId().getValue());
            selected.add(image);
        }

        return toImagesContainer(selected);
    }

    /**
     * Gets all images in the dataset with a certain key
     *
     * @param client The user
     * @param key    Name of the key researched
     *
     * @return ImageContainer list
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public List<ImageContainer> getImagesKey(Client client,
                                             String key)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<ImageData> images = client.getBrowseFacility().getImagesForDatasets(client.getCtx(), Collections.singletonList(dataset.getId()));
        Collection<ImageData> selected = new ArrayList<>(images.size());

        for(ImageData image : images) {
            ImageContainer imageContainer = new ImageContainer(image);

            Collection<NamedValue> pairsKeyValue = imageContainer.getKeyValuePairs(client);

            for(NamedValue pairKeyValue : pairsKeyValue) {
                if(pairKeyValue.name.equals(key)) {
                    selected.add(image);
                    break;
                }
            }
        }

        return toImagesContainer(selected);
    }

    /**
     * Gets all images in the dataset with a certain key value pair from OMERO
     *
     * @param client The user
     * @param key    Name of the key researched
     * @param value  Value associated with the key
     *
     * @return ImageContainer list
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public List<ImageContainer> getImagesPairKeyValue(Client client,
                                                      String key,
                                                      String value)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<ImageData> images = client.getBrowseFacility().getImagesForDatasets(client.getCtx(), Collections.singletonList(dataset.getId()));
        Collection<ImageData> selected = new ArrayList<>(images.size());

        for(ImageData image : images) {
            ImageContainer imageContainer = new ImageContainer(image);

            Collection<NamedValue> pairsKeyValue = imageContainer.getKeyValuePairs(client);

            for(NamedValue pairKeyValue : pairsKeyValue) {
                if(pairKeyValue.name.equals(key) && pairKeyValue.value.equals(value)) {
                    selected.add(image);
                    break;
                }
            }
        }

        return toImagesContainer(selected);
    }




    /**
     * Adds a list of image to the dataset in OMERO.
     *
     * @param client The user
     * @param images Image to add to the dataset
     *
     * @return The objects saved in OMERO
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public Collection<IObject> addImages(Client               client,
                                         List<ImageContainer> images)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<IObject> iObjects = new ArrayList<>(images.size());
        for(ImageContainer image : images) {
            IObject r = addImage(client, image);
            iObjects.add(r);
        }

        return iObjects;
    }

    /**
     * Adds a single image to the dataset in OMERO
     *
     * @param client The user
     * @param image  Image to add
     *
     * @return The object saved in OMERO
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public IObject addImage(Client         client,
                            ImageContainer image)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        DatasetImageLink link = new DatasetImageLinkI();

        link.setChild(image.getImage().asImage());
        link.setParent(new DatasetI(dataset.getId(), false));

        return client.getDm().saveAndReturnObject(client.getCtx(), link);
    }

    /**
     * Imports all images candidates in the paths to the dataset in OMERO.
     *
     * @param client The user
     * @param paths  Paths to the image on your computer
     *
     * @throws Exception   OMEROMetadataStoreClient creation failed
     * @throws ServerError Server connexion error
     */
    public void importImages(Client    client,
                             String... paths)
        throws
            ServerError,
            Exception
    {
        ImportConfig config = client.getConfig();
        config.target.set("Dataset:" + dataset.getId());
        OMEROMetadataStoreClient store;

        store = config.createStore();
        store.logVersionInfo(config.getIniVersionNumber());
        OMEROWrapper reader = new OMEROWrapper(config);
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
     * @param client The user
     * @param file   File to add
     *
     * @return File created in OMERO
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
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

        DatasetAnnotationLink link = new DatasetAnnotationLinkI();
        link.setChild(fa);
        link.setParent(dataset.asDataset());
        return client.getDm().saveAndReturnObject(client.getCtx(), link);
    }




    /**
     * Adds a table to the dataset in OMERO
     *
     * @param client The user
     * @param table  Table to add to the dataset
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public void addTable(Client client,
                         TableContainer table)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        TableData tableData = table.createTable();
        tableData = client.getTablesFacility().addTable(client.getCtx(), dataset, table.getName(), tableData);
        table.setFileId(tableData.getOriginalFileId());
    }

    /**
     * Gets a certain table linked to the dataset in OMERO
     *
     * @param client The user.
     * @param fileId FileId of the table researched
     *
     * @return TableContainer containing the table information
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public TableContainer getTable(Client client,
                                   Long   fileId)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        TableData table = client.getTablesFacility().getTable(client.getCtx(), fileId);

        return new TableContainer(table);
    }

    /**
     * Gets all table linked to the dataset in OMERO.
     *
     * @param client The user
     *
     * @return List of TableContainer containing the tables information
     *
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instantiated
     */
    public List<TableContainer> getTables(Client client)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<FileAnnotationData> tables = client.getTablesFacility().getAvailableTables(client.getCtx(), dataset);
        List<TableContainer> tablesContainer = new ArrayList<>(tables.size());

        for(FileAnnotationData table : tables) {
            TableContainer tableContainer = getTable(client, table.getFileID());
            tableContainer.setId(table.getId());
            tablesContainer.add(tableContainer);
        }

        return tablesContainer;
    }



    /**
     * Constructor of the DatasetContainer class
     *
     * @param name        name of the dataset
     * @param description description of the dataset
     */
    public DatasetContainer(String name,
                            String description)
    {
        this.dataset = new DatasetData();
        this.dataset.setName(name);
        this.dataset.setDescription(description);
    }

    /**
     * Constructor of the DatasetContainer class
     *
     * @param dataset Dataset to be contained
     */
    public DatasetContainer(DatasetData dataset)
    {
        this.dataset = dataset;
    }
}