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
import omero.gateway.model.ChannelData;


/**
 * Class containing a ChannelData object.
 * <p> Wraps function calls to the ChannelData contained.
 */
public class ChannelWrapper extends RemoteObjectWrapper<ChannelData> implements Channel {


    /**
     * Constructor of the class Channel.
     *
     * @param dataObject The ChannelData contained in the Channel.
     */
    public ChannelWrapper(ChannelData dataObject) {
        super(dataObject);
    }


}
