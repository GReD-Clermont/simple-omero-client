package fr.igred.omero;


import fr.igred.omero.client.Browser;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.DataObject;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle objects that can be linked to images (tags, containers, HCS, folders).
 *
 * @param <T> Subclass of {@link DataObject}
 */
public interface ImageLinked<T extends DataObject> extends RemoteObject<T>{



    /**
     * Retrieves the images linked to this object, either directly, or through parents/children.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Image> getImages(Browser browser)
    throws AccessException, ServiceException, ExecutionException;

}
