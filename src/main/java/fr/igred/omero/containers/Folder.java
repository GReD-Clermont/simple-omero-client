/*
 *  Copyright (C) 2020-2025 GReD
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
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;
import omero.model.FolderAnnotationLink;
import omero.model.FolderAnnotationLinkI;
import omero.model.FolderImageLink;
import omero.model.FolderImageLinkI;
import omero.model.IObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;


/**
 * Interface to handle Folders on OMERO.
 */
public interface Folder extends RepositoryObject {

    /** Annotation link name for this type of object */
    String ANNOTATION_LINK = "FolderAnnotationLink";


    /**
     * Returns a DataObject (or a subclass) corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    FolderData asDataObject();


    /**
     * Adds an annotation to the object in OMERO, if possible.
     *
     * @param dm         The data manager.
     * @param annotation Annotation to be added.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default <A extends AnnotationData> void link(DataManager dm, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        FolderAnnotationLink link = new FolderAnnotationLinkI();
        link.setChild(annotation.asAnnotation());
        link.setParent(asDataObject().asFolder());
        long id = ((FolderAnnotationLink) dm.save(link)).getChild().getId().getValue();
        annotation.setId(id);
    }


    /**
     * Sets the name of the folder.
     *
     * @param name The name of the folder. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Sets the description of the folder.
     *
     * @param description The folder description.
     */
    void setDescription(String description);


    /**
     * Retrieves the parent folders for this folder.
     *
     * @return See above
     */
    Folder getParent();


    /**
     * Sets the parent folder for this folder.
     *
     * @param folder The new parent folder.
     */
    void setParent(Folder folder);


    /**
     * Adds a child folder to this folder.
     *
     * @param folder The new child folder.
     */
    void addChild(Folder folder);


    /**
     * Adds children folders to this folder.
     *
     * @param folders The new children folders.
     */
    void addChildren(Collection<? extends Folder> folders);


    /**
     * Retrieves the children folders for this folder.
     *
     * @return See above
     */
    List<Folder> getChildren();


    /**
     * Links images to the folder in OMERO.
     *
     * @param dm     The data manager.
     * @param images Images to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addImages(DataManager dm, Image... images)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> links = new ArrayList<>(images.length);
        List<Long> linkedIds = getImages().stream()
                                          .map(RemoteObject::getId)
                                          .collect(toList());
        for (Image image : images) {
            if (!linkedIds.contains(image.getId())) {
                FolderImageLink link = new FolderImageLinkI();
                link.setChild(image.asDataObject().asImage());
                link.setParent(asDataObject().asFolder());
                links.add(link);
            }
        }
        ExceptionHandler.of(dm.getDMFacility(),
                            d -> d.saveAndReturnObject(dm.getCtx(), links, null, null))
                        .handleOMEROException("Cannot save links.")
                        .rethrow();
    }


    /**
     * Retrieves the images contained in this folder.
     *
     * @return See above
     */
    List<Image> getImages();


    /**
     * Retrieves the images contained in this folder.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImages(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        reload(browser);
        return getImages();
    }


    /**
     * Adds ROIs to the folder and associate them to the provided image ID.
     *
     * @param dm      The data manager.
     * @param imageId The image ID.
     * @param rois    ROIs to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    default void addROIs(DataManager dm, long imageId, ROI... rois)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIData> roiData = Arrays.stream(rois)
                                      .map(ROI::asDataObject)
                                      .collect(toList());
        ROIFacility roiFac = dm.getRoiFacility();
        ExceptionHandler.of(roiFac,
                            rf -> rf.addRoisToFolders(dm.getCtx(),
                                                      imageId,
                                                      roiData,
                                                      singletonList(asDataObject())))
                        .handleOMEROException("Cannot add ROIs to " + this)
                        .rethrow();
    }


    /**
     * Adds ROIs to the folder and associate them to the provided image.
     *
     * @param dm    The data manager.
     * @param image The image.
     * @param rois  ROIs to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    default void addROIs(DataManager dm, Image image, ROI... rois)
    throws ServiceException, AccessException, ExecutionException {
        addROIs(dm, image.getId(), rois);
    }


    /**
     * Gets the ROIs contained in the folder associated with the provided image ID.
     *
     * @param dm      The data manager.
     * @param imageId The image.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<ROI> getROIs(DataManager dm, long imageId)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the ROIs contained in the folder associated with the provided image.
     *
     * @param dm    The data manager.
     * @param image The image.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<ROI> getROIs(DataManager dm, Image image)
    throws ServiceException, AccessException, ExecutionException {
        return getROIs(dm, image.getId());
    }


    /**
     * Unlink all ROIs associated to the provided image ID from the folder.
     * <p> ROIs are now linked to the image directly.
     *
     * @param dm      The data manager.
     * @param imageId The image ID.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void unlinkAllROIs(DataManager dm, long imageId)
    throws ServiceException, AccessException, ExecutionException {
        unlinkROIs(dm, getROIs(dm, imageId));
    }


    /**
     * Unlink all ROIs associated to the provided image from the folder.
     * <p> ROIs are now linked to the image directly.
     *
     * @param dm    The data manager.
     * @param image The image.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void unlinkAllROIs(DataManager dm, Image image)
    throws ServiceException, AccessException, ExecutionException {
        unlinkAllROIs(dm, image.getId());
    }


    /**
     * Unlink all ROIs associated to this folder.
     * <p> The folder must be loaded beforehand. </p>
     * <p> ROIs are now linked to their images directly.</p>
     *
     * @param dm The data manager.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void unlinkAllROIs(DataManager dm)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Unlink ROIs from the folder.
     * <p> The ROIs are now linked to the image directly.
     *
     * @param dm   The data manager.
     * @param rois ROI to unlink.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void unlinkROIs(DataManager dm, ROI... rois)
    throws ServiceException, AccessException, ExecutionException {
        unlinkROIs(dm, Arrays.asList(rois));
    }


    /**
     * Unlink ROIs from the folder.
     * <p> The ROIs are now linked to the image directly.
     *
     * @param dm   The data manager.
     * @param rois ROI to unlink.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void unlinkROIs(DataManager dm, Collection<? extends ROI> rois)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIData> roiData = rois.stream()
                                    .map(ROI::asDataObject)
                                    .collect(toList());
        ExceptionHandler.ofConsumer(dm.getRoiFacility(),
                                    rf -> rf.removeRoisFromFolders(dm.getCtx(),
                                                                   -1L,
                                                                   roiData,
                                                                   singletonList(asDataObject())))
                        .handleOMEROException("Cannot unlink ROI from " + this)
                        .rethrow();
    }

}
