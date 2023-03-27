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


import fr.igred.omero.annotations.AnnotationList;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.GenericAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.RatingAnnotationWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.GenericRepositoryObjectWrapper.ReplacePolicy;
import omero.constants.metadata.NSCLIENTMAPANNOTATION;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.TableData;
import omero.gateway.model.TagAnnotationData;
import omero.model.IObject;
import omero.model.NamedValue;
import omero.model.TagAnnotationI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class AnnotatableWrapper<T extends DataObject> extends GenericObjectWrapper<T> {

    /**
     * Constructor of the class GenericRepositoryObjectWrapper.
     *
     * @param o The annotatable DataObject to wrap in the GenericRepositoryObjectWrapper.
     */
    protected AnnotatableWrapper(T o) {
        super(o);
    }


    /**
     * Returns the type of annotation link for this object.
     *
     * @return See above.
     */
    protected abstract String annotationLinkType();


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
    public <A extends GenericAnnotationWrapper<?>> boolean isLinked(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        return getAnnotations(client).stream().anyMatch(a -> a.getId() == annotation.getId());
    }


    /**
     * Attach an {@link AnnotationData} to this object.
     *
     * @param client     The client handling the connection.
     * @param annotation The {@link AnnotationData}.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    protected <A extends AnnotationData> void link(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        String error = String.format("Cannot add %s to %s", annotation, this);
        ExceptionHandler.of(client.getDm(), d -> d.attachAnnotation(client.getCtx(), annotation, data))
                        .handleServiceOrAccess(error)
                        .rethrow();
    }


    /**
     * Adds an annotation to the object in OMERO, if possible.
     *
     * @param client     The client handling the connection.
     * @param annotation Annotation to be added.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public <A extends GenericAnnotationWrapper<?>> void link(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        link(client, annotation.asDataObject());
    }


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
    public void link(Client client, GenericAnnotationWrapper<?>... annotations)
    throws ServiceException, AccessException, ExecutionException {
        for (GenericAnnotationWrapper<?> annotation : annotations) {
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
    public void linkIfNotLinked(Client client, GenericAnnotationWrapper<?>... annotations)
    throws ServiceException, AccessException, ExecutionException {
        List<Long> annotationIds = getAnnotationData(client).stream()
                                                            .map(DataObject::getId)
                                                            .collect(Collectors.toList());
        link(client, Arrays.stream(annotations)
                           .filter(a -> !annotationIds.contains(a.getId()))
                           .toArray(GenericAnnotationWrapper<?>[]::new));
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
    public void addTag(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotationWrapper tag = new TagAnnotationWrapper(new TagAnnotationData(name));
        tag.setDescription(description);
        link(client, tag);
    }


    /**
     * @param client The client handling the connection.
     * @param tag    Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Adds a tag to the object in OMERO, if possible. Use {@link #link} instead.
     */
    @Deprecated
    public void addTag(Client client, TagAnnotationWrapper tag)
    throws ServiceException, AccessException, ExecutionException {
        link(client, tag);
    }


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
    public void addTag(Client client, Long id)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotationI    tag     = new TagAnnotationI(id, false);
        TagAnnotationData tagData = new TagAnnotationData(tag);
        link(client, new TagAnnotationWrapper(tagData));
    }


    /**
     * @param client The client handling the connection.
     * @param tags   Array of tag annotations to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Adds multiple tag to the object in OMERO, if possible. Use
     * {@link #link(Client, GenericAnnotationWrapper[])} instead.
     */
    @Deprecated
    public void addTags(Client client, TagAnnotationWrapper... tags)
    throws ServiceException, AccessException, ExecutionException {
        link(client, tags);
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
    public List<TagAnnotationWrapper> getTags(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(TagAnnotationData.class);

        List<AnnotationData> annotations = ExceptionHandler.of(client.getMetadata(),
                                                               m -> m.getAnnotations(client.getCtx(),
                                                                                     data,
                                                                                     types,
                                                                                     null))
                                                           .handleServiceOrAccess("Cannot get tags for " + this)
                                                           .get();

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
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<MapAnnotationWrapper> getMapAnnotations(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(MapAnnotationData.class);
        List<AnnotationData> annotations = ExceptionHandler.of(client.getMetadata(),
                                                               m -> m.getAnnotations(client.getCtx(),
                                                                                     data,
                                                                                     types,
                                                                                     null))
                                                           .handleServiceOrAccess("Cannot get map annotations for "
                                                                                  + this)
                                                           .get();

        return annotations.stream()
                          .filter(MapAnnotationData.class::isInstance)
                          .map(MapAnnotationData.class::cast)
                          .map(MapAnnotationWrapper::new)
                          .sorted(Comparator.comparing(MapAnnotationWrapper::getId))
                          .collect(Collectors.toList());
    }


    /**
     * @param client The client handling the connection.
     * @param key    Name of the key.
     * @param value  Value associated to the key.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Adds a single Key-Value pair to the object.
     */
    @Deprecated
    public void addPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        addKeyValuePair(client, key, value);
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
    public void addKeyValuePair(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<NamedValue>     kv  = Collections.singletonList(new NamedValue(key, value));
        MapAnnotationWrapper pkv = new MapAnnotationWrapper(kv);
        pkv.setNameSpace(NSCLIENTMAPANNOTATION.value);
        link(client, pkv);
    }


    /**
     * Gets the List of key-value pairs associated to an object as a map (no duplicate key should exist).
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public Map<String, String> getKeyValuePairs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        return getMapAnnotations(client).stream()
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
     * Rates the object (using a rating annotation).
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
    public void rate(Client client, int rating)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        String error = "Cannot retrieve rating annotations from " + this;

        List<Class<? extends AnnotationData>> types   = Collections.singletonList(RatingAnnotationData.class);
        List<Long>                            userIds = Collections.singletonList(client.getCtx().getExperimenter());

        List<AnnotationData> annotations = ExceptionHandler.of(client.getMetadata(),
                                                               m -> m.getAnnotations(client.getCtx(),
                                                                                     data,
                                                                                     types,
                                                                                     userIds))
                                                           .handleServiceOrAccess(error)
                                                           .get();
        List<RatingAnnotationWrapper> ratings = annotations.stream()
                                                           .filter(RatingAnnotationData.class::isInstance)
                                                           .map(RatingAnnotationData.class::cast)
                                                           .map(RatingAnnotationWrapper::new)
                                                           .sorted(Comparator.comparing(RatingAnnotationWrapper::getId))
                                                           .collect(Collectors.toList());

        if (ratings.isEmpty()) {
            RatingAnnotationWrapper rate = new RatingAnnotationWrapper(rating);
            link(client, rate);
        } else {
            int n = ratings.size();
            if (n > 1) client.delete(ratings.subList(1, n));
            RatingAnnotationWrapper rate = ratings.get(0);
            rate.setRating(rating);
            rate.saveAndUpdate(client);
        }
    }


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
    public int getMyRating(Client client)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot retrieve rating annotations from " + this;

        List<Class<? extends AnnotationData>> types   = Collections.singletonList(RatingAnnotationData.class);
        List<Long>                            userIds = Collections.singletonList(client.getCtx().getExperimenter());

        List<AnnotationData> annotations = ExceptionHandler.of(client.getMetadata(),
                                                               m -> m.getAnnotations(client.getCtx(),
                                                                                     data,
                                                                                     types,
                                                                                     userIds))
                                                           .handleServiceOrAccess(error)
                                                           .get();
        List<RatingAnnotationWrapper> ratings = annotations.stream()
                                                           .filter(RatingAnnotationData.class::isInstance)
                                                           .map(RatingAnnotationData.class::cast)
                                                           .map(RatingAnnotationWrapper::new)
                                                           .sorted(Comparator.comparing(RatingAnnotationWrapper::getId))
                                                           .collect(Collectors.toList());
        int score = 0;
        for (RatingAnnotationWrapper rate : ratings) {
            score += rate.getRating();
        }
        return score / Math.max(1, ratings.size());
    }


    /**
     * @param client        The client handling the connection.
     * @param mapAnnotation The map annotation.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Adds a List of Key-Value pair to the object. Use {@link #link} instead.
     * <p>The list is contained in the map annotation.
     */
    @Deprecated
    public void addMapAnnotation(Client client, MapAnnotationWrapper mapAnnotation)
    throws ServiceException, AccessException, ExecutionException {
        link(client, mapAnnotation);
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
    public void addTable(Client client, TableWrapper table)
    throws ServiceException, AccessException, ExecutionException {
        TablesFacility tablesFacility = client.getTablesFacility();
        TableData tableData = ExceptionHandler.of(tablesFacility,
                                                  tf -> tf.addTable(client.getCtx(),
                                                                    data,
                                                                    table.getName(),
                                                                    table.createTable()))
                                              .handleServiceOrAccess("Cannot add table to " + this)
                                              .get();

        Collection<FileAnnotationData> tables = ExceptionHandler.of(tablesFacility,
                                                                    tf -> tf.getAvailableTables(client.getCtx(),
                                                                                                data))
                                                                .handleServiceOrAccess("Cannot add table to " + this)
                                                                .get();
        long fileId = tableData.getOriginalFileId();

        long id = tables.stream().filter(v -> v.getFileID() == fileId)
                        .mapToLong(DataObject::getId).max().orElse(-1L);
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
    public void addAndReplaceTable(Client client, TableWrapper table, ReplacePolicy policy)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        Collection<FileAnnotationWrapper> tables = wrap(ExceptionHandler.of(client.getTablesFacility(),
                                                                            t -> t.getAvailableTables(
                                                                                    client.getCtx(), data))
                                                                        .handleServiceOrAccess("Cannot get tables from "
                                                                                               + this)
                                                                        .get(),
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
     * @throws OMEROServerError     Server error.
     */
    public void addAndReplaceTable(Client client, TableWrapper table)
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
    public TableWrapper getTable(Client client, Long fileId)
    throws ServiceException, AccessException, ExecutionException {
        TableData info = ExceptionHandler.of(client.getTablesFacility(), tf -> tf.getTableInfo(client.getCtx(), fileId))
                                         .handleServiceOrAccess("Cannot get table from " + this)
                                         .get();
        long nRows = info.getNumberOfRows();
        TableData table = ExceptionHandler.of(client.getTablesFacility(),
                                              tf -> tf.getTable(client.getCtx(), fileId, 0, nRows - 1))
                                          .handleServiceOrAccess("Cannot get table from " + this)
                                          .get();
        String name = ExceptionHandler.of(client.getTablesFacility(),
                                          tf -> tf.getAvailableTables(client.getCtx(), data)
                                                  .stream().filter(t -> t.getFileID() == fileId)
                                                  .map(FileAnnotationData::getDescription)
                                                  .findFirst().orElse(null))
                                      .handleServiceOrAccess("Cannot get table name from " + this)
                                      .get();
        TableWrapper result = new TableWrapper(Objects.requireNonNull(table));
        result.setName(name);
        return result;
    }


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
    public List<TableWrapper> getTables(Client client)
    throws ServiceException, AccessException, ExecutionException {
        Collection<FileAnnotationData> tables = ExceptionHandler.of(client.getTablesFacility(),
                                                                    tf -> tf.getAvailableTables(client.getCtx(), data))
                                                                .handleServiceOrAccess("Cannot get tables from " + this)
                                                                .get();

        List<TableWrapper> tablesWrapper = new ArrayList<>(tables.size());
        for (FileAnnotationData table : tables) {
            TableWrapper tableWrapper = getTable(client, table.getFileID());
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
     * @throws OMEROServerError     Server error.
     */
    public long addAndReplaceFile(Client client, File file, ReplacePolicy policy)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, OMEROServerError {
        List<FileAnnotationWrapper> files = getFileAnnotations(client);

        FileAnnotationData uploaded = client.getDm().attachFile(client.getCtx(),
                                                                file,
                                                                null,
                                                                "",
                                                                file.getName(),
                                                                data).get();
        FileAnnotationWrapper annotation = new FileAnnotationWrapper(uploaded);

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
     * @throws OMEROServerError     Server error.
     */
    public long addAndReplaceFile(Client client, File file)
    throws ExecutionException, InterruptedException, AccessException, ServiceException, OMEROServerError {
        return addAndReplaceFile(client, file, ReplacePolicy.DELETE_ORPHANED);
    }


    /**
     * @param client     The client handling the connection.
     * @param annotation FileAnnotationWrapper to link.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Links a file annotation to the object. Use {@link #link} instead.
     */
    @Deprecated
    public void addFileAnnotation(Client client, FileAnnotationWrapper annotation)
    throws AccessException, ServiceException, ExecutionException {
        link(client, annotation);
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
        String error = "Cannot retrieve file annotations from " + this;

        List<Class<? extends AnnotationData>> types = Collections.singletonList(FileAnnotationData.class);

        List<AnnotationData> annotations = ExceptionHandler.of(client.getMetadata(),
                                                               m -> m.getAnnotations(client.getCtx(),
                                                                                     data,
                                                                                     types,
                                                                                     null))
                                                           .handleServiceOrAccess(error)
                                                           .get();

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
     * @throws OMEROServerError     Server error.
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
     * @throws OMEROServerError     Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    protected void removeLink(Client client, String linkType, long childId)
    throws ServiceException, OMEROServerError, AccessException, ExecutionException, InterruptedException {
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
    private List<AnnotationData> getAnnotationData(Client client)
    throws AccessException, ServiceException, ExecutionException {
        return ExceptionHandler.of(client.getMetadata(), m -> m.getAnnotations(client.getCtx(), data))
                               .handleServiceOrAccess("Cannot get annotations from " + this)
                               .get();
    }


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
    public AnnotationList getAnnotations(Client client)
    throws AccessException, ServiceException, ExecutionException {
        List<AnnotationData> annotationData = getAnnotationData(client);
        AnnotationList       annotations    = new AnnotationList(annotationData.size());
        annotationData.forEach(annotations::add);
        return annotations;
    }


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
    public void copyAnnotationLinks(Client client, AnnotatableWrapper<?> object)
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
