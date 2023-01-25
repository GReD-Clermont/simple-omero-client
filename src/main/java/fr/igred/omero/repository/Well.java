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

package fr.igred.omero.repository;


import omero.gateway.model.WellData;

import java.util.List;


/**
 * Interface to handle Wells on OMERO.
 */
public interface Well extends RepositoryObject<WellData> {

    /**
     * Returns the well samples linked to the well.
     *
     * @return See above.
     */
    List<WellSample> getWellSamples();


    /**
     * Returns the plate containing this Well.
     *
     * @return See above.
     */
    Plate getPlate();


    /**
     * Returns the column used to indicate the location of the well on the grid.
     *
     * @return See above.
     */
    Integer getColumn();


    /**
     * Returns the row used to indicate the location of the well on the grid.
     *
     * @return See above.
     */
    Integer getRow();


    /**
     * Returns the status of the well.
     *
     * @return See above.
     */
    String getStatus();


    /**
     * Sets the status of the well.
     *
     * @param status The status of the well.
     */
    void setStatus(String status);


    /**
     * Returns a human-readable identifier for the screening status e.g. empty, positive control, etc.
     *
     * @return See above.
     */
    String getWellType();


    /**
     * Sets a human-readable identifier for the screening status e.g. empty, positive control, etc.
     *
     * @param type The value to set.
     */
    void setWellType(String type);


    /**
     * Returns the red component of the color associated to the well, or {@code -1}.
     *
     * @return See above.
     */
    int getRed();


    /**
     * Sets the red component of the color associated to the well.
     *
     * @param red The value to set.
     */
    void setRed(Integer red);


    /**
     * Returns the green component of the color associated to the well, or {@code -1}.
     *
     * @return See above.
     */
    int getGreen();


    /**
     * Sets the green component of the color associated to the well.
     *
     * @param green The value to set.
     */
    void setGreen(Integer green);


    /**
     * Returns the blue component of the color associated to the well, or {@code -1}.
     *
     * @return See above.
     */
    int getBlue();


    /**
     * Sets the blue component of the color associated to the well.
     *
     * @param blue The value to set.
     */
    void setBlue(Integer blue);


    /**
     * Returns the alpha component of the color associated to the well, or {@code -1}.
     *
     * @return See above.
     */
    int getAlpha();


    /**
     * Sets the alpha component of the color associated to the well.
     *
     * @param alpha The value to set.
     */
    void setAlpha(Integer alpha);

}
