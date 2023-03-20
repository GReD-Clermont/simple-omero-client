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
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.ReplacePolicy;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.TableData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Interface to handle Annotatable Objects on OMERO.
 */
public interface Annotatable extends RemoteObject {


    /**
     * Checks if a specific annotation is linked to the object.
     *
     * @param browser    The data browser.
     * @param annotation Annotation to be checked.
     * @param <A>        The type of the annotation.
     *
     * @return True if the object is linked to the given annotation, false otherwise.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default <A extends Annotation> boolean isLinked(Browser browser, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        return getAnnotations(browser).stream().anyMatch(a -> a.getId() == annotation.getId());
    }


    /**
     * Attach an {@link AnnotationData} to this object.
     *
     * @param <A>        The type of the annotation.
     * @param dm         The data manager.
     * @param annotation The {@link AnnotationData}.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default <A extends AnnotationData> void link(DataManager dm, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        String error = String.format("Cannot add %s to %s", annotation, this);
        ExceptionHandler.of(dm.getDMFacility(), d -> d.attachAnnotation(dm.getCtx(), annotation, asDataObject()))
                        .handleOMEROException(error)
                        .rethrow();
    }


    /**
     * Adds an annotation to the object in OMERO, if possible (and if it's not a TagSet).
     *
     * @param <A>        The type of the annotation.
     * @param dm         The client handling the connection.
     * @param annotation Annotation to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default <A extends Annotation> void link(DataManager dm, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        if (!(annotation instanceof TagAnnotation) || !((TagAnnotation) annotation).isTagSet()) {
            link(dm, annotation.asDataObject());
        } else {
            throw new IllegalArgumentException("Tag sets should only be linked to tags");
        }
    }


    /**
     * Adds multiple annotations to the object in OMERO, if possible.
     *
     * @param dm          The data manager.
     * @param annotations Annotations to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void link(DataManager dm, Annotation... annotations)
    throws ServiceException, AccessException, ExecutionException {
        for (Annotation annotation : annotations) {
            link(dm, annotation);
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
     * @param dm          The data manager.
     * @param name        Tag Name.
     * @param description Tag description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void addTag(DataManager dm, String name, String description)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Adds a tag to the object in OMERO, if possible.
     *
     * @param dm The data manager.
     * @param id ID of the tag to add to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void addTag(DataManager dm, Long id)
    throws ServiceException, AccessException, ExecutionException;


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
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<TagAnnotation> getTags(Browser browser)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all map annotations linked to an object in OMERO, if possible.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<MapAnnotation> getMapAnnotations(Browser browser)
    throws ServiceException, AccessException, ExecutionException;


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
    void addKeyValuePair(DataManager dm, String key, String value)
    throws ServiceException, AccessException, ExecutionException;


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
     * Rates the object (using a rating annotation).
     * <p>If multiple ratings are present, only one will be kept and updated.
     *
     * @param client The client handling the connection.
     * @param rating The rating.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException The thread was interrupted.
     */
    void rate(Client client, int rating)
    throws ServiceException, AccessException, ExecutionException, InterruptedException;


    /**
     * Returns the user rating for this object (averaged if multiple ratings are linked).
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    int getMyRating(Browser browser)
    throws ServiceException, AccessException, ExecutionException;


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
        TablesFacility tablesFacility = dm.getTablesFacility();
        TableData tableData = ExceptionHandler.of(tablesFacility,
                                                  tf -> tf.addTable(dm.getCtx(),
                                                                    asDataObject(),
                                                                    table.getName(),
                                                                    table.createTable()))
                                              .handleOMEROException("Cannot add table to " + this)
                                              .get();

        Collection<FileAnnotationData> tables = ExceptionHandler.of(tablesFacility,
                                                                    tf -> tf.getAvailableTables(dm.getCtx(),
                                                                                                asDataObject()))
                                                                .handleOMEROException("Cannot add table to " + this)
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
     */
    void addAndReplaceTable(Client client, Table table, ReplacePolicy policy)
    throws ServiceException, AccessException, ExecutionException, InterruptedException;


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
     */
    default void addAndReplaceTable(Client client, Table table)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        addAndReplaceTable(client, table, ReplacePolicy.DELETE_ORPHANED);
    }


    /**
     * Gets a certain table linked to the object in OMERO.
     *
     * @param dm     The data manager.
     * @param fileId FileId of the table researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    Table getTable(DataManager dm, Long fileId)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all tables linked to the object in OMERO.
     *
     * @param dm The data manager.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Table> getTables(DataManager dm)
    throws ServiceException, AccessException, ExecutionException {
        Collection<FileAnnotationData> files = ExceptionHandler.of(dm.getTablesFacility(),
                                                                   tf -> tf.getAvailableTables(dm.getCtx(),
                                                                                               asDataObject()))
                                                               .handleOMEROException("Cannot get tables from " + this)
                                                               .get();

        List<Table> tables = new ArrayList<>(files.size());
        for (FileAnnotationData file : files) {
            Table table = getTable(dm, file.getFileID());
            table.setId(file.getId());
            tables.add(table);
        }

        return tables;
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
    default long addFile(DataManager dm, File file)
    throws ExecutionException, InterruptedException {
        return dm.getDMFacility().attachFile(dm.getCtx(),
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
     */
    long addAndReplaceFile(Client client, File file, ReplacePolicy policy)
    throws ExecutionException, InterruptedException, AccessException, ServiceException;


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
     */
    default long addAndReplaceFile(Client client, File file)
    throws ExecutionException, InterruptedException, AccessException, ServiceException {
        return addAndReplaceFile(client, file, ReplacePolicy.DELETE_ORPHANED);
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
    List<FileAnnotation> getFileAnnotations(Browser browser)
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
     * @throws InterruptedException If block(long) does not return.
     */
    <A extends Annotation> void unlink(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException, InterruptedException;


    /**
     * Retrieves annotations linked to the object.
     *
     * @param browser The data browser.
     *
     * @return A list of annotations, as AnnotationData.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<AnnotationData> getAnnotationData(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        return ExceptionHandler.of(browser.getMetadataFacility(),
                                   m -> m.getAnnotations(browser.getCtx(), asDataObject()))
                               .handleOMEROException("Cannot get annotations from " + this)
                               .get();
    }


    /**
     * Retrieves annotations linked to the object (of known types).
     *
     * @param browser The data browser.
     *
     * @return A list of annotations.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Annotation> getAnnotations(Browser browser)
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
    default void copyAnnotationLinks(Client client, Annotatable object)
    throws AccessException, ServiceException, ExecutionException {
        List<AnnotationData> newAnnotations = object.getAnnotationData(client);
        List<AnnotationData> oldAnnotations = this.getAnnotationData(client);
        for (AnnotationData annotation : oldAnnotations) {
            newAnnotations.removeIf(a -> a.getId() == annotation.getId());
        }
        for (AnnotationData annotation : newAnnotations) {
            link(client, annotation);
        }
    }

}
