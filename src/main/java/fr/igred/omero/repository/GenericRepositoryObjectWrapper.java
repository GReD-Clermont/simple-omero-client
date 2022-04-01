/*
 *  Copyright (C) 2020-2022 GReD
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
import fr.igred.omero.GenericObjectWrapper;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.GenericAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.TableData;
import omero.gateway.model.TagAnnotationData;
import omero.model.IObject;
import omero.model.NamedValue;
import omero.model.TagAnnotationI;

import java.io.File;
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

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class GenericRepositoryObjectWrapper<T extends DataObject> extends GenericObjectWrapper<T> {

    /**
     * Constructor of the class GenericRepositoryObjectWrapper.
     *
     * @param object The object contained in the GenericRepositoryObjectWrapper.
     */
    protected GenericRepositoryObjectWrapper(T object) {
        super(object);
    }


    /**
     * Returns the type of annotation link for this object
     *
     * @return See above.
     */
    protected abstract String annotationLinkType();


    /**
     * Gets the object name.
     *
     * @return See above.
     */
    public abstract String getName();


    /**
     * Gets the object description
     *
     * @return See above.
     */
    public abstract String getDescription();


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
    public void addTag(Client client, TagAnnotationWrapper tag)
    throws ServiceException, AccessException, ExecutionException {
        addTag(client, tag.asTagAnnotationData());
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
        try {
            client.getDm().attachAnnotation(client.getCtx(), tagData, data);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot add tag " + tagData.getTagValue() + " to " + this);
        }
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
    public void addTags(Client client, TagAnnotationWrapper... tags)
    throws ServiceException, AccessException, ExecutionException {
        for (TagAnnotationWrapper tag : tags) {
            addTag(client, tag.asTagAnnotationData());
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
    public void addTags(Client client, Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        for (Long id : ids) {
            addTag(client, id);
        }
    }


    /**
     * Gets all tag linked to an object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     *
     * @return Collection of TagAnnotationWrapper each containing a tag linked to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<TagAnnotationWrapper> getTags(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(TagAnnotationData.class);

        List<AnnotationData> annotations = new ArrayList<>(0);
        try {
            annotations = client.getMetadata().getAnnotations(client.getCtx(), data, types, null);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get tags for " + this);
        }

        return annotations.stream()
                          .filter(TagAnnotationData.class::isInstance)
                          .map(TagAnnotationData.class::cast)
                          .map(TagAnnotationWrapper::new)
                          .sorted(Comparator.comparing(TagAnnotationWrapper::getId))
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
    public void addPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<NamedValue> kv = Collections.singletonList(new NamedValue(key, value));
        addMapAnnotation(client, new MapAnnotationWrapper(kv));
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
    public Map<String, String> getKeyValuePairs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(MapAnnotationData.class);

        List<AnnotationData> annotations = new ArrayList<>(0);
        try {
            annotations = client.getMetadata().getAnnotations(client.getCtx(), data, types, null);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get key-value pairs for " + this);
        }

        return annotations.stream()
                          .filter(MapAnnotationData.class::isInstance)
                          .map(MapAnnotationData.class::cast)
                          .map(MapAnnotationWrapper::new)
                          .map(MapAnnotationWrapper::getContent)
                          .flatMap(List::stream)
                          .collect(Collectors.toMap(n -> n.name, n -> n.value));
    }


    /**
     * Gets the value from a Key-Value pair associated to the object
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
     * Adds a List of Key-Value pair to the object
     * <p>The list is contained in the MapAnnotationWrapper
     *
     * @param client        The client handling the connection.
     * @param mapAnnotation MapAnnotationWrapper containing a list of NamedValue.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addMapAnnotation(Client client, MapAnnotationWrapper mapAnnotation)
    throws ServiceException, AccessException, ExecutionException {
        try {
            client.getDm().attachAnnotation(client.getCtx(),
                                            mapAnnotation.asMapAnnotationData(),
                                            this.data);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot add key-value pairs to " + this);
        }
    }


    /**
     * Adds a table to the object in OMERO
     *
     * @param client The client handling the connection.
     * @param table  Table to add to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTable(Client client, TableWrapper table)
    throws ServiceException, AccessException, ExecutionException {
        TableData tableData = table.createTable();
        try {
            tableData = client.getTablesFacility().addTable(client.getCtx(), data, table.getName(), tableData);

            Collection<FileAnnotationData> tables = client.getTablesFacility()
                                                          .getAvailableTables(client.getCtx(), data);
            final long fileId = tableData.getOriginalFileId();

            long id = tables.stream().filter(v -> v.getFileID() == fileId)
                            .mapToLong(DataObject::getId).max().orElse(-1L);
            table.setId(id);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot add table to " + this);
        }
        table.setFileId(tableData.getOriginalFileId());
    }


    /**
     * Gets a certain table linked to the object in OMERO
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
    public TableWrapper getTable(Client client, Long fileId)
    throws ServiceException, AccessException, ExecutionException {
        TableData table = null;
        try {
            table = client.getTablesFacility().getTable(client.getCtx(), fileId);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get table from " + this);
        }
        return new TableWrapper(Objects.requireNonNull(table));
    }


    /**
     * Gets all table linked to the object in OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return List of TableWrapper containing the tables information.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<TableWrapper> getTables(Client client)
    throws ServiceException, AccessException, ExecutionException {
        Collection<FileAnnotationData> tables = new ArrayList<>(0);
        try {
            tables = client.getTablesFacility().getAvailableTables(client.getCtx(), data);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get tables from " + this);
        }

        List<TableWrapper> tablesWrapper = new ArrayList<>(tables.size());
        for (FileAnnotationData table : tables) {
            TableWrapper tableWrapper = getTable(client, table.getFileID());
            tableWrapper.setId(table.getId());
            tablesWrapper.add(tableWrapper);
        }

        return tablesWrapper;
    }


    /**
     * Links a file to the object
     *
     * @param client The client handling the connection.
     * @param file   File to add.
     *
     * @return ID of the file created in OMERO.
     *
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException The thread was interrupted.
     */
    public long addFile(Client client, File file) throws ExecutionException, InterruptedException {
        return client.getDm().attachFile(client.getCtx(),
                                         file,
                                         null,
                                         "",
                                         file.getName(),
                                         data).get().getId();
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
    public List<FileAnnotationWrapper> getFileAnnotations(Client client)
    throws ExecutionException, ServiceException, AccessException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(FileAnnotationData.class);

        List<AnnotationData> annotations = new ArrayList<>(0);
        try {
            annotations = client.getMetadata().getAnnotations(client.getCtx(), data, types, null);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot retrieve file annotations from " + this);
        }

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
     * @throws OMEROServerError     If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    public <A extends GenericAnnotationWrapper<?>> void unlink(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
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
     * @throws OMEROServerError     If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    protected void removeLink(Client client, String linkType, long childId)
    throws ServiceException, OMEROServerError, AccessException, ExecutionException, InterruptedException {
        List<IObject> os = client.findByQuery("select link from " + linkType +
                                              " link where link.parent = " + getId() +
                                              " and link.child = " + childId);
        delete(client, os.iterator().next());
    }


}
