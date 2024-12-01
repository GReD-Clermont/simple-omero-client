package fr.igred.omero.meta;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.api.IAdminPrx;
import omero.gateway.SecurityContext;
import omero.gateway.facility.AdminFacility;

import java.util.concurrent.ExecutionException;


public interface BasicAdmin {

    /**
     * Returns the current {@link SecurityContext}.
     *
     * @return See above
     */
    SecurityContext getCtx();


    /**
     * Gets the {@link AdminFacility} to use admin specific function.
     *
     * @return See above.
     *
     * @throws ExecutionException If the AdminFacility can't be retrieved or instantiated.
     */
    AdminFacility getAdminFacility() throws ExecutionException;


    /**
     * Returns the {@link IAdminPrx} to use admin specific function.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    IAdminPrx getAdminService() throws AccessException, ServiceException;

}
