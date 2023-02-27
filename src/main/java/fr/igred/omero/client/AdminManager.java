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


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.Experimenter;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.Group;
import fr.igred.omero.meta.GroupWrapper;
import omero.gateway.SecurityContext;
import omero.gateway.facility.AdminFacility;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;


/**
 * Interface to handle admin functions on an OMERO server in a given {@link SecurityContext}.
 */
public interface AdminManager {

    /**
     * Returns the current {@link SecurityContext}.
     *
     * @return See above
     */
    SecurityContext getCtx();


    /**
     * Gets the {@link AdminFacility} to use admin specific function.
     *
     * @return See above.
     *
     * @throws ExecutionException If the AdminFacility can't be retrieved or instantiated.
     */
    AdminFacility getAdminFacility() throws ExecutionException;


    /**
     * Returns the user which matches the username.
     *
     * @param username The name of the user.
     *
     * @return The user matching the username, or null if it does not exist.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     * @throws NoSuchElementException The requested user does not exist.
     */
    default Experimenter getUser(String username)
    throws ExecutionException, ServiceException, AccessException {
        ExperimenterData experimenter = ExceptionHandler.of(getAdminFacility(),
                                                            a -> a.lookupExperimenter(getCtx(), username))
                                                        .handleServiceOrAccess("Cannot retrieve user: " + username)
                                                        .get();
        if (experimenter != null) {
            return new ExperimenterWrapper(experimenter);
        } else {
            throw new NoSuchElementException(String.format("User not found: %s", username));
        }
    }


    /**
     * Returns the group which matches the name.
     *
     * @param groupName The name of the group.
     *
     * @return The group with the appropriate name, if it exists.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     * @throws NoSuchElementException The requested group does not exist.
     */
    default Group getGroup(String groupName)
    throws ExecutionException, ServiceException, AccessException {
        GroupData group = ExceptionHandler.of(getAdminFacility(), a -> a.lookupGroup(getCtx(), groupName))
                                          .handleServiceOrAccess("Cannot retrieve group: " + groupName)
                                          .get();
        if (group != null) {
            return new GroupWrapper(group);
        } else {
            throw new NoSuchElementException(String.format("Group not found: %s", groupName));
        }
    }

}
