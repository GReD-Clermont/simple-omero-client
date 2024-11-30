/*
 *  Copyright (C) 2020-2024 GReD
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
import fr.igred.omero.annotations.AnnotationList;
import fr.igred.omero.annotations.FileAnnotation;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotation;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.RatingAnnotation;
import fr.igred.omero.annotations.RatingAnnotationWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.ReplacePolicy;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.TableData;
import omero.gateway.model.TagAnnotationData;
import omero.model.IObject;
import omero.model.TagAnnotationI;
import omero.sys.ParametersI;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class AnnotatableWrapper<T extends DataObject> extends ObjectWrapper<T> implements Annotatable {

    /**
     * Constructor of the class RepositoryObjectWrapper.
     *
     * @param o The annotatable DataObject to wrap in the RepositoryObjectWrapper.
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
    @Override
    public <A extends Annotation> boolean isLinked(Browser browser, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        return getAnnotations(browser).stream().anyMatch(a -> a.getId() == annotation.getId());
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
        List<Class<? extends AnnotationData>> types = singletonList(TagAnnotationData.class);

        List<AnnotationData> annotations = call(browser.getMetadataFacility(),
                                                m -> m.getAnnotations(browser.getCtx(),
                                                                      data,
                                                                      types,
                                                                      null),
                                                "Cannot get tags for " + this);

        return annotations.stream()
                          .filter(TagAnnotationData.class::isInstance)
                          .map(TagAnnotationData.class::cast)
                          .map(TagAnnotationWrapper::new)
                          .sorted(Comparator.comparing(TagAnnotation::getId))
                          .collect(toList());
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
        List<Class<? extends AnnotationData>> types = singletonList(MapAnnotationData.class);
        List<AnnotationData> annotations = call(browser.getMetadataFacility(),
                                                m -> m.getAnnotations(browser.getCtx(),
                                                                      data,
                                                                      types,
                                                                      null),
                                                "Cannot get map annotations for "
                                                + this);

        return annotations.stream()
                          .filter(MapAnnotationData.class::isInstance)
                          .map(MapAnnotationData.class::cast)
                          .map(MapAnnotationWrapper::new)
                          .sorted(Comparator.comparing(MapAnnotation::getId))
                          .collect(toList());
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
     * Returns all the ratings from the specified user IDs for this object.
     *
     * @param browser The data browser.
     * @param userIds List of user IDs (can be null, i. e. all users).
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<RatingAnnotation> getRatings(Browser browser, List<Long> userIds)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot retrieve rating annotations from " + this;

        List<Class<? extends AnnotationData>> types = singletonList(RatingAnnotationData.class);

        List<AnnotationData> annotations = call(browser.getMetadataFacility(),
                                                m -> m.getAnnotations(browser.getCtx(),
                                                                      data,
                                                                      types,
                                                                      userIds),
                                                error);
        annotations = annotations == null ? Collections.emptyList() : annotations;
        return annotations.stream()
                          .filter(RatingAnnotationData.class::isInstance)
                          .map(RatingAnnotationData.class::cast)
                          .map(RatingAnnotationWrapper::new)
                          .sorted(Comparator.comparing(RatingAnnotation::getId))
                          .collect(toList());
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
        List<Long> userIds = singletonList(client.getCtx().getExperimenter());

        List<RatingAnnotation> ratings = getRatings(client, userIds);
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
        List<Long> userIds = singletonList(browser.getCtx().getExperimenter());

        List<RatingAnnotation> ratings = getRatings(browser, userIds);
        int                    score   = 0;
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
    public void addAndReplaceTable(Client client, TableWrapper table, ReplacePolicy policy)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        String error = "Cannot add table to " + this;
        Collection<FileAnnotation> tables = wrap(call(client.getTablesFacility(),
                                                      t -> t.getAvailableTables(client.getCtx(),
                                                                                data),
                                                      error),
                                                 FileAnnotationWrapper::new);
        addTable(client, table);
        tables.removeIf(t -> !t.getDescription().equals(table.getName()));
        this.unlink(client, tables);
        for (FileAnnotation fileAnnotation : tables) {
            if (policy == ReplacePolicy.DELETE
                || policy == ReplacePolicy.DELETE_ORPHANED
                   && fileAnnotation.countAnnotationLinks(client) == 0) {
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
    public TableWrapper getTable(DataManager dm, Long fileId)
    throws ServiceException, AccessException, ExecutionException {
        TableData info = call(dm.getTablesFacility(),
                              tf -> tf.getTableInfo(dm.getCtx(), fileId),
                              "Cannot get table from " + this);
        long nRows = info.getNumberOfRows();
        TableData table = call(dm.getTablesFacility(),
                               tf -> tf.getTable(dm.getCtx(), fileId,
                                                 0, nRows - 1),
                               "Cannot get table from " + this);
        String name = call(dm.getTablesFacility(),
                           tf -> tf.getAvailableTables(dm.getCtx(), data)
                                   .stream()
                                   .filter(t -> t.getFileID() == fileId)
                                   .map(FileAnnotationData::getDescription)
                                   .findFirst()
                                   .orElse(null),
                           "Cannot get table name from " + this);
        TableWrapper result = new TableWrapper(Objects.requireNonNull(table));
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

        FileAnnotationData uploaded = client.getDMFacility()
                                            .attachFile(client.getCtx(),
                                                        file,
                                                        null,
                                                        "",
                                                        file.getName(),
                                                        data)
                                            .get();
        FileAnnotation annotation = new FileAnnotationWrapper(uploaded);

        files.removeIf(fileAnnotation -> !fileAnnotation.getFileName().equals(annotation.getFileName()));
        for (FileAnnotation fileAnnotation : files) {
            this.unlink(client, fileAnnotation);
            if (policy == ReplacePolicy.DELETE
                || policy == ReplacePolicy.DELETE_ORPHANED
                   && fileAnnotation.countAnnotationLinks(client) == 0) {
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
        String error = "Cannot retrieve file annotations from " + this;

        List<Class<? extends AnnotationData>> types = singletonList(FileAnnotationData.class);

        List<AnnotationData> annotations = call(browser.getMetadataFacility(),
                                                m -> m.getAnnotations(browser.getCtx(),
                                                                      data,
                                                                      types,
                                                                      null),
                                                error);

        return annotations.stream()
                          .filter(FileAnnotationData.class::isInstance)
                          .map(FileAnnotationData.class::cast)
                          .map(FileAnnotationWrapper::new)
                          .collect(toList());
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
     * Unlinks the given annotations from the current object.
     *
     * @param client      The client handling the connection.
     * @param annotations List of annotations
     * @param <A>         The type of the annotation.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    public <A extends Annotation> void unlink(Client client, Collection<A> annotations)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        removeLinks(client,
                    annotationLinkType(),
                    annotations.stream()
                               .map(A::getId)
                               .collect(toList()));
    }


    /**
     * Removes the link of the given type with the given child IDs.
     *
     * @param client   The client handling the connection.
     * @param linkType The link type.
     * @param childIds List of link child IDs.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    protected void removeLinks(Client client, String linkType, Collection<Long> childIds)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        String template = "select link from %s link" +
                          " where link.parent = %d" +
                          " and link.child.id in (:ids)";
        String      query = String.format(template, linkType, getId());
        ParametersI param = new ParametersI();
        param.addIds(childIds);
        List<IObject> os = call(client.getGateway(),
                                g -> g.getQueryService(client.getCtx())
                                      .findAllByQuery(query, param),
                                "Cannot get links from " + this);
        client.delete(os);
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
        removeLinks(client, linkType, singletonList(childId));
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
    public AnnotationList getAnnotations(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        List<AnnotationData> annotationData = getAnnotationData(browser);

        AnnotationList annotations = new AnnotationList(annotationData.size());
        annotationData.forEach(annotations::add);
        return annotations;
    }

}
