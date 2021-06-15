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
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROIWrapper;
import omero.ServerError;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.model.Folder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrServer;


/**
 * Class containing a FolderData.
 * <p> Implements function using the FolderData contained.
 */
public class FolderWrapper extends GenericRepositoryObjectWrapper<FolderData> {

    /** Id of the associated image */
    Long imageId;


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
     * @throws OMEROServerError Server error.
     */
    public FolderWrapper(Client client, String name) throws ServiceException, OMEROServerError {
        super(new FolderData());
        data.setName(name);
        try {
            Folder f = (Folder) client.getGateway()
                                      .getUpdateService(client.getCtx())
                                      .saveAndReturnObject(data.asIObject());
            data.setFolder(f);
        } catch (DSOutOfServiceException | ServerError se) {
            handleServiceOrServer(se, "Could not create Folder with name: " + name);
        }
    }


    /**
     * Gets the folder contained in the FolderWrapper
     *
     * @return the FolderData.
     */
    public FolderData asFolderData() {
        return data;
    }


    /**
     * Gets the name of the folder
     *
     * @return name.
     */
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the image associated to the folder
     *
     * @param id Id of the image to associate.
     */
    public void setImage(Long id) {
        imageId = id;
    }


    /**
     * Sets the image associated to the folder
     *
     * @param image Image to associate.
     */
    public void setImage(ImageWrapper image) {
        imageId = image.getId();
    }


    /**
     * Add an ROI to the folder and associate it to the image id set(an image need to be associated)
     *
     * @param client The client handling the connection.
     * @param roi    ROI to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    public void addROI(Client client, ROIWrapper roi)
    throws ServiceException, AccessException, ExecutionException {
        ROIFacility roiFac = client.getRoiFacility();
        try {
            roiFac.addRoisToFolders(client.getCtx(),
                                    imageId,
                                    Collections.singletonList(roi.getROI()),
                                    Collections.singletonList(data));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot add ROI to " + toString());
        }
    }


    /**
     * Gets the ROI contained in the folder associated with the image id set (an image need to be associated)
     *
     * @param client The client handling the connection.
     *
     * @return List of ROIWrapper containing the ROI.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ROIWrapper> getROIs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        ROIFacility roiFac = client.getRoiFacility();

        Collection<ROIResult> roiResults = new ArrayList<>();
        try {
            roiResults = roiFac.loadROIsForFolder(client.getCtx(), imageId, data.getId());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get ROIs from " + toString());
        }

        List<ROIWrapper> roiWrappers = new ArrayList<>(roiResults.size());
        if (!roiResults.isEmpty()) {
            ROIResult r = roiResults.iterator().next();

            Collection<ROIData> rois = r.getROIs();
            for (ROIData roi : rois) {
                ROIWrapper temp = new ROIWrapper(roi);
                roiWrappers.add(temp);
            }
        }

        return roiWrappers;
    }


    /**
     * Unlink all ROI, associated to the image set, in the folder. ROIs are now linked to the image directly
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void unlinkAllROI(Client client) throws ServiceException, AccessException, ExecutionException {
        try {
            List<ROIWrapper> rois = getROIs(client);
            for (ROIWrapper roi : rois) {
                client.getRoiFacility().removeRoisFromFolders(client.getCtx(),
                                                              this.imageId,
                                                              Collections.singletonList(roi.getROI()),
                                                              Collections.singletonList(data));
            }
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot unlink ROIs from " + toString());
        }
    }


    /**
     * Unlink an ROI, associated to the image set, in the folder. the ROI is now linked to the image directly
     *
     * @param client The client handling the connection.
     * @param roi    ROI to unlink.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void unlinkROI(Client client, ROIWrapper roi)
    throws ServiceException, AccessException, ExecutionException {
        try {
            client.getRoiFacility().removeRoisFromFolders(client.getCtx(),
                                                          this.imageId,
                                                          Collections.singletonList(roi.getROI()),
                                                          Collections.singletonList(data));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot unlink ROI from " + toString());
        }
    }

}