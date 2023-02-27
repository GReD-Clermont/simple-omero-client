package fr.igred.omero;


import fr.igred.omero.client.Browser;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.screen.Plate;
import fr.igred.omero.screen.PlateAcquisition;
import fr.igred.omero.screen.Screen;
import fr.igred.omero.screen.Well;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle objects that can be linked to HCS objects (screens, plates, wells) on OMERO.
 */
public interface HCSLinked extends ImageLinked {


    /**
     * Retrieves the screens linked to this object, either directly, or through parents/children.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    List<Screen> getScreens(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException;


    /**
     * Returns the plates linked to this object, either directly, or through parents/children.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    List<Plate> getPlates(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException;


    /**
     * Returns the plate acquisitions linked to this object, either directly, or through parents/children.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    List<PlateAcquisition> getPlateAcquisitions(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException;


    /**
     * Retrieves the wells linked to this object, either directly, or through parents/children.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    List<Well> getWells(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException;

}