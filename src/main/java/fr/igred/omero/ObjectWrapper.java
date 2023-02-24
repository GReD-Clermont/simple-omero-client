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

package fr.igred.omero;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DataObject;
import omero.model.IObject;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class ObjectWrapper<T extends DataObject> {

    /** Wrapped object */
    protected T data;


    /**
     * Constructor of the class ObjectWrapper.
     *
     * @param o The object contained in the ObjectWrapper.
     */
    protected ObjectWrapper(T o) {
        this.data = o;
    }


    /**
     * Converts a DataObject list to a ObjectWrapper list, sorted by {@code sorter}.
     *
     * @param objects The DataObject list.
     * @param mapper  The method used to map objects.
     * @param sorter  The method used to sort the objects.
     * @param <U>     The type of input (extends DataObject).
     * @param <V>     The type of output (extends ObjectWrapper).
     * @param <W>     The type used to sort the output.
     *
     * @return See above.
     */
    protected static <U extends DataObject, V extends ObjectWrapper<U>, W extends Comparable<W>> List<V>
    wrap(Collection<U> objects, Function<? super U, ? extends V> mapper, Function<? super V, ? extends W> sorter) {
        return objects.stream()
                      .map(mapper)
                      .sorted(Comparator.comparing(sorter))
                      .collect(Collectors.toList());
    }


    /**
     * Converts a DataObject list to a ObjectWrapper list, sorted by {@code sorter}.
     *
     * @param objects The DataObject list.
     * @param mapper  The method used to map objects.
     * @param <U>     The type of input (extends DataObject).
     * @param <V>     The type of output (extends ObjectWrapper).
     *
     * @return See above.
     */
    protected static <U extends DataObject, V extends ObjectWrapper<U>> List<V>
    wrap(Collection<U> objects, Function<? super U, ? extends V> mapper) {
        return wrap(objects, mapper, ObjectWrapper::getId);
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
     * @throws ServerException     Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    protected static void delete(Client client, IObject object)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        client.delete(object);
    }


    /**
     * Only keeps objects with different IDs in a collection.
     *
     * @param objects A collection of objects.
     * @param <T>     The objects type.
     *
     * @return Distinct objects list, sorted by ID.
     */
    public static <T extends ObjectWrapper<?>> List<T> distinct(Collection<? extends T> objects) {
        return objects.stream()
                      .collect(Collectors.toMap(T::getId, o -> o, (o1, o2) -> o1))
                      .values()
                      .stream()
                      .sorted(Comparator.comparing(T::getId))
                      .collect(Collectors.toList());
    }


    /**
     * Flattens a collection of collections and only keeps objects with different IDs.
     *
     * @param lists A collection of objects collections.
     * @param <U>   The objects type.
     *
     * @return Distinct objects list, sorted by ID.
     */
    public static <U extends ObjectWrapper<?>>
    List<U> flatten(Collection<? extends Collection<? extends U>> lists) {
        return lists.stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(U::getId, o -> o, (o1, o2) -> o1))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(U::getId))
                    .collect(Collectors.toList());
    }


    /**
     * Returns the wrapped DataObject.
     *
     * @return An object of type {@link T}.
     */
    public T asDataObject() {
        return data;
    }


    /**
     * Returns the contained DataObject as IObject.
     *
     * @return See above.
     */
    IObject asIObject() {
        return data.asIObject();
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
    @SuppressWarnings("ClassReferencesSubclass")
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
     */
    @Override
    public String toString() {
        return String.format("%s (id=%d)", getClass().getSimpleName(), data.getId());
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
     * Returns {@code true} if the object can be annotated {@code false} otherwise, depending on permissions level.
     *
     * @return See above.
     */
    public boolean canAnnotate() {
        return data.canAnnotate();
    }


    /**
     * Returns {@code true} if the object can be edited by the user currently logged in {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    public boolean canEdit() {
        return data.canEdit();
    }


    /**
     * Returns {@code true} if the object can be linked e.g. image add to dataset, by the user currently logged in,
     * {@code false} otherwise, depending on permissions level.
     *
     * @return See above.
     */
    public boolean canLink() {
        return data.canLink();
    }


    /**
     * Returns {@code true} if the object can be deleted by the user currently logged in, {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    public boolean canDelete() {
        return data.canDelete();
    }


    /**
     * Returns {@code true} if the object can be moved by the user currently logged in, {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    public boolean canChgrp() {
        data.getPermissions().getPermissionsLevel();
        return data.canChgrp();
    }


    /**
     * Returns {@code true} if the object can be given by the user currently logged in, {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    public boolean canChown() {
        return data.canChown();
    }

}
