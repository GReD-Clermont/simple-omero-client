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


import fr.igred.omero.AnnotatableWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.WellSampleData;
import omero.model.IObject;
import omero.model._PlateAcquisitionOperationsNC;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Class containing a PlateAcquisitionData object.
 * <p> Wraps function calls to the PlateAcquisitionData contained.
 */
public class PlateAcquisitionWrapper extends AnnotatableWrapper<PlateAcquisitionData> implements PlateAcquisition {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "PlateAcquisitionAnnotationLink";


    /**
     * Constructor of the class PlateAcquisitionWrapper.
     *
     * @param plateAcquisition The PlateAcquisitionData to wrap in the PlateAcquisitionWrapper.
     */
    public PlateAcquisitionWrapper(PlateAcquisitionData plateAcquisition) {
        super(plateAcquisition);
        initRefPlate();
    }


    /**
     * Initializes the ref. plate ID to what is stored in the underlying IObject.
     */
    private void initRefPlate() {
        omero.model.Plate plate = ((_PlateAcquisitionOperationsNC) data.asIObject()).getPlate();
        if (plate != null) data.setRefPlateId(plate.getId().getValue());
    }


    /**
     * Returns the type of annotation link for this object.
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
    @Override
    public void setName(String name) {
        data.setName(name);
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
    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Retrieves the well samples for this plate acquisition.
     *
     * @return See above.
     */
    @Override
    public List<WellSample> getWellSamples() {
        return ((_PlateAcquisitionOperationsNC) data.asIObject()).copyWellSample()
                                                                 .stream()
                                                                 .map(WellSampleData::new)
                                                                 .map(WellSampleWrapper::new)
                                                                 .collect(Collectors.toList());
    }


    /**
     * Retrieves the well samples for this plate acquisition from OMERO and updates the object.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<WellSample> getWellSamples(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        reload(browser);
        return getWellSamples();
    }


    /**
     * Returns the label associated to the plate acquisition.
     *
     * @return See above.
     */
    @Override
    public String getLabel() {
        return data.getLabel();
    }


    /**
     * Returns the id of the plate of reference.
     *
     * @return See above.
     */
    @Override
    public long getRefPlateId() {
        return data.getRefPlateId();
    }


    /**
     * Sets the id of the plate this plate acquisition is for.
     *
     * @param refPlateId The value to set.
     */
    @Override
    public void setRefPlateId(long refPlateId) {
        data.setRefPlateId(refPlateId);
    }


    /**
     * Returns the time when the first image was collected.
     *
     * @return See above.
     */
    @Override
    public Timestamp getStartTime() {
        return data.getStartTime();
    }


    /**
     * Returns the time when the last image was collected.
     *
     * @return See above.
     */
    @Override
    public Timestamp getEndTime() {
        return data.getEndTime();
    }


    /**
     * Returns the maximum number of fields in any well.
     *
     * @return See above.
     */
    @Override
    public int getMaximumFieldCount() {
        return data.getMaximumFieldCount();
    }


    /**
     * Reloads the plate acquisition from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void reload(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        String query = "select pa from PlateAcquisition as pa " +
                       "left outer join fetch pa.plate as p " +
                       "left outer join fetch pa.wellSample as ws " +
                       "left outer join fetch ws.plateAcquisition as pa2 " +
                       "left outer join fetch ws.well as w " +
                       "left outer join fetch ws.image as img " +
                       "left outer join fetch img.pixels as pix " +
                       "left outer join fetch pix.pixelsType as pt " +
                       "where pa.id=" + getId();
        IObject o = browser.findByQuery(query).iterator().next();
        data = new PlateAcquisitionData((omero.model.PlateAcquisition) o);
        initRefPlate();
    }

}
