/*
 *  Copyright (C) 2020 GReD
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

 package fr.igred.omero.metadata.annotation;

import java.util.List;

import fr.igred.omero.Client;

import omero.gateway.model.MapAnnotationData;
import omero.model.NamedValue;

/**
 * Class containing a MapAnnotationData, a MapAnnotationData contains a list of NamedValue(Key-Value pair).
 * Implements function using the MapAnnotationData contained
 */
public class MapAnnotationContainer {
    ///MapAnnotationData contained
    private MapAnnotationData data;

    /**
     * Get the List of NamedValue contained in the MapAnnotationData.
     * 
     * @return MapAnnotationData content
     */
    public List<NamedValue> getContent()
    {
        return (List<NamedValue>)data.getContent();
    }

    /**
     * Return the MapAnnotationData contained.
     * 
     * @return 
     */
    public MapAnnotationData getMapAnnotation()
    {
        return data;
    }





    /**
     * Set the content of the MapAnnotationData.
     * 
     * @param client The user
     * @param result List of NamedValue(Key-Value pair)
     */
    public void setContent(Client           client,
                           List<NamedValue> result)
    {
        data = new MapAnnotationData();
        data.setContent(result);
    }

    /**
     * Constructor of the MapAnnotationContainer class.
     * 
     * @param data MapAnnotationData to be contained
     */
    public MapAnnotationContainer(MapAnnotationData data)
    {
        this.data = data;
    }

    /**
     * Constructor of the MapAnnotationContainer class.
     * Set the content of the MapAnnotationData
     * 
     * @param result List of NamedValue(Key-Value pair)
     */
    public MapAnnotationContainer(List<NamedValue> result)
    {
        data = new MapAnnotationData();
        data.setContent(result);
    }

    /**
     * Constructor of the MapAnnotationContainer class.
     */
    public MapAnnotationContainer()
    {
        data = new MapAnnotationData();
    }
}