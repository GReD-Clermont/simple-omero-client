/*
 *  Copyright (C) 2020-2022 GReD
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
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import ome.formats.OMEROMetadataStoreClient;
import omero.LockTimeout;
import omero.ServerError;
import omero.gateway.Gateway;
import omero.gateway.JoinSessionCredentials;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.MetadataFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.facility.TablesFacility;
import omero.model.FileAnnotationI;
import omero.model.IObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleException;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrServer;


/**
 * Basic class, contains the gateway, the security context, and multiple facilities.
 * <p>
 * Allows the user to connect to OMERO and browse through all the data accessible to the user.
 */
public abstract class GatewayWrapper {

    /** Gateway linking the code to OMERO, only linked to one group. */
    private final Gateway gateway;

    /** Security context of the user, contains the permissions of the user in this group. */
    private SecurityContext ctx;

    /** User */
    private ExperimenterWrapper user;


    /**
     * Abstract constructor of the GatewayWrapper class.
     *
     * @param gateway The Gateway.
     */
    protected GatewayWrapper(Gateway gateway) {
        this(gateway, null, null);
    }


    /**
     * Abstract constructor of the GatewayWrapper class.
     *
     * @param gateway The Gateway.
     * @param ctx     The Security Context.
     * @param user    The connected user.
     */
    protected GatewayWrapper(Gateway gateway, SecurityContext ctx, ExperimenterWrapper user) {
        this.gateway = gateway;
        this.ctx = ctx;
        this.user = user;
    }


    /**
     * Returns the Gateway.
     *
     * @return The Gateway.
     */
    public Gateway getGateway() {
        return gateway;
    }


    /**
     * Returns the current user.
     *
     * @return The current user.
     */
    public ExperimenterWrapper getUser() {
        return user;
    }


    /**
     * Contains the permissions of the user in the group.
     *
     * @return the {@link SecurityContext} of the user.
     */
    public SecurityContext getCtx() {
        return ctx;
    }


    /**
     * Gets the user id.
     *
     * @return The user ID.
     */
    public long getId() {
        return user.getId();
    }


    /**
     * Gets the current group ID.
     *
     * @return The group ID.
     */
    public long getCurrentGroupId() {
        return ctx.getGroupID();
    }


    /**
     * Get the ID of the current session
     *
     * @return See above
     *
     * @throws ServiceException If the connection is broken, or not logged in
     */
    public String getSessionId() throws ServiceException {
        String sessionId;
        try {
            sessionId = gateway.getSessionId(user.asExperimenterData());
        } catch (DSOutOfServiceException e) {
            throw new ServiceException("Could not retrieve session ID", e, e.getConnectionStatus());
        }
        return sessionId;
    }


    /**
     * Check if the client is still connected to the server
     *
     * @return See above.
     */
    public boolean isConnected() {
        return gateway.isConnected() && ctx != null;
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
    public void connect(String hostname, int port, String sessionId)
    throws ServiceException {
        connect(new JoinSessionCredentials(sessionId, hostname, port));
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
    public void connect(String hostname, int port, String username, char[] password, Long groupID)
    throws ServiceException {
        LoginCredentials cred = new LoginCredentials(username, String.valueOf(password), hostname, port);
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
    public void connect(String hostname, int port, String username, char[] password)
    throws ServiceException {
        connect(new LoginCredentials(username, String.valueOf(password), hostname, port));
    }


    /**
     * Connects the user to OMERO. Gets the SecurityContext and the BrowseFacility.
     *
     * @param cred User credential.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    public void connect(LoginCredentials cred) throws ServiceException {
        disconnect();

        try {
            this.user = new ExperimenterWrapper(gateway.connect(cred));
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException(oos, oos.getConnectionStatus());
        }
        this.ctx = new SecurityContext(user.getGroupId());
        this.ctx.setExperimenter(this.user.asExperimenterData());
    }


    /**
     * Disconnects the user
     */
    public void disconnect() {
        if (gateway.isConnected()) {
            if (ctx != null) {
                ctx.setExperimenter(null);
            }
            ctx = null;
            user = null;
            gateway.disconnect();
        }
    }


    /**
     * Change the current group used by the current user;
     *
     * @param groupId The group ID.
     */
    public void switchGroup(long groupId) {
        boolean sudo = ctx.isSudo();
        ctx = new SecurityContext(groupId);
        ctx.setExperimenter(user.asExperimenterData());
        if (sudo) ctx.sudo();
    }


    /**
     * Gets the BrowseFacility used to access the data from OMERO.
     *
     * @return the {@link BrowseFacility} linked to the gateway.
     *
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public BrowseFacility getBrowseFacility() throws ExecutionException {
        return gateway.getFacility(BrowseFacility.class);
    }


    /**
     * Gets the DataManagerFacility to handle/write data on OMERO. A
     *
     * @return the {@link DataManagerFacility} linked to the gateway.
     *
     * @throws ExecutionException If the DataManagerFacility can't be retrieved or instantiated.
     */
    public DataManagerFacility getDm() throws ExecutionException {
        return gateway.getFacility(DataManagerFacility.class);
    }


    /**
     * Gets the MetadataFacility used to manipulate annotations from OMERO.
     *
     * @return the {@link MetadataFacility} linked to the gateway.
     *
     * @throws ExecutionException If the MetadataFacility can't be retrieved or instantiated.
     */
    public MetadataFacility getMetadata() throws ExecutionException {
        return gateway.getFacility(MetadataFacility.class);
    }


    /**
     * Gets the ROIFacility used to manipulate ROI from OMERO.
     *
     * @return the {@link ROIFacility} linked to the gateway.
     *
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    public ROIFacility getRoiFacility() throws ExecutionException {
        return gateway.getFacility(ROIFacility.class);
    }


    /**
     * Gets the TablesFacility used to manipulate table from OMERO.
     *
     * @return the {@link TablesFacility} linked to the gateway.
     *
     * @throws ExecutionException If the TablesFacility can't be retrieved or instantiated.
     */
    public TablesFacility getTablesFacility() throws ExecutionException {
        return gateway.getFacility(TablesFacility.class);
    }


    /**
     * Gets the AdminFacility linked to the gateway to use admin specific function.
     *
     * @return the {@link AdminFacility} linked to the gateway.
     *
     * @throws ExecutionException If the AdminFacility can't be retrieved or instantiated.
     */
    public AdminFacility getAdminFacility() throws ExecutionException {
        return gateway.getFacility(AdminFacility.class);
    }


    /**
     * Creates or recycles the import store.
     *
     * @return config.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    public OMEROMetadataStoreClient getImportStore() throws ServiceException {
        OMEROMetadataStoreClient store;
        try {
            store = gateway.getImportStore(ctx);
        } catch (DSOutOfServiceException e) {
            throw new ServiceException("Could not retrieve import store", e, e.getConnectionStatus());
        }
        return store;
    }


    /**
     * Finds objects on OMERO through a database query.
     *
     * @param query The database query.
     *
     * @return A list of OMERO objects.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError Server error.
     */
    public List<IObject> findByQuery(String query) throws ServiceException, OMEROServerError {
        List<IObject> results = new ArrayList<>(0);
        try {
            results = gateway.getQueryService(ctx).findAllByQuery(query, null);
        } catch (DSOutOfServiceException | ServerError e) {
            handleServiceOrServer(e, "Query failed: " + query);
        }

        return results;
    }


    /**
     * Saves an object on OMERO.
     *
     * @param object The OMERO object.
     *
     * @return The saved OMERO object
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public IObject save(IObject object) throws ServiceException, AccessException, ExecutionException {
        IObject result = object;
        try {
            result = getDm().saveAndReturnObject(ctx, object);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot save object");
        }
        return result;
    }


    /**
     * Deletes an object from OMERO.
     *
     * @param object The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError     If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    void delete(IObject object)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        final int ms = 500;
        try {
            getDm().delete(ctx, object).loop(10, ms);
        } catch (DSOutOfServiceException | DSAccessException | LockTimeout e) {
            handleException(e, "Cannot delete object");
        }
    }


    /**
     * Deletes a file from OMERO
     *
     * @param id Id of the file to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError     If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    public void deleteFile(Long id)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        FileAnnotationI file = new FileAnnotationI(id, false);
        delete(file);
    }

}

