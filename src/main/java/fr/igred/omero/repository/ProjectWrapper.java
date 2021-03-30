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
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.exception.OMEROServerError;
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

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;


/**
 * Class containing a ProjectData
 * <p> Implements function using the Project contained
 */
public class ProjectWrapper extends ObjectWrapper<ProjectData> {

    /**
     * Constructor of the ProjectWrapper class.
     *
     * @param project ProjectData to be contained.
     */
    public ProjectWrapper(ProjectData project) {
        super(project);
    }


    /**
     * Gets the ProjectData name
     *
     * @return ProjectData name.
     */
    public String getName() {
        return data.getName();
    }


    /**
     * Gets the ProjectData description
     *
     * @return ProjectData description.
     */
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * @return the ProjectData contained.
     */
    public ProjectData getProject() {
        return data;
    }


    /**
     * Gets all the datasets in the project available from OMERO.
     *
     * @return Collection of DatasetWrapper.
     */
    public List<DatasetWrapper> getDatasets() {
        List<DatasetWrapper> wrappers = new ArrayList<>();
        for (DatasetData dataset : data.getDatasets())
            wrappers.add(new DatasetWrapper(dataset));

        return wrappers;
    }


    /**
     * Gets the dataset with the specified name from OMERO
     *
     * @param name Name of the dataset searched.
     *
     * @return List of dataset with the given name.
     */
    public List<DatasetWrapper> getDatasets(String name) {
        List<DatasetWrapper> datasets = getDatasets();
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
    public DatasetWrapper addDataset(Client client, String name, String description)
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
    public DatasetWrapper addDataset(Client client, DatasetWrapper dataset)
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
    private DatasetWrapper addDataset(Client client, DatasetData datasetData)
    throws ServiceException, AccessException, ExecutionException {
        DatasetWrapper newDataset;
        datasetData.setProjects(Collections.singleton(data));
        DatasetData dataset = (DatasetData) PojoMapper.asDataObject(client.save(datasetData.asIObject()));
        newDataset = new DatasetWrapper(dataset);
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
    public void addTag(Client client, TagAnnotationWrapper tag)
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
        link.setParent(new ProjectI(data.getId(), false));

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
        link.setParent(new ProjectI(data.getId(), false));

        client.save(link);
    }


    /**
     * Add multiple tag to the project in OMERO.
     *
     * @param client The user.
     * @param tags   Array of TagAnnotationWrapper to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTags(Client client, TagAnnotationWrapper... tags)
    throws ServiceException, AccessException, ExecutionException {
        for (TagAnnotationWrapper tag : tags) {
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
     * @return Collection of TagAnnotationWrapper each containing a tag linked to the dataset.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<TagAnnotationWrapper> getTags(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<TagAnnotationWrapper> tags    = new ArrayList<>();
        List<Long>                 userIds = new ArrayList<>();
        userIds.add(client.getId());

        List<Class<? extends AnnotationData>> types = new ArrayList<>();
        types.add(TagAnnotationData.class);

        List<AnnotationData> annotations = null;
        try {
            annotations = client.getMetadata().getAnnotations(client.getCtx(), data, types, userIds);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get tags for project ID: " + getId());
        }

        if (annotations != null) {
            for (AnnotationData annotation : annotations) {
                TagAnnotationData tagAnnotation = (TagAnnotationData) annotation;
                tags.add(new TagAnnotationWrapper(tagAnnotation));
            }
        }

        tags.sort(new SortById<>());
        return tags;
    }


    /**
     * Gets all images in the dataset available from OMERO.
     *
     * @return ImageWrapper list.
     */
    private List<ImageWrapper> purge(List<ImageWrapper> images) {
        List<ImageWrapper> purged = new ArrayList<>();

        for (ImageWrapper image : images) {
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
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageWrapper> getImages(Client client) throws ServiceException, AccessException {
        List<ImageWrapper>         images   = new ArrayList<>();
        Collection<DatasetWrapper> datasets = getDatasets();

        for (DatasetWrapper dataset : datasets) {
            images.addAll(dataset.getImages(client));
        }

        images.sort(new SortById<>());

        return purge(images);
    }


    /**
     * Gets all images in the project with a certain from OMERO.
     *
     * @param client The user.
     * @param name   Name searched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageWrapper> getImages(Client client, String name)
    throws ServiceException, AccessException {
        List<ImageWrapper> imageWrappers = new ArrayList<>();

        Collection<DatasetWrapper> datasets = getDatasets();

        for (DatasetWrapper dataset : datasets) {
            imageWrappers.addAll(dataset.getImages(client, name));
        }

        imageWrappers.sort(new SortById<>());

        return purge(imageWrappers);
    }


    /**
     * Gets all images in the project with a certain motif in their name from OMERO.
     *
     * @param client The user.
     * @param motif  Motif searched in an image name.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageWrapper> getImagesLike(Client client, String motif)
    throws ServiceException, AccessException {
        List<ImageWrapper> images = new ArrayList<>();

        for (DatasetWrapper dataset : getDatasets()) {
            images.addAll(dataset.getImagesLike(client, motif));
        }

        images.sort(new SortById<>());

        return purge(images);
    }


    /**
     * Gets all images in the project tagged with a specified tag from OMERO.
     *
     * @param client The user.
     * @param tag    TagAnnotationWrapper containing the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws OMEROServerError Server error.
     */
    public List<ImageWrapper> getImagesTagged(Client client, TagAnnotationWrapper tag)
    throws ServiceException, AccessException, OMEROServerError {
        List<ImageWrapper> images = new ArrayList<>();

        for (DatasetWrapper dataset : getDatasets()) {
            images.addAll(dataset.getImagesTagged(client, tag));
        }

        images.sort(new SortById<>());

        return purge(images);
    }


    /**
     * Gets all images in the project tagged with a specified tag from OMERO.
     *
     * @param client The user.
     * @param tagId  Id of the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws OMEROServerError Server error.
     */
    public List<ImageWrapper> getImagesTagged(Client client, Long tagId)
    throws ServiceException, AccessException, OMEROServerError {
        List<ImageWrapper> images = new ArrayList<>();

        for (DatasetWrapper dataset : getDatasets()) {
            images.addAll(dataset.getImagesTagged(client, tagId));
        }

        images.sort(new SortById<>());

        return purge(images);
    }


    /**
     * Gets all images in the project with a certain key
     *
     * @param client The user.
     * @param key    Name of the key researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesKey(Client client, String key)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageWrapper> images = new ArrayList<>();

        for (DatasetWrapper dataset : getDatasets()) {
            images.addAll(dataset.getImagesKey(client, key));
        }

        images.sort(new SortById<>());

        return purge(images);
    }


    /**
     * Gets all images in the project with a certain key value pair from OMERO.
     *
     * @param client The user.
     * @param key    Name of the key researched.
     * @param value  Value associated with the key.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageWrapper> images = new ArrayList<>();

        for (DatasetWrapper dataset : getDatasets()) {
            images.addAll(dataset.getImagesPairKeyValue(client, key, value));
        }

        images.sort(new SortById<>());

        return purge(images);
    }

}