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


import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.Table;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.containers.Folder;
import omero.ServerError;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.facility.TablesFacility;
import omero.model.FileAnnotationI;
import omero.model.IObject;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


/**
 * Interface to add or remove data on an OMERO server in a given {@link SecurityContext}.
 */
public interface DataManager {

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
    DataManagerFacility getDataManagerFacility() throws ExecutionException;


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
    default IObject save(IObject object) throws ServiceException, AccessException, ExecutionException {
        return handleServiceAndAccess(getDataManagerFacility(),
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
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(IObject object)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        final long wait = 500L;
        ExceptionHandler.ofConsumer(getDataManagerFacility(),
                                    d -> d.delete(getCtx(), object).loop(10, wait),
                                    "Cannot delete object")
                        .rethrow(InterruptedException.class)
                        .rethrow(DSOutOfServiceException.class, ServiceException::new)
                        .rethrow(DSAccessException.class, AccessException::new)
                        .rethrow(ServerError.class, ServerException::new);
    }


    /**
     * Deletes multiple objects from OMERO.
     *
     * @param objects The OMERO objects.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(List<IObject> objects)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        final long wait = 5000L;
        ExceptionHandler.ofConsumer(getDataManagerFacility(),
                                    d -> d.delete(getCtx(), objects).loop(10, wait),
                                    "Cannot delete object")
                        .rethrow(InterruptedException.class)
                        .rethrow(DSOutOfServiceException.class, ServiceException::new)
                        .rethrow(DSAccessException.class, AccessException::new)
                        .rethrow(ServerError.class, ServerException::new);
    }


    /**
     * Deletes a file from OMERO
     *
     * @param id ID of the file to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    default void deleteFile(Long id)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        FileAnnotationI file = new FileAnnotationI(id, false);
        delete(file);
    }


    /**
     * Deletes multiple objects from OMERO.
     * <p> Make sure to reload folders before deleting them.
     *
     * @param objects The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(Collection<? extends RemoteObject<?>> objects)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        for (RemoteObject<?> object : objects) {
            if (object instanceof Folder) {
                ((Folder) object).unlinkAllROIs(this);
            }
        }
        if (!objects.isEmpty()) {
            delete(objects.stream().map(RemoteObject::asIObject).collect(Collectors.toList()));
        }
    }


    /**
     * Deletes an object from OMERO.
     * <p> Make sure to reload a folder before deleting it.
     *
     * @param object The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(RemoteObject<?> object)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        if (object instanceof Folder) {
            ((Folder) object).unlinkAllROIs(this);
        }
        delete(object.asIObject());
    }


    /**
     * Deletes a table from OMERO.
     *
     * @param table Table to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException ID not defined.
     * @throws ServerException          Server error.
     * @throws InterruptedException     If block(long) does not return.
     */
    default void delete(Table table)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        deleteFile(table.getId());
    }

}
