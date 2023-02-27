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


import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.annotations.AnnotationWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import fr.igred.omero.containers.FolderWrapper;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.containers.ProjectWrapper;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.ObjectWrapper.flatten;


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
     * Deletes multiple objects from OMERO.
     *
     * @param objects The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(Collection<? extends ObjectWrapper<?>> objects)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        for (ObjectWrapper<?> object : objects) {
            if (object instanceof FolderWrapper) {
                ((FolderWrapper) object).unlinkAllROIs(this);
            }
        }
        if (!objects.isEmpty()) {
            delete(objects.stream().map(o -> o.asDataObject().asIObject()).collect(Collectors.toList()));
        }
    }


    /**
     * Deletes an object from OMERO.
     * <p> Make sure a folder is loaded before deleting it.
     *
     * @param object The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(ObjectWrapper<?> object)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        if (object instanceof FolderWrapper) {
            ((FolderWrapper) object).unlinkAllROIs(this);
        }
        delete(object.asDataObject().asIObject());
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
    default void delete(TableWrapper table)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        deleteFile(table.getId());
    }

}
