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


import fr.igred.omero.annotations.GenericAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.FolderWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.PlateWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.omero.repository.ScreenWrapper;
import fr.igred.omero.repository.WellWrapper;
import omero.RLong;
import omero.ServerError;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.GenericObjectWrapper.flatten;
import static fr.igred.omero.GenericObjectWrapper.wrap;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrServer;


/**
 * Abstract class to browse data on an OMERO server in a given {@link SecurityContext} and wrap DataObjects.
 */
public abstract class Browser extends GatewayWrapper {


    /**
     * Constructor of the Browser class. Initializes the gateway.
     */
    protected Browser() {
        this(null, null, null);
    }


    /**
     * Constructor of the Browser class.
     *
     * @param gateway The gateway
     * @param ctx     The security context
     * @param user    The user
     */
    protected Browser(Gateway gateway, SecurityContext ctx, ExperimenterWrapper user) {
        super(gateway, ctx, user);
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
    public ProjectWrapper getProject(Long id)
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
    public List<ProjectWrapper> getProjects(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = new ArrayList<>(0);
        try {
            projects = getBrowseFacility().getProjects(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get projects");
        }
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
    public List<ProjectWrapper> getProjects() throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = new ArrayList<>(0);
        try {
            projects = getBrowseFacility().getProjects(getCtx());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get projects");
        }
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
    public List<ProjectWrapper> getProjects(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = new ArrayList<>(0);
        try {
            projects = getBrowseFacility().getProjects(getCtx(), experimenter.getId());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get projects for user " + experimenter);
        }
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
    public List<ProjectWrapper> getProjects(String name) throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = new ArrayList<>(0);
        try {
            projects = getBrowseFacility().getProjects(getCtx(), name);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get projects with name: " + name);
        }
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
    public DatasetWrapper getDataset(Long id)
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
    public List<DatasetWrapper> getDatasets(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<DatasetData> datasets = new ArrayList<>(0);
        try {
            datasets = getBrowseFacility().getDatasets(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get datasets");
        }
        return wrap(datasets, DatasetWrapper::new);
    }


    /**
     * Gets all datasets available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<DatasetWrapper> getDatasets()
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
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
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<DatasetWrapper> getDatasets(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
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
    public List<DatasetWrapper> getDatasets(String name) throws ServiceException, AccessException, ExecutionException {
        Collection<DatasetData> datasets = new ArrayList<>(0);
        try {
            datasets = getBrowseFacility().getDatasets(getCtx(), name);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get datasets with name: " + name);
        }
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
    public ImageWrapper getImage(Long id)
    throws ServiceException, AccessException, ExecutionException {
        ImageData image = null;
        try {
            image = getBrowseFacility().getImage(getCtx(), id);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get image with ID: " + id);
        }
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
    public List<ImageWrapper> getImages(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = new ArrayList<>(0);
        try {
            images = getBrowseFacility().getImages(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get images");
        }
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
    public List<ImageWrapper> getImages() throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = new ArrayList<>(0);
        try {
            images = getBrowseFacility().getUserImages(getCtx());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get images");
        }
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
    public List<ImageWrapper> getImages(String name) throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = new ArrayList<>(0);
        try {
            images = getBrowseFacility().getImages(getCtx(), name);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get images with name: " + name);
        }
        images.removeIf(image -> !image.getName().equals(name));
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
    public abstract List<ImageWrapper> getImages(String projectName, String datasetName, String imageName)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all images with the specified annotation from OMERO.
     *
     * @param annotation TagAnnotation containing the tag researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public abstract List<ImageWrapper> getImages(GenericAnnotationWrapper<?> annotation)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException;


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
    public List<ImageWrapper> getImagesLike(String motif) throws ServiceException, AccessException, ExecutionException {
        List<ImageWrapper> images = getImages();
        String             regexp = ".*" + motif + ".*";
        images.removeIf(image -> !image.getName().matches(regexp));
        return images;
    }


    /**
     * @param tag The tag annotation.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Gets all images tagged with a specified tag from OMERO.
     */
    @Deprecated
    public List<ImageWrapper> getImagesTagged(TagAnnotationWrapper tag)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
        return getImages(tag);
    }


    /**
     * @param tagId Id of the tag researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Gets all images tagged with a specified tag from OMERO.
     */
    @Deprecated
    public List<ImageWrapper> getImagesTagged(Long tagId)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
        return getImagesTagged(getTag(tagId));
    }


    /**
     * @param key Name of the key researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Gets all images with a certain key
     */
    @Deprecated
    public abstract List<ImageWrapper> getImagesKey(String key)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all images with a certain key.
     *
     * @param key Name of the key researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesWithKey(String key)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError {
        List<MapAnnotationWrapper> maps = getMapAnnotations(key);

        Collection<Collection<ImageWrapper>> selected = new ArrayList<>(maps.size());
        for (MapAnnotationWrapper map : maps) {
            selected.add(getImages(map));
        }

        return flatten(selected);
    }


    /**
     * @param key   Name of the key researched.
     * @param value Value associated with the key.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Gets all images with a certain key value pair from OMERO
     */
    @Deprecated
    public abstract List<ImageWrapper> getImagesPairKeyValue(String key, String value)
    throws ServiceException, AccessException, ExecutionException;


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
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesWithKeyValuePair(String key, String value)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError {
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
    public ScreenWrapper getScreen(Long id)
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
    public List<ScreenWrapper> getScreens(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = new ArrayList<>(0);
        try {
            screens = getBrowseFacility().getScreens(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get screens");
        }
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
    public List<ScreenWrapper> getScreens() throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = new ArrayList<>(0);
        try {
            screens = getBrowseFacility().getScreens(getCtx());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get screens");
        }
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
    public List<ScreenWrapper> getScreens(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = new ArrayList<>(0);
        try {
            screens = getBrowseFacility().getScreens(getCtx(), experimenter.getId());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get screens for user " + experimenter);
        }
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
    public PlateWrapper getPlate(Long id)
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
    public List<PlateWrapper> getPlates(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = new ArrayList<>(0);
        try {
            plates = getBrowseFacility().getPlates(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get plates");
        }
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
    public List<PlateWrapper> getPlates() throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = new ArrayList<>(0);
        try {
            plates = getBrowseFacility().getPlates(getCtx());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get plates");
        }
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
    public List<PlateWrapper> getPlates(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = new ArrayList<>(0);
        try {
            plates = getBrowseFacility().getPlates(getCtx(), experimenter.getId());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get plates for user " + experimenter);
        }
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
    public WellWrapper getWell(Long id)
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
    public List<WellWrapper> getWells(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<WellData> wells = new ArrayList<>(0);
        try {
            wells = getBrowseFacility().getWells(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get wells");
        }
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
     * @throws OMEROServerError   Server error.
     */
    public List<WellWrapper> getWells()
    throws ServiceException, AccessException, ExecutionException, OMEROServerError {
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
     * @throws OMEROServerError   Server error.
     */
    public List<WellWrapper> getWells(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError {
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
    public FolderWrapper getFolder(long id)
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
    public List<FolderWrapper> getFolders()
    throws ExecutionException, AccessException, ServiceException {
        Collection<FolderData> folders = new ArrayList<>(0);
        try {
            folders = getBrowseFacility().getFolders(getCtx());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get folders");
        }
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
    public List<FolderWrapper> getFolders(ExperimenterWrapper experimenter)
    throws ExecutionException, AccessException, ServiceException {
        String error = String.format("Cannot get folders for user %s", experimenter);

        Collection<FolderData> folders = new ArrayList<>(0);
        try {
            folders = getBrowseFacility().getFolders(getCtx(), experimenter.getId());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, error);
        }
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
    public List<FolderWrapper> loadFolders(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get folders with IDs: " + Arrays.toString(ids);

        Collection<FolderData> folders = new ArrayList<>(0);
        try {
            folders = getBrowseFacility().loadFolders(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, error);
        }
        return wrap(folders, FolderWrapper::new);
    }


    /**
     * Gets the list of tag annotations available to the user.
     *
     * @return See above.
     *
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<TagAnnotationWrapper> getTags() throws OMEROServerError, ServiceException {
        List<IObject> os = new ArrayList<>(0);
        try {
            os = getQueryService().findAll(TagAnnotation.class.getSimpleName(), null);
        } catch (ServerError e) {
            handleServiceOrServer(e, "Cannot get tags");
        }
        return os.stream()
                 .map(TagAnnotation.class::cast)
                 .map(TagAnnotationData::new)
                 .map(TagAnnotationWrapper::new)
                 .sorted(Comparator.comparing(GenericObjectWrapper::getId))
                 .collect(Collectors.toList());
    }


    /**
     * Gets the list of tag annotations with the specified name available to the user.
     *
     * @param name Name of the tag searched.
     *
     * @return See above.
     *
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<TagAnnotationWrapper> getTags(String name) throws OMEROServerError, ServiceException {
        List<TagAnnotationWrapper> tags = getTags();
        tags.removeIf(tag -> !tag.getName().equals(name));
        tags.sort(Comparator.comparing(GenericObjectWrapper::getId));
        return tags;
    }


    /**
     * Gets a specific tag from the OMERO database.
     *
     * @param id ID of the tag.
     *
     * @return See above.
     *
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public TagAnnotationWrapper getTag(Long id) throws OMEROServerError, ServiceException {
        IObject o = null;
        try {
            o = getQueryService().find(TagAnnotation.class.getSimpleName(), id);
        } catch (ServerError e) {
            handleServiceOrServer(e, "Cannot get tag ID: " + id);
        }

        TagAnnotationData tag = new TagAnnotationData((TagAnnotation) Objects.requireNonNull(o));
        tag.setNameSpace(tag.getContentAsString());

        return new TagAnnotationWrapper(tag);
    }


    /**
     * Gets the list of map annotations available to the user.
     *
     * @return See above.
     *
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<MapAnnotationWrapper> getMapAnnotations() throws OMEROServerError, ServiceException {
        List<MapAnnotationWrapper> kvs = new ArrayList<>(0);
        try {
            kvs = getQueryService().findAll(omero.model.MapAnnotation.class.getSimpleName(), null)
                                   .stream()
                                   .map(omero.model.MapAnnotation.class::cast)
                                   .map(MapAnnotationData::new)
                                   .map(MapAnnotationWrapper::new)
                                   .sorted(Comparator.comparing(GenericObjectWrapper::getId))
                                   .collect(Collectors.toList());
        } catch (ServerError e) {
            handleServiceOrServer(e, "Cannot get map annotations");
        }
        return kvs;
    }


    /**
     * Gets the list of map annotations with the specified key available to the user.
     *
     * @param key Name of the tag searched.
     *
     * @return See above.
     *
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<MapAnnotationWrapper> getMapAnnotations(String key) throws OMEROServerError, ServiceException {
        String q = String.format("select m from MapAnnotation as m join m.mapValue as mv where mv.name = '%s'", key);
        return findByQuery(q).stream()
                             .map(omero.model.MapAnnotation.class::cast)
                             .map(MapAnnotationData::new)
                             .map(MapAnnotationWrapper::new)
                             .sorted(Comparator.comparing(GenericObjectWrapper::getId))
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
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<MapAnnotationWrapper> getMapAnnotations(String key, String value)
    throws OMEROServerError, ServiceException {
        String q = String.format("select m from MapAnnotation as m join m.mapValue as mv " +
                                 "where mv.name = '%s' and mv.value = '%s'", key, value);
        return findByQuery(q).stream()
                             .map(omero.model.MapAnnotation.class::cast)
                             .map(MapAnnotationData::new)
                             .map(MapAnnotationWrapper::new)
                             .sorted(Comparator.comparing(GenericObjectWrapper::getId))
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
    public MapAnnotationWrapper getMapAnnotation(Long id) throws ServiceException, ExecutionException, AccessException {
        MapAnnotationData kv = null;
        try {
            kv = getBrowseFacility().findObject(getCtx(), MapAnnotationData.class, id);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get map annotation with ID: " + id);
        }
        return new MapAnnotationWrapper(kv);
    }

}
