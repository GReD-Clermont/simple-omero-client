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

package fr.igred.omero.meta;


import fr.igred.omero.GenericObjectWrapper;
import omero.gateway.model.PlaneInfoData;
import omero.model.Length;
import omero.model.Time;


public class PlaneInfoWrapper extends GenericObjectWrapper<PlaneInfoData> implements PlaneInfo {

    /**
     * Constructor of the class PlaneInfoWrapper.
     *
     * @param object The PlaneInfoData to wrap in the PlaneInfoWrapper.
     */
    public PlaneInfoWrapper(PlaneInfoData object) {
        super(object);
    }


    /**
     * @return See above.
     *
     * @deprecated Returns the PlaneInfoData contained. Use {@link #asDataObject()} instead.
     */
    @Deprecated
    public PlaneInfoData asPlaneInfoData() {
        return data;
    }


    /**
     * Retrieves the plane deltaT.
     *
     * @return See above.
     */
    @Override
    public Time getDeltaT() {
        return data.getDeltaT();
    }


    /**
     * Retrieves the exposure time.
     *
     * @return See above.
     */
    @Override
    public Time getExposureTime() {
        return data.getExposureTime();
    }


    /**
     * Retrieves the X stage position.
     *
     * @return See above.
     */
    @Override
    public Length getPositionX() {
        return data.getPositionX();
    }


    /**
     * Retrieves the Y stage position.
     *
     * @return See above.
     */
    @Override
    public Length getPositionY() {
        return data.getPositionY();
    }


    /**
     * Retrieves the Z stage position.
     *
     * @return See above.
     */
    @Override
    public Length getPositionZ() {
        return data.getPositionZ();
    }


    /**
     * Retrieves the plane channel index.
     *
     * @return See above.
     */
    @Override
    public int getTheC() {
        return data.getTheC();
    }


    /**
     * Retrieves the plane time index.
     *
     * @return See above.
     */
    @Override
    public int getTheT() {
        return data.getTheT();
    }


    /**
     * Retrieves the plane slice index.
     *
     * @return See above.
     */
    @Override
    public int getTheZ() {
        return data.getTheZ();
    }

}
