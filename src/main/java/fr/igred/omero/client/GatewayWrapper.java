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


import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import ome.formats.OMEROMetadataStoreClient;
import omero.api.IQueryPrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.MetadataFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.ExperimenterData;
import omero.log.SimpleLogger;

import java.util.concurrent.ExecutionException;


/**
 * Basic class, contains the gateway, the security context, and multiple facilities.
 * <p>
 * Allows the user to connect to OMERO and browse through all the data accessible to the user.
 */
public abstract class GatewayWrapper implements Browser,ConnectionHandler,DataManager {

    /** Gateway linking the code to OMERO, only linked to one group. */
    private Gateway gateway;

    /** Security context of the user, contains the permissions of the user in this group. */
    private SecurityContext ctx;

    /** User */
    private ExperimenterWrapper user;


    /**
     * Abstract constructor of the GatewayWrapper class.
     * <p> Null arguments will be replaced with default empty objects.
     *
     * @param gateway The Gateway.
     * @param ctx     The Security Context.
     * @param user    The connected user.
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
     * @param cred User credential.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Override
    public void connect(LoginCredentials cred) throws ServiceException {
        disconnect();

        try {
            this.user = new ExperimenterWrapper(gateway.connect(cred));
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException(oos, oos.getConnectionStatus());
        }
        this.ctx = new SecurityContext(user.getGroupId());
        this.ctx.setExperimenter(this.user.asDataObject());
        this.ctx.setServerInformation(cred.getServer());
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
     * Gets the {@link BrowseFacility} used to access the data from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public BrowseFacility getBrowseFacility() throws ExecutionException {
        return gateway.getFacility(BrowseFacility.class);
    }


    /**
     * Returns the {@link IQueryPrx} used to find objects on OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     */
    @Override
    public IQueryPrx getQueryService() throws ServiceException {
        return ExceptionHandler.of(gateway, g -> g.getQueryService(ctx))
                               .rethrow(DSOutOfServiceException.class, ServiceException::new,
                                        "Could not retrieve Query Service")
                               .get();
    }


    /**
     * Gets the {@link MetadataFacility} used to retrieve annotations from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the MetadataFacility can't be retrieved or instantiated.
     */
    @Override
    public MetadataFacility getMetadata() throws ExecutionException {
        return gateway.getFacility(MetadataFacility.class);
    }


    /**
     * Gets the {@link DataManagerFacility} to handle/write data on OMERO. A
     *
     * @return See above.
     *
     * @throws ExecutionException If the DataManagerFacility can't be retrieved or instantiated.
     */
    @Override
    public DataManagerFacility getDm() throws ExecutionException {
        return gateway.getFacility(DataManagerFacility.class);
    }


    /**
     * Gets the {@link ROIFacility} used to manipulate ROIs from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    @Override
    public ROIFacility getRoiFacility() throws ExecutionException {
        return gateway.getFacility(ROIFacility.class);
    }


    /**
     * Gets the {@link TablesFacility} used to manipulate tables on OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the TablesFacility can't be retrieved or instantiated.
     */
    @Override
    public TablesFacility getTablesFacility() throws ExecutionException {
        return gateway.getFacility(TablesFacility.class);
    }


    /**
     * Gets the {@link AdminFacility} to use admin specific function.
     *
     * @return See above.
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
    @Override
    public OMEROMetadataStoreClient getImportStore() throws ServiceException {
        return ExceptionHandler.of(gateway, g -> g.getImportStore(ctx))
                               .rethrow(DSOutOfServiceException.class, ServiceException::new,
                                        "Could not retrieve import store")
                               .get();
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

