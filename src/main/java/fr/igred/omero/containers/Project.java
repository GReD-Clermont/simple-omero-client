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

package fr.igred.omero.containers;


import fr.igred.omero.RemoteObject;
import fr.igred.omero.RepositoryObject;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.client.BasicBrowser;
import fr.igred.omero.client.BasicDataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.ImageBrowser;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.ProjectData;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.RemoteObject.distinct;
import static fr.igred.omero.RemoteObject.flatten;
import static java.util.Comparator.comparing;


/**
 * Interface to handle Projects on OMERO.
 */
public interface Project extends RepositoryObject {

    /** Annotation link name for this type of object */
    String ANNOTATION_LINK = "ProjectAnnotationLink";


    /**
     * Returns a {@link ProjectData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    ProjectData asDataObject();


    /**
     * Sets the name of the project.
     *
     * @param name The name of the project. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Sets the description of the project.
     *
     * @param description The description of the project.
     */
    void setDescription(String description);


    /**
     * Gets all the datasets in the project available from OMERO.
     *
     * @return Collection of DatasetWrapper.
     */
    List<Dataset> getDatasets();


    /**
     * Gets the dataset with the specified name from OMERO
     *
     * @param name Name of the dataset searched.
     *
     * @return List of dataset with the given name.
     */
    default List<Dataset> getDatasets(String name) {
        List<Dataset> datasets = getDatasets();
        datasets.removeIf(dataset -> !dataset.getName().equals(name));
        return datasets;
    }


    /**
     * Creates a dataset and adds it to the project in OMERO.
     * <p>The project needs to be reloaded afterwards to list the new dataset.</p>
     *
     * @param dm          The data manager.
     * @param name        Dataset name.
     * @param description Dataset description.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    Dataset addDataset(BasicDataManager dm, String name, String description)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Adds a dataset to the project in OMERO.
     *
     * @param dm      The data manager.
     * @param dataset Dataset to be added.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default Dataset addDataset(BasicDataManager dm, Dataset dataset)
    throws ServiceException, AccessException, ExecutionException {
        dataset.saveAndUpdate(dm);
        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setChild(dataset.asDataObject().asDataset());
        link.setParent(asDataObject().asProject());

        dm.save(link);
        return dataset;
    }


    /**
     * Removes a dataset from the project in OMERO.
     *
     * @param client  The client handling the connection.
     * @param dataset Dataset to remove.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    <C extends BasicBrowser & BasicDataManager>
    void removeDataset(C client, Dataset dataset)
    throws ServiceException, AccessException, ExecutionException, InterruptedException;


    /**
     * Gets all images in the project available from OMERO.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImages(BasicBrowser browser)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all images in the project with a certain name from OMERO.
     *
     * @param browser The data browser.
     * @param name    Name searched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImages(BasicBrowser browser, String name)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImages(browser, name));
        }
        return flatten(lists);
    }


    /**
     * Gets all images with a certain name from datasets with the specified name inside this project on OMERO.
     *
     * @param browser     The data browser.
     * @param datasetName Expected dataset name.
     * @param imageName   Expected image name.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImages(BasicBrowser browser, String datasetName, String imageName)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets(datasetName);

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImages(browser, imageName));
        }
        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Gets all images in the project with a certain motif in their name from OMERO.
     *
     * @param browser The data browser.
     * @param motif   Motif searched in an image name.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImagesLike(BasicBrowser browser, String motif)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesLike(browser, motif));
        }
        return flatten(lists);
    }


    /**
     * Gets all images in the project tagged with a specified tag from OMERO.
     *
     * @param browser The data browser.
     * @param tag     TagAnnotationWrapper containing the tag researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImagesTagged(ImageBrowser browser, TagAnnotation tag)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesTagged(browser, tag));
        }
        return flatten(lists);
    }


    /**
     * Gets all images in the project tagged with a specified tag from OMERO.
     *
     * @param browser The data browser.
     * @param tagId   Id of the tag researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImagesTagged(ImageBrowser browser, Long tagId)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesTagged(browser, tagId));
        }
        return flatten(lists);
    }


    /**
     * Gets all images in the project with a certain key
     *
     * @param browser The data browser.
     * @param key     Name of the key researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImagesWithKey(BasicBrowser browser, String key)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesWithKey(browser, key));
        }
        return flatten(lists);
    }


    /**
     * Gets all images in the project with a certain key value pair from OMERO.
     *
     * @param browser The data browser.
     * @param key     Name of the key researched.
     * @param value   Value associated with the key.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImagesWithKeyValuePair(BasicBrowser browser, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesWithKeyValuePair(browser, key, value));
        }
        return flatten(lists);
    }

}
