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
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.Experimenter;
import omero.gateway.model.DataObject;
import omero.model.IObject;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Generic interface to handle OMERO objects.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public interface RemoteObject<T extends DataObject> {

    /**
     * Only keeps objects with different IDs in a collection.
     *
     * @param objects A collection of objects.
     * @param <T>     The objects type.
     *
     * @return Distinct objects list, sorted by ID.
     */
    static <T extends RemoteObject<?>> List<T> distinct(Collection<? extends T> objects) {
        return objects.stream()
                      .collect(Collectors.toMap(T::getId, o -> o))
                      .values()
                      .stream()
                      .sorted(Comparator.comparing(T::getId))
                      .collect(Collectors.toList());
    }


    /**
     * Returns a DataObject (or a subclass) corresponding to the handled object.
     *
     * @return An object of type {@link T}.
     */
    T asDataObject();


    /**
     * Returns the contained DataObject as an IObject.
     *
     * @return See above.
     */
    default IObject asIObject() {
        return asDataObject().asIObject();
    }


    /**
     * Gets the object id
     *
     * @return id.
     */
    long getId();


    /**
     * Gets the object creation date
     *
     * @return creation date.
     */
    Timestamp getCreated();


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
     * Overridden to return the name of the class and the object id.
     */
    @Override
    String toString();


    /**
     * Saves and updates object.
     *
     * @param dm The data manager.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void saveAndUpdate(DataManager dm) throws ExecutionException, ServiceException, AccessException;


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