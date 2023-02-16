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


import fr.igred.omero.ImageLinked;
import fr.igred.omero.RepositoryObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import omero.gateway.model.FolderData;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Folders on OMERO.
 */
public interface Folder extends RepositoryObject<FolderData>, ImageLinked<FolderData> {

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
     * Reloads the folder from OMERO, to update all links.
     */
    void reload(Browser browser)
    throws AccessException, ServiceException, ExecutionException;


    /**
     * Retrieves the images contained in this folder.
     *
     * @return See above
     */
    List<Image> getImages();


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
    void addROIs(DataManager dm, long imageId, ROI... rois)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Add an ROI to the folder and associate it to the provided image.
     *
     * @param dm    The data manager.
     * @param image The image .
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
     * Gets the ROI contained in the folder associated with the provided image ID.
     *
     * @param dm      The data manager.
     * @param imageId The image ID.
     *
     * @return List of ROIWrapper containing the ROI.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<ROI> getROIs(DataManager dm, long imageId)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the ROI contained in the folder associated with the provided image ID.
     *
     * @param dm    The data manager.
     * @param image The image.
     *
     * @return List of ROIWrapper containing the ROI.
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
    void unlinkAllROIs(DataManager dm, long imageId)
    throws ServiceException, AccessException, ExecutionException;


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
     * <p> ROIs are now linked to their images directly.
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
     * @param dm      The data manager.
     * @param rois    ROI to unlink.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void unlinkROIs(DataManager dm, ROI... rois)
    throws ServiceException, AccessException, ExecutionException;

}
