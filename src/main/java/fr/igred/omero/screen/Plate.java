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


import fr.igred.omero.RemoteObject;
import fr.igred.omero.RepositoryObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ome.model.units.BigResult;
import omero.RLong;
import omero.gateway.model.PlateData;
import omero.model.IObject;
import omero.model.Length;
import omero.model.enums.UnitsLength;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Interface to handle Plates on OMERO.
 */
public interface Plate extends RepositoryObject {

    /**
     * Returns an {@link PlateData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    PlateData asDataObject();


    /**
     * Sets the name of the plate.
     *
     * @param name The name of the plate. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Sets the description of the plate.
     *
     * @param description The description of the plate.
     */
    void setDescription(String description);


    /**
     * Retrieves the screens containing this plate.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Screen> getScreens(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os = browser.findByQuery("select link.parent from ScreenPlateLink as link " +
                                               "where link.child=" + getId());
        return browser.getScreens(os.stream().map(IObject::getId).map(RLong::getValue).distinct().toArray(Long[]::new));
    }


    /**
     * Returns the plate acquisitions related to this plate.
     *
     * @return See above.
     */
    List<PlateAcquisition> getPlateAcquisitions();


    /**
     * Reloads the plate and returns the plate acquisitions related to it.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<PlateAcquisition> getPlateAcquisitions(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        reload(browser);
        return getPlateAcquisitions();
    }


    /**
     * Gets all wells in the plate available from OMERO.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Well> getWells(Browser browser)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Returns the images contained in the wells of this plate.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Image> getImages(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return getWells(browser).stream()
                                .map(Well::getImages)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toMap(RemoteObject::getId, i -> i, (i1, i2) -> i1))
                                .values()
                                .stream()
                                .sorted(Comparator.comparing(RemoteObject::getId))
                                .collect(Collectors.toList());
    }


    /**
     * Returns the index indicating how to label a column.
     *
     * @return See above.
     */
    int getColumnSequenceIndex();


    /**
     * Returns the index indicating how to label a row.
     *
     * @return See above.
     */
    int getRowSequenceIndex();


    /**
     * Returns the currently selected field or {@code 0}.
     *
     * @return See above.
     */
    int getDefaultSample();


    /**
     * Sets the default sample.
     *
     * @param value The value to set.
     */
    void setDefaultSample(int value);


    /**
     * Returns the status of the plate.
     *
     * @return See above.
     */
    String getStatus();


    /**
     * Sets the status.
     *
     * @param value The value to set.
     */
    void setStatus(String value);


    /**
     * Returns the external identifier of the plate.
     *
     * @return See above.
     */
    String getExternalIdentifier();


    /**
     * Sets the external identifier.
     *
     * @param value The value to set.
     */
    void setExternalIdentifier(String value);


    /**
     * Returns the type of plate e.g. A 384-Well Plate, 96-Well Plate.
     *
     * @return See above.
     */
    String getPlateType();


    /**
     * Returns the x-coordinate in 2D-space of the well.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    Length getWellOriginX(UnitsLength unit) throws BigResult;


    /**
     * Returns the y-coordinate in 2D-space of the well.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    Length getWellOriginY(UnitsLength unit) throws BigResult;


    /**
     * Reloads the plate from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    void reload(Browser browser)
    throws ServiceException, AccessException, ExecutionException;

}
