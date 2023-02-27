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

package fr.igred.omero.roi;


import omero.gateway.model.MaskData;

import java.awt.geom.Rectangle2D;


/**
 * Interface to handle Mask shapes on OMERO.
 */
public interface Mask extends Shape, Rectangular {

    /**
     * Returns an {@link MaskData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    MaskData asDataObject();


    /**
     * Converts the shape to an {@link java.awt.Shape}.
     *
     * @return The converted AWT Shape.
     */
    @Override
    default java.awt.Shape toAWTShape() {
        return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
    }


    /**
     * Returns the mask image.
     *
     * @return See above.
     */
    int[][] getMaskAsBinaryArray();


    /**
     * Returns the mask as a byte array.
     *
     * @return See above.
     */
    byte[] getMask();


    /**
     * Sets the mask image.
     *
     * @param mask See above.
     */
    void setMask(byte[] mask);


    /**
     * Sets the mask
     *
     * @param mask The binary mask (int[width][height])
     */
    void setMask(int[][] mask);


    /**
     * Sets the mask
     *
     * @param mask The binary mask (boolean[width][height])
     */
    void setMask(boolean[][] mask);

}
