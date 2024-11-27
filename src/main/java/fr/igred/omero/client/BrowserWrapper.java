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

package fr.igred.omero.client;


import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.containers.DatasetWrapper;
import fr.igred.omero.containers.FolderWrapper;
import fr.igred.omero.containers.ProjectWrapper;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.screen.PlateWrapper;
import fr.igred.omero.screen.ScreenWrapper;
import fr.igred.omero.screen.WellWrapper;
import omero.gateway.SecurityContext;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FolderData;
import omero.gateway.model.ImageData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.WellData;
import omero.model.IObject;
import omero.model.TagAnnotation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.ObjectWrapper.wrap;
import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;


/**
 * Abstract class to browse data on an OMERO server in a given {@link SecurityContext} and wrap DataObjects.
 */
public abstract class BrowserWrapper implements Browser {


    /**
     * Abstract constructor of the BrowserWrapper class.
     */
    protected BrowserWrapper() {
    }


    /**
     * Gets the projects with the specified ids from OMERO.
     *
     * @param ids Project IDs
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ProjectWrapper> getProjects(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = call(getBrowseFacility(),
                                                bf -> bf.getProjects(getCtx(),
                                                                     asList(ids)),
                                                "Cannot get projects with IDs: "
                                                + Arrays.toString(ids));
        return wrap(projects, ProjectWrapper::new);
    }


    /**
     * Gets all projects available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ProjectWrapper> getProjects()
    throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = call(getBrowseFacility(),
                                                bf -> bf.getProjects(getCtx()),
                                                "Cannot get projects");
        return wrap(projects, ProjectWrapper::new);
    }


    /**
     * Gets all projects available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ProjectWrapper> getProjects(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        long exId = experimenter.getId();
        Collection<ProjectData> projects = call(getBrowseFacility(),
                                                bf -> bf.getProjects(getCtx(),
                                                                     exId),
                                                "Cannot get projects for user "
                                                + experimenter);
        return wrap(projects, ProjectWrapper::new);
    }


    /**
     * Gets all projects with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ProjectWrapper> getProjects(String name)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = call(getBrowseFacility(),
                                                bf -> bf.getProjects(getCtx(),
                                                                     name),
                                                "Cannot get projects with name: "
                                                + name);
        return wrap(projects, ProjectWrapper::new);
    }


    /**
     * Gets the datasets with the specified ids from OMERO.
     *
     * @param ids Dataset IDs
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<DatasetWrapper> getDatasets(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<DatasetData> datasets = call(getBrowseFacility(),
                                                bf -> bf.getDatasets(getCtx(),
                                                                     asList(ids)),
                                                "Cannot get datasets with IDs: "
                                                + Arrays.toString(ids));
        return wrap(datasets, DatasetWrapper::new);
    }


    /**
     * Gets all datasets with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<DatasetWrapper> getDatasets(String name)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get datasets with name: " + name;
        Collection<DatasetData> datasets = call(getBrowseFacility(),
                                                bf -> bf.getDatasets(getCtx(),
                                                                     name),
                                                error);
        return wrap(datasets, DatasetWrapper::new);
    }


    /**
     * Returns the image with the specified ID from OMERO.
     *
     * @param id ID of the image.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    @Override
    public ImageWrapper getImage(Long id)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get image with ID: " + id;
        ImageData image = call(getBrowseFacility(),
                               bf -> bf.getImage(getCtx(), id),
                               error);
        if (image == null) {
            String msg = format("Image %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
        }
        return new ImageWrapper(image);
    }


    /**
     * Gets the images with the specified ids from OMERO
     *
     * @param ids Image IDs
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ImageWrapper> getImages(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with IDs: " + Arrays.toString(ids);
        Collection<ImageData> images = call(getBrowseFacility(),
                                            bf -> bf.getImages(getCtx(),
                                                               asList(ids)),
                                            error);
        return wrap(images, ImageWrapper::new);
    }


    /**
     * Gets all images owned by the current user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ImageWrapper> getImages()
    throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = call(getBrowseFacility(),
                                            bf -> bf.getUserImages(getCtx()),
                                            "Cannot get images");
        return wrap(images, ImageWrapper::new);
    }


    /**
     * Gets all images with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ImageWrapper> getImages(String name)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with name: " + name;
        Collection<ImageData> images = call(getBrowseFacility(),
                                            bf -> bf.getImages(getCtx(), name),
                                            error);
        images.removeIf(image -> !image.getName().equals(name));
        return wrap(images, ImageWrapper::new);
    }


    /**
     * Gets all orphaned images owned by the specified user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ImageWrapper> getOrphanedImages(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        long exId = experimenter.getId();
        Collection<ImageData> images = call(getBrowseFacility(),
                                            bf -> bf.getOrphanedImages(getCtx(),
                                                                       exId),
                                            "Cannot get orphaned images");
        return wrap(images, ImageWrapper::new);
    }


    /**
     * Gets the screens with the specified ids from OMERO.
     *
     * @param ids A list of screen ids
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ScreenWrapper> getScreens(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = call(getBrowseFacility(),
                                              bf -> bf.getScreens(getCtx(),
                                                                  asList(ids)),
                                              "Cannot get screens with IDs: "
                                              + Arrays.toString(ids));
        return wrap(screens, ScreenWrapper::new);
    }


    /**
     * Gets all screens available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ScreenWrapper> getScreens()
    throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = call(getBrowseFacility(),
                                              bf -> bf.getScreens(getCtx()),
                                              "Cannot get screens");
        return wrap(screens, ScreenWrapper::new);
    }


    /**
     * Gets all screens available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ScreenWrapper> getScreens(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        String error = format("Cannot get screens for user %s", experimenter);
        long   exId  = experimenter.getId();
        Collection<ScreenData> screens = call(getBrowseFacility(),
                                              bf -> bf.getScreens(getCtx(),
                                                                  exId),
                                              error);
        return wrap(screens, ScreenWrapper::new);
    }


    /**
     * Gets the plates with the specified ids from OMERO.
     *
     * @param ids A list of plate ids
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<PlateWrapper> getPlates(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = call(getBrowseFacility(),
                                            bf -> bf.getPlates(getCtx(),
                                                               asList(ids)),
                                            "Cannot get plates with IDs: "
                                            + Arrays.toString(ids));
        return wrap(plates, PlateWrapper::new);
    }


    /**
     * Gets all plates available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<PlateWrapper> getPlates()
    throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = call(getBrowseFacility(),
                                            bf -> bf.getPlates(getCtx()),
                                            "Cannot get plates");
        return wrap(plates, PlateWrapper::new);
    }


    /**
     * Gets all plates available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<PlateWrapper> getPlates(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        long exId = experimenter.getId();
        Collection<PlateData> plates = call(getBrowseFacility(),
                                            bf -> bf.getPlates(getCtx(), exId),
                                            "Cannot get plates for user "
                                            + experimenter);
        return wrap(plates, PlateWrapper::new);
    }


    /**
     * Gets the wells with the specified ids from OMERO.
     *
     * @param ids A list of well ids
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<WellWrapper> getWells(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<WellData> wells = call(getBrowseFacility(),
                                          bf -> bf.getWells(getCtx(),
                                                            asList(ids)),
                                          "Cannot get wells with IDs: "
                                          + Arrays.toString(ids));
        return wrap(wells, WellWrapper::new);
    }


    /**
     * Gets all folders available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<FolderWrapper> getFolders()
    throws ExecutionException, AccessException, ServiceException {
        Collection<FolderData> folders = call(getBrowseFacility(),
                                              b -> b.getFolders(getCtx()),
                                              "Cannot get folders");
        return wrap(folders, FolderWrapper::new);
    }


    /**
     * Gets all the folders owned by a given user from OMERO.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<FolderWrapper> getFolders(ExperimenterWrapper experimenter)
    throws ExecutionException, AccessException, ServiceException {
        String error = format("Cannot get folders for user %s", experimenter);
        long   exId  = experimenter.getId();
        Collection<FolderData> folders = call(getBrowseFacility(),
                                              b -> b.getFolders(getCtx(), exId),
                                              error);
        return wrap(folders, FolderWrapper::new);
    }


    /**
     * Gets the folders with the specified IDs from OMERO (fully loaded).
     *
     * @param ids Project IDs
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<FolderWrapper> getFolders(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get folders with IDs: " + Arrays.toString(ids);
        Collection<FolderData> folders = call(getBrowseFacility(),
                                              bf -> bf.loadFolders(getCtx(),
                                                                   asList(ids)),
                                              error);
        return wrap(folders, FolderWrapper::new);
    }


    /**
     * Gets the list of tag annotations available to the user.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Override
    public List<TagAnnotationWrapper> getTags()
    throws AccessException, ServiceException {
        String klass = TagAnnotation.class.getSimpleName();
        List<IObject> os = call(getQueryService(),
                                qs -> qs.findAll(klass, null),
                                "Cannot get tags");
        return os.stream()
                 .map(TagAnnotation.class::cast)
                 .map(TagAnnotationData::new)
                 .map(TagAnnotationWrapper::new)
                 .sorted(Comparator.comparing(ObjectWrapper::getId))
                 .collect(Collectors.toList());
    }


    /**
     * Gets a specific tag from the OMERO database.
     *
     * @param id ID of the tag.
     *
     * @return See above.
     *
     * @throws AccessException        Cannot access data.
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws NoSuchElementException No element with this ID.
     */
    @Override
    public TagAnnotationWrapper getTag(Long id)
    throws AccessException, ServiceException {
        IObject o = call(getQueryService(),
                         qs -> qs.find(TagAnnotation.class.getSimpleName(), id),
                         "Cannot get tag ID: " + id);
        TagAnnotationData tag;
        if (o == null) {
            String msg = format("Tag %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
        } else {
            tag = new TagAnnotationData((TagAnnotation) requireNonNull(o));
        }
        return new TagAnnotationWrapper(tag);
    }


    /**
     * Gets the list of map annotations available to the user.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Override
    public List<MapAnnotationWrapper> getMapAnnotations()
    throws AccessException, ServiceException {
        String klass = omero.model.MapAnnotation.class.getSimpleName();
        List<IObject> os = call(getQueryService(),
                                qs -> qs.findAll(klass, null),
                                "Cannot get map annotations");
        return os.stream()
                 .map(omero.model.MapAnnotation.class::cast)
                 .map(MapAnnotationData::new)
                 .map(MapAnnotationWrapper::new)
                 .sorted(Comparator.comparing(ObjectWrapper::getId))
                 .collect(Collectors.toList());
    }


    /**
     * Gets the list of map annotations with the specified key available to the user.
     *
     * @param key Name of the tag searched.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Override
    public List<MapAnnotationWrapper> getMapAnnotations(String key)
    throws AccessException, ServiceException {
        String template = "select m from MapAnnotation as m" +
                          " join m.mapValue as mv" +
                          " where mv.name = '%s'";
        String q = format(template, key);
        return findByQuery(q).stream()
                             .map(omero.model.MapAnnotation.class::cast)
                             .map(MapAnnotationData::new)
                             .map(MapAnnotationWrapper::new)
                             .sorted(Comparator.comparing(ObjectWrapper::getId))
                             .collect(Collectors.toList());
    }


    /**
     * Gets the list of map annotations with the specified key and value available to the user.
     *
     * @param key   The required key.
     * @param value The required value.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Override
    public List<MapAnnotationWrapper> getMapAnnotations(String key, String value)
    throws AccessException, ServiceException {
        String template = "select m from MapAnnotation as m" +
                          " join m.mapValue as mv" +
                          " where mv.name = '%s' and mv.value = '%s'";
        String q = format(template, key, value);
        return findByQuery(q).stream()
                             .map(omero.model.MapAnnotation.class::cast)
                             .map(MapAnnotationData::new)
                             .map(MapAnnotationWrapper::new)
                             .sorted(Comparator.comparing(ObjectWrapper::getId))
                             .collect(Collectors.toList());
    }


    /**
     * Gets a specific map annotation (key/value pairs) from the OMERO database.
     *
     * @param id ID of the map annotation.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public MapAnnotationWrapper getMapAnnotation(Long id)
    throws ServiceException, ExecutionException, AccessException {
        MapAnnotationData kv = call(getBrowseFacility(),
                                    b -> b.findObject(getCtx(),
                                                      MapAnnotationData.class,
                                                      id),
                                    "Cannot get map annotation with ID: " + id);

        return new MapAnnotationWrapper(kv);
    }

}
