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

package fr.igred.omero.containers;


import fr.igred.omero.ContainerLinked;
import fr.igred.omero.RepositoryObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.ReplacePolicy;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Datasets on OMERO.
 */
public interface Dataset extends RepositoryObject<DatasetData>, ContainerLinked<DatasetData> {

    /**
     * Sets the name of the dataset.
     *
     * @param name The name of the dataset. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Sets the description of the dataset.
     *
     * @param description The description of the dataset.
     */
    void setDescription(String description);


    /**
     * Gets all images in the dataset with a certain name from OMERO.
     *
     * @param browser The data browser.
     * @param name    Name searched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImages(Browser browser, String name)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all images in the dataset with a certain motif in their name from OMERO.
     *
     * @param browser The data browser.
     * @param motif   Motif searched in an image name.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImagesLike(Browser browser, String motif)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all images in the dataset tagged with a specified tag from OMERO.
     *
     * @param browser The data browser.
     * @param tag     TagAnnotationWrapper containing the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImagesTagged(Browser browser, TagAnnotation tag)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all images in the dataset tagged with a specified tag from OMERO.
     *
     * @param browser The data browser.
     * @param tagId   Id of the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImagesTagged(Browser browser, Long tagId)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Gets all images in the dataset with a certain key
     *
     * @param browser The data browser.
     * @param key     Name of the key researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImagesWithKey(Browser browser, String key)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all images in the dataset with a certain key value pair from OMERO
     *
     * @param browser The data browser.
     * @param key     Name of the key researched.
     * @param value   Value associated with the key.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImagesWithKeyValuePair(Browser browser, String key, String value)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Adds a list of image to the dataset in OMERO.
     *
     * @param client The client handling the connection.
     * @param images Image to add to the dataset.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void addImages(Client client, Iterable<? extends Image> images)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Adds a single image to the dataset in OMERO
     *
     * @param client The client handling the connection.
     * @param image  Image to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void addImage(Client client, Image image)
    throws ServiceException, AccessException, ExecutionException;


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
    void removeImage(Client client, RemoteObject<? extends ImageData> image)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException;


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
    boolean importImages(Client client, String... paths)
    throws ServiceException, ServerException, AccessException, IOException, ExecutionException;


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
    List<Long> importImage(Client client, String path)
    throws ServiceException, AccessException, ServerException, ExecutionException;


    /**
     * Replaces (and unlinks) a collection of images from this dataset by a new image, after copying their annotations
     * and ROIs, and concatenating the descriptions (on new lines).
     *
     * @param client    The client handling the connection.
     * @param oldImages The list of old images to replace.
     * @param newImage  The new image.
     *
     * @return The list of images that became orphaned once replaced.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ServerException      Server error.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    List<Image> replaceImages(Client client, Collection<? extends Image> oldImages, Image newImage)
    throws AccessException, ServiceException, ExecutionException, ServerException, InterruptedException;


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
    List<Long> importAndReplaceImages(Client client, String path, ReplacePolicy policy)
    throws ServiceException, AccessException, ServerException, ExecutionException, InterruptedException;


    /**
     * Imports one image file to the dataset in OMERO and replace older images sharing the same name after copying their
     * annotations and ROIs, and concatenating the descriptions (on new lines) by unlinking them.
     *
     * @param client The client handling the connection.
     * @param path   Path to the image on the computer.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ServerException      Server error.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    List<Long> importAndReplaceImages(Client client, String path)
    throws ServiceException, AccessException, ServerException, ExecutionException, InterruptedException;


    /**
     * Refreshes the wrapped dataset.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void refresh(Browser browser) throws ServiceException, AccessException, ExecutionException;

}
