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


import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.PlateData;
import omero.gateway.model.WellData;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


/**
 * Class containing a PlateData object.
 * <p> Wraps function calls to the PlateData contained.
 */
public class PlateWrapper extends RepositoryObjectWrapper<PlateData> implements Plate {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "PlateAnnotationLink";


    /**
     * Constructor of the class Plate.
     *
     * @param dataObject The plate contained in the Plate.
     */
    public PlateWrapper(PlateData dataObject) {
        super(dataObject);
    }


    /**
     * Returns the type of annotation link for this object
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Returns the plate acquisitions related to this plate.
     *
     * @return See above.
     */
    @Override
    public List<PlateAcquisition> getPlateAcquisitions() {
        return wrap(asDataObject().getPlateAcquisitions(), PlateAcquisitionWrapper::new);
    }


    /**
     * Gets all wells in the plate available from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Well list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Well> getWells(Client client) throws ServiceException, AccessException, ExecutionException {
        Collection<WellData> wells = handleServiceAndAccess(client.getBrowseFacility(),
                                                            bf -> bf.getWells(client.getCtx(), asDataObject().getId()),
                                                            "Cannot get wells from " + this);

        return wells.stream()
                    .map(WellWrapper::new)
                    .sorted(Comparator.comparing(Well::getRow)
                                      .thenComparing(Well::getColumn))
                    .collect(Collectors.toList());
    }

}
