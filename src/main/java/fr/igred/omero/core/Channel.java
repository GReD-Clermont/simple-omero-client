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

package fr.igred.omero.core;


import fr.igred.omero.RemoteObject;

import java.awt.Color;


/**
 * Interface to handle Channel information on OMERO.
 */
public interface Channel extends RemoteObject {

    /**
     * Returns whether the channel contains all the RGBA values or not.
     *
     * @return See above.
     */
    boolean hasRGBA();


    /**
     * Returns the channel index.
     *
     * @return See above.
     */
    int getIndex();


    /**
     * Returns the label of the channel.
     * <p>Following the specification: Name&rarr;Fluor&rarr;Emission wavelength&rarr;index.
     *
     * @return See above.
     */
    String getChannelLabeling();


    /**
     * Returns the name of the channel.
     *
     * @return See above.
     */
    String getName();


    /**
     * Sets the name of the channel.
     *
     * @param name The name of the channel.
     */
    void setName(String name);


    /**
     * Gets the original channel color. Defaults to {@link Color#WHITE} if RGBA values are missing.
     *
     * @return The original channel color.
     */
    default Color getColor() {
        Color color = Color.WHITE;
        if (hasRGBA()) color = new Color(getRed(), getGreen(), getBlue(), getAlpha());
        return color;
    }


    /**
     * Gets the sRGB alpha value of the channel.
     *
     * @return See above.
     */
    int getAlpha();


    /**
     * Gets the sRGB red value of the channel.
     *
     * @return See above.
     */
    int getRed();


    /**
     * Gets the sRGB green value of the channel.
     *
     * @return See above.
     */
    int getGreen();


    /**
     * Gets the sRGB blue value of the channel.
     *
     * @return See above.
     */
    int getBlue();

}
