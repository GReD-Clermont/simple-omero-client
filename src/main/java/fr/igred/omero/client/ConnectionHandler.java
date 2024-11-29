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


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import ome.formats.OMEROMetadataStoreClient;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import static java.lang.String.valueOf;


/**
 * Interface to handle the connection to an OMERO server through a {@link Gateway} for a given user and in a specific
 * {@link SecurityContext}.
 */
public interface ConnectionHandler {

    /**
     * Returns the {@link Gateway} used for the connection.
     *
     * @return See above.
     */
    Gateway getGateway();


    /**
     * Returns the current user.
     *
     * @return See above.
     */
    ExperimenterWrapper getUser();


    /**
     * Returns the current {@link SecurityContext}.
     *
     * @return See above
     */
    SecurityContext getCtx();


    /**
     * Gets the user id.
     *
     * @return The user ID.
     */
    default long getId() {
        return getUser().getId();
    }


    /**
     * Gets the current group ID.
     *
     * @return The group ID.
     */
    default long getCurrentGroupId() {
        return getCtx().getGroupID();
    }


    /**
     * Get the ID of the current session
     *
     * @return See above
     *
     * @throws ServiceException If the connection is broken, or not logged in
     */
    default String getSessionId() throws ServiceException {
        return ExceptionHandler.of(getGateway(),
                                   g -> g.getSessionId(getUser().asDataObject()))
                               .rethrow(DSOutOfServiceException.class,
                                        ServiceException::new,
                                        "Could not retrieve session ID")
                               .get();
    }


    /**
     * Check if the client is still connected to the server
     *
     * @return See above.
     */
    default boolean isConnected() {
        return getGateway().isConnected();
    }


    /**
     * Connects to OMERO using a session ID.
     *
     * @param hostname  Name of the host.
     * @param port      Port used by OMERO.
     * @param sessionId The session ID.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    default void connect(String hostname, int port, String sessionId)
    throws ServiceException {
        connect(new LoginCredentials(sessionId, sessionId, hostname, port));
    }


    /**
     * Connects the user to OMERO.
     * <p> Uses the argument to connect to the gateway.
     * <p> Connects to the group specified in the argument.
     *
     * @param hostname Name of the host.
     * @param port     Port used by OMERO.
     * @param username Username of the user.
     * @param password Password of the user.
     * @param groupID  ID of the group to connect.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    default void connect(String hostname, int port, String username, char[] password, Long groupID)
    throws ServiceException {
        LoginCredentials cred = new LoginCredentials(username,
                                                     valueOf(password),
                                                     hostname,
                                                     port);
        cred.setGroupID(groupID);
        connect(cred);
    }


    /**
     * Connects the user to OMERO.
     * <p> Uses the argument to connect to the gateway.
     * <p> Connects to the default group of the user.
     *
     * @param hostname Name of the host.
     * @param port     Port used by OMERO.
     * @param username Username of the user.
     * @param password Password of the user.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    default void connect(String hostname, int port, String username, char[] password)
    throws ServiceException {
        connect(new LoginCredentials(username,
                                     valueOf(password),
                                     hostname,
                                     port));
    }


    /**
     * Connects the user to OMERO. Gets the SecurityContext and the BrowseFacility.
     *
     * @param credentials User credentials.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    void connect(LoginCredentials credentials) throws ServiceException;


    /**
     * Disconnects the user
     */
    void disconnect();


    /**
     * Change the current group used by the current user;
     *
     * @param groupId The group ID.
     */
    void switchGroup(long groupId);


    /**
     * Creates or recycles the import store.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    OMEROMetadataStoreClient getImportStore()
    throws ServiceException;


    /**
     * Closes the import store.
     */
    default void closeImport() {
        getGateway().closeImport(getCtx(), null);
    }


    /**
     * Returns a ConnectionHandler associated with the provided username.
     * <p>The user calling this function needs to have administrator rights.
     * <p>All actions realized with the returned ConnectionHandler will be considered as his.
     *
     * @param username The username.
     *
     * @return The client corresponding to the new user.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     * @throws NoSuchElementException The requested user does not exist.
     */
    ConnectionHandler sudo(String username)
    throws ServiceException, AccessException, ExecutionException;

}
