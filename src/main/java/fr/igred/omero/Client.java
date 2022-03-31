/*
 *  Copyright (C) 2020-2021 GReD
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


import fr.igred.omero.GenericObjectWrapper.SortById;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.FolderWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.PlateWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.omero.repository.ScreenWrapper;
import fr.igred.omero.repository.WellWrapper;
import ome.formats.OMEROMetadataStoreClient;
import omero.LockTimeout;
import omero.RLong;
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
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.WellData;
import omero.log.SimpleLogger;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.TagAnnotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleException;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrServer;


/**
 * Basic class, contains the gateway, the security context, and multiple facilities.
 * <p>
 * Allows the user to connect to OMERO and browse through all the data accessible to the user.
 */
public class Client {

    /** Gateway linking the code to OMERO, only linked to one group. */
    private final Gateway gateway;

    /** Security context of the user, contains the permissions of the user in this group. */
    private SecurityContext ctx;

    /** User */
    private ExperimenterWrapper user;


    /**
     * Constructor of the Client class. Initializes the gateway.
     */
    public Client() {
        this(new Gateway(new SimpleLogger()));
    }


    /**
     * Constructor of the Client class.
     *
     * @param gateway The Gateway.
     */
    protected Client(Gateway gateway) {
        this(gateway, null, null);
    }


    /**
     * Constructor of the Client class..
     */
    protected Client(Gateway gateway, SecurityContext ctx, ExperimenterWrapper user) {
        this.gateway = gateway;
        this.ctx = ctx;
        this.user = user;
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
     * Transforms a collection of ImageData in a list of ImageWrapper sorted by the ImageData id.
     *
     * @param images ImageData Collection.
     *
     * @return ImageWrapper list sorted.
     */
    private static List<ImageWrapper> toImageWrappers(Collection<? extends ImageData> images) {
        return images.stream()
                     .map(ImageWrapper::new)
                     .sorted(new SortById<>())
                     .collect(Collectors.toList());
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
            results = getGateway().getQueryService(getCtx()).findAllByQuery(query, null);
        } catch (DSOutOfServiceException | ServerError e) {
            handleServiceOrServer(e, "Query failed: " + query);
        }

        return results;
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
     * Returns the Gateway.
     *
     * @return The Gateway.
     */
    public Gateway getGateway() {
        return gateway;
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
     * Gets the project with the specified id from OMERO.
     *
     * @param id Id of the project.
     *
     * @return ProjectWrapper containing the project.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public ProjectWrapper getProject(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<ProjectWrapper> projects = getProjects(id);
        if (projects.isEmpty()) {
            throw new NoSuchElementException(String.format("Project %d doesn't exist in this context", id));
        }
        return projects.iterator().next();
    }


    /**
     * Gets the projects with the specified ids from OMERO.
     *
     * @param ids Project IDs
     *
     * @return List of ProjectWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ProjectWrapper> getProjects(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = new ArrayList<>(0);
        try {
            projects = getBrowseFacility().getProjects(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get projects");
        }
        return projects.stream()
                       .map(ProjectWrapper::new)
                       .sorted(new SortById<>())
                       .collect(Collectors.toList());
    }


    /**
     * Gets all projects available from OMERO.
     *
     * @return Collection of ProjectWrapper.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ProjectWrapper> getProjects() throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = new ArrayList<>(0);
        try {
            projects = getBrowseFacility().getProjects(getCtx());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get projects");
        }
        return projects.stream()
                       .map(ProjectWrapper::new)
                       .sorted(new SortById<>())
                       .collect(Collectors.toList());
    }


    /**
     * Gets all projects with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return Collection of ProjectWrapper.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ProjectWrapper> getProjects(String name) throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = new ArrayList<>(0);
        try {
            projects = getBrowseFacility().getProjects(getCtx(), name);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get projects with name: " + name);
        }
        return projects.stream()
                       .map(ProjectWrapper::new)
                       .sorted(new SortById<>())
                       .collect(Collectors.toList());
    }


    /**
     * Gets the dataset with the specified id from OMERO.
     *
     * @param id Id of the Dataset.
     *
     * @return ProjectWrapper containing the project.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public DatasetWrapper getDataset(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<DatasetWrapper> datasets = getDatasets(id);
        if (datasets.isEmpty()) {
            throw new NoSuchElementException(String.format("Dataset %d doesn't exist in this context", id));
        }
        return datasets.iterator().next();
    }


    /**
     * Gets the datasets with the specified ids from OMERO.
     *
     * @param ids Dataset IDs
     *
     * @return List of DatasetWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<DatasetWrapper> getDatasets(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<DatasetData> datasets = new ArrayList<>(0);
        try {
            datasets = getBrowseFacility().getDatasets(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get datasets");
        }
        return datasets.stream()
                       .map(DatasetWrapper::new)
                       .sorted(new SortById<>())
                       .collect(Collectors.toList());
    }


    /**
     * Gets all datasets available from OMERO.
     *
     * @return List of DatasetWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<DatasetWrapper> getDatasets()
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
        Long[] ids = this.findByQuery("select d from Dataset d")
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getDatasets(ids);
    }


    /**
     * Gets all datasets with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return Collection of DatasetWrapper.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<DatasetWrapper> getDatasets(String name) throws ServiceException, AccessException, ExecutionException {
        Collection<DatasetData> datasets = new ArrayList<>(0);
        try {
            datasets = getBrowseFacility().getDatasets(getCtx(), name);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get datasets with name: " + name);
        }
        return datasets.stream()
                       .map(DatasetWrapper::new)
                       .sorted(new SortById<>())
                       .collect(Collectors.toList());
    }


    /**
     * Returns an ImageWrapper that contains the image with the specified id from OMERO.
     *
     * @param id Id of the image.
     *
     * @return ImageWrapper containing the image.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public ImageWrapper getImage(Long id)
    throws ServiceException, AccessException, ExecutionException {
        ImageData image = null;
        try {
            image = getBrowseFacility().getImage(getCtx(), id);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get image with ID: " + id);
        }
        if (image == null) {
            throw new NoSuchElementException(String.format("Image %d doesn't exist in this context", id));
        }
        return new ImageWrapper(image);
    }


    /**
     * Gets the images with the specified ids from OMERO
     *
     * @param ids Image IDs
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImages(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = new ArrayList<>(0);
        try {
            images = getBrowseFacility().getImages(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get images");
        }

        return toImageWrappers(images);
    }


    /**
     * Gets all images owned by the current user.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImages() throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = new ArrayList<>(0);
        try {
            images = getBrowseFacility().getUserImages(getCtx());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get images");
        }
        return toImageWrappers(images);
    }


    /**
     * Gets all images with a certain from OMERO.
     *
     * @param name Name searched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImages(String name) throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = new ArrayList<>(0);
        try {
            images = getBrowseFacility().getImages(getCtx(), name);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get images with name: " + name);
        }
        images.removeIf(image -> !image.getName().equals(name));
        return toImageWrappers(images);
    }


    /**
     * Gets all images with a certain motif in their name from OMERO.
     *
     * @param motif Motif searched in an image name.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesLike(String motif) throws ServiceException, AccessException, ExecutionException {
        List<ImageWrapper> images = getImages();
        final String       regexp = ".*" + motif + ".*";
        images.removeIf(image -> !image.getName().matches(regexp));
        return images;
    }


    /**
     * Gets all images tagged with a specified tag from OMERO.
     *
     * @param tag TagAnnotationWrapper containing the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesTagged(TagAnnotationWrapper tag)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
        return tag.getImages(this);
    }


    /**
     * Gets all images tagged with a specified tag from OMERO.
     *
     * @param tagId Id of the tag researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesTagged(Long tagId)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
        return getImagesTagged(getTag(tagId));
    }


    /**
     * Gets all images with a certain key
     *
     * @param key Name of the key researched.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesKey(String key)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageWrapper> images   = getImages();
        List<ImageWrapper> selected = new ArrayList<>();
        for (ImageWrapper image : images) {
            Map<String, String> pairsKeyValue = image.getKeyValuePairs(this);
            if (pairsKeyValue.get(key) != null) {
                selected.add(image);
            }
        }

        return selected;
    }


    /**
     * Gets all images with a certain key value pair from OMERO
     *
     * @param key   Name of the key researched.
     * @param value Value associated with the key.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImagesPairKeyValue(String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageWrapper> images   = getImages();
        List<ImageWrapper> selected = new ArrayList<>();
        for (ImageWrapper image : images) {
            Map<String, String> pairsKeyValue = image.getKeyValuePairs(this);
            if (pairsKeyValue.get(key) != null && pairsKeyValue.get(key).equals(value)) {
                selected.add(image);
            }
        }

        return selected;
    }


    /**
     * Gets the screen with the specified id from OMERO.
     *
     * @param id Id of the screen.
     *
     * @return ScreenWrapper containing the screen.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public ScreenWrapper getScreen(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<ScreenWrapper> screens = getScreens(id);
        if (screens.isEmpty()) {
            throw new NoSuchElementException(String.format("Screen %d doesn't exist in this context", id));
        }
        return screens.iterator().next();
    }


    /**
     * Gets the screens with the specified ids from OMERO.
     *
     * @param ids A list of screen ids
     *
     * @return List of ScreenWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ScreenWrapper> getScreens(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = new ArrayList<>(0);
        try {
            screens = getBrowseFacility().getScreens(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get screens");
        }
        return screens.stream()
                      .map(ScreenWrapper::new)
                      .sorted(new SortById<>())
                      .collect(Collectors.toList());
    }


    /**
     * Gets all screens available from OMERO.
     *
     * @return List of ScreenWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ScreenWrapper> getScreens() throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = new ArrayList<>(0);
        try {
            screens = getBrowseFacility().getScreens(getCtx());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get screens");
        }

        return screens.stream()
                      .map(ScreenWrapper::new)
                      .sorted(new SortById<>())
                      .collect(Collectors.toList());
    }


    /**
     * Gets the plate with the specified id from OMERO.
     *
     * @param id Id of the plate.
     *
     * @return PlateWrapper containing the plate.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public PlateWrapper getPlate(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<PlateWrapper> plates = getPlates(id);
        if (plates.isEmpty()) {
            throw new NoSuchElementException(String.format("Plate %d doesn't exist in this context", id));
        }
        return plates.iterator().next();
    }


    /**
     * Gets the plates with the specified ids from OMERO.
     *
     * @param ids A list of plate ids
     *
     * @return List of PlateWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<PlateWrapper> getPlates(Long... ids) throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = new ArrayList<>(0);
        try {
            plates = getBrowseFacility().getPlates(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get plates");
        }
        return plates.stream()
                     .map(PlateWrapper::new)
                     .sorted(new SortById<>())
                     .collect(Collectors.toList());
    }


    /**
     * Gets all plates available from OMERO.
     *
     * @return List of PlateWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<PlateWrapper> getPlates() throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = new ArrayList<>(0);
        try {
            plates = getBrowseFacility().getPlates(getCtx());
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get plates");
        }
        return plates.stream()
                     .map(PlateWrapper::new)
                     .sorted(new SortById<>())
                     .collect(Collectors.toList());
    }


    /**
     * Gets the well with the specified id from OMERO.
     *
     * @param id Id of the well.
     *
     * @return WellWrapper containing the well.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError       Server error.
     */
    public WellWrapper getWell(Long id)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError {
        List<WellWrapper> wells = getWells(id);
        if (wells.isEmpty()) {
            throw new NoSuchElementException(String.format("Plate %d doesn't exist in this context", id));
        }
        return wells.iterator().next();
    }


    /**
     * Gets the wells with the specified ids from OMERO.
     *
     * @param ids A list of well ids
     *
     * @return List of WellWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<WellWrapper> getWells(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        Collection<WellData> wells = new ArrayList<>(0);
        try {
            wells = getBrowseFacility().getWells(getCtx(), Arrays.asList(ids));
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get wells");
        }
        return wells.stream()
                    .map(WellWrapper::new)
                    .sorted(new SortById<>())
                    .collect(Collectors.toList());
    }


    /**
     * Gets all wells available from OMERO.
     *
     * @return List of WellWrapper.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws OMEROServerError   Server error.
     */
    public List<WellWrapper> getWells()
    throws ServiceException, AccessException, ExecutionException, OMEROServerError {
        Long[] ids = this.findByQuery("select w from Well w")
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getWells(ids);
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
        context.setExperimenter(sudoUser.asExperimenterData());
        context.sudo();

        return new Client(this.getGateway(), context, sudoUser);
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
     * Gets the list of TagAnnotationWrapper available to the user
     *
     * @return list of TagAnnotationWrapper.
     *
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<TagAnnotationWrapper> getTags() throws OMEROServerError, ServiceException {
        List<IObject> os = new ArrayList<>(0);
        try {
            os = getGateway().getQueryService(getCtx()).findAll(TagAnnotation.class.getSimpleName(), null);
        } catch (DSOutOfServiceException | ServerError e) {
            handleServiceOrServer(e, "Cannot get tags");
        }
        return os.stream()
                 .map(TagAnnotation.class::cast)
                 .map(TagAnnotationData::new)
                 .map(TagAnnotationWrapper::new)
                 .sorted(new SortById<>())
                 .collect(Collectors.toList());
    }


    /**
     * Gets the list of TagAnnotationWrapper with the specified name available to the user
     *
     * @param name Name of the tag searched.
     *
     * @return list of TagAnnotationWrapper.
     *
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<TagAnnotationWrapper> getTags(String name) throws OMEROServerError, ServiceException {
        List<TagAnnotationWrapper> tags = getTags();
        tags.removeIf(tag -> !tag.getName().equals(name));
        tags.sort(new SortById<>());
        return tags;
    }


    /**
     * Gets a specific tag from the OMERO database
     *
     * @param id Id of the tag.
     *
     * @return TagAnnotationWrapper containing the specified tag.
     *
     * @throws OMEROServerError Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public TagAnnotationWrapper getTag(Long id) throws OMEROServerError, ServiceException {
        IObject o = null;
        try {
            o = getGateway().getQueryService(getCtx()).find(TagAnnotation.class.getSimpleName(), id);
        } catch (DSOutOfServiceException | ServerError e) {
            handleServiceOrServer(e, "Cannot get tag ID: " + id);
        }

        TagAnnotationData tag = new TagAnnotationData((TagAnnotation) Objects.requireNonNull(o));
        tag.setNameSpace(tag.getContentAsString());

        return new TagAnnotationWrapper(tag);
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
            result = getDm().saveAndReturnObject(getCtx(), object);
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
        try {
            getDm().delete(getCtx(), object).loop(10, 500);
        } catch (DSOutOfServiceException | DSAccessException | LockTimeout e) {
            handleException(e, "Cannot delete object");
        }
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
    public void delete(GenericObjectWrapper<?> object)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        if (object instanceof FolderWrapper) {
            ((FolderWrapper) object).unlinkAllROI(this);
        }
        delete(object.asIObject());
    }


    /**
     * Deletes a table from OMERO
     *
     * @param table TableWrapper containing the table to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException Id not defined.
     * @throws OMEROServerError         If the thread was interrupted.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void delete(TableWrapper table)
    throws ServiceException, AccessException, ExecutionException, OMEROServerError, InterruptedException {
        deleteFile(table.getId());
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
     * @throws NoSuchElementException The requested user does not exist.
     */
    public ExperimenterWrapper getUser(String username)
    throws ExecutionException, ServiceException, AccessException {
        ExperimenterData experimenter = null;
        try {
            experimenter = getAdminFacility().lookupExperimenter(getCtx(), username);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot retrieve user: " + username);
        }
        if (experimenter != null) {
            return new ExperimenterWrapper(experimenter);
        } else {
            throw new NoSuchElementException(String.format("User not found: %s", username));
        }
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
     * @throws NoSuchElementException The requested group does not exist.
     */
    public GroupWrapper getGroup(String groupName)
    throws ExecutionException, ServiceException, AccessException {
        GroupData group = null;
        try {
            group = getAdminFacility().lookupGroup(getCtx(), groupName);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot retrieve group: " + groupName);
        }
        if (group != null) {
            return new GroupWrapper(group);
        } else {
            throw new NoSuchElementException(String.format("Group not found: %s", groupName));
        }
    }


    /**
     * Check if the client is still connected to the server
     *
     * @return See above.
     */
    public boolean isConnected() {
        return gateway.isConnected() && ctx != null;
    }

}

