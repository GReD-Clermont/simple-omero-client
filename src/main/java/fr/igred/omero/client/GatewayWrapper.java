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
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ExperimenterData;
import omero.log.SimpleLogger;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;


/**
 * Allows the user to connect to OMERO and browse through all the data accessible to the user.
 * <p>
 * Contains the gateway, the security context, and the current user.
 */
public class GatewayWrapper implements Client {

    /** Gateway linking the code to OMERO, only linked to one group. */
    private Gateway gateway;

    /** Security context of the user, contains the permissions of the user in this group. */
    private SecurityContext ctx;

    /** User */
    private ExperimenterWrapper user;


    /**
     * Constructor of the Client class. Initializes the gateway.
     */
    public GatewayWrapper() {
        this(null, null, null);
    }


    /**
     * Constructor of the GatewayWrapper class.
     * <p> Null arguments will be replaced with default empty objects.
     *
     * @param gateway The {@link Gateway}.
     * @param ctx     The security context.
     * @param user    The user.
     */
    protected GatewayWrapper(Gateway gateway, SecurityContext ctx, ExperimenterWrapper user) {
        this.gateway = gateway != null ? gateway : new Gateway(new SimpleLogger());
        this.user = user != null ? user : new ExperimenterWrapper(new ExperimenterData());
        this.ctx = ctx != null ? ctx : new SecurityContext(-1);
    }


    /**
     * Returns the Gateway.
     *
     * @return The Gateway.
     */
    @Override
    public Gateway getGateway() {
        return gateway;
    }


    /**
     * Returns the current user.
     *
     * @return The current user.
     */
    @Override
    public ExperimenterWrapper getUser() {
        return user;
    }


    /**
     * Contains the permissions of the user in the group.
     *
     * @return the {@link SecurityContext} of the user.
     */
    @Override
    public SecurityContext getCtx() {
        return ctx;
    }


    /**
     * Connects the user to OMERO. Gets the SecurityContext and the BrowseFacility.
     *
     * @param credentials User credentials.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Override
    public void connect(LoginCredentials credentials) throws ServiceException {
        disconnect();

        try {
            this.user = new ExperimenterWrapper(gateway.connect(credentials));
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException(oos, oos.getConnectionStatus());
        }
        this.ctx = new SecurityContext(user.getGroupId());
        this.ctx.setExperimenter(this.user.asDataObject());
        this.ctx.setServerInformation(credentials.getServer());
    }


    /**
     * Disconnects the user
     */
    @Override
    public void disconnect() {
        if (isConnected()) {
            boolean sudo = ctx.isSudo();
            user = new ExperimenterWrapper(new ExperimenterData());
            ctx = new SecurityContext(-1);
            ctx.setExperimenter(user.asDataObject());
            if (sudo) {
                gateway = new Gateway(gateway.getLogger());
            } else {
                gateway.disconnect();
            }
        }
    }


    /**
     * Change the current group used by the current user;
     *
     * @param groupId The group ID.
     */
    @Override
    public void switchGroup(long groupId) {
        boolean sudo = ctx.isSudo();
        ctx = new SecurityContext(groupId);
        ctx.setExperimenter(user.asDataObject());
        if (sudo) ctx.sudo();
    }


    /**
     * Returns a Client associated with the given username.
     * <p> All actions realized with the returned Client will be considered as his.
     * <p> The user calling this function needs to have administrator rights.
     *
     * @param username The user name.
     *
     * @return The connection and context corresponding to the new user.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     * @throws NoSuchElementException The requested user does not exist.
     */
    @Override
    public Client sudo(String username) throws ServiceException, AccessException, ExecutionException {
        ExperimenterWrapper sudoUser = getUser(username);

        SecurityContext context = new SecurityContext(sudoUser.getDefaultGroup().getId());
        context.setExperimenter(sudoUser.asDataObject());
        context.sudo();

        return new GatewayWrapper(this.gateway, context, sudoUser);
    }


    /**
     * Overridden to return the host name, the group ID, the username and the connection status.
     *
     * @return See above.
     */
    @Override
    public String toString() {
        String host = ctx.getServerInformation() != null ? ctx.getServerInformation().getHost() : "null";
        return String.format("%s{host=%s, groupID=%d, userID=%d, connected=%b}",
                             getClass().getSimpleName(),
                             host,
                             ctx.getGroupID(),
                             user.getId(),
                             gateway.isConnected());
    }

}
