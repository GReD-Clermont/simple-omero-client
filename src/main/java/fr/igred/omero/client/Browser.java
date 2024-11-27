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
import fr.igred.omero.annotations.AnnotationWrapper;
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
import omero.RLong;
import omero.api.IQueryPrx;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.MetadataFacility;
import omero.model.IObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.ObjectWrapper.flatten;
import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.lang.String.format;


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
     * @throws AccessException  Cannot access data.
     */
    IQueryPrx getQueryService() throws ServiceException, AccessException;


    /**
     * Gets the {@link MetadataFacility} used to retrieve annotations from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the MetadataFacility can't be retrieved or instantiated.
     */
    MetadataFacility getMetadataFacility() throws ExecutionException;


    /**
     * Returns the current user.
     *
     * @return See above.
     */
    ExperimenterWrapper getUser();


    /**
     * Finds objects on OMERO through a database query.
     *
     * @param query The database query.
     *
     * @return A list of OMERO objects.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    default List<IObject> findByQuery(String query)
    throws ServiceException, AccessException {
        return call(getQueryService(),
                    qs -> qs.findAllByQuery(query, null),
                    "Query failed: " + query);
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
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default ProjectWrapper getProject(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<ProjectWrapper> projects = getProjects(id);
        if (projects.isEmpty()) {
            String msg = format("Project %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
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
    List<ProjectWrapper> getProjects(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all projects available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<ProjectWrapper> getProjects()
    throws ServiceException, AccessException, ExecutionException;


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
    List<ProjectWrapper> getProjects(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException;


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
    List<ProjectWrapper> getProjects(String name)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the dataset with the specified id from OMERO.
     *
     * @param id ID of the dataset.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default DatasetWrapper getDataset(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<DatasetWrapper> datasets = getDatasets(id);
        if (datasets.isEmpty()) {
            String msg = format("Dataset %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
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
    List<DatasetWrapper> getDatasets(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all datasets available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<DatasetWrapper> getDatasets()
    throws ServiceException, AccessException, ExecutionException {
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
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<DatasetWrapper> getDatasets(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        String template = "select d from Dataset d where d.details.owner.id=%d";
        String query    = format(template, experimenter.getId());
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
    List<DatasetWrapper> getDatasets(String name)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all orphaned datasets available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<DatasetWrapper> getOrphanedDatasets(ExperimenterWrapper experimenter)
    throws ServiceException, ExecutionException, AccessException {
        String template = "select dataset from Dataset as dataset" +
                          " join fetch dataset.details.owner as o" +
                          " where o.id = %d" +
                          " and not exists" +
                          " (select obl from ProjectDatasetLink as obl" +
                          " where obl.child = dataset.id)";
        String query = format(template, experimenter.getId());
        Long[] ids = this.findByQuery(query)
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getDatasets(ids);
    }


    /**
     * Gets all orphaned datasets available from OMERO owned by the current user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<DatasetWrapper> getOrphanedDatasets()
    throws ServiceException, ExecutionException, AccessException {
        return getOrphanedDatasets(getUser());
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
    ImageWrapper getImage(Long id)
    throws ServiceException, AccessException, ExecutionException;


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
    List<ImageWrapper> getImages(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all images owned by the current user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<ImageWrapper> getImages()
    throws ServiceException, AccessException, ExecutionException;


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
    List<ImageWrapper> getImages(String name)
    throws ServiceException, AccessException, ExecutionException;


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
    List<ImageWrapper> getOrphanedImages(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all orphaned images owned by the current user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ImageWrapper> getOrphanedImages()
    throws ServiceException, AccessException, ExecutionException {
        return getOrphanedImages(getUser());
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
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ImageWrapper> getImages(AnnotationWrapper<?> annotation)
    throws ServiceException, AccessException, ExecutionException {
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
    default List<ImageWrapper> getImagesLike(String motif)
    throws ServiceException, AccessException, ExecutionException {
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
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ImageWrapper> getImagesWithKey(String key)
    throws ServiceException, AccessException, ExecutionException {
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
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ImageWrapper> getImagesWithKeyValuePair(String key, String value)
    throws ServiceException, AccessException, ExecutionException {
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
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default ScreenWrapper getScreen(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<ScreenWrapper> screens = getScreens(id);
        if (screens.isEmpty()) {
            String msg = format("Screen %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
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
    List<ScreenWrapper> getScreens(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all screens available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<ScreenWrapper> getScreens()
    throws ServiceException, AccessException, ExecutionException;


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
    List<ScreenWrapper> getScreens(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the plate with the specified id from OMERO.
     *
     * @param id ID of the plate.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default PlateWrapper getPlate(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<PlateWrapper> plates = getPlates(id);
        if (plates.isEmpty()) {
            String msg = format("Plate %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
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
    List<PlateWrapper> getPlates(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all plates available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<PlateWrapper> getPlates()
    throws ServiceException, AccessException, ExecutionException;


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
    List<PlateWrapper> getPlates(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all orphaned plates available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<PlateWrapper> getOrphanedPlates(ExperimenterWrapper experimenter)
    throws ServiceException, ExecutionException, AccessException {
        String template = "select plate from Plate as plate" +
                          " join fetch plate.details.owner as o" +
                          " where o.id = %d" +
                          " and not exists" +
                          " (select obl from ScreenPlateLink as obl" +
                          " where obl.child = plate.id)";
        String query = format(template, experimenter.getId());
        Long[] ids = this.findByQuery(query)
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getPlates(ids);
    }


    /**
     * Gets all orphaned plates available from OMERO for the current user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<PlateWrapper> getOrphanedPlates()
    throws ServiceException, ExecutionException, AccessException {
        return getOrphanedPlates(getUser());
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
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default WellWrapper getWell(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<WellWrapper> wells = getWells(id);
        if (wells.isEmpty()) {
            String msg = format("Plate %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
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
    List<WellWrapper> getWells(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all wells available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<WellWrapper> getWells()
    throws ServiceException, AccessException, ExecutionException {
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
     */
    default List<WellWrapper> getWells(ExperimenterWrapper experimenter)
    throws ServiceException, AccessException, ExecutionException {
        String template = "select w from Well w where w.details.owner.id=%d";
        String query    = format(template, experimenter.getId());
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
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default FolderWrapper getFolder(long id)
    throws ServiceException, AccessException, ExecutionException {
        List<FolderWrapper> folders = getFolders(id);
        if (folders.isEmpty()) {
            String msg = format("Folder %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
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
    List<FolderWrapper> getFolders()
    throws ExecutionException, AccessException, ServiceException;


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
    List<FolderWrapper> getFolders(ExperimenterWrapper experimenter)
    throws ExecutionException, AccessException, ServiceException;


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
    List<FolderWrapper> getFolders(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the list of tag annotations available to the user.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    List<TagAnnotationWrapper> getTags()
    throws AccessException, ServiceException;


    /**
     * Gets the list of tag annotations with the specified name available to the user.
     *
     * @param name Name of the tag searched.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    default List<TagAnnotationWrapper> getTags(String name)
    throws AccessException, ServiceException {
        List<TagAnnotationWrapper> tags = getTags();
        tags.removeIf(tag -> !tag.getName().equals(name));
        tags.sort(Comparator.comparing(ObjectWrapper::getId));
        return tags;
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
    TagAnnotationWrapper getTag(Long id)
    throws AccessException, ServiceException;


    /**
     * Gets the list of map annotations available to the user.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    List<MapAnnotationWrapper> getMapAnnotations()
    throws AccessException, ServiceException;


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
    List<MapAnnotationWrapper> getMapAnnotations(String key)
    throws AccessException, ServiceException;


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
    List<MapAnnotationWrapper> getMapAnnotations(String key, String value)
    throws AccessException, ServiceException;


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
    MapAnnotationWrapper getMapAnnotation(Long id)
    throws ServiceException, ExecutionException, AccessException;

}
