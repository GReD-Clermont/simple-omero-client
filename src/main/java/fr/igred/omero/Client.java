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

package fr.igred.omero;


import fr.igred.omero.annotations.GenericAnnotationWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import fr.igred.omero.repository.FolderWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.GenericObjectWrapper.flatten;


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
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ImageWrapper> getImages(GenericAnnotationWrapper<?> annotation)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
        return annotation.getImages(this);
    }


    /**
     * @param key Name of the key researched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Gets all images with a certain key
     */
    @Deprecated
    @Override
    public List<ImageWrapper> getImagesKey(String key)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageWrapper> images   = getImages();
        List<ImageWrapper> selected = new ArrayList<>(images.size());
        for (ImageWrapper image : images) {
            Map<String, String> pairsKeyValue = image.getKeyValuePairs(this);
            if (pairsKeyValue.get(key) != null) {
                selected.add(image);
            }
        }

        return selected;
    }


    /**
     * @param key   Name of the key researched.
     * @param value Value associated with the key.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @deprecated Gets all images with a certain key value pair from OMERO
     */
    @Deprecated
    @Override
    public List<ImageWrapper> getImagesPairKeyValue(String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageWrapper> images   = getImages();
        List<ImageWrapper> selected = new ArrayList<>(images.size());
        for (ImageWrapper image : images) {
            Map<String, String> pairsKeyValue = image.getKeyValuePairs(this);
            if (pairsKeyValue.get(key) != null && pairsKeyValue.get(key).equals(value)) {
                selected.add(image);
            }
        }
        return selected;
    }


    /**
     * Deletes multiple objects from OMERO.
     *
     * @param objects The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError     Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    public void delete(Collection<? extends GenericObjectWrapper<?>> objects)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        for (GenericObjectWrapper<?> object : objects) {
            if (object instanceof FolderWrapper) {
                ((FolderWrapper) object).unlinkAllROIs(this);
            }
        }
        if (!objects.isEmpty()) {
            delete(objects.stream().map(GenericObjectWrapper::asIObject).collect(Collectors.toList()));
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
     * @throws OMEROServerError     Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    public void delete(GenericObjectWrapper<?> object)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        if (object instanceof FolderWrapper) {
            ((FolderWrapper) object).unlinkAllROIs(this);
        }
        delete(object.asIObject());
    }


    /**
     * @param table Table to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException ID not defined.
     * @throws OMEROServerError         Server error.
     * @throws InterruptedException     If block(long) does not return.
     * @deprecated Deletes a table from OMERO.
     */
    @Deprecated
    public void delete(TableWrapper table)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        deleteTable(table);
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
     * @throws OMEROServerError         Server error.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void deleteTable(TableWrapper table)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
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
     * @throws OMEROServerError         Server error.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void deleteTables(Collection<? extends TableWrapper> tables)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
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
                                                        .handleServiceOrAccess("Cannot retrieve user: " + username)
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
     * @throws OMEROServerError       Server error.
     * @throws NoSuchElementException The requested user cannot be found.
     */
    public ExperimenterWrapper getUser(long userId)
    throws ServiceException, OMEROServerError {
        Experimenter user = ExceptionHandler.of(getGateway(), g -> g.getAdminService(getCtx()).getExperimenter(userId))
                                            .rethrow(ApiUsageException.class,
                                                     (m, e) -> new NoSuchElementException(m),
                                                     "User not found: " + userId)
                                            .handleServiceOrServer("Cannot retrieve user: " + userId)
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
                                          .handleServiceOrAccess("Cannot retrieve group: " + groupName)
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
     * @throws OMEROServerError       Server error.
     * @throws NoSuchElementException The requested group cannot be found.
     */
    public GroupWrapper getGroup(long groupId)
    throws ServiceException, OMEROServerError {
        ExperimenterGroup group = ExceptionHandler.of(getGateway(), g -> g.getAdminService(getCtx()).getGroup(groupId))
                                                  .rethrow(ApiUsageException.class,
                                                           (m, e) -> new NoSuchElementException(m),
                                                           "User not found: " + groupId)
                                                  .handleServiceOrServer("Cannot retrieve group: " + groupId)
                                                  .get();
        return new GroupWrapper(new GroupData(group));
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
    public Client sudoGetUser(String username) throws ServiceException, AccessException, ExecutionException {
        ExperimenterWrapper sudoUser = getUser(username);

        SecurityContext context = new SecurityContext(sudoUser.getDefaultGroup().getId());
        context.setExperimenter(sudoUser.asDataObject());
        context.sudo();

        return new Client(this.getGateway(), context, sudoUser);
    }

}
