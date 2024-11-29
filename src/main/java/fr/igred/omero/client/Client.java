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


import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.containers.FolderWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import ome.formats.OMEROMetadataStoreClient;
import omero.ApiUsageException;
import omero.api.IQueryPrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.MetadataFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotationI;
import omero.model.IObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.call;


/**
 * Client interface to connect to OMERO, browse through all the data accessible to the user and modify it.
 */
public interface Client extends Browser {


    /**
     * Returns the Gateway.
     *
     * @return The Gateway.
     */
    Gateway getGateway();


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
                                                     String.valueOf(password),
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
                                     String.valueOf(password),
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
     * Gets the {@link BrowseFacility} used to access the data from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default BrowseFacility getBrowseFacility() throws ExecutionException {
        return getGateway().getFacility(BrowseFacility.class);
    }


    /**
     * Returns the {@link IQueryPrx} used to find objects on OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    @Override
    default IQueryPrx getQueryService()
    throws ServiceException, AccessException {
        return call(getGateway(),
                    g -> g.getQueryService(getCtx()),
                    "Could not retrieve Query Service");
    }


    /**
     * Gets the {@link MetadataFacility} used to retrieve annotations from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the MetadataFacility can't be retrieved or instantiated.
     */
    @Override
    default MetadataFacility getMetadataFacility() throws ExecutionException {
        return getGateway().getFacility(MetadataFacility.class);
    }


    /**
     * Gets the {@link DataManagerFacility} to handle/write data on OMERO. A
     *
     * @return See above.
     *
     * @throws ExecutionException If the DataManagerFacility can't be retrieved or instantiated.
     */
    default DataManagerFacility getDm() throws ExecutionException {
        return getGateway().getFacility(DataManagerFacility.class);
    }


    /**
     * Gets the {@link ROIFacility} used to manipulate ROIs from OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    default ROIFacility getRoiFacility() throws ExecutionException {
        return getGateway().getFacility(ROIFacility.class);
    }


    /**
     * Gets the {@link TablesFacility} used to manipulate tables on OMERO.
     *
     * @return See above.
     *
     * @throws ExecutionException If the TablesFacility can't be retrieved or instantiated.
     */
    default TablesFacility getTablesFacility() throws ExecutionException {
        return getGateway().getFacility(TablesFacility.class);
    }


    /**
     * Gets the {@link AdminFacility} to use admin specific function.
     *
     * @return See above.
     *
     * @throws ExecutionException If the AdminFacility can't be retrieved or instantiated.
     */
    default AdminFacility getAdminFacility() throws ExecutionException {
        return getGateway().getFacility(AdminFacility.class);
    }


    /**
     * Deletes multiple objects from OMERO.
     *
     * @param objects The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(Collection<? extends ObjectWrapper<?>> objects)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        for (ObjectWrapper<?> object : objects) {
            if (object instanceof FolderWrapper) {
                ((FolderWrapper) object).unlinkAllROIs(this);
            }
        }
        if (!objects.isEmpty()) {
            delete(objects.stream()
                          .map(o -> o.asDataObject().asIObject())
                          .collect(Collectors.toList()));
        }
    }


    /**
     * Deletes an object from OMERO.
     * <p> Make sure a folder is loaded before deleting it.
     *
     * @param object The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(ObjectWrapper<?> object)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        if (object instanceof FolderWrapper) {
            ((FolderWrapper) object).unlinkAllROIs(this);
        }
        delete(object.asDataObject().asIObject());
    }


    /**
     * Deletes a table from OMERO.
     *
     * @param table Table to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException ID not defined.
     * @throws InterruptedException     If block(long) does not return.
     */
    default void deleteTable(TableWrapper table)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        deleteFile(table.getId());
    }


    /**
     * Deletes tables from OMERO.
     *
     * @param tables List of tables to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException ID not defined.
     * @throws InterruptedException     If block(long) does not return.
     */
    default void deleteTables(Collection<? extends TableWrapper> tables)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        deleteFiles(tables.stream()
                          .map(TableWrapper::getId)
                          .toArray(Long[]::new));
    }


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
    default ExperimenterWrapper getUser(String username)
    throws ExecutionException, ServiceException, AccessException {
        ExperimenterData user = call(getAdminFacility(),
                                     a -> a.lookupExperimenter(getCtx(),
                                                               username),
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
    default ExperimenterWrapper getUser(long userId)
    throws ServiceException, AccessException {
        Experimenter user = ExceptionHandler.of(getGateway(),
                                                g -> g.getAdminService(getCtx())
                                                      .getExperimenter(userId))
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
    default GroupWrapper getGroup(String groupName)
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
    default GroupWrapper getGroup(long groupId)
    throws ServiceException, AccessException {
        ExperimenterGroup group = ExceptionHandler.of(getGateway(),
                                                      g -> g.getAdminService(getCtx())
                                                            .getGroup(groupId))
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
    default List<GroupWrapper> getGroups()
    throws ServiceException, AccessException {
        String error = "Cannot retrieve the groups on OMERO";
        List<ExperimenterGroup> groups = call(getGateway(),
                                              g -> g.getAdminService(getCtx())
                                                    .lookupGroups(),
                                              error);
        return groups.stream()
                     .filter(Objects::nonNull)
                     .map(GroupData::new)
                     .map(GroupWrapper::new)
                     .collect(Collectors.toList());
    }


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
    default IObject save(IObject object)
    throws ServiceException, AccessException, ExecutionException {
        return call(getDm(),
                    d -> d.saveAndReturnObject(getCtx(), object),
                    "Cannot save object");
    }


    /**
     * Deletes an object from OMERO.
     *
     * @param object The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(IObject object)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        final long wait = 500L;
        ExceptionHandler.ofConsumer(getDm(),
                                    d -> d.delete(getCtx(), object).loop(10, wait))
                        .rethrow(InterruptedException.class)
                        .handleOMEROException("Cannot delete object")
                        .rethrow();
    }


    /**
     * Deletes multiple objects from OMERO.
     *
     * @param objects The OMERO objects.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void delete(List<IObject> objects)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        final long wait = 500L;
        ExceptionHandler.ofConsumer(getDm(),
                                    d -> d.delete(getCtx(), objects).loop(10, wait))
                        .rethrow(InterruptedException.class)
                        .handleOMEROException("Cannot delete objects")
                        .rethrow();
    }


    /**
     * Deletes a file from OMERO
     *
     * @param id ID of the file to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void deleteFile(Long id)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        deleteFiles(id);
    }


    /**
     * Deletes files from OMERO.
     *
     * @param ids List of files IDs to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    default void deleteFiles(Long... ids)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        List<IObject> files = Arrays.stream(ids)
                                    .map(id -> new FileAnnotationI(id, false))
                                    .collect(Collectors.toList());
        delete(files);
    }


    /**
     * Returns a Client associated with the provided username.
     * <p>The user calling this function needs to have administrator rights.
     * <p>All actions realized with the returned Client will be considered as his.
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
    Client sudo(String username)
    throws ServiceException, AccessException, ExecutionException;

}
