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


import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.*;
import omero.model.*;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class GenericObjectWrapper<T extends DataObject> {

    protected T data;


    /**
     * Constructor of the class GenericObjectWrapper.
     *
     * @param object The object contained in the GenericObjectWrapper.
     */
    protected GenericObjectWrapper(T object) {
        this.data = object;
    }


    /**
     * Gets the wrapped object type.
     *
     * @return Name of the class for the wrapped object.
     */
    private String getTypeAndId() {
        return String.format("%s ID: %d", data.getClass().getSimpleName(), getId());
    }


    /**
     * Gets the object id
     *
     * @return id.
     */
    public Long getId() {
        return data.getId();
    }


    /**
     * Gets the object creation date
     *
     * @return creation date.
     */
    public Timestamp getCreated() {
        return data.getCreated();
    }


    /**
     * Gets the owner ID
     *
     * @return owner id.
     */
    public Long getOwnerId() {
        return data.getOwner().getId();
    }


    /**
     * Gets the group ID
     *
     * @return group id.
     */
    public Long getGroupId() {
        return data.getGroupId();
    }


    /**
     * Adds a newly created tag to the object in OMERO, if possible.
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
     * Adds a tag to the object in OMERO, if possible.
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
     * Private function. Adds a tag to the object in OMERO, if possible.
     *
     * @param client  The user.
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
            handleServiceOrAccess(e, "Cannot add tag " + tagData.getTagValue() + " to " + getTypeAndId());
        }
    }


    /**
     * Adds multiple tags to the object in OMERO, if possible.
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
        TagAnnotationI    tag     = new TagAnnotationI(id, false);
        TagAnnotationData tagData = new TagAnnotationData(tag);
        addTag(client, tagData);
    }


    /**
     * Adds multiple tag to the object in OMERO, if possible.
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
     * Adds multiple tags by ID to the object in OMERO, if possible.
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
     * Gets all tag linked to an object in OMERO, if possible.
     *
     * @param client The user.
     *
     * @return Collection of TagAnnotationWrapper each containing a tag linked to the object.
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
            handleServiceOrAccess(e, "Cannot get tags for " + getTypeAndId());
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
     * Adds a single Key-Value pair to the object.
     *
     * @param client The user.
     * @param key    Name of the key.
     * @param value  Value associated to the key.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<NamedValue> result = new ArrayList<>();
        result.add(new NamedValue(key, value));

        MapAnnotationData mapData = new MapAnnotationData();
        mapData.setContent(result);
        addMapAnnotation(client, new MapAnnotationWrapper(mapData));
    }


    /**
     * Gets the List of NamedValue (Key-Value pair) associated to an object.
     *
     * @param client The user.
     *
     * @return Collection of NamedValue.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<NamedValue> getKeyValuePairs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<NamedValue> keyValuePairs = new ArrayList<>();

        List<Long> userIds = new ArrayList<>();
        userIds.add(client.getId());

        List<Class<? extends AnnotationData>> types = new ArrayList<>();
        types.add(MapAnnotationData.class);

        List<AnnotationData> annotations = null;
        try {
            annotations = client.getMetadata().getAnnotations(client.getCtx(), data, types, userIds);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get k/v pairs for " + getTypeAndId());
        }

        if (annotations != null) {
            for (AnnotationData annotation : annotations) {
                MapAnnotationData mapAnnotation = (MapAnnotationData) annotation;

                @SuppressWarnings("unchecked")
                List<NamedValue> list = (List<NamedValue>) mapAnnotation.getContent();

                keyValuePairs.addAll(list);
            }
        }

        return keyValuePairs;
    }


    /**
     * Gets the value from a Key-Value pair associated to the object
     *
     * @param client The user.
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
    throws ServiceException, AccessException, NoSuchElementException, ExecutionException {
        Collection<NamedValue> keyValuePairs = getKeyValuePairs(client);

        for (NamedValue namedValue : keyValuePairs) {
            if (namedValue.name.equals(key)) {
                return namedValue.value;
            }
        }

        throw new NoSuchElementException("Key value pair " + key + " not found");
    }


    /**
     * Adds a List of Key-Value pair to the object
     * <p>The list is contained in the MapAnnotationWrapper
     *
     * @param client        The user.
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
                                            mapAnnotation.getMapAnnotation(),
                                            this.data);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot add k/v pairs to " + getTypeAndId());
        }
    }


    /**
     * Adds a table to the object in OMERO
     *
     * @param client The user.
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
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot add table to " + getTypeAndId());
        }
        table.setFileId(tableData.getOriginalFileId());
    }


    /**
     * Gets a certain table linked to the object in OMERO
     *
     * @param client The user.
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
            handleServiceOrAccess(e, "Cannot get table from " + getTypeAndId());
        }
        return new TableWrapper(Objects.requireNonNull(table));
    }


    /**
     * Gets all table linked to the object in OMERO.
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
            handleServiceOrAccess(e, "Cannot get tables from " + getTypeAndId());
        }

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
     * @param client The user.
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
     * Class used to sort TagAnnotationWrappers
     */
    public static class SortById<U extends GenericObjectWrapper<?>> implements Comparator<U> {

        /**
         * Compare 2 ObjectWrappers. Compare the id of the ObjectWrappers.
         *
         * @param object1 First object to compare.
         * @param object2 Second object to compare.
         *
         * @return <ul><li>-1 if the id of object1 is lower than the id object2.</li>
         * <li>0  if the ids are the same.</li>
         * <li>1 if the id of object1 is greater than the id of object2.</li></ul>
         */
        public int compare(U object1, U object2) {
            return Long.compare(object1.getId(), object2.getId());
        }

    }

}
