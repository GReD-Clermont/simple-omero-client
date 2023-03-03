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

package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.ImageLinked;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import omero.gateway.model.FolderData;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Folders on OMERO.
 */
public interface Folder extends RepositoryObject, ImageLinked {

    /**
     * Returns a DataObject (or a subclass) corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    FolderData asDataObject();


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
    List<? extends Folder> getChildren();


    /**
     * Links images to the folder in OMERO.
     * <p> The folder needs to be reloaded afterwards.
     *
     * @param client     The client handling the connection.
     * @param images Images to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void addImages(Client client, Image... images)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Retrieves the images contained in this folder.
     *
     * @return See above
     */
    List<? extends Image> getImages();


    /**
     * Retrieves the images contained in this folder.
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends Image> getImages(Client client) throws AccessException, ServiceException, ExecutionException;


    /**
     * Adds ROIs to the folder and associate them to the provided image ID.
     *
     * @param client      The client handling the connection.
     * @param imageId The image ID.
     * @param rois    ROIs to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    void addROIs(Client client, long imageId, ROI... rois)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Adds ROIs to the folder and associate them to the provided image.
     *
     * @param client    The client handling the connection.
     * @param image The image.
     * @param rois  ROIs to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    void addROIs(Client client, Image image, ROI... rois)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the ROIs contained in the folder associated with the provided image ID.
     *
     * @param client      The client handling the connection.
     * @param imageId The image.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends ROI> getROIs(Client client, long imageId)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the ROIs contained in the folder associated with the provided image.
     *
     * @param client    The client handling the connection.
     * @param image The image.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<? extends ROI> getROIs(Client client, Image image)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Unlink all ROIs associated to the provided image ID from the folder.
     * <p> ROIs are now linked to the image directly.
     *
     * @param client      The client handling the connection.
     * @param imageId The image ID.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void unlinkAllROIs(Client client, long imageId)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Unlink all ROIs associated to the provided image from the folder.
     * <p> ROIs are now linked to the image directly.
     *
     * @param client    The client handling the connection.
     * @param image The image.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void unlinkAllROIs(Client client, Image image)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Unlink all ROIs associated to this folder.
     * <p> The folder must be loaded beforehand. </p>
     * <p> ROIs are now linked to their images directly.</p>
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void unlinkAllROIs(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Unlink ROIs from the folder.
     * <p> The ROIs are now linked to the image directly.
     *
     * @param client   The client handling the connection.
     * @param rois ROI to unlink.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void unlinkROIs(Client client, ROI... rois)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Reloads the folder from OMERO, to update all links.
     *
     * @param client The client handling the connection.
     *
     * @throws AccessException    Cannot access data.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void reload(Client client)
    throws AccessException, ServiceException, ExecutionException;

}