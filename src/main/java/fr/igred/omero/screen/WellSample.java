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

package fr.igred.omero.screen;


import fr.igred.omero.RemoteObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ome.model.units.BigResult;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.WellSampleData;
import omero.model.IObject;
import omero.model.Length;
import omero.model.enums.UnitsLength;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Class containing a WellSampleData object.
 * <p> Wraps function calls to the WellSampleData contained.
 */
public class WellSample extends RemoteObject<WellSampleData> {


    /**
     * Constructor of the class WellSample.
     *
     * @param wellSample The WellSampleData to wrap in the WellSample.
     */
    public WellSample(WellSampleData wellSample) {
        super(wellSample);
    }


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
     */
    public List<Screen> getScreens(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
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
    public List<Plate> getPlates(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return Collections.singletonList(getWell(browser).getPlate());
    }


    /**
     * Returns the plate acquisition containing this well sample.
     *
     * @return See above.
     */
    public PlateAcquisition getPlateAcquisition() {
        return new PlateAcquisition(new PlateAcquisitionData(data.asWellSample().getPlateAcquisition()));
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
    public List<PlateAcquisition> getPlateAcquisitions(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return getWell(browser).getPlateAcquisitions(browser);
    }


    /**
     * Retrieves the well containing this well sample
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public Well getWell(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        return browser.getWell(asDataObject().asWellSample().getWell().getId().getValue());
    }


    /**
     * Returns the image related to that sample if any.
     *
     * @return See above.
     */
    public Image getImage() {
        return new Image(data.getImage());
    }


    /**
     * Sets the image linked to this well sample.
     *
     * @param image The image to set.
     */
    public void setImage(Image image) {
        data.setImage(image.asDataObject());
    }


    /**
     * Returns the position X.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above.
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getPositionX(UnitsLength unit) throws BigResult {
        return data.getPositionX(unit);
    }


    /**
     * Returns the position Y.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above.
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getPositionY(UnitsLength unit) throws BigResult {
        return data.getPositionY(unit);
    }


    /**
     * Returns the time at which the field was acquired.
     *
     * @return See above.
     */
    public long getStartTime() {
        return data.getStartTime();
    }


    /**
     * Reloads the well sample from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public void reload(Browser browser)
    throws ServiceException, AccessException {
        String query = "select ws from WellSample as ws" +
                       " left outer join fetch ws.plateAcquisition as pa" +
                       " left outer join fetch ws.well as w" +
                       " left outer join fetch ws.image as img" +
                       " left outer join fetch img.pixels as pix" +
                       " where ws.id=" + getId();
        IObject o = browser.findByQuery(query).iterator().next();
        data = new WellSampleData((omero.model.WellSample) o);
    }

}
