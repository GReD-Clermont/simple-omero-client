package fr.igred.omero;


import fr.igred.omero.client.Browser;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.screen.Plate;
import fr.igred.omero.screen.PlateAcquisition;
import fr.igred.omero.screen.Screen;
import fr.igred.omero.screen.Well;
import omero.gateway.model.DataObject;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle objects that can be linked to HCS objects (screens, plates, wells) on OMERO.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public interface HCSLinked<T extends DataObject> extends ImageLinked<T> {


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
    throws AccessException, ServiceException, ExecutionException, ServerException;


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
     */
    List<Plate> getPlates(Browser browser)
    throws AccessException, ServiceException, ExecutionException;


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
     */
    List<PlateAcquisition> getPlateAcquisitions(Browser browser)
    throws AccessException, ServiceException, ExecutionException;


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
     */
    List<Well> getWells(Browser browser)
    throws AccessException, ServiceException, ExecutionException;

}
