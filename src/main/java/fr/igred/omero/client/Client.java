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

package fr.igred.omero.client;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import omero.api.IQueryPrx;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.MetadataFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.facility.TablesFacility;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;


/**
 * Client interface to connect to OMERO, browse through all the data accessible to the user and modify it.
 */
public interface Client extends ConnectionHandler, Browser, DataManager, AdminManager {


    /**
     * Gets the {@link BrowseFacility} used to access the data from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default BrowseFacility getBrowseFacility() throws ExecutionException {
        return getGateway().getFacility(BrowseFacility.class);
    }


    /**
     * Returns the {@link IQueryPrx} used to find objects on OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Override
    default IQueryPrx getQueryService() throws ServiceException {
        return ExceptionHandler.of(getGateway(), g -> g.getQueryService(getCtx()))
                               .rethrow(DSOutOfServiceException.class, ServiceException::new,
                                        "Could not retrieve Query Service")
                               .get();
    }


    /**
     * Gets the {@link MetadataFacility} used to retrieve annotations from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the MetadataFacility can't be retrieved or instantiated.
     */
    @Override
    default MetadataFacility getMetadataFacility() throws ExecutionException {
        return getGateway().getFacility(MetadataFacility.class);
    }


    /**
     * Gets the {@link DataManagerFacility} to handle/write data on OMERO. A
     *
     * @return See above.
     *
     * @throws ExecutionException If the DataManagerFacility can't be retrieved or instantiated.
     */
    @Override
    default DataManagerFacility getDMFacility() throws ExecutionException {
        return getGateway().getFacility(DataManagerFacility.class);
    }


    /**
     * Gets the {@link ROIFacility} used to manipulate ROIs from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    @Override
    default ROIFacility getRoiFacility() throws ExecutionException {
        return getGateway().getFacility(ROIFacility.class);
    }


    /**
     * Gets the {@link TablesFacility} used to manipulate tables on OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the TablesFacility can't be retrieved or instantiated.
     */
    @Override
    default TablesFacility getTablesFacility() throws ExecutionException {
        return getGateway().getFacility(TablesFacility.class);
    }


    /**
     * Gets the {@link AdminFacility} to use admin specific function.
     *
     * @return See above.
     *
     * @throws ExecutionException If the AdminFacility can't be retrieved or instantiated.
     */
    @Override
    default AdminFacility getAdminFacility() throws ExecutionException {
        return getGateway().getFacility(AdminFacility.class);
    }


    /**
     * Gets the client associated with the username in the parameters. The user calling this function needs to have
     * administrator rights. All action realized with the client returned will be considered as his.
     *
     * @param username Username of user.
     *
     * @return The client corresponding to the new user.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     * @throws NoSuchElementException The requested user does not exist.
     */
    Client sudo(String username) throws ServiceException, AccessException, ExecutionException;


}

