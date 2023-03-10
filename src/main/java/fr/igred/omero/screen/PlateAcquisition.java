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
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.PlateAcquisitionData;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.RemoteObject.distinct;


/**
 * Interface to handle Plate Acquisitions on OMERO.
 */
public interface PlateAcquisition extends RepositoryObject {

    /**
     * Returns an {@link PlateAcquisitionData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    PlateAcquisitionData asDataObject();


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
     * Attach an {@link AnnotationData} to this object.
     *
     * @param <A>        The type of the annotation.
     * @param dm         The data manager.
     * @param annotation The {@link AnnotationData}.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default <A extends AnnotationData> void link(DataManager dm, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        PlateAcquisitionAnnotationLink link = new PlateAcquisitionAnnotationLinkI();
        link.setChild(annotation.asAnnotation());
        link.setParent((omero.model.PlateAcquisition) asDataObject().asIObject());
        dm.save(link);
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
    default List<Screen> getScreens(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        Plate plate = browser.getPlate(getRefPlateId());
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
     * @throws ServerException    Server error.
     */
    default List<Plate> getPlates(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        reload(browser);
        return browser.getPlates(getRefPlateId());
    }


    /**
     * Retrieves the wells containing the well samples for this plate acquisition.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    default List<Well> getWells(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        List<WellSample> wellSamples = getWellSamples(browser);
        Collection<Well> wells       = new ArrayList<>(wellSamples.size());
        for (WellSample ws : wellSamples) {
            wells.add(ws.getWell(browser));
        }
        return distinct(wells);
    }


    /**
     * Retrieves the well samples for this plate acquisition.
     *
     * @return See above.
     */
    List<WellSample> getWellSamples();


    /**
     * Retrieves the well samples for this plate acquisition and reloads them from OMERO.
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
    List<WellSample> getWellSamples(Browser browser)
    throws AccessException, ServiceException, ExecutionException, ServerException;


    /**
     * Retrieves the images contained in the well samples.
     *
     * @return See above
     */
    default List<Image> getImages() {
        return getWellSamples().stream().map(WellSample::getImage).collect(Collectors.toList());
    }


    /**
     * Retrieves the images contained in the well samples from OMERO.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    default List<Image> getImages(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        return getWellSamples(browser).stream().map(WellSample::getImage).collect(Collectors.toList());
    }


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


    /**
     * Reloads the plate acquisition from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    @Override
    void reload(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException;

}
