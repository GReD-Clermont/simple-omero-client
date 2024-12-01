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


import fr.igred.omero.annotations.MapAnnotation;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.containers.Project;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.Experimenter;
import fr.igred.omero.screen.Plate;
import omero.gateway.SecurityContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.RemoteObject.flatten;


/**
 * Interface to browse data on an OMERO server in a given {@link SecurityContext}.
 */
public interface Browser extends fr.igred.omero.containers.ContainersBrowser,
                                 fr.igred.omero.core.ImageBrowser,
                                 fr.igred.omero.screen.ScreenBrowser,
                                 fr.igred.omero.annotations.AnnotationsBrowser {


    /**
     * Returns the current user.
     *
     * @return See above.
     */
    Experimenter getUser();


    /**
     * Gets all orphaned datasets available from OMERO owned by the current user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default List<Dataset> getOrphanedDatasets()
    throws ServiceException, ExecutionException, AccessException {
        return getOrphanedDatasets(getUser());
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
    @Override
    default List<Image> getImages(String projectName, String datasetName, String imageName)
    throws ServiceException, AccessException, ExecutionException {
        List<Project> projects = getProjects(projectName);

        Collection<List<Image>> lists = new ArrayList<>(projects.size());
        for (Project project : projects) {
            lists.add(project.getImages(this, datasetName, imageName));
        }

        return flatten(lists);
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
    @Override
    default List<Image> getImagesWithKey(String key)
    throws ServiceException, AccessException, ExecutionException {
        List<MapAnnotation> maps = getMapAnnotations(key);

        Collection<Collection<Image>> selected = new ArrayList<>(maps.size());
        for (MapAnnotation map : maps) {
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
    @Override
    default List<Image> getImagesWithKeyValuePair(String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<MapAnnotation> maps = getMapAnnotations(key, value);

        Collection<Collection<Image>> selected = new ArrayList<>(maps.size());
        for (MapAnnotation map : maps) {
            selected.add(getImages(map));
        }

        return flatten(selected);
    }


    /**
     * Gets all orphaned images owned by the current user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default List<Image> getOrphanedImages()
    throws ServiceException, AccessException, ExecutionException {
        return getOrphanedImages(getUser());
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
    default List<Plate> getOrphanedPlates()
    throws ServiceException, ExecutionException, AccessException {
        return getOrphanedPlates(getUser());
    }


}
