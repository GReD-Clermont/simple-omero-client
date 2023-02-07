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

package fr.igred.omero.screen;


import fr.igred.omero.RepositoryObject;
import omero.gateway.model.PlateAcquisitionData;

import java.sql.Timestamp;


/**
 * Interface to handle Plate Acquisitions on OMERO.
 */
public interface PlateAcquisition extends RepositoryObject<PlateAcquisitionData> {

    /**
     * Sets the name of the plate acquisition.
     *
     * @param name The name of the plate acquisition. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Sets the description of the plate acquisition.
     *
     * @param description The description of the plate acquisition.
     */
    void setDescription(String description);


    /**
     * Returns the label associated to the plate acquisition.
     *
     * @return See above.
     */
    String getLabel();


    /**
     * Returns the id of the plate of reference.
     *
     * @return See above.
     */
    long getRefPlateId();


    /**
     * Sets the id of the plate this plate acquisition is for.
     *
     * @param refPlateId The value to set.
     */
    void setRefPlateId(long refPlateId);


    /**
     * Returns the time when the first image was collected.
     *
     * @return See above.
     */
    Timestamp getStartTime();


    /**
     * Returns the time when the last image was collected.
     *
     * @return See above.
     */
    Timestamp getEndTime();


    /**
     * Returns the maximum number of fields in any well.
     *
     * @return See above.
     */
    int getMaximumFieldCount();

}
