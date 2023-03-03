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

package fr.igred.omero;


import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.annotations.FileAnnotation;
import fr.igred.omero.annotations.MapAnnotation;
import fr.igred.omero.annotations.Table;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.GenericRepositoryObjectWrapper.ReplacePolicy;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.TableData;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Interface to handle Annotatable Objects on OMERO.
 */
public interface Annotatable extends RemoteObject {


    /**
     * Checks if a specific annotation is linked to the object.
     *
     * @param client     The client handling the connection.
     * @param annotation Annotation to be checked.
     * @param <A>        The type of the annotation.
     *
     * @return True if the object is linked to the given annotation, false otherwise.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default <A extends Annotation> boolean isLinked(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        return getAnnotations(client).stream().anyMatch(a -> a.getId() == annotation.getId());
    }


    /**
     * Attach an {@link AnnotationData} to this object.
     *
     * @param <A>        The type of the annotation.
     * @param client     The client handling the connection.
     * @param annotation The {@link AnnotationData}.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    <A extends AnnotationData> void link(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Adds an annotation to the object in OMERO, if possible.
     *
     * @param <A>        The type of the annotation.
     * @param client     The client handling the connection.
     * @param annotation Annotation to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    <A extends Annotation> void link(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Adds multiple annotations to the object in OMERO, if possible.
     *
     * @param client      The client handling the connection.
     * @param annotations Annotations to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void link(Client client, Annotation... annotations)
    throws ServiceException, AccessException, ExecutionException {
        for (Annotation annotation : annotations) {
            link(client, annotation);
        }
    }


    /**
     * Adds multiple annotations to the object in OMERO if they are not already linked.
     *
     * @param client      The client handling the connection.
     * @param annotations Annotations to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void linkIfNotLinked(Client client, Annotation... annotations)
    throws ServiceException, AccessException, ExecutionException {
        List<Long> annotationIds = getAnnotationData(client).stream()
                                                            .map(DataObject::getId)
                                                            .collect(Collectors.toList());
        link(client, Arrays.stream(annotations)
                           .filter(a -> !annotationIds.contains(a.getId()))
                           .toArray(Annotation[]::new));
    }


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
     * @param id     ID of the tag to add to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void addTag(Client client, Long id)
    throws ServiceException, AccessException, ExecutionException;


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
    void addTags(Client client, Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all tags linked to an object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends TagAnnotation> getTags(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all map annotations linked to an object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends MapAnnotation> getMapAnnotations(Client client)
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
    void addKeyValuePair(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the List of Key-Value pairs associated to an object.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
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
    String getValue(Client client, String key)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Rates the object (using a rating annotation).
     * <p>If multiple ratings are present, only one will be kept and updated.
     *
     * @param client The client handling the connection.
     * @param rating The rating.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError     Server error.
     * @throws InterruptedException The thread was interrupted.
     */
    void rate(Client client, int rating)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException;


    /**
     * Returns the user rating for this object (averaged if multiple ratings are linked).
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    int getMyRating(Client client)
    throws ServiceException, AccessException, ExecutionException;


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
        TablesFacility tablesFacility = client.getTablesFacility();
        TableData tableData = ExceptionHandler.of(tablesFacility,
                                                  tf -> tf.addTable(client.getCtx(),
                                                                    asDataObject(),
                                                                    table.getName(),
                                                                    table.createTable()))
                                              .handleServiceOrAccess("Cannot add table to " + this)
                                              .get();

        Collection<FileAnnotationData> tables = ExceptionHandler.of(tablesFacility,
                                                                    tf -> tf.getAvailableTables(client.getCtx(),
                                                                                                asDataObject()))
                                                                .handleServiceOrAccess("Cannot add table to " + this)
                                                                .get();
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
     * @throws OMEROServerError     Server error.
     */
    void addAndReplaceTable(Client client, Table table, ReplacePolicy policy)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException;


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
     * @throws OMEROServerError     Server error.
     */
    default void addAndReplaceTable(Client client, Table table)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        addAndReplaceTable(client, table, ReplacePolicy.DELETE_ORPHANED);
    }


    /**
     * Gets a certain table linked to the object in OMERO.
     *
     * @param client The client handling the connection.
     * @param fileId FileId of the table researched.
     *
     * @return See above.
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
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends Table> getTables(Client client)
    throws ServiceException, AccessException, ExecutionException;


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
    long addFile(Client client, File file)
    throws ExecutionException, InterruptedException;


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
     * @throws OMEROServerError     Server error.
     */
    long addAndReplaceFile(Client client, File file, ReplacePolicy policy)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, OMEROServerError;


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
     * @throws OMEROServerError     Server error.
     */
    long addAndReplaceFile(Client client, File file)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, OMEROServerError;


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
    List<? extends FileAnnotation> getFileAnnotations(Client client)
    throws ExecutionException, ServiceException, AccessException;


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
     * @throws OMEROServerError     Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    <A extends Annotation> void unlink(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException;


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
    List<AnnotationData> getAnnotationData(Client client)
    throws AccessException, ServiceException, ExecutionException;


    /**
     * Retrieves annotations linked to the object (of known types).
     *
     * @param client The client handling the connection.
     *
     * @return A list of annotations.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends Annotation> getAnnotations(Client client)
    throws AccessException, ServiceException, ExecutionException;


    /**
     * Copies annotation links from some other object to this one.
     *
     * @param client The client handling the connection.
     * @param object Other repository object to copy annotations from.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void copyAnnotationLinks(Client client, Annotatable object)
    throws AccessException, ServiceException, ExecutionException;

}
