package fr.igred.omero;


import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.annotations.FileAnnotation;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotation;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.Table;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.ReplacePolicy;
import omero.constants.metadata.NSCLIENTMAPANNOTATION;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.TableData;
import omero.gateway.model.TagAnnotationData;
import omero.model.TagAnnotationI;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.ObjectWrapper.wrap;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


public interface Annotatable<T extends DataObject> extends RemoteObject<T> {


    /**
     * Adds a newly created tag to the object in OMERO, if possible.
     *
     * @param dm          The data manager.
     * @param name        Tag Name.
     * @param description Tag description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addTag(DataManager dm, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotation tag = new TagAnnotationWrapper(new TagAnnotationData(name));
        tag.setDescription(description);
        addTag(dm, tag);
    }


    /**
     * Adds a tag to the object in OMERO, if possible.
     *
     * @param dm  The data manager.
     * @param tag Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addTag(DataManager dm, TagAnnotation tag)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot add tag " + tag.getId() + " to " + this;
        handleServiceAndAccess(dm.getDataManagerFacility(),
                               d -> d.attachAnnotation(dm.getCtx(), tag.asDataObject(), asDataObject()),
                               error);
    }


    /**
     * Adds multiple tags to the object in OMERO, if possible.
     *
     * @param dm The data manager.
     * @param id ID of the tag to add to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addTag(DataManager dm, Long id)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotationI    tag     = new TagAnnotationI(id, false);
        TagAnnotationData tagData = new TagAnnotationData(tag);
        addTag(dm, new TagAnnotationWrapper(tagData));
    }


    /**
     * Adds multiple tag to the object in OMERO, if possible.
     *
     * @param dm   The data manager.
     * @param tags Array of TagAnnotationWrapper to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addTags(DataManager dm, TagAnnotation... tags)
    throws ServiceException, AccessException, ExecutionException {
        for (TagAnnotation tag : tags) {
            addTag(dm, tag);
        }
    }


    /**
     * Adds multiple tags by ID to the object in OMERO, if possible.
     *
     * @param dm  The data manager.
     * @param ids Array of tag id in OMERO to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addTags(DataManager dm, Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        for (Long id : ids) {
            addTag(dm, id);
        }
    }


    /**
     * Gets all tags linked to an object in OMERO, if possible.
     *
     * @param browser The data browser.
     *
     * @return List of TagAnnotationWrappers each containing a tag linked to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<TagAnnotation> getTags(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(TagAnnotationData.class);

        List<AnnotationData> annotations = handleServiceAndAccess(browser.getMetadata(),
                                                                  m -> m.getAnnotations(browser.getCtx(),
                                                                                        asDataObject(),
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
     * @param browser The data browser.
     *
     * @return List of MapAnnotationWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<MapAnnotation> getMapAnnotations(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(MapAnnotationData.class);
        List<AnnotationData> annotations = handleServiceAndAccess(browser.getMetadata(),
                                                                  m -> m.getAnnotations(browser.getCtx(),
                                                                                        asDataObject(),
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
     * @param dm    The data manager.
     * @param key   Name of the key.
     * @param value Value associated to the key.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addKeyValuePair(DataManager dm, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<Map.Entry<String, String>> kv = Collections.singletonList(new AbstractMap.SimpleEntry<>(key, value));

        MapAnnotation pkv = new MapAnnotationWrapper(kv);
        pkv.setNameSpace(NSCLIENTMAPANNOTATION.value);
        addMapAnnotation(dm, pkv);
    }


    /**
     * Gets the List of Key-Value pairs associated to an object.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Map.Entry<String, String>> getKeyValuePairs(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return getMapAnnotations(browser).stream()
                                         .map(MapAnnotation::getContent)
                                         .flatMap(List::stream)
                                         .collect(Collectors.toList());
    }


    /**
     * Gets the value from a Key-Value pair associated to the object.
     *
     * @param browser The data browser.
     * @param key     Key researched.
     *
     * @return Value associated to the key.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException Key not found.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default List<String> getValues(Browser browser, String key)
    throws ServiceException, AccessException, ExecutionException {
        return getMapAnnotations(browser).stream()
                                         .map(MapAnnotation::getContentAsMap)
                                         .map(kv -> kv.get(key))
                                         .filter(Objects::nonNull)
                                         .flatMap(List::stream)
                                         .collect(Collectors.toList());
    }


    /**
     * Adds a List of Key-Value pair to the object.
     * <p>The list is contained in the MapAnnotationWrapper.
     *
     * @param dm            The data manager.
     * @param mapAnnotation MapAnnotation containing a list of key-value pairs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addMapAnnotation(DataManager dm, MapAnnotation mapAnnotation)
    throws ServiceException, AccessException, ExecutionException {
        handleServiceAndAccess(dm.getDataManagerFacility(),
                               d -> d.attachAnnotation(dm.getCtx(),
                                                       mapAnnotation.asDataObject(),
                                                       this.asDataObject()),
                               "Cannot add key-value pairs to " + this);
    }


    /**
     * Adds a table to the object in OMERO.
     *
     * @param dm    The data manager.
     * @param table Table to add to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addTable(DataManager dm, Table table)
    throws ServiceException, AccessException, ExecutionException {
        String         error          = "Cannot add table to " + this;
        TablesFacility tablesFacility = dm.getTablesFacility();
        TableData tableData = handleServiceAndAccess(tablesFacility,
                                                     tf -> tf.addTable(dm.getCtx(),
                                                                       asDataObject(),
                                                                       table.getName(),
                                                                       table.createTable()),
                                                     error);

        Collection<FileAnnotationData> tables = handleServiceAndAccess(tablesFacility,
                                                                       tf -> tf.getAvailableTables(dm.getCtx(),
                                                                                                   asDataObject()),
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
    default void addAndReplaceTable(Client client, Table table, ReplacePolicy policy)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        String error = String.format("Cannot get tables from %s", this);

        Collection<FileAnnotation> tables = wrap(handleServiceAndAccess(client.getTablesFacility(),
                                                                        t -> t.getAvailableTables(
                                                                                client.getCtx(), asDataObject()),
                                                                        error),
                                                 FileAnnotationWrapper::new);
        addTable(client, table);
        tables.removeIf(t -> !t.getDescription().equals(table.getName()));
        for (FileAnnotation fileAnnotation : tables) {
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
    default void addAndReplaceTable(Client client, Table table)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        addAndReplaceTable(client, table, ReplacePolicy.DELETE_ORPHANED);
    }


    /**
     * Gets a certain table linked to the object in OMERO.
     *
     * @param dm     The data manager.
     * @param fileId FileId of the table researched.
     *
     * @return TableWrapper containing the table information.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default Table getTable(DataManager dm, Long fileId)
    throws ServiceException, AccessException, ExecutionException {
        TableData table = handleServiceAndAccess(dm.getTablesFacility(),
                                                 tf -> tf.getTable(dm.getCtx(), fileId),
                                                 "Cannot get table from " + this);
        String name = handleServiceAndAccess(dm.getTablesFacility(),
                                             tf -> tf.getAvailableTables(dm.getCtx(), asDataObject())
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
     * @param dm The data manager.
     *
     * @return List of TableWrappers containing the tables.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Table> getTables(DataManager dm)
    throws ServiceException, AccessException, ExecutionException {
        Collection<FileAnnotationData> tables = handleServiceAndAccess(dm.getTablesFacility(),
                                                                       tf -> tf.getAvailableTables(dm.getCtx(),
                                                                                                   asDataObject()),
                                                                       "Cannot get tables from " + this);

        List<Table> tablesWrapper = new ArrayList<>(tables.size());
        for (FileAnnotationData table : tables) {
            Table tableWrapper = getTable(dm, table.getFileID());
            tableWrapper.setId(table.getId());
            tablesWrapper.add(tableWrapper);
        }

        return tablesWrapper;
    }


    /**
     * Uploads a file and links it to the object
     *
     * @param dm   The data manager.
     * @param file File to add.
     *
     * @return ID of the file created in OMERO.
     *
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException The thread was interrupted.
     */
    default long addFile(DataManager dm, File file) throws ExecutionException, InterruptedException {
        return dm.getDataManagerFacility().attachFile(dm.getCtx(),
                                                      file,
                                                      null,
                                                      "",
                                                      file.getName(),
                                                      asDataObject()).get().getId();
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
    default long addAndReplaceFile(Client client, File file, ReplacePolicy policy)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, ServerException {
        List<FileAnnotation> files = getFileAnnotations(client);

        FileAnnotationData uploaded = client.getDataManagerFacility().attachFile(client.getCtx(),
                                                                                 file,
                                                                                 null,
                                                                                 "",
                                                                                 file.getName(),
                                                                                 asDataObject()).get();
        FileAnnotation annotation = new FileAnnotationWrapper(uploaded);

        files.removeIf(fileAnnotation -> !fileAnnotation.getFileName().equals(annotation.getFileName()));
        for (FileAnnotation fileAnnotation : files) {
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
    default long addAndReplaceFile(Client client, File file)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, ServerException {
        return addAndReplaceFile(client, file, ReplacePolicy.DELETE_ORPHANED);
    }


    /**
     * Links a file annotation to the object
     *
     * @param dm         The data manager.
     * @param annotation FileAnnotationWrapper to link.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addFileAnnotation(DataManager dm, FileAnnotation annotation)
    throws AccessException, ServiceException, ExecutionException {
        handleServiceAndAccess(dm.getDataManagerFacility(),
                               dmf -> dmf.attachAnnotation(dm.getCtx(), annotation.asDataObject(), asDataObject()),
                               "Cannot link file annotation to " + this);
    }


    /**
     * Returns the file annotations
     *
     * @param browser The data browser.
     *
     * @return The list of tile annotations.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<FileAnnotation> getFileAnnotations(Browser browser)
    throws ExecutionException, ServiceException, AccessException {
        String error = "Cannot retrieve file annotations from " + this;

        List<Class<? extends AnnotationData>> types = Collections.singletonList(FileAnnotationData.class);

        List<AnnotationData> annotations = handleServiceAndAccess(browser.getMetadata(),
                                                                  m -> m.getAnnotations(browser.getCtx(),
                                                                                        asDataObject(),
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
    <A extends Annotation<?>> void unlink(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException;


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
    default List<AnnotationData> getAnnotations(Client client)
    throws AccessException, ServiceException, ExecutionException {
        return handleServiceAndAccess(client.getMetadata(),
                                      m -> m.getAnnotations(client.getCtx(), asDataObject()),
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
    default void copyAnnotationLinks(Client client, Annotatable<?> object)
    throws AccessException, ServiceException, ExecutionException {
        List<AnnotationData> newAnnotations = object.getAnnotations(client);
        List<AnnotationData> oldAnnotations = this.getAnnotations(client);
        for (AnnotationData annotation : oldAnnotations) {
            newAnnotations.removeIf(a -> a.getId() == annotation.getId());
        }
        for (AnnotationData annotation : newAnnotations) {
            handleServiceAndAccess(client.getDataManagerFacility(),
                                   dm -> dm.attachAnnotation(client.getCtx(), annotation, asDataObject()),
                                   "Cannot link annotations to " + this);
        }
    }

}
