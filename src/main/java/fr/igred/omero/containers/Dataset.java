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
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.ImageBrowser;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.util.ReplacePolicy;
import omero.RLong;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;


/**
 * Interface to handle Datasets on OMERO.
 */
public interface Dataset extends RepositoryObject {

    /** Annotation link name for this type of object */
    String ANNOTATION_LINK = "DatasetAnnotationLink";


    /**
     * Returns a {@link DatasetData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    DatasetData asDataObject();


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
     * Retrieves the projects containing this dataset.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Project> getProjects(ContainersBrowser browser)
    throws ServiceException, AccessException, ExecutionException {
        String query = "select link.parent from ProjectDatasetLink as link" +
                       " where link.child=" + getId();
        List<IObject> os = browser.findByQuery(query);
        return browser.getProjects(os.stream()
                                     .map(IObject::getId)
                                     .map(RLong::getValue)
                                     .distinct()
                                     .toArray(Long[]::new));
    }


    /**
     * Gets all the images in the dataset (if it was properly loaded from OMERO).
     *
     * @return See above.
     */
    List<Image> getImages();


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
    List<Image> getImages(BasicBrowser browser)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all images in the dataset with a certain name from OMERO.
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
        List<Image> images = getImages(browser);
        images.removeIf(image -> !image.getName().equals(name));
        return images;
    }


    /**
     * Gets all images in the dataset with a certain motif in their name from OMERO.
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
        List<Image> images = getImages(browser);

        String regexp = ".*" + motif + ".*";
        images.removeIf(image -> !image.getName().matches(regexp));
        return images;
    }


    /**
     * Gets all images in the dataset tagged with a specified tag from OMERO.
     *
     * @param browser The data browser.
     * @param tag     The tag annotation.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImagesTagged(ImageBrowser browser, TagAnnotation tag)
    throws ServiceException, AccessException, ExecutionException {
        return getImagesTagged(browser, tag.getId());
    }


    /**
     * Gets all images in the dataset tagged with a specified tag from OMERO.
     *
     * @param browser The data browser.
     * @param tagId   ID of the tag researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImagesTagged(ImageBrowser browser, Long tagId)
    throws ServiceException, AccessException, ExecutionException {
        Long[] ids = browser.findByQuery("select link.parent" +
                                         " from ImageAnnotationLink link" +
                                         " where link.child = " +
                                         tagId +
                                         " and link.parent in" +
                                         " (select link2.child" +
                                         " from DatasetImageLink link2" +
                                         " where link2.parent = " +
                                         getId() + ")")
                            .stream()
                            .map(IObject::getId)
                            .map(RLong::getValue)
                            .toArray(Long[]::new);
        return browser.getImages(ids);
    }


    /**
     * Gets all images in the dataset with a certain key
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
        String error = "Cannot get images with key \"" + key + "\" from " + this;
        Collection<ImageData> images = call(browser.getBrowseFacility(),
                                            bf -> bf.getImagesForDatasets(browser.getCtx(),
                                                                          singletonList(getId())),
                                            error);

        List<Image> selected = new ArrayList<>(images.size());
        for (ImageData image : images) {
            Image imageWrapper = new ImageWrapper(image);

            Map<String, List<String>> pairs = imageWrapper.getKeyValuePairs(browser)
                                                          .stream()
                                                          .collect(groupingBy(Map.Entry::getKey,
                                                                              mapping(Map.Entry::getValue, toList())));
            if (pairs.get(key) != null) {
                selected.add(imageWrapper);
            }
        }
        selected.sort(Comparator.comparing(RemoteObject::getId));

        return selected;
    }


    /**
     * Gets all images in the dataset with a certain key value pair from OMERO
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
        Collection<ImageData> images = call(browser.getBrowseFacility(),
                                            bf -> bf.getImagesForDatasets(browser.getCtx(),
                                                                          singletonList(getId())),
                                            "Cannot get images with key-value pair from " + this);

        List<Image> selected = new ArrayList<>(images.size());
        for (ImageData image : images) {
            Image imageWrapper = new ImageWrapper(image);

            Map<String, List<String>> pairs = imageWrapper.getKeyValuePairs(browser)
                                                          .stream()
                                                          .collect(groupingBy(Map.Entry::getKey,
                                                                              mapping(Map.Entry::getValue, toList())));
            if (pairs.get(key) != null && pairs.get(key).contains(value)) {
                selected.add(imageWrapper);
            }
        }
        selected.sort(Comparator.comparing(RemoteObject::getId));

        return selected;
    }


    /**
     * Adds a list of image to the dataset in OMERO.
     *
     * @param dm     The data manager.
     * @param images Image to add to the dataset.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addImages(BasicDataManager dm, Iterable<? extends Image> images)
    throws ServiceException, AccessException, ExecutionException {
        for (Image image : images) {
            addImage(dm, image);
        }
    }


    /**
     * Adds a single image to the dataset in OMERO
     *
     * @param dm    The data manager.
     * @param image Image to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addImage(BasicDataManager dm, Image image)
    throws ServiceException, AccessException, ExecutionException {
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild(image.asDataObject().asImage());
        link.setParent(new DatasetI(getId(), false));
        dm.save(link);
    }


    /**
     * Removes an image from the dataset in OMERO.
     *
     * @param client The client handling the connection.
     * @param image  Image to remove.
     * @param <C>    The type of the client.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    <C extends BasicBrowser & BasicDataManager> void removeImage(C client, Image image)
    throws ServiceException, AccessException, ExecutionException, InterruptedException;


    /**
     * Imports all images candidates in the paths to the dataset in OMERO.
     *
     * @param conn  The connection handler.
     * @param paths Paths to the image files on the computer.
     *
     * @return If the import did not exit because of an error.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot read file.
     */
    default boolean importImages(ConnectionHandler conn, String... paths)
    throws ServiceException, AccessException, IOException {
        return importImages(conn, 1, paths);
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
    boolean importImages(ConnectionHandler conn, int threads, String... paths)
    throws ServiceException, AccessException, IOException;


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
    List<Long> importImage(ConnectionHandler conn, String path)
    throws ServiceException, AccessException, IOException;


    /**
     * Replaces (and unlinks) a collection of images from this dataset by a new image, after copying their annotations
     * and ROIs, and concatenating the descriptions (on new lines).
     *
     * @param client    The client handling the connection.
     * @param oldImages The list of old images to replace.
     * @param newImage  The new image.
     * @param <C>    The type of the client.
     *
     * @return The list of images that became orphaned once replaced.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default <C extends ContainersBrowser & BasicDataManager>
    List<Image> replaceImages(C client,
                              Collection<? extends Image> oldImages,
                              Image newImage)
    throws AccessException, ServiceException, ExecutionException, InterruptedException {
        Collection<String> descriptions = new ArrayList<>(oldImages.size() + 1);
        List<Image>        orphaned     = new ArrayList<>(oldImages.size());
        descriptions.add(newImage.getDescription());
        for (Image oldImage : oldImages) {
            descriptions.add(oldImage.getDescription());
            newImage.copyAnnotationLinks(client, oldImage);
            List<ROI> rois = oldImage.getROIs(client);
            newImage.saveROIs(client, rois);
            List<Folder> folders = oldImage.getFolders(client);
            for (Folder folder : folders) {
                folder.addImages(client, newImage);
            }
            this.removeImage(client, oldImage);
            if (oldImage.isOrphaned(client)) {
                orphaned.add(oldImage);
            }
        }
        descriptions.removeIf(s -> s == null || s.trim().isEmpty());
        //noinspection HardcodedLineSeparator
        newImage.setDescription(String.join("\n", descriptions));
        newImage.saveAndUpdate(client);
        return orphaned;
    }


    /**
     * Imports one image file to the dataset in OMERO and replace older images sharing the same name after copying their
     * annotations and ROIs, and concatenating the descriptions (on new lines) by unlinking or even deleting them.
     *
     * @param client The client handling the connection.
     * @param path   Path to the image on the computer.
     * @param policy Whether older images should be unlinked, deleted or deleted only if they become orphaned.
     * @param <C>    The type of the client.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws IOException          Cannot read file.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    <C extends ImageBrowser & DataManager & ConnectionHandler & ContainersBrowser>
    List<Long> importAndReplaceImages(C client, String path, ReplacePolicy policy)
    throws ServiceException, AccessException, IOException, ExecutionException, InterruptedException;


    /**
     * Imports one image file to the dataset in OMERO and replace older images sharing the same name after copying their
     * annotations and ROIs, and concatenating the descriptions (on new lines) by unlinking them.
     *
     * @param client The client handling the connection.
     * @param path   Path to the image on the computer.
     * @param <C>    The type of the client.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws IOException          Cannot read file.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default <C extends ImageBrowser & DataManager & ConnectionHandler & ContainersBrowser>
    List<Long> importAndReplaceImages(C client, String path)
    throws ServiceException, AccessException, IOException, ExecutionException, InterruptedException {
        return importAndReplaceImages(client, path, ReplacePolicy.UNLINK);
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
    void reload(BasicBrowser browser)
    throws ServiceException, AccessException, ExecutionException;

}
