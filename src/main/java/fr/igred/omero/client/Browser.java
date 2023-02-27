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

package fr.igred.omero.client;


import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.annotations.AnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.containers.DatasetWrapper;
import fr.igred.omero.containers.FolderWrapper;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.screen.PlateWrapper;
import fr.igred.omero.containers.ProjectWrapper;
import fr.igred.omero.screen.ScreenWrapper;
import fr.igred.omero.screen.WellWrapper;
import omero.RLong;
import omero.ServerError;
import omero.api.IQueryPrx;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.MetadataFacility;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.ObjectWrapper.flatten;
import static fr.igred.omero.ObjectWrapper.wrap;


/**
 * Interface to browse data on an OMERO server in a given {@link SecurityContext}.
 */
@SuppressWarnings("ClassWithTooManyMethods")
//Fewer methods than counted because of polymorphism.
public interface Browser {

    /**
     * Returns the current {@link SecurityContext}.
     *
     * @return See above
     */
    SecurityContext getCtx();


    /**
     * Gets the {@link BrowseFacility} used to access the data from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    BrowseFacility getBrowseFacility() throws ExecutionException;


    /**
     * Returns the {@link IQueryPrx} used to find objects on OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    IQueryPrx getQueryService() throws ServiceException;


    /**
     * Gets the {@link MetadataFacility} used to retrieve annotations from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the MetadataFacility can't be retrieved or instantiated.
     */
    MetadataFacility getMetadata() throws ExecutionException;


    /**
     * Finds objects on OMERO through a database query.
     *
     * @param query The database query.
     *
     * @return A list of OMERO objects.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    default List<IObject> findByQuery(String query) throws ServiceException, ServerException {
        String error = "Query failed: " + query;
        return ExceptionHandler.of(getQueryService(),
                                   qs -> qs.findAllByQuery(query, null))
                               .rethrow(ServerError.class, ServerException::new, error)
                               .get();
    }


    /**
     * Gets the project with the specified id from OMERO.
     *
     * @param id ID of the project.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default ProjectWrapper getProject(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<ProjectWrapper> projects = getProjects(id);
        if (projects.isEmpty()) {
            throw new NoSuchElementException(String.format("Project %d doesn't exist in this context", id));
        }
        return projects.iterator().next();
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
    default List<ProjectWrapper> getProjects(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get projects with IDs: " + Arrays.toString(ids);
        Collection<ProjectData> projects = ExceptionHandler.of(getBrowseFacility(),
                                                               bf -> bf.getProjects(getCtx(), Arrays.asList(ids)))
                                                           .handleServiceOrAccess(error)
                                                           .get();
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
    default List<ProjectWrapper> getProjects() throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = ExceptionHandler.of(getBrowseFacility(),
                                                               bf -> bf.getProjects(getCtx()))
                                                           .handleServiceOrAccess("Cannot get projects")
                                                           .get();
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
    default List<ProjectWrapper> getProjects(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        String error = String.format("Cannot get projects for user %s", experimenter);
        Collection<ProjectData> projects = ExceptionHandler.of(getBrowseFacility(),
                                                               bf -> bf.getProjects(getCtx(), experimenter.getId()))
                                                           .handleServiceOrAccess(error)
                                                           .get();
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
    default List<ProjectWrapper> getProjects(String name) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get projects with name: " + name;
        Collection<ProjectData> projects = ExceptionHandler.of(getBrowseFacility(),
                                                               bf -> bf.getProjects(getCtx(), name))
                                                           .handleServiceOrAccess(error)
                                                           .get();
        return wrap(projects, ProjectWrapper::new);
    }


    /**
     * Gets the dataset with the specified id from OMERO.
     *
     * @param id ID of the dataset.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default DatasetWrapper getDataset(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<DatasetWrapper> datasets = getDatasets(id);
        if (datasets.isEmpty()) {
            throw new NoSuchElementException(String.format("Dataset %d doesn't exist in this context", id));
        }
        return datasets.iterator().next();
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
    default List<DatasetWrapper> getDatasets(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get dataset with ID: " + Arrays.toString(ids);
        Collection<DatasetData> datasets = ExceptionHandler.of(getBrowseFacility(),
                                                               bf -> bf.getDatasets(getCtx(), Arrays.asList(ids)))
                                                           .handleServiceOrAccess(error)
                                                           .get();
        return wrap(datasets, DatasetWrapper::new);
    }


    /**
     * Gets all datasets available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<DatasetWrapper> getDatasets()
    throws ServiceException, AccessException, ServerException, ExecutionException {
        Long[] ids = this.findByQuery("select d from Dataset d")
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getDatasets(ids);
    }


    /**
     * Gets all datasets available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<DatasetWrapper> getDatasets(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        String query = String.format("select d from Dataset d where d.details.owner.id=%d", experimenter.getId());
        Long[] ids = this.findByQuery(query)
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getDatasets(ids);
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
    default List<DatasetWrapper> getDatasets(String name) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get datasets with name: " + name;
        Collection<DatasetData> datasets = ExceptionHandler.of(getBrowseFacility(),
                                                               bf -> bf.getDatasets(getCtx(), name))
                                                           .handleServiceOrAccess(error)
                                                           .get();
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
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default ImageWrapper getImage(Long id)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get image with ID: " + id;
        ImageData image = ExceptionHandler.of(getBrowseFacility(),
                                              bf -> bf.getImage(getCtx(), id))
                                          .handleServiceOrAccess(error)
                                          .get();
        if (image == null) {
            throw new NoSuchElementException(String.format("Image %d doesn't exist in this context", id));
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
    default List<ImageWrapper> getImages(Long... ids) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with IDs: " + Arrays.toString(ids);
        Collection<ImageData> images = ExceptionHandler.of(getBrowseFacility(),
                                                           bf -> bf.getImages(getCtx(), Arrays.asList(ids)))
                                                       .handleServiceOrAccess(error)
                                                       .get();
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
    default List<ImageWrapper> getImages() throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = ExceptionHandler.of(getBrowseFacility(),
                                                           bf -> bf.getUserImages(getCtx()))
                                                       .handleServiceOrAccess("Cannot get images")
                                                       .get();
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
    default List<ImageWrapper> getImages(String name) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with name: " + name;
        Collection<ImageData> images = ExceptionHandler.of(getBrowseFacility(),
                                                           bf -> bf.getImages(getCtx(), name))
                                                       .handleServiceOrAccess(error)
                                                       .get();
        return wrap(images, ImageWrapper::new);
    }


    /**
     * Gets all images with the name specified inside projects and datasets with the given names.
     *
     * @param projectName Expected project name.
     * @param datasetName Expected dataset name.
     * @param imageName   Expected image name.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ImageWrapper> getImages(String projectName, String datasetName, String imageName)
    throws ServiceException, AccessException, ExecutionException {
        List<ProjectWrapper> projects = getProjects(projectName);

        Collection<List<ImageWrapper>> lists = new ArrayList<>(projects.size());
        for (ProjectWrapper project : projects) {
            lists.add(project.getImages(this, datasetName, imageName));
        }

        return flatten(lists);
    }


    /**
     * Gets all images with the specified annotation from OMERO.
     *
     * @param annotation TagAnnotation containing the tag researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ImageWrapper> getImages(AnnotationWrapper<?> annotation)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        return annotation.getImages(this);
    }


    /**
     * Gets all images with a certain motif in their name from OMERO.
     *
     * @param motif Motif searched in an image name.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ImageWrapper> getImagesLike(String motif) throws ServiceException, AccessException, ExecutionException {
        List<ImageWrapper> images = getImages();
        String             regexp = ".*" + motif + ".*";
        images.removeIf(image -> !image.getName().matches(regexp));
        return images;
    }


    /**
     * Gets all images with a certain key.
     *
     * @param key Name of the key researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ImageWrapper> getImagesWithKey(String key)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        List<MapAnnotationWrapper> maps = getMapAnnotations(key);

        Collection<Collection<ImageWrapper>> selected = new ArrayList<>(maps.size());
        for (MapAnnotationWrapper map : maps) {
            selected.add(getImages(map));
        }

        return flatten(selected);
    }


    /**
     * Gets all images with a certain key value pair from OMERO
     *
     * @param key   Name of the key researched.
     * @param value Value associated with the key.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ImageWrapper> getImagesWithKeyValuePair(String key, String value)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        List<MapAnnotationWrapper> maps = getMapAnnotations(key, value);

        Collection<Collection<ImageWrapper>> selected = new ArrayList<>(maps.size());
        for (MapAnnotationWrapper map : maps) {
            selected.add(getImages(map));
        }

        return flatten(selected);
    }


    /**
     * Gets the screen with the specified id from OMERO.
     *
     * @param id ID of the screen.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default ScreenWrapper getScreen(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<ScreenWrapper> screens = getScreens(id);
        if (screens.isEmpty()) {
            throw new NoSuchElementException(String.format("Screen %d doesn't exist in this context", id));
        }
        return screens.iterator().next();
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
    default List<ScreenWrapper> getScreens(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = ExceptionHandler.of(getBrowseFacility(),
                                                             bf -> bf.getScreens(getCtx(), Arrays.asList(ids)))
                                                         .handleServiceOrAccess("Cannot get screens with IDs: "
                                                                                + Arrays.toString(ids))
                                                         .get();
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
    default List<ScreenWrapper> getScreens() throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = ExceptionHandler.of(getBrowseFacility(),
                                                             bf -> bf.getScreens(getCtx()))
                                                         .handleServiceOrAccess("Cannot get screens")
                                                         .get();
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
    default List<ScreenWrapper> getScreens(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        String error = String.format("Cannot get screens for user %s", experimenter);
        Collection<ScreenData> screens = ExceptionHandler.of(getBrowseFacility(),
                                                             bf -> bf.getScreens(getCtx(), experimenter.getId()))
                                                         .handleServiceOrAccess(error)
                                                         .get();
        return wrap(screens, ScreenWrapper::new);
    }


    /**
     * Gets the plate with the specified id from OMERO.
     *
     * @param id ID of the plate.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default PlateWrapper getPlate(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<PlateWrapper> plates = getPlates(id);
        if (plates.isEmpty()) {
            throw new NoSuchElementException(String.format("Plate %d doesn't exist in this context", id));
        }
        return plates.iterator().next();
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
    default List<PlateWrapper> getPlates(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = ExceptionHandler.of(getBrowseFacility(),
                                                           bf -> bf.getPlates(getCtx(), Arrays.asList(ids)))
                                                       .handleServiceOrAccess("Cannot get plates with IDs: "
                                                                              + Arrays.toString(ids))
                                                       .get();
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
    default List<PlateWrapper> getPlates() throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = ExceptionHandler.of(getBrowseFacility(),
                                                           bf -> bf.getPlates(getCtx()))
                                                       .handleServiceOrAccess("Cannot get plates")
                                                       .get();
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
    default List<PlateWrapper> getPlates(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = ExceptionHandler.of(getBrowseFacility(),
                                                           bf -> bf.getPlates(getCtx(), experimenter.getId()))
                                                       .handleServiceOrAccess("Cannot get plates for user "
                                                                              + experimenter)
                                                       .get();
        return wrap(plates, PlateWrapper::new);
    }


    /**
     * Gets the well with the specified id from OMERO.
     *
     * @param id ID of the well.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default WellWrapper getWell(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<WellWrapper> wells = getWells(id);
        if (wells.isEmpty()) {
            throw new NoSuchElementException(String.format("Plate %d doesn't exist in this context", id));
        }
        return wells.iterator().next();
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
    default List<WellWrapper> getWells(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get wells with IDs: " + Arrays.toString(ids);
        Collection<WellData> wells = ExceptionHandler.of(getBrowseFacility(),
                                                         bf -> bf.getWells(getCtx(), Arrays.asList(ids)))
                                                     .handleServiceOrAccess(error)
                                                     .get();
        return wrap(wells, WellWrapper::new);
    }


    /**
     * Gets all wells available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    default List<WellWrapper> getWells()
    throws ServiceException, AccessException, ExecutionException, ServerException {
        Long[] ids = this.findByQuery("select w from Well w")
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getWells(ids);
    }


    /**
     * Gets all wells available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    default List<WellWrapper> getWells(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        String query = String.format("select w from Well w where w.details.owner.id=%d", experimenter.getId());
        Long[] ids = this.findByQuery(query)
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getWells(ids);
    }


    /**
     * Gets the folder with the specified ID from OMERO, fully loaded.
     *
     * @param id ID of the folder.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default FolderWrapper getFolder(long id)
    throws ServiceException, AccessException, ExecutionException {
        List<FolderWrapper> folders = loadFolders(id);
        if (folders.isEmpty()) {
            throw new NoSuchElementException(String.format("Folder %d doesn't exist in this context", id));
        }
        return folders.iterator().next();
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
    default List<FolderWrapper> getFolders()
    throws ExecutionException, AccessException, ServiceException {
        Collection<FolderData> folders = ExceptionHandler.of(getBrowseFacility(),
                                                             b -> b.getFolders(getCtx()))
                                                         .handleServiceOrAccess("Cannot get folders")
                                                         .get();
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
    default List<FolderWrapper> getFolders(ExperimenterWrapper experimenter)
    throws ExecutionException, AccessException, ServiceException {
        String error = String.format("Cannot get folders for user %s", experimenter);
        Collection<FolderData> folders = ExceptionHandler.of(getBrowseFacility(),
                                                             b -> b.getFolders(getCtx(), experimenter.getId()))
                                                         .handleServiceOrAccess(error)
                                                         .get();
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
    default List<FolderWrapper> loadFolders(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get folders with IDs: " + Arrays.toString(ids);
        Collection<FolderData> folders = ExceptionHandler.of(getBrowseFacility(),
                                                             bf -> bf.loadFolders(getCtx(), Arrays.asList(ids)))
                                                         .handleServiceOrAccess(error)
                                                         .get();
        return wrap(folders, FolderWrapper::new);
    }


    /**
     * Gets the list of tag annotations available to the user.
     *
     * @return See above.
     *
     * @throws ServerException  Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    default List<TagAnnotationWrapper> getTags() throws ServerException, ServiceException {
        return ExceptionHandler.of(getQueryService(),
                                   qs -> qs.findAll(TagAnnotation.class.getSimpleName(), null))
                               .handleServiceOrServer("Cannot get tags")
                               .get()
                               .stream()
                               .map(TagAnnotation.class::cast)
                               .map(TagAnnotationData::new)
                               .map(TagAnnotationWrapper::new)
                               .sorted(Comparator.comparing(ObjectWrapper::getId))
                               .collect(Collectors.toList());
    }


    /**
     * Gets the list of tag annotations with the specified name available to the user.
     *
     * @param name Name of the tag searched.
     *
     * @return See above.
     *
     * @throws ServerException  Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    default List<TagAnnotationWrapper> getTags(String name) throws ServerException, ServiceException {
        String query = String.format("select t from TagAnnotation as t where t.textValue = '%s'", name);
        return findByQuery(query).stream()
                                 .map(omero.model.TagAnnotation.class::cast)
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
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default TagAnnotationWrapper getTag(Long id) throws ServiceException, ExecutionException, AccessException {
        TagAnnotationData tag = ExceptionHandler.of(getBrowseFacility(),
                                                    b -> b.findObject(getCtx(), TagAnnotationData.class, id))
                                                .handleServiceOrAccess("Cannot get tag with ID: " + id)
                                                .get();
        tag.setNameSpace(tag.getContentAsString());

        return new TagAnnotationWrapper(tag);
    }


    /**
     * Gets the list of map annotations available to the user.
     *
     * @return See above.
     *
     * @throws ServerException  Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    default List<MapAnnotationWrapper> getMapAnnotations() throws ServerException, ServiceException {
        return ExceptionHandler.of(getQueryService(),
                                   qs -> qs.findAll(omero.model.MapAnnotation.class.getSimpleName(), null))
                               .handleServiceOrServer("Cannot get map annotations")
                               .get()
                               .stream()
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
     * @throws ServerException  Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    default List<MapAnnotationWrapper> getMapAnnotations(String key) throws ServerException, ServiceException {
        String q = String.format("select m from MapAnnotation as m join m.mapValue as mv where mv.name = '%s'", key);
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
     * @throws ServerException  Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    default List<MapAnnotationWrapper> getMapAnnotations(String key, String value)
    throws ServerException, ServiceException {
        String q = String.format("select m from MapAnnotation as m join m.mapValue as mv " +
                                 "where mv.name = '%s' and mv.value = '%s'", key, value);
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
    default MapAnnotationWrapper getMapAnnotation(Long id) throws ServiceException, ExecutionException, AccessException {
        MapAnnotationData kv = ExceptionHandler.of(getBrowseFacility(), b -> b.findObject(getCtx(),
                                                                                          MapAnnotationData.class,
                                                                                          id))
                                               .handleServiceOrAccess("Cannot get map annotation with ID: " + id)
                                               .get();

        return new MapAnnotationWrapper(kv);
    }

}
