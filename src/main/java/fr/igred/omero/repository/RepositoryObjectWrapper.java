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

package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.GatewayWrapper;
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.annotations.AnnotationWrapper;
import fr.igred.omero.annotations.FileAnnotation;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.Table;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.cli.ErrorHandler;
import ome.formats.importer.cli.LoggingImportMonitor;
import omero.ServerError;
import omero.constants.metadata.NSCLIENTMAPANNOTATION;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.TableData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.util.PojoMapper;
import omero.model.IObject;
import omero.model.NamedValue;
import omero.model.Pixels;
import omero.model.TagAnnotationI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class RepositoryObjectWrapper<T extends DataObject> extends ObjectWrapper<T> implements RepositoryObject<T> {

    /**
     * Constructor of the class RepositoryObjectWrapper.
     *
     * @param object The object contained in the RepositoryObjectWrapper.
     */
    protected RepositoryObjectWrapper(T object) {
        super(object);
    }


    /**
     * Imports all images candidates in the paths to the target in OMERO.
     *
     * @param client The client handling the connection.
     * @param target The import target.
     * @param paths  Paths to the image files on the computer.
     *
     * @return If the import did not exit because of an error.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     * @throws IOException      Cannot read file.
     */
    protected static boolean importImages(GatewayWrapper client, DataObject target, String... paths)
    throws ServiceException, ServerException, IOException {
        boolean success;

        ImportConfig config = new ImportConfig();
        String       type   = PojoMapper.getGraphType(target.getClass());
        config.target.set(type + ":" + target.getId());
        config.username.set(client.getUser().getUserName());
        config.email.set(client.getUser().getEmail());

        OMEROMetadataStoreClient store = client.getImportStore();
        try (OMEROWrapper reader = new OMEROWrapper(config)) {
            ExceptionHandler.ofConsumer(store,
                                        s -> s.logVersionInfo(config.getIniVersionNumber()),
                                        "Cannot log version information during import.")
                            .rethrow(ServerError.class, ServerException::new);
            reader.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.ALL));

            ImportLibrary library = new ImportLibrary(store, reader);
            library.addObserver(new LoggingImportMonitor());

            ErrorHandler handler = new ErrorHandler(config);

            ImportCandidates candidates = new ImportCandidates(reader, paths, handler);
            success = library.importCandidates(config, candidates);
        } finally {
            store.logout();
        }

        return success;
    }


    /**
     * Imports one image file to the target in OMERO.
     *
     * @param client The client handling the connection.
     * @param target The import target.
     * @param path   Path to the image file on the computer.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    protected static List<Long> importImage(GatewayWrapper client, DataObject target, String path)
    throws ServiceException, ServerException {
        ImportConfig config = new ImportConfig();
        String       type   = PojoMapper.getGraphType(target.getClass());
        config.target.set(type + ":" + target.getId());
        config.username.set(client.getUser().getUserName());
        config.email.set(client.getUser().getEmail());

        Collection<Pixels> pixels = new ArrayList<>(1);

        OMEROMetadataStoreClient store = client.getImportStore();
        try (OMEROWrapper reader = new OMEROWrapper(config)) {
            store.logVersionInfo(config.getIniVersionNumber());
            reader.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.ALL));

            ImportLibrary library = new ImportLibrary(store, reader);
            library.addObserver(new LoggingImportMonitor());

            ErrorHandler handler = new ErrorHandler(config);

            ImportCandidates candidates = new ImportCandidates(reader, new String[]{path}, handler);

            ExecutorService uploadThreadPool = Executors.newFixedThreadPool(config.parallelUpload.get());

            List<ImportContainer> containers = candidates.getContainers();
            if (containers != null) {
                for (int i = 0; i < containers.size(); i++) {
                    ImportContainer container = containers.get(i);
                    container.setTarget(target.asIObject());
                    List<Pixels> imported = library.importImage(container, uploadThreadPool, i);
                    pixels.addAll(imported);
                }
            }
            uploadThreadPool.shutdown();
        } catch (Throwable e) {
            throw new ServerException("Error during image import.", e);
        } finally {
            store.logout();
        }

        List<Long> ids = new ArrayList<>(pixels.size());
        pixels.forEach(pix -> ids.add(pix.getImage().getId().getValue()));
        return ids.stream().distinct().collect(Collectors.toList());
    }


    /**
     * Returns the type of annotation link for this object
     *
     * @return See above.
     */
    protected abstract String annotationLinkType();


    /**
     * Adds a newly created tag to the object in OMERO, if possible.
     *
     * @param client      The client handling the connection.
     * @param name        Tag Name.
     * @param description Tag description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addTag(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotationData tagData = new TagAnnotationData(name);
        tagData.setTagDescription(description);
        addTag(client, tagData);
    }


    /**
     * Adds a tag to the object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     * @param tag    Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addTag(Client client, TagAnnotationWrapper tag)
    throws ServiceException, AccessException, ExecutionException {
        addTag(client, tag.asDataObject());
    }


    /**
     * Protected function. Adds a tag to the object in OMERO, if possible.
     *
     * @param client  The client handling the connection.
     * @param tagData Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    protected void addTag(Client client, TagAnnotationData tagData)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot add tag " + tagData.getId() + " to " + this;
        handleServiceAndAccess(client.getDm(),
                               d -> d.attachAnnotation(client.getCtx(), tagData, data),
                               error);
    }


    /**
     * Adds multiple tags to the object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     * @param id     ID of the tag to add to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addTag(Client client, Long id)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotationI    tag     = new TagAnnotationI(id, false);
        TagAnnotationData tagData = new TagAnnotationData(tag);
        addTag(client, tagData);
    }


    /**
     * Adds multiple tag to the object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     * @param tags   Array of TagAnnotationWrapper to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addTags(Client client, TagAnnotationWrapper... tags)
    throws ServiceException, AccessException, ExecutionException {
        for (TagAnnotationWrapper tag : tags) {
            addTag(client, tag.asDataObject());
        }
    }


    /**
     * Adds multiple tags by ID to the object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     * @param ids    Array of tag id in OMERO to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addTags(Client client, Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        for (Long id : ids) {
            addTag(client, id);
        }
    }


    /**
     * Gets all tags linked to an object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     *
     * @return List of TagAnnotationWrappers each containing a tag linked to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<TagAnnotationWrapper> getTags(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(TagAnnotationData.class);

        List<AnnotationData> annotations = handleServiceAndAccess(client.getMetadata(),
                                                                  m -> m.getAnnotations(client.getCtx(),
                                                                                        data,
                                                                                        types,
                                                                                        null),
                                                                  "Cannot get tags for " + this);

        return annotations.stream()
                          .filter(TagAnnotationData.class::isInstance)
                          .map(TagAnnotationData.class::cast)
                          .map(TagAnnotationWrapper::new)
                          .sorted(Comparator.comparing(TagAnnotationWrapper::getId))
                          .collect(Collectors.toList());
    }


    /**
     * Gets all map annotations linked to an object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     *
     * @return List of MapAnnotationWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<MapAnnotationWrapper> getMapAnnotations(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(MapAnnotationData.class);
        List<AnnotationData> annotations = handleServiceAndAccess(client.getMetadata(),
                                                                  m -> m.getAnnotations(client.getCtx(),
                                                                                        data,
                                                                                        types,
                                                                                        null),
                                                                  "Cannot get map annotations for " + this);

        return annotations.stream()
                          .filter(MapAnnotationData.class::isInstance)
                          .map(MapAnnotationData.class::cast)
                          .map(MapAnnotationWrapper::new)
                          .sorted(Comparator.comparing(MapAnnotationWrapper::getId))
                          .collect(Collectors.toList());
    }


    /**
     * Adds a single Key-Value pair to the object.
     *
     * @param client The client handling the connection.
     * @param key    Name of the key.
     * @param value  Value associated to the key.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<NamedValue>     kv  = Collections.singletonList(new NamedValue(key, value));
        MapAnnotationWrapper pkv = new MapAnnotationWrapper(kv);
        pkv.setNameSpace(NSCLIENTMAPANNOTATION.value);
        addMapAnnotation(client, pkv);
    }


    /**
     * Gets the List of NamedValue (Key-Value pair) associated to an object.
     *
     * @param client The client handling the connection.
     *
     * @return Collection of NamedValue.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public Map<String, String> getKeyValuePairs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get key-value pairs for " + this;

        List<Class<? extends AnnotationData>> types = Collections.singletonList(MapAnnotationData.class);

        List<AnnotationData> annotations = handleServiceAndAccess(client.getMetadata(),
                                                                  m -> m.getAnnotations(client.getCtx(),
                                                                                        data,
                                                                                        types,
                                                                                        null),
                                                                  error);

        return annotations.stream()
                          .filter(MapAnnotationData.class::isInstance)
                          .map(MapAnnotationData.class::cast)
                          .map(MapAnnotationWrapper::new)
                          .map(MapAnnotationWrapper::getContent)
                          .flatMap(List::stream)
                          .collect(Collectors.toMap(nv -> nv.name, nv -> nv.value));
    }


    /**
     * Gets the value from a Key-Value pair associated to the object.
     *
     * @param client The client handling the connection.
     * @param key    Key researched.
     *
     * @return Value associated to the key.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException Key not found.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    @Override
    public String getValue(Client client, String key)
    throws ServiceException, AccessException, ExecutionException {
        Map<String, String> keyValuePairs = getKeyValuePairs(client);
        String              value         = keyValuePairs.get(key);
        if (value != null) {
            return value;
        } else {
            throw new NoSuchElementException("Key \"" + key + "\" not found");
        }
    }


    /**
     * Adds a List of Key-Value pair to the object.
     * <p>The list is contained in the MapAnnotationWrapper.
     *
     * @param client        The client handling the connection.
     * @param mapAnnotation MapAnnotationWrapper containing a list of NamedValue.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addMapAnnotation(Client client, MapAnnotationWrapper mapAnnotation)
    throws ServiceException, AccessException, ExecutionException {
        handleServiceAndAccess(client.getDm(),
                               d -> d.attachAnnotation(client.getCtx(),
                                                       mapAnnotation.asDataObject(),
                                                       this.data),
                               "Cannot add key-value pairs to " + this);
    }


    /**
     * Adds a table to the object in OMERO.
     *
     * @param client The client handling the connection.
     * @param table  Table to add to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addTable(Client client, Table table)
    throws ServiceException, AccessException, ExecutionException {
        String         error          = "Cannot add table to " + this;
        TablesFacility tablesFacility = client.getTablesFacility();
        TableData tableData = handleServiceAndAccess(tablesFacility,
                                                     tf -> tf.addTable(client.getCtx(),
                                                                       data,
                                                                       table.getName(),
                                                                       table.createTable()),
                                                     error);

        Collection<FileAnnotationData> tables = handleServiceAndAccess(tablesFacility,
                                                                       tf -> tf.getAvailableTables(client.getCtx(),
                                                                                                   data),
                                                                       error);
        long fileId = tableData.getOriginalFileId();

        long id = tables.stream()
                        .filter(v -> v.getFileID() == fileId)
                        .mapToLong(DataObject::getId)
                        .max().orElse(-1L);
        table.setId(id);
        table.setFileId(tableData.getOriginalFileId());
    }


    /**
     * Adds a table to the object in OMERO and unlinks or deletes previous tables with the same name.
     *
     * @param client The client handling the connection.
     * @param table  Table to add to the object.
     * @param policy Whether older tables should be unlinked, deleted or deleted only if they become orphaned.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException The thread was interrupted.
     * @throws ServerException      Server error.
     */
    @Override
    public void addAndReplaceTable(Client client, Table table, ReplacePolicy policy)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        String error = String.format("Cannot get tables from %s", this);

        Collection<FileAnnotationWrapper> tables = wrap(handleServiceAndAccess(client.getTablesFacility(),
                                                                               t -> t.getAvailableTables(
                                                                                       client.getCtx(), data),
                                                                               error),
                                                        FileAnnotationWrapper::new);
        addTable(client, table);
        tables.removeIf(t -> !t.getDescription().equals(table.getName()));
        for (FileAnnotationWrapper fileAnnotation : tables) {
            this.unlink(client, fileAnnotation);
            if (policy == ReplacePolicy.DELETE ||
                policy == ReplacePolicy.DELETE_ORPHANED && fileAnnotation.countAnnotationLinks(client) == 0) {
                client.deleteFile(fileAnnotation.getId());
            }
        }
    }


    /**
     * Adds a table to the object in OMERO and unlinks previous tables with the same name, or deletes them if they're
     * orphaned.
     *
     * @param client The client handling the connection.
     * @param table  Table to add to the object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException The thread was interrupted.
     * @throws ServerException      Server error.
     */
    @Override
    public void addAndReplaceTable(Client client, Table table)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        addAndReplaceTable(client, table, ReplacePolicy.DELETE_ORPHANED);
    }


    /**
     * Gets a certain table linked to the object in OMERO.
     *
     * @param client The client handling the connection.
     * @param fileId FileId of the table researched.
     *
     * @return TableWrapper containing the table information.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public Table getTable(Client client, Long fileId)
    throws ServiceException, AccessException, ExecutionException {
        TableData table = handleServiceAndAccess(client.getTablesFacility(),
                                                 tf -> tf.getTable(client.getCtx(), fileId),
                                                 "Cannot get table from " + this);
        String name = handleServiceAndAccess(client.getTablesFacility(),
                                             tf -> tf.getAvailableTables(client.getCtx(), data)
                                                     .stream().filter(t -> t.getFileID() == fileId)
                                                     .map(FileAnnotationData::getDescription)
                                                     .findFirst().orElse(null),
                                             "Cannot get table name from " + this);
        Table result = new TableWrapper(Objects.requireNonNull(table));
        result.setName(name);
        return result;
    }


    /**
     * Gets all tables linked to the object in OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return List of TableWrappers containing the tables.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Table> getTables(Client client)
    throws ServiceException, AccessException, ExecutionException {
        Collection<FileAnnotationData> tables = handleServiceAndAccess(client.getTablesFacility(),
                                                                       tf -> tf.getAvailableTables(client.getCtx(),
                                                                                                   data),
                                                                       "Cannot get tables from " + this);

        List<Table> tablesWrapper = new ArrayList<>(tables.size());
        for (FileAnnotationData table : tables) {
            Table tableWrapper = getTable(client, table.getFileID());
            tableWrapper.setId(table.getId());
            tablesWrapper.add(tableWrapper);
        }

        return tablesWrapper;
    }


    /**
     * Uploads a file and links it to the object
     *
     * @param client The client handling the connection.
     * @param file   File to add.
     *
     * @return ID of the file created in OMERO.
     *
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException The thread was interrupted.
     */
    @Override
    public long addFile(Client client, File file) throws ExecutionException, InterruptedException {
        return client.getDm().attachFile(client.getCtx(),
                                         file,
                                         null,
                                         "",
                                         file.getName(),
                                         data).get().getId();
    }


    /**
     * Uploads a file, links it to the object and unlinks or deletes previous files with the same name.
     *
     * @param client The client handling the connection.
     * @param file   File to add.
     * @param policy Whether older files should be unlinked, deleted or deleted only if they become orphaned.
     *
     * @return ID of the file created in OMERO.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException The thread was interrupted.
     * @throws ServerException      Server error.
     */
    @Override
    public long addAndReplaceFile(Client client, File file, ReplacePolicy policy)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, ServerException {
        List<FileAnnotationWrapper> files = getFileAnnotations(client);

        FileAnnotationData uploaded = client.getDm().attachFile(client.getCtx(),
                                                                file,
                                                                null,
                                                                "",
                                                                file.getName(),
                                                                data).get();
        FileAnnotation annotation = new FileAnnotationWrapper(uploaded);

        files.removeIf(fileAnnotation -> !fileAnnotation.getFileName().equals(annotation.getFileName()));
        for (FileAnnotationWrapper fileAnnotation : files) {
            this.unlink(client, fileAnnotation);
            if (policy == ReplacePolicy.DELETE ||
                policy == ReplacePolicy.DELETE_ORPHANED && fileAnnotation.countAnnotationLinks(client) == 0) {
                client.deleteFile(fileAnnotation.getId());
            }
        }
        return annotation.getFileID();
    }


    /**
     * Uploads a file, links it to the object and unlinks previous files with the same name, or deletes them if they're
     * orphaned.
     *
     * @param client The client handling the connection.
     * @param file   File to add.
     *
     * @return ID of the file created in OMERO.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException The thread was interrupted.
     * @throws ServerException      Server error.
     */
    @Override
    public long addAndReplaceFile(Client client, File file)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, ServerException {
        return addAndReplaceFile(client, file, ReplacePolicy.DELETE_ORPHANED);
    }


    /**
     * Links a file annotation to the object
     *
     * @param client     The client handling the connection.
     * @param annotation FileAnnotationWrapper to link.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addFileAnnotation(Client client, FileAnnotationWrapper annotation)
    throws AccessException, ServiceException, ExecutionException {
        handleServiceAndAccess(client.getDm(),
                               dm -> dm.attachAnnotation(client.getCtx(), annotation.asDataObject(),
                                                         this.data),
                               "Cannot link file annotation to " + this);
    }


    /**
     * Returns the file annotations
     *
     * @param client The client handling the connection.
     *
     * @return The list of tile annotations.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<FileAnnotationWrapper> getFileAnnotations(Client client)
    throws ExecutionException, ServiceException, AccessException {
        String error = "Cannot retrieve file annotations from " + this;

        List<Class<? extends AnnotationData>> types = Collections.singletonList(FileAnnotationData.class);

        List<AnnotationData> annotations = handleServiceAndAccess(client.getMetadata(),
                                                                  m -> m.getAnnotations(client.getCtx(),
                                                                                        data,
                                                                                        types,
                                                                                        null),
                                                                  error);

        return annotations.stream()
                          .filter(FileAnnotationData.class::isInstance)
                          .map(FileAnnotationData.class::cast)
                          .map(FileAnnotationWrapper::new)
                          .collect(Collectors.toList());
    }


    /**
     * Unlinks the given annotation from the current object.
     *
     * @param client     The client handling the connection.
     * @param annotation An annotation.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    public <A extends AnnotationWrapper<?>> void unlink(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        removeLink(client, annotationLinkType(), annotation.getId());
    }


    /**
     * Removes the link of the given type with the given child ID.
     *
     * @param client   The client handling the connection.
     * @param linkType The link type.
     * @param childId  Link child ID.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    protected void removeLink(Client client, String linkType, long childId)
    throws ServiceException, ServerException, AccessException, ExecutionException, InterruptedException {
        List<IObject> os = client.findByQuery("select link from " + linkType +
                                              " link where link.parent = " + getId() +
                                              " and link.child = " + childId);
        delete(client, os.iterator().next());
    }


    /**
     * Retrieves annotations linked to the object.
     *
     * @param client The client handling the connection.
     *
     * @return A list of annotations, as AnnotationData.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<AnnotationData> getAnnotations(Client client)
    throws AccessException, ServiceException, ExecutionException {
        return handleServiceAndAccess(client.getMetadata(),
                                      m -> m.getAnnotations(client.getCtx(), data),
                                      "Cannot get annotations from " + this);
    }


    /**
     * Copies annotation links from some other object to this one
     *
     * @param client The client handling the connection.
     * @param object Other repository object to copy annotations from.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void copyAnnotationLinks(Client client, RepositoryObject<?> object)
    throws AccessException, ServiceException, ExecutionException {
        List<AnnotationData> newAnnotations = object.getAnnotations(client);
        List<AnnotationData> oldAnnotations = this.getAnnotations(client);
        for (AnnotationData annotation : oldAnnotations) {
            newAnnotations.removeIf(a -> a.getId() == annotation.getId());
        }
        for (AnnotationData annotation : newAnnotations) {
            handleServiceAndAccess(client.getDm(),
                                   dm -> dm.attachAnnotation(client.getCtx(), annotation, this.data),
                                   "Cannot link annotations to " + this);
        }
    }


}
