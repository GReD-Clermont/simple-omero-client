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


import fr.igred.omero.RepositoryObjectWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;
import static fr.igred.omero.util.Wrapper.wrap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;


/**
 * Class containing a DatasetData object.
 * <p> Wraps function calls to the DatasetData contained.
 */
public class DatasetWrapper extends RepositoryObjectWrapper<DatasetData> implements Dataset {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "DatasetAnnotationLink";

    private static final Long[] LONGS = new Long[0];


    /**
     * Constructor of the DatasetWrapper class
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
     * Returns the type of annotation link for this object
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Retrieves the projects containing this dataset.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServerException    Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Project> getProjects(Browser browser)
    throws ServerException, ServiceException, AccessException, ExecutionException {
        List<IObject> os = browser.findByQuery("select link.parent from ProjectDatasetLink as link " +
                                               "where link.child=" + getId());
        return browser.getProjects(
                os.stream().map(IObject::getId).map(RLong::getValue).distinct().toArray(Long[]::new));
    }


    /**
     * Retrieves this dataset as a singleton list.
     *
     * @param browser The data browser (unused).
     *
     * @return See above.
     */
    @Override
    public List<Dataset> getDatasets(Browser browser) {
        return Collections.singletonList(this);
    }


    /**
     * Gets all images in the dataset available from OMERO.
     *
     * @param browser The data browser.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImages(Browser browser) throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = handleServiceAndAccess(browser.getBrowseFacility(),
                                                              bf -> bf.getImagesForDatasets(browser.getCtx(),
                                                                                            Collections.singletonList(
                                                                                                    data.getId())),
                                                              "Cannot get images from " + this);
        return wrap(images, ImageWrapper::new);
    }


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
    @Override
    public List<Image> getImages(Browser browser, String name)
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
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesLike(Browser browser, String motif)
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
     * @param tag     TagAnnotationWrapper containing the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesTagged(Browser browser, TagAnnotation tag)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        return getImagesTagged(browser, tag.getId());
    }


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
    @Override
    public List<Image> getImagesTagged(Browser browser, Long tagId)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        Long[] ids = browser.findByQuery("select link.parent " +
                                         "from ImageAnnotationLink link " +
                                         "where link.child = " +
                                         tagId +
                                         " and link.parent in " +
                                         "(select link2.child " +
                                         "from DatasetImageLink link2 " +
                                         "where link2.parent = " +
                                         data.getId() + ")")
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
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesWithKey(Browser browser, String key)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with key \"" + key + "\" from " + this;
        Collection<ImageData> images = handleServiceAndAccess(browser.getBrowseFacility(),
                                                              bf -> bf.getImagesForDatasets(browser.getCtx(),
                                                                                            Collections.singletonList(
                                                                                                    data.getId())),
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
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImagesWithKeyValuePair(Browser browser, String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with key-value pair from " + this;
        Collection<ImageData> images = handleServiceAndAccess(browser.getBrowseFacility(),
                                                              bf -> bf.getImagesForDatasets(browser.getCtx(),
                                                                                            Collections.singletonList(
                                                                                                    data.getId())),
                                                              error);

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
     * @param client The client handling the connection.
     * @param images Image to add to the dataset.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void addImages(Client client, Iterable<? extends Image> images)
    throws ServiceException, AccessException, ExecutionException {
        for (Image image : images) {
            addImage(client, image);
        }
    }


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
    @Override
    public void addImage(Client client, Image image)
    throws ServiceException, AccessException, ExecutionException {
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild(image.asDataObject().asImage());
        link.setParent(new DatasetI(data.getId(), false));

        client.save(link);
        refresh(client);
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
    @Override
    public List<Image> replaceImages(Client client,
                                     Collection<? extends Image> oldImages,
                                     Image newImage)
    throws AccessException, ServiceException, ExecutionException, ServerException, InterruptedException {
        Collection<String> descriptions = new ArrayList<>(oldImages.size() + 1);
        List<Image>        orphaned     = new ArrayList<>(oldImages.size());
        descriptions.add(newImage.getDescription());
        for (Image oldImage : oldImages) {
            descriptions.add(oldImage.getDescription());
            newImage.copyAnnotationLinks(client, oldImage);
            List<ROI> rois = oldImage.getROIs(client);
            newImage.saveROIs(client, rois);
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
            List<Long> idsToDelete = toDelete.stream().map(RemoteObject::getId).collect(toList());

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
    @Override
    public List<Long> importAndReplaceImages(Client client, String path)
    throws ServiceException, AccessException, ServerException, ExecutionException, InterruptedException {
        return importAndReplaceImages(client, path, ReplacePolicy.UNLINK);
    }


    /**
     * Refreshes the wrapped dataset.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void refresh(Browser browser) throws ServiceException, AccessException, ExecutionException {
        data = handleServiceAndAccess(browser.getBrowseFacility(),
                                      bf -> bf.getDatasets(browser.getCtx(),
                                                           Collections.singletonList(this.getId()))
                                              .iterator().next(),
                                      "Cannot refresh " + this);
    }

}