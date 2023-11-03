/*
 *  Copyright (C) 2020-2024 GReD
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


import fr.igred.omero.Browser;
import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.WellSampleData;
import omero.model.IObject;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model._PlateAcquisitionOperationsNC;

import java.sql.Timestamp;
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
        if (plate != null) {
            data.setRefPlateId(plate.getId().getValue());
        }
    }


    /**
     * Initializes the ref. plate ID to what is stored in the underlying IObject.
     */
    private void initRefPlate() {
        omero.model.Plate plate = ((_PlateAcquisitionOperationsNC) data.asIObject()).getPlate();
        if (plate != null) {
            data.setRefPlateId(plate.getId().getValue());
        }
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
    protected <A extends AnnotationData> void link(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        PlateAcquisitionAnnotationLink link = new PlateAcquisitionAnnotationLinkI();
        link.setChild(annotation.asAnnotation());
        link.setParent((omero.model.PlateAcquisition) data.asIObject());
        client.save(link);
    }


    /**
     * Retrieves the screens containing the parent plates.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ScreenWrapper> getScreens(Client client)
    throws ServiceException, AccessException, ExecutionException {
        PlateWrapper plate = client.getPlate(getRefPlateId());
        return plate.getScreens(client);
    }


    /**
     * Returns the (updated) parent plate as a singleton list.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<PlateWrapper> getPlates(Client client)
    throws ServiceException, AccessException, ExecutionException {
        return client.getPlates(getRefPlateId());
    }


    /**
     * Retrieves the wells contained in the parent plate.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<WellWrapper> getWells(Client client)
    throws ServiceException, AccessException, ExecutionException {
        return getPlates(client).iterator().next().getWells(client);
    }


    /**
     * Retrieves the well samples for this plate acquisition.
     *
     * @return See above.
     */
    public List<WellSampleWrapper> getWellSamples() {
        _PlateAcquisitionOperationsNC pa = (_PlateAcquisitionOperationsNC) data.asIObject();
        return pa.copyWellSample()
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
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<WellSampleWrapper> getWellSamples(Browser browser)
    throws AccessException, ServiceException {
        reload(browser);
        return getWellSamples();
    }


    /**
     * Retrieves the images contained in the well samples.
     *
     * @return See above
     */
    public List<ImageWrapper> getImages() {
        return getWellSamples().stream()
                               .map(WellSampleWrapper::getImage)
                               .collect(Collectors.toList());
    }


    /**
     * Retrieves the images contained in the wells in the parent plate.
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageWrapper> getImages(Client client)
    throws ServiceException, AccessException {
        return getWellSamples(client).stream()
                                     .map(WellSampleWrapper::getImage)
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


    /**
     * Reloads the plate acquisition from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    @Override
    public void reload(Browser browser)
    throws ServiceException, AccessException {
        String query = "select pa from PlateAcquisition as pa " +
                       " left outer join fetch pa.plate as p" +
                       " left outer join fetch pa.wellSample as ws" +
                       " left outer join fetch ws.plateAcquisition as pa2" +
                       " left outer join fetch ws.well as w" +
                       " left outer join fetch ws.image as img" +
                       " left outer join fetch img.pixels as pix" +
                       " left outer join fetch pix.pixelsType as pt" +
                       " where pa.id=" + getId();
        IObject o = browser.findByQuery(query).iterator().next();
        data = new PlateAcquisitionData((omero.model.PlateAcquisition) o);
        initRefPlate();
    }

}
