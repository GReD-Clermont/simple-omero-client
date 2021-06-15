/*
 *  Copyright (C) 2020 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.

 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DataObject;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class GenericObjectWrapper<T extends DataObject> {

    protected T data;


    /**
     * Constructor of the class GenericObjectWrapper.
     *
     * @param object The object contained in the GenericObjectWrapper.
     */
    protected GenericObjectWrapper(T object) {
        this.data = object;
    }


    /**
     * Gets the object id
     *
     * @return id.
     */
    public long getId() {
        return data.getId();
    }


    /**
     * Gets the object creation date
     *
     * @return creation date.
     */
    public Timestamp getCreated() {
        return data.getCreated();
    }


    /**
     * Gets the owner ID
     *
     * @return owner id.
     */
    public ExperimenterWrapper getOwner() {
        return new ExperimenterWrapper(data.getOwner());
    }


    /**
     * Gets the group ID
     *
     * @return group id.
     */
    public Long getGroupId() {
        return data.getGroupId();
    }


    /**
     * Overridden to return the name of the class and the object id.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s (id=%d)", getClass().getSimpleName(), getId());
    }


    /**
     * Saves and updates object.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @SuppressWarnings("unchecked")
    public void saveAndUpdate(Client client) throws ExecutionException, ServiceException, AccessException {
        try {
            data = (T) client.getDm().saveAndReturnObject(client.getCtx(), data);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot save and update object.");
        }
    }


    /**
     * Class used to sort wrappers.
     */
    public static class SortById<U extends GenericObjectWrapper<?>> implements Comparator<U> {

        /**
         * Compare 2 ObjectWrappers. Compare the id of the ObjectWrappers.
         *
         * @param object1 First object to compare.
         * @param object2 Second object to compare.
         *
         * @return <ul><li>-1 if the id of object1 is lower than the id object2.</li>
         * <li>0  if the ids are the same.</li>
         * <li>1 if the id of object1 is greater than the id of object2.</li></ul>
         */
        public int compare(U object1, U object2) {
            return Long.compare(object1.getId(), object2.getId());
        }

    }

}
