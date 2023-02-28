package fr.igred.omero;


import fr.igred.omero.client.Browser;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.containers.Project;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle objects that can be linked to containers (projects, datasets) on OMERO.
 */
public interface ContainerLinked extends ImageLinked {


    /**
     * Retrieves the projects linked to this object, either directly, or through parents/children.
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
    List<Project> getProjects(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException;


    /**
     * Retrieves the datasets linked to this object, either directly, or through parents/children.
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
    List<Dataset> getDatasets(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException;

}
