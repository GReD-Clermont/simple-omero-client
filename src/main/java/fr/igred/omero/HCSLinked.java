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

package fr.igred.omero;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.Plate;
import fr.igred.omero.repository.PlateAcquisition;
import fr.igred.omero.repository.Screen;
import fr.igred.omero.repository.Well;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle objects that can be linked to HCS objects (screens, plates, wells) on OMERO.
 */
public interface HCSLinked extends ImageLinked {


    /**
     * Retrieves the screens linked to this object, either directly, or through parents/children.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError   Server error.
     */
    List<? extends Screen> getScreens(Client client)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError;


    /**
     * Returns the plates linked to this object, either directly, or through parents/children.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError   Server error.
     */
    List<? extends Plate> getPlates(Client client)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError;


    /**
     * Returns the plate acquisitions linked to this object, either directly, or through parents/children.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError   Server error.
     */
    List<? extends PlateAcquisition> getPlateAcquisitions(Client client)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError;


    /**
     * Retrieves the wells linked to this object, either directly, or through parents/children.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError   Server error.
     */
    List<? extends Well> getWells(Client client)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError;

}
