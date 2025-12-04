/*
 *  Copyright (C) 2020-2025 GReD
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
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.Group;
import fr.igred.omero.meta.GroupWrapper;
import omero.ApiUsageException;
import omero.api.IAdminPrx;
import omero.gateway.SecurityContext;
import omero.gateway.facility.AdminFacility;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.call;


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
     * Returns the {@link IAdminPrx} to use admin specific function.
     *
     * @return See above.
     *
     * @throws AccessException  Cannot access data.
     * @throws ServiceException Cannot connect to OMERO.
     */
    IAdminPrx getAdminService() throws AccessException, ServiceException;


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
     * @throws NoSuchElementException The requested user cannot be found.
     */
    default fr.igred.omero.meta.Experimenter getUser(String username)
    throws ExecutionException, ServiceException, AccessException {
        ExperimenterData user = call(getAdminFacility(),
                                     a -> a.lookupExperimenter(getCtx(), username),
                                     "Cannot retrieve user: " + username);
        if (user != null) {
            return new ExperimenterWrapper(user);
        } else {
            String msg = String.format("User not found: %s", username);
            throw new NoSuchElementException(msg);
        }
    }


    /**
     * Returns the user which matches the user ID.
     *
     * @param userId The ID of the user.
     *
     * @return The user matching the user ID, or null if it does not exist.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException The requested user cannot be found.
     */
    default fr.igred.omero.meta.Experimenter getUser(long userId)
    throws ServiceException, AccessException {
        Experimenter user = ExceptionHandler.of(getAdminService(),
                                                a -> a.getExperimenter(userId))
                                            .rethrow(ApiUsageException.class,
                                                     (m, e) -> new NoSuchElementException(m),
                                                     "User not found: " + userId)
                                            .handleOMEROException("Cannot retrieve user: " + userId)
                                            .get();
        return new ExperimenterWrapper(new ExperimenterData(user));
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
     * @throws NoSuchElementException The requested group cannot be found.
     */
    default Group getGroup(String groupName)
    throws ExecutionException, ServiceException, AccessException {
        GroupData group = call(getAdminFacility(),
                               a -> a.lookupGroup(getCtx(), groupName),
                               "Cannot retrieve group: " + groupName);
        if (group != null) {
            return new GroupWrapper(group);
        } else {
            String msg = String.format("Group not found: %s", groupName);
            throw new NoSuchElementException(msg);
        }
    }


    /**
     * Returns the group which matches the group ID.
     *
     * @param groupId The ID of the group.
     *
     * @return The group with the appropriate group ID, if it exists.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException The requested group cannot be found.
     */
    default Group getGroup(long groupId)
    throws ServiceException, AccessException {
        ExperimenterGroup group = ExceptionHandler.of(getAdminService(),
                                                      a -> a.getGroup(groupId))
                                                  .rethrow(ApiUsageException.class,
                                                           (m, e) -> new NoSuchElementException(m),
                                                           "Group not found: " + groupId)
                                                  .handleOMEROException("Cannot retrieve group: " + groupId)
                                                  .get();
        return new GroupWrapper(new GroupData(group));
    }


    /**
     * Returns all the groups on OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    default List<Group> getGroups()
    throws ServiceException, AccessException {
        String error = "Cannot retrieve the groups on OMERO";
        List<ExperimenterGroup> groups = call(getAdminService(),
                                              IAdminPrx::lookupGroups,
                                              error);
        return groups.stream()
                     .filter(Objects::nonNull)
                     .map(GroupData::new)
                     .map(GroupWrapper::new)
                     .collect(Collectors.toList());
    }

}
