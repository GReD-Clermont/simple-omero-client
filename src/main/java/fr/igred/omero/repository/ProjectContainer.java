/*
 *  Copyright (C) 2020 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.

 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.ImageContainer;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.sort.SortImageContainer;
import fr.igred.omero.sort.SortTagAnnotationContainer;
import fr.igred.omero.exception.ServerError;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.util.PojoMapper;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectI;
import omero.model.TagAnnotationI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Class containing a ProjectData
 * <p> Implements function using the Project contained
 */
public class ProjectContainer {

    /** ProjectData contained */
    private final ProjectData project;


    /**
     * Constructor of the ProjectContainer class.
     *
     * @param p ProjectData to be contained.
     */
    public ProjectContainer(ProjectData p) {
        project = p;
    }


    /**
     * Gets the ProjectData id
     *
     * @return ProjectData id.
     */
    public Long getId() {
        return project.getId();
    }


    /**
     * Gets the ProjectData name
     *
     * @return ProjectData name.
     */
    public String getName() {
        return project.getName();
    }


    /**
     * Gets the ProjectData description
     *
     * @return ProjectData description.
     */
    public String getDescription() {
        return project.getDescription();
    }


    /**
     * @return the ProjectData contained.
     */
    public ProjectData getProject() {
        return project;
    }


    /**
     * Gets all the datasets in the project available from OMERO.
     *
     * @return Collection of DatasetContainer.
     */
    public List<DatasetContainer> getDatasets() {
        List<DatasetContainer> datasetsContainer = new ArrayList<>();

        Collection<DatasetData> datasets = project.getDatasets();

        for (DatasetData dataset : datasets)
            datasetsContainer.add(new DatasetContainer(dataset));

        return datasetsContainer;
    }


    /**
     * Gets the dataset with the specified name from OMERO
     *
     * @param name Name of the dataset searched.
     *
     * @return List of dataset with the given name.
     */
    public List<DatasetContainer> getDatasets(String name) {
        List<DatasetContainer> datasets = getDatasets();
        datasets.removeIf(dataset -> !dataset.getName().equals(name));
        return datasets;
    }


    /**
     * Add a dataset to the project in OMERO. Create the dataset.
     *
     * @param client      The user.
     * @param name        Dataset name.
     * @param description Dataset description.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public DatasetContainer addDataset(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        DatasetData datasetData = new DatasetData();
        datasetData.setName(name);
        datasetData.setDescription(description);
        return addDataset(client, datasetData);
    }


    /**
     * Add a dataset to the project in OMERO.
     *
     * @param client  The user.
     * @param dataset Dataset to be added.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public DatasetContainer addDataset(Client client, DatasetContainer dataset)
    throws ServiceException, AccessException, ExecutionException {
        return addDataset(client, dataset.getDataset());
    }


    /**
     * Private function. Add a dataset to the project.
     *
     * @param client      The user.
     * @param datasetData Dataset to be added.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    private DatasetContainer addDataset(Client client, DatasetData datasetData)
    throws ServiceException, AccessException, ExecutionException {
        DatasetContainer newDataset;
        datasetData.setProjects(Collections.singleton(project));
        DatasetData dataset = (DatasetData) PojoMapper.asDataObject(client.save(datasetData.asIObject()));
        newDataset = new DatasetContainer(dataset);
        return newDataset;
    }


    /**
     * Add a tag to the project in OMERO. Create the tag.
     *
     * @param client      The user.
     * @param name        Tag Name.
     * @param description Tag description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTag(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        TagAnnotationData tagData = new TagAnnotationData(name);
        tagData.setTagDescription(description);

        addTag(client, tagData);
    }


    /**
     * Add a tag to the project in OMERO.
     *
     * @param client The user.
     * @param tag    Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTag(Client client, TagAnnotationContainer tag)
    throws ServiceException, AccessException, ExecutionException {
        addTag(client, tag.getTag());
    }


    /**
     * Private function. Add a tag to the project in OMERO.
     *
     * @param client  The user.
     * @param tagData Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    private void addTag(Client client, TagAnnotationData tagData)
    throws ServiceException, AccessException, ExecutionException {
        ProjectAnnotationLink link = new ProjectAnnotationLinkI();
        link.setChild(tagData.asAnnotation());
        link.setParent(new ProjectI(project.getId(), false));

        client.save(link);
    }


    /**
     * Add multiple tag to the project in OMERO.
     *
     * @param client The user.
     * @param id     Id in OMERO of the tag to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTag(Client client, Long id)
    throws ServiceException, AccessException, ExecutionException {
        ProjectAnnotationLink link = new ProjectAnnotationLinkI();
        link.setChild(new TagAnnotationI(id, false));
        link.setParent(new ProjectI(project.getId(), false));

        client.save(link);
    }


    /**
     * Add multiple tag to the project in OMERO.
     *
     * @param client The user.
     * @param tags   Array of TagAnnotationContainer to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTags(Client client, TagAnnotationContainer... tags)
    throws ServiceException, AccessException, ExecutionException {
        for (TagAnnotationContainer tag : tags) {
            addTag(client, tag.getTag());
        }
    }


    /**
     * Add multiple tag to the project in OMERO. The tags id is used
     *
     * @param client The user.
     * @param ids    Array of tag id to add.
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
     * Gets all tag linked to a project in OMERO
     *
     * @param client The user.
     *
     * @return Collection of TagAnnotationContainer each containing a tag linked to the dataset.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<TagAnnotationContainer> getTags(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<TagAnnotationContainer> tags    = new ArrayList<>();
        List<Long>                   userIds = new ArrayList<>();
        userIds.add(client.getId());

        List<Class<? extends AnnotationData>> types = new ArrayList<>();
        types.add(TagAnnotationData.class);

        List<AnnotationData> annotations;
        try {
            annotations = client.getMetadata().getAnnotations(client.getCtx(), project, types, userIds);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        if (annotations != null) {
            for (AnnotationData annotation : annotations) {
                TagAnnotationData tagAnnotation = (TagAnnotationData) annotation;
                tags.add(new TagAnnotationContainer(tagAnnotation));
            }
        }

        tags.sort(new SortTagAnnotationContainer());
        return tags;
    }


    /**
     * Gets all images in the dataset available from OMERO.
     *
     * @return ImageContainer list.
     */
    private List<ImageContainer> purge(List<ImageContainer> images) {
        List<ImageContainer> purged = new ArrayList<>();

        for (ImageContainer image : images) {
            if (purged.isEmpty() || !purged.get(purged.size() - 1).getId().equals(image.getId())) {
                purged.add(image);
            }
        }

        return purged;
    }


    /**
     * Gets all images in the project available from OMERO.
     *
     * @param client The user.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageContainer> getImages(Client client) throws ServiceException, AccessException {
        List<ImageContainer>         imagesContainer = new ArrayList<>();
        Collection<DatasetContainer> datasets        = getDatasets();

        for (DatasetContainer dataset : datasets) {
            imagesContainer.addAll(dataset.getImages(client));
        }

        imagesContainer.sort(new SortImageContainer());

        return purge(imagesContainer);
    }


    /**
     * Gets all images in the project with a certain from OMERO.
     *
     * @param client The user.
     * @param name   Name searched.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageContainer> getImages(Client client, String name)
    throws ServiceException, AccessException {
        List<ImageContainer> imagesContainer = new ArrayList<>();

        Collection<DatasetContainer> datasets = getDatasets();

        for (DatasetContainer dataset : datasets) {
            imagesContainer.addAll(dataset.getImages(client, name));
        }

        imagesContainer.sort(new SortImageContainer());

        return purge(imagesContainer);
    }


    /**
     * Gets all images in the project with a certain motif in their name from OMERO.
     *
     * @param client The user.
     * @param motif  Motif searched in an image name.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageContainer> getImagesLike(Client client, String motif)
    throws ServiceException, AccessException {
        List<ImageContainer> imagesContainer = new ArrayList<>();

        Collection<DatasetContainer> datasets = getDatasets();

        for (DatasetContainer dataset : datasets) {
            imagesContainer.addAll(dataset.getImagesLike(client, motif));
        }

        imagesContainer.sort(new SortImageContainer());

        return purge(imagesContainer);
    }


    /**
     * Gets all images in the project tagged with a specified tag from OMERO.
     *
     * @param client The user.
     * @param tag    TagAnnotationContainer containing the tag researched.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws ServerError      Server error.
     */
    public List<ImageContainer> getImagesTagged(Client client, TagAnnotationContainer tag)
    throws ServiceException, AccessException, ServerError {
        List<ImageContainer> imagesContainer = new ArrayList<>();

        Collection<DatasetContainer> datasets = getDatasets();

        for (DatasetContainer dataset : datasets) {
            imagesContainer.addAll(dataset.getImagesTagged(client, tag));
        }

        imagesContainer.sort(new SortImageContainer());

        return purge(imagesContainer);
    }


    /**
     * Gets all images in the project tagged with a specified tag from OMERO.
     *
     * @param client The user.
     * @param tagId  Id of the tag researched.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws ServerError      Server error.
     */
    public List<ImageContainer> getImagesTagged(Client client, Long tagId)
    throws ServiceException, AccessException, ServerError {
        List<ImageContainer> imagesContainer = new ArrayList<>();

        Collection<DatasetContainer> datasets = getDatasets();

        for (DatasetContainer dataset : datasets) {
            imagesContainer.addAll(dataset.getImagesTagged(client, tagId));
        }

        imagesContainer.sort(new SortImageContainer());

        return purge(imagesContainer);
    }


    /**
     * Gets all images in the project with a certain key
     *
     * @param client The user.
     * @param key    Name of the key researched.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageContainer> getImagesKey(Client client, String key)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageContainer> imagesContainer = new ArrayList<>();

        Collection<DatasetContainer> datasets = getDatasets();

        for (DatasetContainer dataset : datasets) {
            imagesContainer.addAll(dataset.getImagesKey(client, key));
        }

        imagesContainer.sort(new SortImageContainer());

        return purge(imagesContainer);
    }


    /**
     * Gets all images in the project with a certain key value pair from OMERO.
     *
     * @param client The user.
     * @param key    Name of the key researched.
     * @param value  Value associated with the key.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageContainer> getImagesPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageContainer> imagesContainer = new ArrayList<>();

        Collection<DatasetContainer> datasets = getDatasets();

        for (DatasetContainer dataset : datasets) {
            imagesContainer.addAll(dataset.getImagesPairKeyValue(client, key, value));
        }

        imagesContainer.sort(new SortImageContainer());

        return purge(imagesContainer);
    }

}