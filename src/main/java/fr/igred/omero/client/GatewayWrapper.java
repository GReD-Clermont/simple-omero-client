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
import fr.igred.omero.meta.Experimenter;
import ome.formats.OMEROMetadataStoreClient;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.ServerInformation;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ExperimenterData;
import omero.log.SimpleLogger;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Basic class, containing the gateway, the security context, and the current user.
 * <p>
 * Allows the user to connect to OMERO and browse through all the data accessible to the user.
 */
public class GatewayWrapper extends BrowserWrapper implements Client {

    /** Number of requested import stores */
    private final AtomicInteger storeUses = new AtomicInteger(0);

    /** Import store lock */
    private final Lock storeLock = new ReentrantLock(true);

    /** Gateway linking the code to OMERO, only linked to one group. */
    private Gateway gateway;

    /** Security context of the user, contains the permissions of the user in this group. */
    private SecurityContext ctx;

    /** User */
    private Experimenter user;


    /**
     * Constructor of the GatewayWrapper class. Initializes the gateway.
     */
    public GatewayWrapper() {
        this(null, null, null);
    }


    /**
     * Constructor of the GatewayWrapper class.
     * <p> Null arguments will be replaced with default empty objects.
     *
     * @param gateway The {@link Gateway}.
     * @param ctx     The Security Context.
     * @param user    The connected user.
     */
    public GatewayWrapper(Gateway gateway, SecurityContext ctx, Experimenter user) {
        this.gateway = gateway != null ? gateway : new Gateway(new SimpleLogger());
        this.user    = user != null ? user : new Experimenter(new ExperimenterData());
        this.ctx     = ctx != null ? ctx : new SecurityContext(-1);
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
     * Retrieves the shared import store in a thread-safe way.
     *
     * @throws DSOutOfServiceException If the connection is broken, or not logged in.
     */
    private OMEROMetadataStoreClient getImportStoreLocked()
    throws DSOutOfServiceException {
        storeLock.lock();
        try {
            return gateway.getImportStore(ctx);
        } finally {
            storeLock.unlock();
        }
    }


    /**
     * Closes the import store in a thread-safe manner.
     */
    private void closeImportStoreLocked() {
        if (storeLock.tryLock()) {
            try {
                Client.super.closeImport();
            } finally {
                storeLock.unlock();
            }
        }
    }


    /**
     * Returns the current user.
     *
     * @return The current user.
     */
    @Override
    public Experimenter getUser() {
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
            this.user = new Experimenter(gateway.connect(credentials));
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
            storeUses.set(0);
            closeImport();
            user = new Experimenter(new ExperimenterData());
            ctx  = new SecurityContext(-1);
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
        if (sudo) {
            ctx.sudo();
        }
    }


    /**
     * Creates or recycles the import store.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Override
    public OMEROMetadataStoreClient getImportStore() throws ServiceException {
        storeUses.incrementAndGet();
        return ExceptionHandler.of(this, GatewayWrapper::getImportStoreLocked)
                               .rethrow(DSOutOfServiceException.class,
                                        ServiceException::new,
                                        "Could not retrieve import store")
                               .get();
    }


    /**
     * Closes the import store.
     */
    @Override
    public void closeImport() {
        int remainingStores = storeUses.decrementAndGet();
        if (remainingStores <= 0) {
            closeImportStoreLocked();
        }
    }


    /**
     * Returns a Client associated with the given username.
     * <p> All actions realized with the returned Client will be considered as his.
     * <p> The user calling this function needs to have administrator rights.
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
    @Override
    public Client sudo(String username)
    throws ServiceException, AccessException, ExecutionException {
        Experimenter sudoUser = getUser(username);
        long         groupId  = sudoUser.getDefaultGroup().getId();

        SecurityContext context = new SecurityContext(groupId);
        context.setExperimenter(sudoUser.asDataObject());
        context.sudo();

        return new GatewayWrapper(gateway, context, sudoUser);
    }


    /**
     * Overridden to return the host name, the group ID, the username and the connection status.
     *
     * @return See above.
     */
    @Override
    public String toString() {
        ServerInformation serverInfo = ctx.getServerInformation();

        String host = serverInfo != null ? serverInfo.getHost() : "null";
        return String.format("%s{host=%s, groupID=%d, userID=%d, connected=%b}",
                             getClass().getSimpleName(),
                             host,
                             ctx.getGroupID(),
                             user.getId(),
                             gateway.isConnected());
    }

}
