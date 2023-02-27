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


import fr.igred.omero.client.Browser;
import fr.igred.omero.client.Client;
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.annotations.AnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.RepositoryObjectWrapper;
import omero.gateway.model.PlateAcquisitionData;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model._PlateAcquisitionOperationsNC;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Class containing a PlateAcquisitionData object.
 * <p> Wraps function calls to the PlateAcquisitionData contained.
 */
public class PlateAcquisitionWrapper extends RepositoryObjectWrapper<PlateAcquisitionData> {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "PlateAcquisitionAnnotationLink";


    /**
     * Constructor of the class PlateAcquisitionWrapper.
     *
     * @param plateAcquisition The PlateAcquisitionData to wrap in the PlateAcquisitionWrapper.
     */
    public PlateAcquisitionWrapper(PlateAcquisitionData plateAcquisition) {
        super(plateAcquisition);
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
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Adds a tag to the object in OMERO, if possible.
     *
     * @param client     The client handling the connection.
     * @param annotation Tag to be added.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public <A extends AnnotationWrapper<?>> void link(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        PlateAcquisitionAnnotationLink link = new PlateAcquisitionAnnotationLinkI();
        link.setChild(annotation.asDataObject().asAnnotation());
        link.setParent((omero.model.PlateAcquisition) data.asIObject());
        client.save(link);
    }


    /**
     * Retrieves the screens containing the parent plates.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    public List<ScreenWrapper> getScreens(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        PlateWrapper plate = browser.getPlate(getRefPlateId());
        return plate.getScreens(browser);
    }


    /**
     * Returns the (updated) parent plate as a singleton list.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<PlateWrapper> getPlates(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return browser.getPlates(getRefPlateId());
    }


    /**
     * Retrieves the wells contained in the parent plate.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<WellWrapper> getWells(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return getPlates(browser).iterator().next().getWells(browser);
    }


    /**
     * Retrieves the images contained in the wells in the parent plate.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImages(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return getWells(browser).stream()
                                .map(WellWrapper::getImages)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toMap(ObjectWrapper::getId, i -> i, (i1, i2) -> i1))
                                .values()
                                .stream()
                                .sorted(Comparator.comparing(ObjectWrapper::getId))
                                .collect(Collectors.toList());
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
