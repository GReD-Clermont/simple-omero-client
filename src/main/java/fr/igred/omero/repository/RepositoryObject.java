/*
 *  Copyright (C) 2020-2023 GReD
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
import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.annotations.FileAnnotation;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotation;
import fr.igred.omero.annotations.Table;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.TableData;
import omero.gateway.model.TagAnnotationData;
import omero.model.TagAnnotationI;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


public interface RepositoryObject<T extends DataObject> extends RemoteObject<T> {

    /**
     * Gets the object name.
     *
     * @return See above.
     */
    String getName();


    /**
     * Gets the object description
     *
     * @return See above.
     */
    String getDescription();


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
    void addTag(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException;


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
    default void addTag(Client client, TagAnnotation tag)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot add tag " + tag.getId() + " to " + this;

        TagAnnotationData tagData = tag.asDataObject();
        handleServiceAndAccess(client.getDm(),
                               d -> d.attachAnnotation(client.getCtx(), tagData, asDataObject()),
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
    default void addTag(Client client, Long id)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot add tag " + id + " to " + this;

        TagAnnotationData tagData = new TagAnnotationData(new TagAnnotationI(id, false));
        handleServiceAndAccess(client.getDm(),
                               d -> d.attachAnnotation(client.getCtx(), tagData, asDataObject()),
                               error);
    }


    /**
     * Adds multiple tag to the object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     * @param tags   Array of TagAnnotation to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addTags(Client client, TagAnnotation... tags)
    throws ServiceException, AccessException, ExecutionException {
        for (TagAnnotation tag : tags) {
            addTag(client, tag);
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
    default void addTags(Client client, Long... ids)
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
     * @return List of TagAnnotations each containing a tag linked to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<TagAnnotation> getTags(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all map annotations linked to an object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     *
     * @return List of MapAnnotations.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<MapAnnotation> getMapAnnotations(Client client)
    throws ServiceException, AccessException, ExecutionException;


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
    void addPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException;


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
    Map<String, String> getKeyValuePairs(Client client)
    throws ServiceException, AccessException, ExecutionException;


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
    default String getValue(Client client, String key)
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
     * <p>The list is contained in the MapAnnotation.
     *
     * @param client        The client handling the connection.
     * @param mapAnnotation MapAnnotation containing a list of NamedValue.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addMapAnnotation(Client client, MapAnnotation mapAnnotation)
    throws ServiceException, AccessException, ExecutionException {
        handleServiceAndAccess(client.getDm(),
                               d -> d.attachAnnotation(client.getCtx(),
                                                       mapAnnotation.asDataObject(),
                                                       asDataObject()),
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
    default void addTable(Client client, Table table)
    throws ServiceException, AccessException, ExecutionException {
        String         error          = "Cannot add table to " + this;
        TablesFacility tablesFacility = client.getTablesFacility();
        TableData tableData = handleServiceAndAccess(tablesFacility,
                                                     tf -> tf.addTable(client.getCtx(),
                                                                       asDataObject(),
                                                                       table.getName(),
                                                                       table.createTable()),
                                                     error);

        Collection<FileAnnotationData> tables = handleServiceAndAccess(tablesFacility,
                                                                       tf -> tf.getAvailableTables(client.getCtx(),
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
    void addAndReplaceTable(Client client, Table table, ReplacePolicy policy)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException;


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
     * @param client The client handling the connection.
     * @param fileId FileId of the table researched.
     *
     * @return Table containing the table information.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    Table getTable(Client client, Long fileId)
    throws ServiceException, AccessException, ExecutionException;


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
    default List<Table> getTables(Client client)
    throws ServiceException, AccessException, ExecutionException {
        Collection<FileAnnotationData> files = handleServiceAndAccess(client.getTablesFacility(),
                                                                      tf -> tf.getAvailableTables(client.getCtx(),
                                                                                                  asDataObject()),
                                                                      "Cannot get tables from " + this);

        List<Table> tables = new ArrayList<>(files.size());
        for (FileAnnotationData file : files) {
            Table table = getTable(client, file.getFileID());
            table.setId(file.getId());
            tables.add(table);
        }

        return tables;
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
    default long addFile(Client client, File file) throws ExecutionException, InterruptedException {
        return client.getDm().attachFile(client.getCtx(),
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

        FileAnnotationData uploaded = client.getDm().attachFile(client.getCtx(),
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
     * @param client     The client handling the connection.
     * @param annotation FileAnnotation to link.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addFileAnnotation(Client client, FileAnnotation annotation)
    throws AccessException, ServiceException, ExecutionException {
        handleServiceAndAccess(client.getDm(),
                               dm -> dm.attachAnnotation(client.getCtx(), annotation.asDataObject(), asDataObject()),
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
    default List<FileAnnotation> getFileAnnotations(Client client)
    throws ExecutionException, ServiceException, AccessException {
        String error = "Cannot retrieve file annotations from " + this;

        List<Class<? extends AnnotationData>> types = Collections.singletonList(FileAnnotationData.class);

        List<AnnotationData> annotations = handleServiceAndAccess(client.getMetadata(),
                                                                  m -> m.getAnnotations(client.getCtx(),
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
    default void copyAnnotationLinks(Client client, RepositoryObject<?> object)
    throws AccessException, ServiceException, ExecutionException {
        List<AnnotationData> newAnnotations = object.getAnnotations(client);
        List<AnnotationData> oldAnnotations = this.getAnnotations(client);
        for (AnnotationData annotation : oldAnnotations) {
            newAnnotations.removeIf(a -> a.getId() == annotation.getId());
        }
        for (AnnotationData annotation : newAnnotations) {
            handleServiceAndAccess(client.getDm(),
                                   dm -> dm.attachAnnotation(client.getCtx(), annotation, asDataObject()),
                                   "Cannot link annotations to " + this);
        }
    }


    /**
     * Policy to specify how to handle objects when they are replaced.
     */
    enum ReplacePolicy {
        /** Unlink objects only */
        UNLINK,

        /** Delete all objects */
        DELETE,

        /** Delete orphaned objects */
        DELETE_ORPHANED
    }

}
