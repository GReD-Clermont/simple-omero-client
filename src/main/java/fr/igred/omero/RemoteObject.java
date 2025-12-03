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

package fr.igred.omero;


import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.Experimenter;
import omero.gateway.model.DataObject;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


/**
 * Generic interface to handle OMERO objects.
 */
public interface RemoteObject {


    /**
     * Gets a list of elements of the specified class from a collection of RemoteObjects.
     *
     * @param clazz      Class of the wanted elements.
     * @param collection The collection of RemoteObjects
     * @param <U>        Subclass of RemoteObject.
     * @param <V>        Subclass of U.
     *
     * @return See above.
     */
    static <U extends RemoteObject, V extends U> List<V> getElementsOf(Collection<? extends U> collection,
                                                                       Class<? extends V> clazz) {
        return collection.stream()
                         .filter(clazz::isInstance)
                         .map(clazz::cast)
                         .collect(toList());
    }


    /**
     * Only keeps objects with different IDs in a collection.
     *
     * @param objects A collection of objects.
     * @param <U>     The objects type.
     *
     * @return Distinct objects list, sorted by ID.
     */
    static <U extends RemoteObject> List<U> distinct(Collection<? extends U> objects) {
        return objects.stream()
                      .collect(toMap(U::getId, o -> o, (o1, o2) -> o1))
                      .values()
                      .stream()
                      .sorted(Comparator.comparing(U::getId))
                      .collect(toList());
    }


    /**
     * Flattens a collection of collections and only keeps objects with different IDs.
     *
     * @param lists A collection of objects collections.
     * @param <U>   The objects type.
     *
     * @return Distinct objects list, sorted by ID.
     */
    static <U extends RemoteObject>
    List<U> flatten(Collection<? extends Collection<? extends U>> lists) {
        return lists.stream()
                    .flatMap(Collection::stream)
                    .collect(toMap(U::getId, o -> o, (o1, o2) -> o1))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(U::getId))
                    .collect(toList());
    }


    /**
     * Returns the corresponding DataObject.
     *
     * @return The object as a DataObject.
     */
    DataObject asDataObject();


    /**
     * Gets the object id
     *
     * @return id.
     */
    long getId();


    /**
     * Gets the time when the object was created.
     *
     * @return See above.
     */
    Timestamp getCreated();


    /**
     * Gets the time when the object was last updated.
     *
     * @return See above.
     */
    Timestamp getUpdated();


    /**
     * Gets the owner ID
     *
     * @return owner id.
     */
    @SuppressWarnings("ClassReferencesSubclass")
    Experimenter getOwner();


    /**
     * Gets the group ID
     *
     * @return group id.
     */
    Long getGroupId();


    /**
     * Saves and updates object.
     *
     * @param dm The data manager.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void saveAndUpdate(DataManager dm)
    throws ExecutionException, ServiceException, AccessException;


    /**
     * Returns {@code true} if the object can be annotated {@code false} otherwise, depending on permissions level.
     *
     * @return See above.
     */
    boolean canAnnotate();


    /**
     * Returns {@code true} if the object can be edited by the user currently logged in {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    boolean canEdit();


    /**
     * Returns {@code true} if the object can be linked e.g. image add to dataset, by the user currently logged in,
     * {@code false} otherwise, depending on permissions level.
     *
     * @return See above.
     */
    boolean canLink();


    /**
     * Returns {@code true} if the object can be deleted by the user currently logged in, {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    boolean canDelete();


    /**
     * Returns {@code true} if the object can be moved by the user currently logged in, {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    boolean canChgrp();


    /**
     * Returns {@code true} if the object can be given by the user currently logged in, {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    boolean canChown();

}
