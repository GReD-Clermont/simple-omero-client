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

package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ProjectData;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.RemoteObject.distinct;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


/**
 * Class containing a ProjectData object.
 * <p> Wraps function calls to the Project contained
 */
public class ProjectWrapper extends RepositoryObjectWrapper<ProjectData> implements Project {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "ProjectAnnotationLink";


    /**
     * Constructor of the ProjectWrapper class.
     *
     * @param project ProjectData to be contained.
     */
    public ProjectWrapper(ProjectData project) {
        super(project);
    }


    /**
     * Constructor of the ProjectWrapper class. Creates a new project and save it to OMERO.
     *
     * @param client      The client handling the connection.
     * @param name        Project name.
     * @param description Project description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ProjectWrapper(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        super(new ProjectData());
        data.setName(name);
        data.setDescription(description);
        super.saveAndUpdate(client);
    }


    /**
     * Gets the ProjectData name
     *
     * @return ProjectData name.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the project.
     *
     * @param name The name of the project. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    @Override
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Gets the project description
     *
     * @return The project description.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the project.
     *
     * @param description The description of the project.
     */
    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Returns the type of annotation link for this object
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Gets all the datasets in the project available from OMERO.
     *
     * @return Collection of DatasetWrapper.
     */
    @Override
    public List<Dataset> getDatasets() {
        return wrap(data.getDatasets(), DatasetWrapper::new);
    }


    /**
     * Gets the dataset with the specified name from OMERO
     *
     * @param name Name of the dataset searched.
     *
     * @return List of dataset with the given name.
     */
    @Override
    public List<Dataset> getDatasets(String name) {
        List<Dataset> datasets = getDatasets();
        datasets.removeIf(dataset -> !dataset.getName().equals(name));
        return datasets;
    }


    /**
     * Adds a dataset to the project in OMERO. Create the dataset.
     *
     * @param client      The client handling the connection.
     * @param name        Dataset name.
     * @param description Dataset description.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public Dataset addDataset(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        Dataset dataset = new DatasetWrapper(name, description);
        dataset.saveAndUpdate(client);
        return addDataset(client, dataset);
    }


    /**
     * Adds a dataset to the project in OMERO.
     *
     * @param client  The client handling the connection.
     * @param dataset Dataset to be added.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public Dataset addDataset(Client client, Dataset dataset)
    throws ServiceException, AccessException, ExecutionException {
        dataset.saveAndUpdate(client);
        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setChild(dataset.asDataObject().asDataset());
        link.setParent(data.asProject());

        client.save(link);
        refresh(client);
        dataset.refresh(client);
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
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    public void removeDataset(Client client, RemoteObject<DatasetData> dataset)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        removeLink(client, "ProjectDatasetLink", dataset.getId());
        refresh(client);
    }


    /**
     * Gets all images in the project available from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImages(Client client) throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImages(client));
        }
        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(Comparator.comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Gets all images in the project with a certain name from OMERO.
     *
     * @param client The client handling the connection.
     * @param name   Name searched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImages(Client client, String name)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImages(client, name));
        }
        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(Comparator.comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Gets all images with a certain name from datasets with the specified name inside this project on OMERO.
     *
     * @param client      The client handling the connection.
     * @param datasetName Expected dataset name.
     * @param imageName   Expected image name.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImages(Client client, String datasetName, String imageName)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets(datasetName);

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImages(client, imageName));
        }
        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(Comparator.comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Gets all images in the project with a certain motif in their name from OMERO.
     *
     * @param client The client handling the connection.
     * @param motif  Motif searched in an image name.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesLike(Client client, String motif)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesLike(client, motif));
        }
        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(Comparator.comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Gets all images in the project tagged with a specified tag from OMERO.
     *
     * @param client The client handling the connection.
     * @param tag    TagAnnotationWrapper containing the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesTagged(Client client, TagAnnotationWrapper tag)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesTagged(client, tag));
        }
        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(Comparator.comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Gets all images in the project tagged with a specified tag from OMERO.
     *
     * @param client The client handling the connection.
     * @param tagId  Id of the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesTagged(Client client, Long tagId)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesTagged(client, tagId));
        }
        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(Comparator.comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Gets all images in the project with a certain key
     *
     * @param client The client handling the connection.
     * @param key    Name of the key researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesKey(Client client, String key)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesKey(client, key));
        }
        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(Comparator.comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Gets all images in the project with a certain key value pair from OMERO.
     *
     * @param client The client handling the connection.
     * @param key    Name of the key researched.
     * @param value  Value associated with the key.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        Collection<Dataset> datasets = getDatasets();

        Collection<List<Image>> lists = new ArrayList<>(datasets.size());
        for (Dataset dataset : datasets) {
            lists.add(dataset.getImagesPairKeyValue(client, key, value));
        }
        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(Comparator.comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Refreshes the wrapped project.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void refresh(Client client) throws ServiceException, AccessException, ExecutionException {
        data = handleServiceAndAccess(client.getBrowseFacility(),
                                      bf -> bf.getProjects(client.getCtx(),
                                                           Collections.singletonList(this.getId()))
                                              .iterator().next(),
                                      "Cannot refresh " + this);
    }

}