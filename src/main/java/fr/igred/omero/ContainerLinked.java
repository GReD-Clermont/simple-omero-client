package fr.igred.omero;


import fr.igred.omero.client.Browser;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.containers.Project;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.DataObject;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle objects that can be linked to containers (projects, datasets) on OMERO.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public interface ContainerLinked<T extends DataObject> extends ImageLinked<T> {


    /**
     * Retrieves the projects linked to this object, either directly, or through parents/children.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServerException    Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Project> getProjects(Browser browser)
    throws ServerException, ServiceException, AccessException, ExecutionException;


    /**
     * Retrieves the datasets linked to this object, either directly, or through parents/children.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServerException    Server error.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Dataset> getDatasets(Browser browser)
    throws ServerException, ServiceException, AccessException, ExecutionException;

}
