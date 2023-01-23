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
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import ome.model.units.BigResult;
import omero.RLong;
import omero.gateway.model.PlateData;
import omero.model.IObject;
import omero.model.Length;
import omero.model.enums.UnitsLength;

import java.util.List;
import java.util.concurrent.ExecutionException;


public interface Plate extends RepositoryObject<PlateData> {

    /**
     * Gets the plate name.
     *
     * @return See above.
     */
    @Override
    default String getName() {
        return asDataObject().getName();
    }


    /**
     * Sets the name of the plate.
     *
     * @param name The name of the plate. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    default void setName(String name) {
        asDataObject().setName(name);
    }


    /**
     * Returns the PlateData contained.
     *
     * @return See above.
     */
    default PlateData asPlateData() {
        return asDataObject();
    }


    /**
     * Gets the plate description
     *
     * @return See above.
     */
    @Override
    default String getDescription() {
        return asDataObject().getDescription();
    }


    /**
     * Sets the description of the plate.
     *
     * @param description The description of the plate.
     */
    default void setDescription(String description) {
        asDataObject().setDescription(description);
    }


    /**
     * Retrieves the screens containing this dataset.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServerException    Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Screen> getScreens(Client client)
    throws ServerException, ServiceException, AccessException, ExecutionException {
        List<IObject> os = client.findByQuery("select link.parent from ScreenPlateLink as link " +
                                              "where link.child=" + getId());
        return client.getScreens(os.stream().map(IObject::getId).map(RLong::getValue).distinct().toArray(Long[]::new));
    }


    /**
     * Returns the plate acquisitions related to this plate.
     *
     * @return See above.
     */
    List<PlateAcquisition> getPlateAcquisitions();


    /**
     * Gets all wells in the plate available from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Well list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Well> getWells(Client client) throws ServiceException, AccessException, ExecutionException;


    /**
     * Returns the index indicating how to label a column.
     *
     * @return See above.
     */
    default int getColumnSequenceIndex() {
        return asDataObject().getColumnSequenceIndex();
    }


    /**
     * Returns the index indicating how to label a row.
     *
     * @return See above.
     */
    default int getRowSequenceIndex() {
        return asDataObject().getRowSequenceIndex();
    }


    /**
     * Returns the currently selected field or {@code 0}.
     *
     * @return See above.
     */
    default int getDefaultSample() {
        return asDataObject().getDefaultSample();
    }


    /**
     * Sets the default sample.
     *
     * @param value The value to set.
     */
    default void setDefaultSample(int value) {
        asDataObject().setDefaultSample(value);
    }


    /**
     * Returns the status of the plate.
     *
     * @return See above.
     */
    default String getStatus() {
        return asDataObject().getStatus();
    }


    /**
     * Sets the status.
     *
     * @param value The value to set.
     */
    default void setStatus(String value) {
        asDataObject().setStatus(value);
    }


    /**
     * Returns the external identifier of the plate.
     *
     * @return See above.
     */
    default String getExternalIdentifier() {
        return asDataObject().getExternalIdentifier();
    }


    /**
     * Sets the external identifier.
     *
     * @param value The value to set.
     */
    default void setExternalIdentifier(String value) {
        asDataObject().setExternalIdentifier(value);
    }


    /**
     * Returns the type of plate e.g. A 384-Well Plate, 96-Well Plate.
     *
     * @return See above.
     */
    default String getPlateType() {
        return asDataObject().getPlateType();
    }


    /**
     * Returns the x-coordinate in 2D-space of the well.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    default Length getWellOriginX(UnitsLength unit) throws BigResult {
        return asDataObject().getWellOriginX(unit);
    }


    /**
     * Returns the y-coordinate in 2D-space of the well.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    default Length getWellOriginY(UnitsLength unit) throws BigResult {
        return asDataObject().getWellOriginY(unit);
    }

}
