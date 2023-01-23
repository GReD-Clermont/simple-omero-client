/*
 *  Copyright (C) 2020-2023 GReD
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
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.DataObject;
import omero.model.IObject;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class RemoteObjectWrapper<T extends DataObject> implements RemoteObject<T> {

    /** Wrapped object */
    protected T data;


    /**
     * Constructor of the class RemoteObject.
     *
     * @param dataObject The object contained in the RemoteObject.
     */
    protected RemoteObjectWrapper(T dataObject) {
        this.data = dataObject;
    }


    /**
     * Converts a DataObject list to a RemoteObject list, sorted by {@code sorter}.
     *
     * @param objects The DataObject list.
     * @param mapper  The method used to map objects.
     * @param sorter  The method used to sort the objects.
     * @param <U>     The type of input (extends DataObject).
     * @param <V>     The type of output (extends RemoteObject).
     * @param <W>     The type used to sort the output.
     *
     * @return See above.
     */
    protected static <U extends DataObject, V extends RemoteObject<U>, W extends Comparable<W>> List<V>
    wrap(Collection<U> objects, Function<? super U, ? extends V> mapper, Function<? super V, ? extends W> sorter) {
        return objects.stream()
                      .map(mapper)
                      .sorted(Comparator.comparing(sorter))
                      .collect(Collectors.toList());
    }


    /**
     * Converts a DataObject list to a RemoteObject list, sorted by {@code sorter}.
     *
     * @param objects The DataObject list.
     * @param mapper  The method used to map objects.
     * @param <U>     The type of input (extends DataObject).
     * @param <V>     The type of output (extends RemoteObject).
     *
     * @return See above.
     */
    protected static <U extends DataObject, V extends RemoteObject<U>> List<V>
    wrap(Collection<U> objects, Function<? super U, ? extends V> mapper) {
        return wrap(objects, mapper, RemoteObject::getId);
    }


    /**
     * Deletes an object from OMERO.
     *
     * @param client The client handling the connection.
     * @param object The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    protected static void delete(Client client, IObject object)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        client.delete(object);
    }


    /**
     * Returns a DataObject (or a subclass) corresponding to the handled object.
     *
     * @return An object of type {@link T}.
     */
    @Override
    public T asDataObject() {
        return data;
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
    @Override
    @SuppressWarnings("unchecked")
    public void saveAndUpdate(Client client) throws ExecutionException, ServiceException, AccessException {
        data = (T) handleServiceAndAccess(client.getDm(),
                                          d -> d.saveAndReturnObject(client.getCtx(), asDataObject()),
                                          "Cannot save and update object.");
    }


    /**
     * Overridden to return the name of the class and the object id.
     */
    @Override
    public String toString() {
        return String.format("%s (id=%d)", getClass().getSimpleName(), data.getId());
    }


}
