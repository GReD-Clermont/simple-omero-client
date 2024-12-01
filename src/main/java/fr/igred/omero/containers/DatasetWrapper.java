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
import fr.igred.omero.RepositoryObjectWrapper;
import fr.igred.omero.client.BasicBrowser;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.ReplacePolicy;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;


/**
 * Class containing a DatasetData object.
 * <p> Wraps function calls to the DatasetData contained.
 */
public class DatasetWrapper extends RepositoryObjectWrapper<DatasetData> implements Dataset {

    private static final Long[] LONGS = new Long[0];


    /**
     * Constructor of the DatasetWrapper class
     *
     * @param name        Name of the dataset.
     * @param description Description of the dataset.
     */
    public DatasetWrapper(String name, String description) {
        super(new DatasetData());
        data.setName(name);
        data.setDescription(description);
    }


    /**
     * Constructor of the DatasetWrapper class
     *
     * @param dataset Dataset to be contained.
     */
    public DatasetWrapper(DatasetData dataset) {
        super(dataset);
    }


    /**
     * Gets the DatasetData name
     *
     * @return DatasetData name.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the dataset.
     *
     * @param name The name of the dataset. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    @Override
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Gets the DatasetData description
     *
     * @return DatasetData description.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the dataset.
     *
     * @param description The description of the dataset.
     */
    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Returns the type of annotation link for this object.
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Gets all the images in the dataset (if it was properly loaded from OMERO).
     *
     * @return See above.
     */
    @Override
    public List<Image> getImages() {
        return wrap(data.getImages(), ImageWrapper::new);
    }


    /**
     * Gets all images in the dataset available from OMERO.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImages(BasicBrowser browser)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = call(browser.getBrowseFacility(),
                                            bf -> bf.getImagesForDatasets(browser.getCtx(),
                                                                          singletonList(data.getId())),
                                            "Cannot get images from " + this);
        return wrap(images, ImageWrapper::new);
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
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    public void removeImage(Client client, Image image)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        removeLink(client, "DatasetImageLink", image.getId());
    }


    /**
     * Imports all images candidates in the paths to the dataset in OMERO.
     *
     * @param conn    The connection handler.
     * @param threads The number of threads (same value used for filesets and uploads).
     * @param paths   Paths to the image files on the computer.
     *
     * @return If the import did not exit because of an error.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot read file.
     */
    @Override
    public boolean importImages(ConnectionHandler conn, int threads, String... paths)
    throws ServiceException, AccessException, IOException {
        return importImages(conn, data, threads, paths);
    }


    /**
     * Imports one image file to the dataset in OMERO.
     *
     * @param conn The connection handler.
     * @param path Path to the image file on the computer.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot read file.
     */
    @Override
    public List<Long> importImage(ConnectionHandler conn, String path)
    throws ServiceException, AccessException, IOException {
        return importImage(conn, data, path);
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
     * @throws IOException          Cannot read file.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    public List<Long> importAndReplaceImages(Client client, String path, ReplacePolicy policy)
    throws ServiceException, AccessException, IOException, ExecutionException, InterruptedException {
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
            List<Long> idsToDelete = toDelete.stream()
                                             .map(RemoteObject::getId)
                                             .collect(toList());

            Iterable<Image> orphans = new ArrayList<>(toDelete);
            for (Image orphan : orphans) {
                for (Image other : orphan.getFilesetImages(client)) {
                    if (other.isOrphaned(client) && !idsToDelete.contains(other.getId())) {
                        toDelete.add(other);
                    }
                }
            }
        }
        client.delete(toDelete);
        return ids;
    }


    /**
     * Reloads the dataset from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void reload(BasicBrowser browser)
    throws ServiceException, AccessException, ExecutionException {
        data = call(browser.getBrowseFacility(),
                    bf -> bf.getDatasets(browser.getCtx(),
                                         singletonList(getId()))
                            .iterator()
                            .next(),
                    "Cannot reload " + this);
    }

}