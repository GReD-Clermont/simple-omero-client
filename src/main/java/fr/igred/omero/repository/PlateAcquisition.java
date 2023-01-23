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
import omero.gateway.model.PlateAcquisitionData;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;

import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;


public interface PlateAcquisition extends RepositoryObject<PlateAcquisitionData> {

    /**
     * Gets the plate acquisition name.
     *
     * @return See above.
     */
    @Override
    default String getName() {
        return asDataObject().getName();
    }


    /**
     * Sets the name of the plate acquisition.
     *
     * @param name The name of the plate acquisition. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    default void setName(String name) {
        asDataObject().setName(name);
    }


    /**
     * Gets the plate acquisition description
     *
     * @return See above.
     */
    @Override
    default String getDescription() {
        return asDataObject().getDescription();
    }


    /**
     * Sets the description of the plate acquisition.
     *
     * @param description The description of the plate acquisition.
     */
    default void setDescription(String description) {
        asDataObject().setDescription(description);
    }


    /**
     * Protected function. Adds a tag to the object in OMERO, if possible.
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
        PlateAcquisitionAnnotationLink link = new PlateAcquisitionAnnotationLinkI();
        link.setChild(tag.asDataObject().asAnnotation());
        link.setParent((omero.model.PlateAcquisition) asIObject());
        client.save(link);
    }


    /**
     * Returns the label associated to the plate acquisition.
     *
     * @return See above.
     */
    default String getLabel() {
        return asDataObject().getLabel();
    }


    /**
     * Returns the id of the plate of reference.
     *
     * @return See above.
     */
    default long getRefPlateId() {
        return asDataObject().getRefPlateId();
    }


    /**
     * Sets the id of the plate this plate acquisition is for.
     *
     * @param refPlateId The value to set.
     */
    default void setRefPlateId(long refPlateId) {
        asDataObject().setRefPlateId(refPlateId);
    }


    /**
     * Returns the time when the first image was collected.
     *
     * @return See above.
     */
    default Timestamp getStartTime() {
        return asDataObject().getStartTime();
    }


    /**
     * Returns the time when the last image was collected.
     *
     * @return See above.
     */
    default Timestamp getEndTime() {
        return asDataObject().getEndTime();
    }


    /**
     * Returns the maximum number of fields in any well.
     *
     * @return See above.
     */
    default int getMaximumFieldCount() {
        return asDataObject().getMaximumFieldCount();
    }

}
