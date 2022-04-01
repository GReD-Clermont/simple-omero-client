/*
 *  Copyright (C) 2020-2022 GReD
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


import fr.igred.omero.GenericObjectWrapper;
import ome.model.units.BigResult;
import omero.gateway.model.WellSampleData;
import omero.model.Length;
import omero.model.enums.UnitsLength;


public class WellSampleWrapper extends GenericObjectWrapper<WellSampleData> {


    /**
     * Constructor of the class WellSampleWrapper.
     *
     * @param wellSample The well sample contained in the WellSampleWrapper.
     */
    public WellSampleWrapper(WellSampleData wellSample) {
        super(wellSample);
    }


    /**
     * @return the WellSampleData contained.
     */
    public WellSampleData asWellSampleData() {
        return data;
    }


    /**
     * Returns the image related to that sample if any.
     *
     * @return See above.
     */
    public ImageWrapper getImage() {
        return new ImageWrapper(data.getImage());
    }


    /**
     * Sets the image linked to this well sample.
     *
     * @param image The image to set.
     */
    public void setImage(ImageWrapper image) {
        data.setImage(image.asImageData());
    }


    /**
     * Returns the position X.
     *
     * @param unit The unit (may be null, in which case no conversion will be performed)
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
     * @param unit The unit (may be null, in which case no conversion will be performed)
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

}
