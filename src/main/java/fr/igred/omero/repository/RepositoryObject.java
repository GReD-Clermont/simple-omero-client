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


import fr.igred.omero.Browser;
import fr.igred.omero.Client;
import fr.igred.omero.DataManager;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.annotations.FileAnnotation;
import fr.igred.omero.annotations.MapAnnotation;
import fr.igred.omero.annotations.Table;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Repository Objects on OMERO.
 *
 * @param <T> Subclass of {@link DataObject}
 */
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
     * @param dm  The data manager.
     * @param tag Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void addTag(DataManager dm, TagAnnotation tag)
    throws ServiceException, AccessException, ExecutionException;


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
    void addTag(DataManager dm, Long id)
    throws ServiceException, AccessException, ExecutionException;


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
    void addTags(DataManager dm, TagAnnotation... tags)
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
    void addTags(DataManager dm, Long... ids)
    throws ServiceException, AccessException, ExecutionException;


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
    List<TagAnnotation> getTags(Browser browser)
    throws ServiceException, AccessException, ExecutionException;


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
    void addPairKeyValue(DataManager dm, String key, String value)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the List of NamedValue (Key-Value pair) associated to an object.
     *
     * @param browser The data browser.
     *
     * @return Collection of NamedValue.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    Map<String, String> getKeyValuePairs(Browser browser)
    throws ServiceException, AccessException, ExecutionException;


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
    String getValue(Browser browser, String key)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Adds a List of Key-Value pair to the object.
     * <p>The list is contained in the MapAnnotationWrapper.
     *
     * @param dm            The data manager.
     * @param mapAnnotation MapAnnotationWrapper containing a list of NamedValue.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void addMapAnnotation(DataManager dm, MapAnnotation mapAnnotation)
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
    void addTable(DataManager dm, Table table)
    throws ServiceException, AccessException, ExecutionException;


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
    void addAndReplaceTable(Client client, Table table)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException;


    /**
     * Gets a certain table linked to the object in OMERO.
     *
     * @param dm     The data manager.
     * @param fileId FileId of the table researched.
     *
     * @return Table containing the table information.
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
     * @return List of Tables containing the tables.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Table> getTables(DataManager dm)
    throws ServiceException, AccessException, ExecutionException;


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
    long addFile(DataManager dm, File file) throws ExecutionException, InterruptedException;


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
    long addAndReplaceFile(Client client, File file, ReplacePolicy policy)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, ServerException;


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
    long addAndReplaceFile(Client client, File file)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, ServerException;


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
    void addFileAnnotation(DataManager dm, FileAnnotation annotation)
    throws AccessException, ServiceException, ExecutionException;


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
    List<FileAnnotation> getFileAnnotations(Client client)
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
    List<AnnotationData> getAnnotations(Client client)
    throws AccessException, ServiceException, ExecutionException;


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
    void copyAnnotationLinks(Client client, RepositoryObject<?> object)
    throws AccessException, ServiceException, ExecutionException;


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
