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


import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.Experimenter;
import fr.igred.omero.meta.ExperimenterWrapper;
import omero.gateway.model.DataObject;

import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class ObjectWrapper<T extends DataObject> implements RemoteObject<T> {

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
     * Returns the wrapped DataObject.
     *
     * @return An object of type {@link T}.
     */
    @Override
    public T asDataObject() {
        return data;
    }


    /**
     * Gets the object id
     *
     * @return id.
     */
    @Override
    public long getId() {
        return data.getId();
    }


    /**
     * Gets the object creation date
     *
     * @return creation date.
     */
    @Override
    public Timestamp getCreated() {
        return data.getCreated();
    }


    /**
     * Gets the owner ID
     *
     * @return owner id.
     */
    @Override
    public Experimenter getOwner() {
        return new ExperimenterWrapper(data.getOwner());
    }


    /**
     * Gets the group ID
     *
     * @return group id.
     */
    @Override
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
     * @param dm The data manager.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void saveAndUpdate(DataManager dm) throws ExecutionException, ServiceException, AccessException {
        data = (T) ExceptionHandler.of(dm.getDataManagerFacility(),
                                       d -> d.saveAndReturnObject(dm.getCtx(), data))
                                   .handleServiceOrAccess("Cannot save and update object.")
                                   .get();
    }


    /**
     * Returns {@code true} if the object can be annotated {@code false} otherwise, depending on permissions level.
     *
     * @return See above.
     */
    @Override
    public boolean canAnnotate() {
        return data.canAnnotate();
    }


    /**
     * Returns {@code true} if the object can be edited by the user currently logged in {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    @Override
    public boolean canEdit() {
        return data.canEdit();
    }


    /**
     * Returns {@code true} if the object can be linked e.g. image add to dataset, by the user currently logged in,
     * {@code false} otherwise, depending on permissions level.
     *
     * @return See above.
     */
    @Override
    public boolean canLink() {
        return data.canLink();
    }


    /**
     * Returns {@code true} if the object can be deleted by the user currently logged in, {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    @Override
    public boolean canDelete() {
        return data.canDelete();
    }


    /**
     * Returns {@code true} if the object can be moved by the user currently logged in, {@code false} otherwise,
     * depending on permissions level.
     *
     * @return See above.
     */
    @Override
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
    @Override
    public boolean canChown() {
        return data.canChown();
    }

}
