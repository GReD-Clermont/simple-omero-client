/*
 *  Copyright (C) 2020-2024 GReD
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
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.containers.Folder;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.SecurityContext;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Interface to add or remove data on an OMERO server in a given {@link SecurityContext}.
 */
public interface DataManager extends BasicDataManager {


    /**
     * Deletes multiple objects from OMERO.
     *
     * @param objects The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(Collection<? extends RemoteObject> objects)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        for (RemoteObject object : objects) {
            if (object instanceof Folder) {
                ((Folder) object).unlinkAllROIs(this);
            }
        }
        if (!objects.isEmpty()) {
            delete(objects.stream()
                          .map(o -> o.asDataObject().asIObject())
                          .collect(Collectors.toList()));
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
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(RemoteObject object)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        if (object instanceof Folder) {
            ((Folder) object).unlinkAllROIs(this);
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
     * @throws InterruptedException     If block(long) does not return.
     */
    default void deleteTable(TableWrapper table)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        deleteFile(table.getId());
    }


    /**
     * Deletes tables from OMERO.
     *
     * @param tables List of tables to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException ID not defined.
     * @throws InterruptedException     If block(long) does not return.
     */
    default void deleteTables(Collection<? extends TableWrapper> tables)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        deleteFiles(tables.stream()
                          .map(TableWrapper::getId)
                          .toArray(Long[]::new));
    }

}
