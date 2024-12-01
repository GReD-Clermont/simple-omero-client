package fr.igred.omero.client;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.api.IQueryPrx;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.MetadataFacility;
import omero.model.IObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.call;


public interface BasicBrowser {

    /**
     * Returns the current {@link SecurityContext}.
     *
     * @return See above
     */
    SecurityContext getCtx();


    /**
     * Gets the {@link BrowseFacility} used to access the data from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    BrowseFacility getBrowseFacility() throws ExecutionException;


    /**
     * Returns the {@link IQueryPrx} used to find objects on OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    IQueryPrx getQueryService() throws ServiceException, AccessException;


    /**
     * Gets the {@link MetadataFacility} used to retrieve annotations from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the MetadataFacility can't be retrieved or instantiated.
     */
    MetadataFacility getMetadataFacility() throws ExecutionException;


    /**
     * Finds objects on OMERO through a database query.
     *
     * @param query The database query.
     *
     * @return A list of OMERO objects.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    default List<IObject> findByQuery(String query)
    throws ServiceException, AccessException {
        return call(getQueryService(),
                    qs -> qs.findAllByQuery(query, null),
                    "Query failed: " + query);
    }

}
