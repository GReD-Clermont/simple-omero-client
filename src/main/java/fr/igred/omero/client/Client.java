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
import fr.igred.omero.annotations.AnnotationWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import fr.igred.omero.containers.FolderWrapper;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.containers.ProjectWrapper;
import omero.ApiUsageException;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.ObjectWrapper.flatten;


/**
 * Basic class, contains the gateway, the security context, and multiple facilities.
 * <p>
 * Allows the user to connect to OMERO and browse through all the data accessible to the user.
 */
public class Client extends Browser {


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
     * Gets all images with the name specified inside projects and datasets with the given names.
     *
     * @param projectName Expected project name.
     * @param datasetName Expected dataset name.
     * @param imageName   Expected image name.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ImageWrapper> getImages(String projectName, String datasetName, String imageName)
    throws ServiceException, AccessException, ExecutionException {
        List<ProjectWrapper> projects = getProjects(projectName);

        Collection<List<ImageWrapper>> lists = new ArrayList<>(projects.size());
        for (ProjectWrapper project : projects) {
            lists.add(project.getImages(this, datasetName, imageName));
        }

        return flatten(lists);
    }


    /**
     * Gets all images with the specified annotation from OMERO.
     *
     * @param annotation TagAnnotation containing the tag researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ImageWrapper> getImages(AnnotationWrapper<?> annotation)
    throws ServiceException, AccessException, ExecutionException {
        return annotation.getImages(this);
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
    public void delete(Collection<? extends ObjectWrapper<?>> objects)
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
    public void delete(ObjectWrapper<?> object)
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
    public void deleteTable(TableWrapper table)
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
    public void deleteTables(Collection<? extends TableWrapper> tables)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        deleteFiles(tables.stream().map(TableWrapper::getId).toArray(Long[]::new));
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
    public ExperimenterWrapper getUser(String username)
    throws ExecutionException, ServiceException, AccessException {
        ExperimenterData experimenter = ExceptionHandler.of(getAdminFacility(),
                                                            a -> a.lookupExperimenter(getCtx(), username))
                                                        .handleOMEROException("Cannot retrieve user: " + username)
                                                        .get();
        if (experimenter != null) {
            return new ExperimenterWrapper(experimenter);
        } else {
            throw new NoSuchElementException(String.format("User not found: %s", username));
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
    public ExperimenterWrapper getUser(long userId)
    throws ServiceException, AccessException {
        Experimenter user = ExceptionHandler.of(getGateway(), g -> g.getAdminService(getCtx()).getExperimenter(userId))
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
    public GroupWrapper getGroup(String groupName)
    throws ExecutionException, ServiceException, AccessException {
        GroupData group = ExceptionHandler.of(getAdminFacility(), a -> a.lookupGroup(getCtx(), groupName))
                                          .handleOMEROException("Cannot retrieve group: " + groupName)
                                          .get();
        if (group != null) {
            return new GroupWrapper(group);
        } else {
            throw new NoSuchElementException(String.format("Group not found: %s", groupName));
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
    public GroupWrapper getGroup(long groupId)
    throws ServiceException, AccessException {
        ExperimenterGroup group = ExceptionHandler.of(getGateway(), g -> g.getAdminService(getCtx()).getGroup(groupId))
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
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     */
    public List<GroupWrapper> getGroups()
    throws ServiceException, AccessException {
        String error = "Cannot retrieve the groups on OMERO";
        return ExceptionHandler.of(getGateway(),
                                   g -> g.getAdminService(getCtx()).lookupGroups())
                               .handleOMEROException(error)
                               .get()
                               .stream()
                               .filter(Objects::nonNull)
                               .map(GroupData::new)
                               .map(GroupWrapper::new)
                               .collect(Collectors.toList());
    }


    /**
     * Gets the client associated with the username in the parameters. The user calling this function needs to have
     * administrator rights. All action realized with the client returned will be considered as his.
     *
     * @param username Username of user.
     *
     * @return The client corresponding to the new user.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     * @throws NoSuchElementException The requested user does not exist.
     */
    public Client sudo(String username)
    throws ServiceException, AccessException, ExecutionException {
        ExperimenterWrapper sudoUser = getUser(username);

        SecurityContext context = new SecurityContext(sudoUser.getDefaultGroup().getId());
        context.setExperimenter(sudoUser.asDataObject());
        context.sudo();

        return new Client(this.getGateway(), context, sudoUser);
    }

}
