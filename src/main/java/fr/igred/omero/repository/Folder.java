/*
 *  Copyright (C) 2020-2023 GReD
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
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import omero.gateway.model.FolderData;
import omero.model.FolderAnnotationLink;
import omero.model.FolderAnnotationLinkI;
import omero.model.FolderI;

import java.util.List;
import java.util.concurrent.ExecutionException;


public interface Folder extends RepositoryObject<FolderData> {

    /**
     * Private function. Adds a tag to the object in OMERO, if possible.
     *
     * @param client The client handling the connection.
     * @param tag    Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default void addTag(Client client, TagAnnotation tag)
    throws ServiceException, AccessException, ExecutionException {
        FolderAnnotationLink link = new FolderAnnotationLinkI();
        link.setChild(tag.asDataObject().asAnnotation());
        link.setParent(new FolderI(asDataObject().getId(), false));
        client.save(link);
    }


    /**
     * Gets the name of the folder
     *
     * @return name.
     */
    @Override
    default String getName() {
        return asDataObject().getName();
    }


    /**
     * Sets the name of the folder.
     *
     * @param name The name of the folder. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    default void setName(String name) {
        asDataObject().setName(name);
    }


    /**
     * Gets the folder description
     *
     * @return The folder description.
     */
    @Override
    default String getDescription() {
        return asDataObject().getDescription();
    }


    /**
     * Sets the description of the folder.
     *
     * @param desc The folder description.
     */
    default void setDescription(String desc) {
        asDataObject().setDescription(desc);
    }


    /**
     * Sets the image associated to the folder
     *
     * @param id ID of the image to associate.
     */
    void setImage(long id);


    /**
     * Sets the image associated to the folder
     *
     * @param image Image to associate.
     */
    void setImage(Image image);


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
    void addROI(Client client, ROI roi)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the ROI contained in the folder associated with the image id set (an image need to be associated)
     *
     * @param client The client handling the connection.
     *
     * @return List of ROI containing the ROI.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<ROI> getROIs(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Unlink all ROI, associated to the image set, in the folder. ROIs are now linked to the image directly
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void unlinkAllROI(Client client) throws ServiceException, AccessException, ExecutionException;


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
    void unlinkROI(Client client, ROI roi)
    throws ServiceException, AccessException, ExecutionException;

}
