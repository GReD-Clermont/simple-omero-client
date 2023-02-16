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


import fr.igred.omero.RemoteObject;
import fr.igred.omero.RepositoryObjectWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.ROIWrapper;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.model.FolderAnnotationLink;
import omero.model.FolderAnnotationLinkI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndServer;
import static fr.igred.omero.util.Wrapper.wrap;


/**
 * Class containing a FolderData object.
 * <p> Wraps function calls to the FolderData contained.
 */
public class FolderWrapper extends RepositoryObjectWrapper<FolderData> implements Folder {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "FolderAnnotationLink";

    /** Empty ROI array for fast list conversion */
    private static final ROI[] EMPTY_ROI_ARRAY = new ROI[0];


    /**
     * Constructor of the FolderWrapper class.
     *
     * @param folder FolderData to contain.
     */
    public FolderWrapper(FolderData folder) {
        super(folder);
    }


    /**
     * Constructor of the FolderWrapper class.
     *
     * @param folder Folder to contain.
     */
    public FolderWrapper(omero.model.Folder folder) {
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
        omero.model.Folder f = (omero.model.Folder) handleServiceAndServer(client.getGateway(),
                                                                           g -> g.getUpdateService(client.getCtx())
                                                                                 .saveAndReturnObject(data.asIObject()),
                                                                           "Could not create Folder with name: " +
                                                                           name);
        data.setFolder(f);
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
     * Adds an annotation to the object in OMERO, if possible.
     *
     * @param dm         The data manager.
     * @param annotation Annotation to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public <A extends Annotation<?>> void link(DataManager dm, A annotation)
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
    @Override
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
    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }


    @Override
    public void reload(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        data = browser.getFolder(getId()).asDataObject();
    }


    /**
     * Retrieves the images contained in this folder.
     *
     * @return See above
     */
    public List<Image> getImages() {
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
    @Override
    public List<Image> getImages(Browser browser) throws AccessException, ServiceException, ExecutionException {
        reload(browser);
        return getImages();
    }


    /**
     * Add an ROI to the folder and associate it to the provided image ID.
     *
     * @param dm      The data manager.
     * @param imageId The image ID.
     * @param rois    ROIs to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    @Override
    public void addROIs(DataManager dm, long imageId, ROI... rois)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIData> roiData = Arrays.stream(rois).map(RemoteObject::asDataObject).collect(Collectors.toList());
        ROIFacility   roiFac  = dm.getRoiFacility();
        handleServiceAndAccess(roiFac,
                               rf -> rf.addRoisToFolders(dm.getCtx(),
                                                         imageId,
                                                         roiData,
                                                         Collections.singletonList(data)),
                               "Cannot add ROIs to " + this);
    }


    /**
     * Gets the ROI contained in the folder associated with the provided image ID.
     *
     * @param dm      The data manager.
     * @param imageId The image.
     *
     * @return List of ROIWrapper containing the ROI.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ROI> getROIs(DataManager dm, long imageId)
    throws ServiceException, AccessException, ExecutionException {
        ROIFacility roiFac = dm.getRoiFacility();

        Collection<ROIResult> roiResults = handleServiceAndAccess(roiFac,
                                                                  rf -> rf.loadROIsForFolder(dm.getCtx(),
                                                                                             imageId,
                                                                                             data.getId()),
                                                                  "Cannot get ROIs from " + this);

        Collection<Collection<ROI>> rois = new ArrayList<>(roiResults.size());
        for (ROIResult r : roiResults) {
            rois.add(wrap(r.getROIs(), ROIWrapper::new));
        }
        return RemoteObject.flatten(rois);
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
    @Override
    public void unlinkAllROIs(DataManager dm, long imageId)
    throws ServiceException, AccessException, ExecutionException {
        unlinkROIs(dm, getROIs(dm, imageId).toArray(EMPTY_ROI_ARRAY));
    }


    /**
     * Unlink all ROIs associated to this folder.
     * <p> ROIs are now linked to their images directly.
     *
     * @param dm The data manager.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void unlinkAllROIs(DataManager dm)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ROI> rois = wrap(data.copyROILinks(), ROIWrapper::new);
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
    @Override
    public void unlinkROIs(DataManager dm, ROI... rois)
    throws ServiceException, AccessException, ExecutionException {
        String        error   = "Cannot unlink ROIs from " + this;
        List<ROIData> roiData = Arrays.stream(rois).map(RemoteObject::asDataObject).collect(Collectors.toList());
        ExceptionHandler.ofConsumer(dm.getRoiFacility(),
                                    rf -> rf.removeRoisFromFolders(dm.getCtx(),
                                                                   -1L,
                                                                   roiData,
                                                                   Collections.singletonList(data)),
                                    error)
                        .rethrow(DSOutOfServiceException.class, ServiceException::new)
                        .rethrow(DSAccessException.class, AccessException::new);
    }

}