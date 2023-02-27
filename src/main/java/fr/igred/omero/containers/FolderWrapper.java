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
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.annotations.AnnotationWrapper;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROIWrapper;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.DataObject;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.model.Folder;
import omero.model.FolderAnnotationLink;
import omero.model.FolderAnnotationLinkI;
import omero.model.FolderImageLink;
import omero.model.FolderImageLinkI;
import omero.model.IObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Class containing a FolderData object.
 * <p> Wraps function calls to the FolderData contained.
 */
public class FolderWrapper extends RepositoryObjectWrapper<FolderData> {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "FolderAnnotationLink";

    /** Empty ROI array for fast list conversion */
    private static final ROIWrapper[] EMPTY_ROI_ARRAY = new ROIWrapper[0];


    /**
     * Constructor of the FolderWrapper class.
     *
     * @param folder The FolderData to wrap.
     */
    public FolderWrapper(FolderData folder) {
        super(folder);
    }


    /**
     * Constructor of the FolderWrapper class.
     *
     * @param folder Folder to contain.
     */
    public FolderWrapper(Folder folder) {
        super(new FolderData(folder));
    }


    /**
     * Constructor of the FolderWrapper class. Save the folder in OMERO
     *
     * @param client The client handling the connection.
     * @param name   Name of the folder.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    public FolderWrapper(ConnectionHandler client, String name) throws ServiceException, ServerException {
        super(new FolderData());
        data.setName(name);
        Folder f = (Folder) ExceptionHandler.of(client.getGateway(),
                                                g -> g.getUpdateService(client.getCtx())
                                                      .saveAndReturnObject(data.asIObject()))
                                            .handleServiceOrServer("Could not create Folder with name: " + name)
                                            .get();
        data.setFolder(f);
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
    public <A extends AnnotationWrapper<?>> void link(DataManager dm, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        FolderAnnotationLink link = new FolderAnnotationLinkI();
        link.setChild(annotation.asDataObject().asAnnotation());
        link.setParent(data.asFolder());
        dm.save(link);
    }


    /**
     * Gets the name of the folder
     *
     * @return name.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the folder.
     *
     * @param name The name of the folder. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Gets the folder description
     *
     * @return The folder description.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the folder.
     *
     * @param description The folder description.
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Retrieves the parent folders for this folder.
     *
     * @return See above
     */
    public FolderWrapper getParent() {
        return new FolderWrapper(data.getParentFolder());
    }


    /**
     * Sets the parent folder for this folder.
     *
     * @param folder The new parent folder.
     */
    public void setParent(FolderWrapper folder) {
        data.setParentFolder(folder.asDataObject().asFolder());
    }


    /**
     * Adds a child folder to this folder.
     *
     * @param folder The new child folder.
     */
    public void addChild(FolderWrapper folder) {
        data.asFolder().addChildFolders(folder.asDataObject().asFolder());
    }


    /**
     * Adds children folders to this folder.
     *
     * @param folders The new children folders.
     */
    public void addChildren(Collection<? extends FolderWrapper> folders) {
        data.asFolder().addAllChildFoldersSet(folders.stream()
                                                     .map(ObjectWrapper::asDataObject)
                                                     .map(DataObject::asFolder)
                                                     .collect(Collectors.toList()));
    }


    /**
     * Retrieves the children folders for this folder.
     *
     * @return See above
     */
    public List<FolderWrapper> getChildren() {
        return wrap(data.copyChildFolders(), FolderWrapper::new);
    }


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
    public void addImages(DataManager dm, ImageWrapper... images)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> links     = new ArrayList<>(images.length);
        List<Long>    linkedIds = getImages().stream().map(ObjectWrapper::getId).collect(Collectors.toList());
        for (ImageWrapper image : images) {
            if (!linkedIds.contains(image.getId())) {
                FolderImageLink link = new FolderImageLinkI();
                link.setChild(image.asDataObject().asImage());
                link.setParent(data.asFolder());
                links.add(link);
            }
        }
        ExceptionHandler.of(dm.getDm(), d -> d.saveAndReturnObject(dm.getCtx(), links, null, null))
                        .handleServiceOrAccess("Cannot save links.")
                        .rethrow();
    }


    /**
     * Retrieves the images contained in this folder.
     *
     * @return See above
     */
    public List<ImageWrapper> getImages() {
        return wrap(data.copyImageLinks(), ImageWrapper::new);
    }


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
    public List<ImageWrapper> getImages(Browser browser) throws AccessException, ServiceException, ExecutionException {
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
    public void addROIs(DataManager dm, long imageId, ROIWrapper... rois)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIData> roiData = Arrays.stream(rois)
                                      .map(ObjectWrapper::asDataObject)
                                      .collect(Collectors.toList());
        ROIFacility roiFac = dm.getRoiFacility();
        ExceptionHandler.of(roiFac,
                            rf -> rf.addRoisToFolders(dm.getCtx(),
                                                      imageId,
                                                      roiData,
                                                      Collections.singletonList(data)))
                        .handleServiceOrAccess("Cannot add ROIs to " + this)
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
    public void addROIs(DataManager dm, ImageWrapper image, ROIWrapper... rois)
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
    public List<ROIWrapper> getROIs(DataManager dm, long imageId)
    throws ServiceException, AccessException, ExecutionException {
        ROIFacility roiFac = dm.getRoiFacility();

        Collection<ROIResult> roiResults = ExceptionHandler.of(roiFac,
                                                               rf -> rf.loadROIsForFolder(dm.getCtx(), imageId,
                                                                                          data.getId()))
                                                           .handleServiceOrAccess("Cannot get ROIs from " + this)
                                                           .get();

        List<ROIWrapper> roiWrappers = roiResults.stream()
                                                 .map(ROIResult::getROIs)
                                                 .flatMap(Collection::stream)
                                                 .map(ROIWrapper::new)
                                                 .sorted(Comparator.comparing(ObjectWrapper::getId))
                                                 .collect(Collectors.toList());

        return distinct(roiWrappers);
    }


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
    public List<ROIWrapper> getROIs(DataManager dm, ImageWrapper image)
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
    public void unlinkAllROIs(DataManager dm, long imageId)
    throws ServiceException, AccessException, ExecutionException {
        unlinkROIs(dm, getROIs(dm, imageId).toArray(EMPTY_ROI_ARRAY));
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
    public void unlinkAllROIs(DataManager dm, ImageWrapper image)
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
    public void unlinkAllROIs(DataManager dm)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ROIWrapper> rois = wrap(data.copyROILinks(), ROIWrapper::new);
        unlinkROIs(dm, rois.toArray(EMPTY_ROI_ARRAY));
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
    public void unlinkROIs(DataManager dm, ROIWrapper... rois)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIData> roiData = Arrays.stream(rois)
                                      .map(ObjectWrapper::asDataObject)
                                      .collect(Collectors.toList());
        ExceptionHandler.ofConsumer(dm.getRoiFacility(),
                                    rf -> rf.removeRoisFromFolders(dm.getCtx(),
                                                                   -1L,
                                                                   roiData,
                                                                   Collections.singletonList(data)))
                        .handleServiceOrAccess("Cannot unlink ROI from " + this)
                        .rethrow();
    }


    /**
     * Reloads the folder from OMERO, to update all links.
     *
     * @param browser The data browser.
     *
     * @throws AccessException    Cannot access data.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void reload(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        data = ExceptionHandler.of(browser.getBrowseFacility(),
                                   bf -> bf.loadFolders(browser.getCtx(), Collections.singletonList(data.getId())))
                               .handleServiceOrAccess("Cannot reload " + this)
                               .get()
                               .iterator()
                               .next();
    }

}