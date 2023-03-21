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
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotation;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.RatingAnnotation;
import fr.igred.omero.annotations.RatingAnnotationWrapper;
import fr.igred.omero.annotations.Table;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.ReplacePolicy;
import fr.igred.omero.util.Wrapper;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.TableData;
import omero.gateway.model.TagAnnotationData;
import omero.model.IObject;
import omero.model.TagAnnotationI;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;


/**
 * Generic class containing an annotatable DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class AnnotatableWrapper<T extends DataObject> extends ObjectWrapper<T> implements Annotatable {

    /**
     * Constructor of the class AnnotatableWrapper.
     *
     * @param o The annotatable DataObject to wrap in the AnnotatableWrapper.
     */
    protected AnnotatableWrapper(T o) {
        super(o);
    }


    /**
     * Retrieves annotations of a given type linked to the object, for specific users.
     *
     * @param browser The data browser.
     * @param type    The type of annotation.
     * @param userIds The user IDs.
     *
     * @return A list of annotations, as AnnotationData.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    private <A extends AnnotationData> List<A> getAnnotations(Browser browser,
                                                              Class<? extends A> type,
                                                              List<Long> userIds)
    throws AccessException, ServiceException, ExecutionException {
        List<Class<? extends AnnotationData>> types = Collections.singletonList(type);
        return ExceptionHandler.of(browser.getMetadataFacility(),
                                   m -> m.getAnnotations(browser.getCtx(), data, types, userIds))
                               .handleOMEROException("Cannot get annotations for " + this)
                               .get()
                               .stream()
                               .filter(type::isInstance)
                               .map(type::cast)
                               .collect(toList());
    }


    /**
     * Retrieves annotations of a given type linked to the object.
     *
     * @param browser The data browser.
     * @param type    The type of annotation.
     *
     * @return A list of annotations, as AnnotationData.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    private <A extends AnnotationData> List<A> getAnnotations(Browser browser, Class<? extends A> type)
    throws AccessException, ServiceException, ExecutionException {
        return getAnnotations(browser, type, null);
    }


    /**
     * Returns the type of annotation link for this object.
     *
     * @return See above.
     */
    protected abstract String annotationLinkType();


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
    @Override
    public void addTag(DataManager dm, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotation tag = new TagAnnotationWrapper(new TagAnnotationData(name));
        tag.setDescription(description);
        link(dm, tag);
    }


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
    @Override
    public void addTag(DataManager dm, Long id)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotationI    tag     = new TagAnnotationI(id, false);
        TagAnnotationData tagData = new TagAnnotationData(tag);
        link(dm, tagData);
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
    @Override
    public List<TagAnnotation> getTags(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<TagAnnotationData> tags = getAnnotations(browser, TagAnnotationData.class);
        return wrap(tags, TagAnnotationWrapper::new);
    }


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
    @Override
    public List<MapAnnotation> getMapAnnotations(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<MapAnnotationData> maps = getAnnotations(browser, MapAnnotationData.class);
        return wrap(maps, MapAnnotationWrapper::new);
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
    @Override
    public void addKeyValuePair(DataManager dm, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        MapAnnotation pkv = new MapAnnotationWrapper(key, value);
        link(dm, pkv);
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
    @Override
    public void rate(Client client, int rating)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        List<Long> uids = Collections.singletonList(client.getCtx().getExperimenter());

        List<RatingAnnotationData> rad = getAnnotations(client, RatingAnnotationData.class, uids);

        List<RatingAnnotation> ratings = wrap(rad, RatingAnnotationWrapper::new);
        if (ratings.isEmpty()) {
            RatingAnnotation rate = new RatingAnnotationWrapper(rating);
            link(client, rate);
        } else {
            int n = ratings.size();
            if (n > 1) {
                client.delete(ratings.subList(1, n));
            }
            RatingAnnotation rate = ratings.get(0);
            rate.setRating(rating);
            rate.saveAndUpdate(client);
        }
    }


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
    @Override
    public int getMyRating(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<Long> uids = Collections.singletonList(browser.getCtx().getExperimenter());

        List<RatingAnnotationData> rad = getAnnotations(browser, RatingAnnotationData.class, uids);

        List<RatingAnnotation> ratings = wrap(rad, RatingAnnotationWrapper::new);

        int score = 0;
        for (RatingAnnotation rate : ratings) {
            score += rate.getRating();
        }
        return score / Math.max(1, ratings.size());
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
    @Override
    public void addAndReplaceTable(Client client, Table table, ReplacePolicy policy)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        String error = "Cannot get tables from " + this;
        Collection<FileAnnotation> tables = wrap(ExceptionHandler.of(client.getTablesFacility(),
                                                                     t -> t.getAvailableTables(client.getCtx(), data))
                                                                 .handleOMEROException(error)
                                                                 .get(),
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
    @Override
    public Table getTable(DataManager dm, Long fileId)
    throws ServiceException, AccessException, ExecutionException {
        TableData table = ExceptionHandler.of(dm.getTablesFacility(),
                                              tf -> tf.getTable(dm.getCtx(), fileId))
                                          .handleOMEROException("Cannot get table from " + this)
                                          .get();
        String name = ExceptionHandler.of(dm.getTablesFacility(),
                                          tf -> tf.getAvailableTables(dm.getCtx(), data)
                                                  .stream().filter(t -> t.getFileID() == fileId)
                                                  .map(FileAnnotationData::getDescription)
                                                  .findFirst().orElse(null))
                                      .handleOMEROException("Cannot get table name from " + this)
                                      .get();
        Table result = new TableWrapper(Objects.requireNonNull(table));
        result.setName(name);
        return result;
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
    @Override
    public long addAndReplaceFile(Client client, File file, ReplacePolicy policy)
    throws ExecutionException, InterruptedException, AccessException, ServiceException {
        List<FileAnnotation> files = getFileAnnotations(client);

        FileAnnotationData uploaded = client.getDMFacility().attachFile(client.getCtx(),
                                                                        file,
                                                                        null,
                                                                        "",
                                                                        file.getName(),
                                                                        data).get();
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
    @Override
    public List<FileAnnotation> getFileAnnotations(Browser browser)
    throws ExecutionException, ServiceException, AccessException {
        List<FileAnnotationData> files = getAnnotations(browser, FileAnnotationData.class);
        return wrap(files, FileAnnotationWrapper::new);
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
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    public <A extends Annotation> void unlink(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
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
     * @throws InterruptedException If block(long) does not return.
     */
    protected void removeLink(Client client, String linkType, long childId)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        String query = "select link from " + linkType +
                       " link where link.parent = " + getId() +
                       " and link.child = " + childId;
        List<IObject> os = client.findByQuery(query);
        client.delete(os.iterator().next());
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
    @Override
    public List<Annotation> getAnnotations(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        List<AnnotationData> annotationData = getAnnotationData(browser);
        List<Annotation>     annotations    = new ArrayList<>(annotationData.size());
        annotationData.forEach(a -> annotations.add(Wrapper.wrap(a)));
        return annotations;
    }

}
