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
import omero.gateway.SecurityContext;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;


/**
 * Basic class, contains the gateway, the security context, and multiple facilities.
 * <p>
 * Allows the user to connect to OMERO and browse through all the data accessible to the user.
 */
public class Client extends GatewayWrapper {


    /**
     * Constructor of the Client class. Initializes the gateway.
     */
    public Client() {
        this(null, null, null);
    }


    /**
     * Constructor of the Client class.
     *
     * @param gateway The gateway
     * @param ctx     The security context
     * @param user    The user
     */
    public Client(Gateway gateway, SecurityContext ctx, ExperimenterWrapper user) {
        super(gateway, ctx, user);
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

        return new Client(this.getGateway(), context, sudoUser);
    }

}
