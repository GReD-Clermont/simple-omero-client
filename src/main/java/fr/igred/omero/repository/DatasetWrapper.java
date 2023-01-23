/*
 *  Copyright (C) 2020-2023 GReD
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
import fr.igred.omero.RemoteObject;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


/**
 * Class containing a DatasetData object.
 * <p> Wraps function calls to the DatasetData contained.
 */
public class DatasetWrapper extends RepositoryObjectWrapper<DatasetData> implements Dataset {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "DatasetAnnotationLink";

    private static final Long[] LONGS = new Long[0];


    /**
     * Constructor of the Dataset class
     *
     * @param name        Name of the dataset.
     * @param description Description of the dataset.
     */
    public DatasetWrapper(String name, String description) {
        super(new DatasetData());
        this.data.setName(name);
        this.data.setDescription(description);
    }


    /**
     * Constructor of the Dataset class
     *
     * @param dataObject Dataset to be contained.
     */
    public DatasetWrapper(DatasetData dataObject) {
        super(dataObject);
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
     * Gets all images in the dataset available from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImages(Client client) throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = handleServiceAndAccess(client.getBrowseFacility(),
                                                              bf -> bf.getImagesForDatasets(client.getCtx(),
                                                                                            Collections.singletonList(
                                                                                                    asDataObject().getId())),
                                                              "Cannot get images from " + this);
        return wrap(images, ImageWrapper::new);
    }


    /**
     * Gets all images in the dataset with a certain key
     *
     * @param client The client handling the connection.
     * @param key    Name of the key researched.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesKey(Client client, String key)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with key \"" + key + "\" from " + this;
        Collection<ImageData> images = handleServiceAndAccess(client.getBrowseFacility(),
                                                              bf -> bf.getImagesForDatasets(client.getCtx(),
                                                                                            Collections.singletonList(
                                                                                                    asDataObject().getId())),
                                                              error);

        List<Image> selected = new ArrayList<>(images.size());
        for (ImageData image : images) {
            Image imageWrapper = new ImageWrapper(image);

            Map<String, String> pairsKeyValue = imageWrapper.getKeyValuePairs(client);
            if (pairsKeyValue.get(key) != null) {
                selected.add(imageWrapper);
            }
        }
        selected.sort(Comparator.comparing(RemoteObject::getId));

        return selected;
    }


    /**
     * Gets all images in the dataset with a certain key value pair from OMERO
     *
     * @param client The client handling the connection.
     * @param key    Name of the key researched.
     * @param value  Value associated with the key.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesPairKeyValue(Client client, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with key-value pair from " + this;
        Collection<ImageData> images = handleServiceAndAccess(client.getBrowseFacility(),
                                                              bf -> bf.getImagesForDatasets(client.getCtx(),
                                                                                            Collections.singletonList(
                                                                                                    asDataObject().getId())),
                                                              error);

        List<Image> selected = new ArrayList<>(images.size());
        for (ImageData image : images) {
            Image imageWrapper = new ImageWrapper(image);

            Map<String, String> pairsKeyValue = imageWrapper.getKeyValuePairs(client);
            if (pairsKeyValue.get(key) != null && pairsKeyValue.get(key).equals(value)) {
                selected.add(imageWrapper);
            }
        }
        selected.sort(Comparator.comparing(RemoteObject::getId));

        return selected;
    }


    /**
     * Removes an image from the dataset in OMERO.
     *
     * @param client The client handling the connection.
     * @param image  Image to remove.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    public void removeImage(Client client, Image image)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        removeLink(client, "DatasetImageLink", image.getId());
    }


    /**
     * Imports all images candidates in the paths to the dataset in OMERO.
     *
     * @param client The client handling the connection.
     * @param paths  Paths to the image files on the computer.
     *
     * @return If the import did not exit because of an error.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws IOException        Cannot read file.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public boolean importImages(Client client, String... paths)
    throws ServiceException, ServerException, AccessException, IOException, ExecutionException {
        boolean success = importImages(client, data, paths);
        refresh(client);
        return success;
    }


    /**
     * Imports one image file to the dataset in OMERO.
     *
     * @param client The client handling the connection.
     * @param path   Path to the image file on the computer.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Long> importImage(Client client, String path)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        List<Long> ids = importImage(client, data, path);
        refresh(client);
        return ids;
    }


    /**
     * Imports one image file to the dataset in OMERO and replace older images sharing the same name after copying their
     * annotations and ROIs, and concatenating the descriptions (on new lines) by unlinking or even deleting them.
     *
     * @param client The client handling the connection.
     * @param path   Path to the image on the computer.
     * @param policy Whether older images should be unlinked, deleted or deleted only if they become orphaned.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ServerException      Server error.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    public List<Long> importAndReplaceImages(Client client, String path, ReplacePolicy policy)
    throws ServiceException, AccessException, ServerException, ExecutionException, InterruptedException {
        List<Long> ids    = importImage(client, path);
        Long[]     newIds = ids.toArray(LONGS);

        List<Image>       newImages = client.getImages(newIds);
        Collection<Image> toDelete  = new ArrayList<>(newImages.size());
        for (Image image : newImages) {
            List<Image> oldImages = getImages(client, image.getName());
            oldImages.removeIf(img -> ids.contains(img.getId()));
            List<Image> orphaned = replaceImages(client, oldImages, image);
            if (policy == ReplacePolicy.DELETE) {
                toDelete.addAll(oldImages);
            } else if (policy == ReplacePolicy.DELETE_ORPHANED) {
                toDelete.addAll(orphaned);
            }
        }
        if (policy == ReplacePolicy.DELETE_ORPHANED) {
            List<Long> idsToDelete = toDelete.stream().map(RemoteObject::getId).collect(Collectors.toList());

            Iterable<Image> orphans = new ArrayList<>(toDelete);
            for (Image orphan : orphans) {
                for (Image other : orphan.getFilesetImages(client)) {
                    if (!idsToDelete.contains(other.getId()) && other.isOrphaned(client)) {
                        toDelete.add(other);
                    }
                }
            }
        }
        client.delete(toDelete);
        return ids;
    }


    /**
     * Refreshes the wrapped dataset.
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
                                      bf -> bf.getDatasets(client.getCtx(),
                                                           Collections.singletonList(this.getId()))
                                              .iterator().next(),
                                      "Cannot refresh " + this);
    }

}