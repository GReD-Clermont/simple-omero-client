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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import fr.igred.omero.metadata.ROIContainer;
import omero.ServerError;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.model.Folder;

/**
 * Class containing a FodlerData.
 * Implements function using the FolderData contained.
 */
public class FolderContainer {
    //Folder contained
    FolderData folder;
    //Id of the associated image
    Long imageId;

    /**
     * Get the folder contained in the FolderContainer
     *
     * @return the FolderData
     */
    public FolderData getFolder()
    {
        return folder;
    }

    /**
     * Get the folder id
     *
     * @return Id
     */
    public Long getId() {
        return folder.getId();
    }

    /**
     * Get the name of the folder
     *
     * @return name
     */
    public String getName() {
        return folder.getName();
    }

    /**
     * Set the image associated to the folder
     *
     * @param id Id of the image to associate
     */
    public void setImage(Long id)
    {
        imageId = id;
    }

    /**
     * Set the image associated to the folder
     *
     * @param image image to associate
     */
    public void setImage(ImageContainer image)
    {
        imageId = image.getId();
    }

    /**
     * Add an ROI to the folder and associate it to the image id set(an image need to be associated)
     *
     * @param client The user
     * @param roi    ROI to add
     *
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     * @throws ExecutionException
     */
    public void addROI(Client client, ROIContainer roi)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        ROIFacility roifac = client.getRoiFacility();

        roifac.addRoisToFolders(client.getCtx(), imageId, Arrays.asList(roi.getROI()), Arrays.asList(folder));
    }

    /**
     * Get the ROI contained in the folder associated with the image id set (an image need to be associated)
     *
     * @param client The user
     *
     * @return List of ROIContainer containing the ROI
     *
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     * @throws ExecutionException
     */
    public List<ROIContainer> getROIs(Client client)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        ROIFacility roifac = client.getRoiFacility();

        Collection<ROIResult> roiresults = roifac.loadROIsForFolder(client.getCtx(), imageId, folder.getId());
        List<ROIContainer> roiContainers;

        if(roiresults.size() != 0)
        {
            ROIResult r = roiresults.iterator().next();
            Collection<ROIData> rois = r.getROIs();

            roiContainers = new ArrayList<ROIContainer>(rois.size());
            for(ROIData roi : rois) {
                ROIContainer temp = new ROIContainer(roi);

                roiContainers.add(temp);
            }
        }
        else{
            roiContainers =new ArrayList<ROIContainer>();
        }

        return roiContainers;
    }

    /**
     * Unlink all ROI, associated to the image set, in the folder.
     * ROIs are now linked to the image directly
     *
     * @param client The user
     *
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     * @throws ExecutionException
     */
    public void unlinkAllROI(Client client)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        List<ROIContainer> rois = getROIs(client);

        for(ROIContainer roi : rois) {
            client.getRoiFacility().removeRoisFromFolders(client.getCtx(), this.imageId, Arrays.asList(roi.getROI()), Arrays.asList(folder));
        }
    }

    /**
     * Unlink an ROI, associated to the image set, in the folder.
     * the ROI is now linked to the image directly
     *
     * @param client The user
     * @param roi    ROI to unlink
     *
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     * @throws ExecutionException
     */
    public void unlinkROI(Client client, ROIContainer roi)
        throws
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        client.getRoiFacility().removeRoisFromFolders(client.getCtx(), this.imageId, Arrays.asList(roi.getROI()), Arrays.asList(folder));
    }

    /**
     * Constructor of the FolderContainer class.
     *
     * @param folder FolderData to contain
     */
    public FolderContainer(FolderData folder)
    {
        this.folder = folder;
    }

    /**
     * Constructor of the FolderContainer class.
     *
     * @param folder Folder to contain
     */
    public FolderContainer(Folder folder)
    {
        this.folder = new FolderData(folder);
    }

    /**
     * Constructor of the FolderContainer class.
     * Save the folder in OMERO
     *
     * @param client The user
     * @param name   Name of the folder
     *
     * @throws DSOutOfServiceException
     * @throws ServerError
     */
    public FolderContainer(Client client, String name)
        throws
            DSOutOfServiceException,
            ServerError
    {
        folder = new FolderData();
        folder.setName(name);

        Folder f = (Folder)client.getGateway().getUpdateService(client.getCtx()).saveAndReturnObject(folder.asIObject());
        folder.setFolder(f);
    }
}