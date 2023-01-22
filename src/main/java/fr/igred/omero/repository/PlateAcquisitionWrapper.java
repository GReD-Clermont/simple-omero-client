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
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.TagAnnotationData;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;

import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;


/**
 * Class containing a PlateAcquisitionData object.
 * <p> Wraps function calls to the PlateAcquisitionData contained.
 */
public class PlateAcquisitionWrapper extends GenericRepositoryObjectWrapper<PlateAcquisitionData> {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "PlateAcquisitionAnnotationLink";


    /**
     * Constructor of the class PlateAcquisitionWrapper.
     *
     * @param plateAcquisition The plate acquisition contained in the PlateAcquisitionWrapper.
     */
    public PlateAcquisitionWrapper(PlateAcquisitionData plateAcquisition) {
        super(plateAcquisition);
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
     * Gets the plate acquisition name.
     *
     * @return See above.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the plate acquisition.
     *
     * @param name The name of the plate acquisition. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Returns the PlateAcquisitionData contained.
     *
     * @return See above.
     */
    public PlateAcquisitionData asPlateAcquisitionData() {
        return data;
    }


    /**
     * Gets the plate acquisition description
     *
     * @return See above.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the plate acquisition.
     *
     * @param description The description of the plate acquisition.
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Protected function. Adds a tag to the object in OMERO, if possible.
     *
     * @param client  The client handling the connection.
     * @param tagData Tag to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    protected void addTag(Client client, TagAnnotationData tagData)
    throws ServiceException, AccessException, ExecutionException {
        PlateAcquisitionAnnotationLink link = new PlateAcquisitionAnnotationLinkI();
        link.setChild(tagData.asAnnotation());
        link.setParent((PlateAcquisition) data.asIObject());
        client.save(link);
    }


    /**
     * Returns the label associated to the plate acquisition.
     *
     * @return See above.
     */
    public String getLabel() {
        return data.getLabel();
    }


    /**
     * Returns the id of the plate of reference.
     *
     * @return See above.
     */
    public long getRefPlateId() {
        return data.getRefPlateId();
    }


    /**
     * Sets the id of the plate this plate acquisition is for.
     *
     * @param refPlateId The value to set.
     */
    public void setRefPlateId(long refPlateId) {
        data.setRefPlateId(refPlateId);
    }


    /**
     * Returns the time when the first image was collected.
     *
     * @return See above.
     */
    public Timestamp getStartTime() {
        return data.getStartTime();
    }


    /**
     * Returns the time when the last image was collected.
     *
     * @return See above.
     */
    public Timestamp getEndTime() {
        return data.getEndTime();
    }


    /**
     * Returns the maximum number of fields in any well.
     *
     * @return See above.
     */
    public int getMaximumFieldCount() {
        return data.getMaximumFieldCount();
    }

}
