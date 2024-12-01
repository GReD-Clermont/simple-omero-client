package fr.igred.omero.annotations;


import fr.igred.omero.RemoteObject;
import fr.igred.omero.client.BasicBrowser;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;


public interface AnnotationsBrowser extends BasicBrowser {

    /**
     * Gets the list of tag annotations available to the user.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    List<TagAnnotation> getTags()
    throws AccessException, ServiceException;


    /**
     * Gets the list of tag annotations with the specified name available to the user.
     *
     * @param name Name of the tag searched.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    default List<TagAnnotation> getTags(String name)
    throws AccessException, ServiceException {
        List<TagAnnotation> tags = getTags();
        tags.removeIf(tag -> !tag.getName().equals(name));
        tags.sort(Comparator.comparing(RemoteObject::getId));
        return tags;
    }


    /**
     * Gets a specific tag from the OMERO database.
     *
     * @param id ID of the tag.
     *
     * @return See above.
     *
     * @throws AccessException        Cannot access data.
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws NoSuchElementException No element with this ID.
     */
    TagAnnotationWrapper getTag(Long id)
    throws AccessException, ServiceException;


    /**
     * Gets the list of map annotations available to the user.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    List<MapAnnotation> getMapAnnotations()
    throws AccessException, ServiceException;


    /**
     * Gets the list of map annotations with the specified key available to the user.
     *
     * @param key Name of the tag searched.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    List<MapAnnotation> getMapAnnotations(String key)
    throws AccessException, ServiceException;


    /**
     * Gets the list of map annotations with the specified key and value available to the user.
     *
     * @param key   The required key.
     * @param value The required value.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    List<MapAnnotation> getMapAnnotations(String key, String value)
    throws AccessException, ServiceException;


    /**
     * Gets a specific map annotation (key/value pairs) from the OMERO database.
     *
     * @param id ID of the map annotation.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    MapAnnotation getMapAnnotation(Long id)
    throws ServiceException, ExecutionException, AccessException;

}
