package fr.igred.omero.client;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.SecurityContext;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.facility.TablesFacility;
import omero.model.FileAnnotationI;
import omero.model.IObject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.call;


public interface BasicDataManager {

    /**
     * Returns the current {@link SecurityContext}.
     *
     * @return See above
     */
    SecurityContext getCtx();


    /**
     * Gets the {@link DataManagerFacility} to handle/write data on OMERO. A
     *
     * @return See above.
     *
     * @throws ExecutionException If the DataManagerFacility can't be retrieved or instantiated.
     */
    DataManagerFacility getDMFacility() throws ExecutionException;


    /**
     * Gets the {@link ROIFacility} used to manipulate ROIs from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    ROIFacility getRoiFacility() throws ExecutionException;


    /**
     * Gets the {@link TablesFacility} used to manipulate tables on OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the TablesFacility can't be retrieved or instantiated.
     */
    TablesFacility getTablesFacility() throws ExecutionException;


    /**
     * Saves an object on OMERO.
     *
     * @param object The OMERO object.
     *
     * @return The saved OMERO object
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default IObject save(IObject object)
    throws ServiceException, AccessException, ExecutionException {
        return call(getDMFacility(),
                    d -> d.saveAndReturnObject(getCtx(), object),
                    "Cannot save object");
    }


    /**
     * Deletes an object from OMERO.
     *
     * @param object The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(IObject object)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        final long wait = 500L;
        ExceptionHandler.ofConsumer(getDMFacility(),
                                    d -> d.delete(getCtx(), object).loop(10, wait))
                        .rethrow(InterruptedException.class)
                        .handleOMEROException("Cannot delete object")
                        .rethrow();
    }


    /**
     * Deletes multiple objects from OMERO.
     *
     * @param objects The OMERO objects.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(List<IObject> objects)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        final long wait = 500L;
        ExceptionHandler.ofConsumer(getDMFacility(),
                                    d -> d.delete(getCtx(), objects).loop(10, wait))
                        .rethrow(InterruptedException.class)
                        .handleOMEROException("Cannot delete objects")
                        .rethrow();
    }


    /**
     * Deletes a file from OMERO
     *
     * @param id ID of the file to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void deleteFile(Long id)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        deleteFiles(id);
    }


    /**
     * Deletes files from OMERO.
     *
     * @param ids List of files IDs to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void deleteFiles(Long... ids)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        List<IObject> files = Arrays.stream(ids)
                                    .map(id -> new FileAnnotationI(id, false))
                                    .collect(Collectors.toList());
        delete(files);
    }

}
