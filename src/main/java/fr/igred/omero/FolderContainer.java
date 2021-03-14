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

package fr.igred.omero;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.metadata.ROIContainer;
import fr.igred.omero.exception.ServerError;
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


/**
 * Class containing a FolderData.
 * <p> Implements function using the FolderData contained.
 */
public class FolderContainer {

    /** Folder contained */
    final FolderData folder;
    /** Id of the associated image */
    Long imageId;


    /**
     * Constructor of the FolderContainer class.
     *
     * @param folder FolderData to contain.
     */
    public FolderContainer(FolderData folder) {
        this.folder = folder;
    }


    /**
     * Constructor of the FolderContainer class.
     *
     * @param folder Folder to contain.
     */
    public FolderContainer(Folder folder) {
        this.folder = new FolderData(folder);
    }


    /**
     * Constructor of the FolderContainer class. Save the folder in OMERO
     *
     * @param client The user.
     * @param name   Name of the folder.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerError      Server error.
     */
    public FolderContainer(Client client, String name) throws ServiceException, ServerError {
        folder = new FolderData();
        folder.setName(name);
        try {
            Folder f = (Folder) client.getGateway()
                                      .getUpdateService(client.getCtx())
                                      .saveAndReturnObject(folder.asIObject());
            folder.setFolder(f);
        } catch (DSOutOfServiceException os) {
            throw new ServiceException("Cannot connect to OMERO", os, os.getConnectionStatus());
        } catch (omero.ServerError se) {
            throw new ServerError("Server error", se);
        }
    }


    /**
     * Gets the folder contained in the FolderContainer
     *
     * @return the FolderData.
     */
    public FolderData getFolder() {
        return folder;
    }


    /**
     * Gets the folder id
     *
     * @return Id.
     */
    public Long getId() {
        return folder.getId();
    }


    /**
     * Gets the name of the folder
     *
     * @return name.
     */
    public String getName() {
        return folder.getName();
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
    public void setImage(ImageContainer image) {
        imageId = image.getId();
    }


    /**
     * Add an ROI to the folder and associate it to the image id set(an image need to be associated)
     *
     * @param client The user.
     * @param roi    ROI to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    public void addROI(Client client, ROIContainer roi)
    throws ServiceException, AccessException, ExecutionException {
        ROIFacility roiFac = client.getRoiFacility();
        try {
            roiFac.addRoisToFolders(client.getCtx(),
                                    imageId,
                                    Collections.singletonList(roi.getROI()),
                                    Collections.singletonList(folder));
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }
    }


    /**
     * Gets the ROI contained in the folder associated with the image id set (an image need to be associated)
     *
     * @param client The user.
     *
     * @return List of ROIContainer containing the ROI.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ROIContainer> getROIs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIContainer>    roiContainers = new ArrayList<>();
        Collection<ROIResult> roiResults;
        ROIFacility           roiFac = client.getRoiFacility();

        try {
            roiResults = roiFac.loadROIsForFolder(client.getCtx(), imageId, folder.getId());
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        if (roiResults.size() != 0) {
            ROIResult           r    = roiResults.iterator().next();
            Collection<ROIData> rois = r.getROIs();

            for (ROIData roi : rois) {
                ROIContainer temp = new ROIContainer(roi);
                roiContainers.add(temp);
            }
        }

        return roiContainers;
    }


    /**
     * Unlink all ROI, associated to the image set, in the folder. ROIs are now linked to the image directly
     *
     * @param client The user.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void unlinkAllROI(Client client) throws ServiceException, AccessException, ExecutionException {
        try {
            List<ROIContainer> rois = getROIs(client);
            for (ROIContainer roi : rois) {
                client.getRoiFacility().removeRoisFromFolders(client.getCtx(),
                                                              this.imageId,
                                                              Collections.singletonList(roi.getROI()),
                                                              Collections.singletonList(folder));
            }
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }
    }


    /**
     * Unlink an ROI, associated to the image set, in the folder. the ROI is now linked to the image directly
     *
     * @param client The user.
     * @param roi    ROI to unlink.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void unlinkROI(Client client, ROIContainer roi)
    throws ServiceException, AccessException, ExecutionException {
        try {
            client.getRoiFacility().removeRoisFromFolders(client.getCtx(),
                                                          this.imageId,
                                                          Collections.singletonList(roi.getROI()),
                                                          Collections.singletonList(folder));
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }
    }

}