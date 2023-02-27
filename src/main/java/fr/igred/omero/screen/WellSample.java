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
import fr.igred.omero.client.Browser;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import ome.model.units.BigResult;
import omero.gateway.model.WellSampleData;
import omero.model.Length;
import omero.model.enums.UnitsLength;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle Well Samples on OMERO.
 */
public interface WellSample extends RemoteObject {

    /**
     * Returns an {@link WellSampleData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    WellSampleData asDataObject();


    /**
     * Returns the screens containing the parent Well.
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
        return getWell(browser).getScreens(browser);
    }


    /**
     * Returns the plates containing the parent Well.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Plate> getPlates(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return Collections.singletonList(getWell(browser).getPlate());
    }


    /**
     * Returns the plate acquisitions linked to the parent Well.
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
    throws ServiceException, AccessException, ExecutionException {
        return getWell(browser).getPlateAcquisitions(browser);
    }


    /**
     * Retrieves the well containing this well sample.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default Well getWell(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        return browser.getWell(asDataObject().asWellSample().getWell().getId().getValue());
    }


    /**
     * Returns the image related to that sample if any.
     *
     * @return See above.
     */
    Image getImage();


    /**
     * Sets the image linked to this well sample.
     *
     * @param image The image to set.
     */
    void setImage(Image image);


    /**
     * Returns the position X.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above.
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    Length getPositionX(UnitsLength unit) throws BigResult;


    /**
     * Returns the position Y.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above.
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    Length getPositionY(UnitsLength unit) throws BigResult;


    /**
     * Returns the time at which the field was acquired.
     *
     * @return See above.
     */
    long getStartTime();

}
