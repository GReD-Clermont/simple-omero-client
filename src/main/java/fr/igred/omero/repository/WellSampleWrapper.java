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


import fr.igred.omero.RemoteObjectWrapper;
import omero.gateway.model.WellSampleData;


/**
 * Class containing a WellSampleData object.
 * <p> Wraps function calls to the WellSampleData contained.
 */
public class WellSampleWrapper extends RemoteObjectWrapper<WellSampleData> implements WellSample {


    /**
     * Constructor of the class WellSample.
     *
     * @param dataObject The well sample contained in the WellSample.
     */
    public WellSampleWrapper(WellSampleData dataObject) {
        super(dataObject);
    }


    /**
     * Returns the image related to that sample if any.
     *
     * @return See above.
     */
    @Override
    public Image getImage() {
        return new ImageWrapper(asDataObject().getImage());
    }

}
