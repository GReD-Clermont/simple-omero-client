/*
 *  Copyright (C) 2020 GReD
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
import fr.igred.omero.exception.ServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.metadata.ROIContainer;
import fr.igred.omero.metadata.TableContainer;
import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ProjectContainer;
import fr.igred.omero.sort.SortImageContainer;
import fr.igred.omero.sort.SortTagAnnotationContainer;
import ome.formats.importer.ImportConfig;
import omero.LockTimeout;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.MetadataFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.facility.RawDataFacility;
import omero.gateway.facility.TablesFacility;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.ImageData;
import omero.log.SimpleLogger;
import omero.model.DatasetI;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.ImageI;
import omero.model.NamedValue;
import omero.model.ProjectI;
import omero.model.RoiI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;


/**
 * Basic class, contain the gateway, the security context, and multiple facility.
 * <p>
 * Allows the connexion of user to connect to OMERO and browse through all the data accessible to the user.
 */
public class Client {

    /** User */
    private ExperimenterData user;
    /** Gateway linking the code to OMERO, only linked to one group. */
    private Gateway          gateway;
    /** Security context of the user, contains the permissions of the user in this group. */
    private SecurityContext  ctx;
    private BrowseFacility   browse;
    private ImportConfig     config;


    /**
     * Constructor of the Client class. Initializes the gateway.
     */
    public Client() {
        gateway = new Gateway(new SimpleLogger());
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
     * Gets the BrowseFacility used to access the data from OMERO.
     *
     * @return the {@link BrowseFacility} linked to the gateway.
     */
    public BrowseFacility getBrowseFacility() {
        return browse;
    }


    /**
     * Gets the DataManagerFacility to handle/write data on OMERO.
     *
     * @return the {@link DataManagerFacility} linked to the gateway.
     *
     * @throws ExecutionException If the DataManagerFacility can't be retrieved or instantiated.
     */
    public DataManagerFacility getDm() throws ExecutionException {
        return gateway.getFacility(DataManagerFacility.class);
    }


    /**
     * Gets the MetadataFacility used to manipulate metadata from OMERO.
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
     * Gets the RawDataFacility linked to the gateway to access the raw image data.
     *
     * @return the {@link ExecutionException} linked to the gateway.
     *
     * @throws ExecutionException If the ExecutionException can't be retrieved or instantiated.
     */
    public RawDataFacility getRdf() throws ExecutionException {
        return gateway.getFacility(RawDataFacility.class);
    }


    /**
     * Finds objects on OMERO through a database query.
     *
     * @param query The database query.
     *
     * @return A list of OMERO objects.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerError      Server error.
     */
    public List<IObject> findByQuery(String query) throws ServiceException, ServerError {
        List<IObject> results;
        try {
            results = gateway.getQueryService(ctx).findAllByQuery(query, null);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (omero.ServerError se) {
            throw new ServerError("Server error", se);
        }

        return results;
    }


    /**
     * Gets the importation config for the user.
     *
     * @return config.
     */
    public ImportConfig getConfig() {
        return config;
    }


    /**
     * Gets the user id.
     *
     * @return id.
     */
    public Long getId() {
        return user.getId();
    }


    public Long getGroupId() {
        return user.getGroupId();
    }


    public Gateway getGateway() {
        return gateway;
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
     * @param groupID  Id of the group to connect.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void connect(String hostname, int port, String username, String password, Long groupID)
    throws ServiceException, ExecutionException {
        LoginCredentials cred = createCred(hostname, port, username, password);

        cred.setGroupID(groupID);

        createConfig(hostname, port, username, password);

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
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void connect(String hostname, int port, String username, String password)
    throws ServiceException, ExecutionException {
        LoginCredentials cred = createCred(hostname, port, username, password);

        createConfig(hostname, port, username, password);

        connect(cred);
    }


    /**
     * Creates the credential used to log the user to OMERO.
     *
     * @param hostname Name of the host.
     * @param port     Port used by OMERO.
     * @param username Username of the user.
     * @param password Password of the user.
     */
    private LoginCredentials createCred(String hostname, int port, String username, String password) {
        LoginCredentials cred = new LoginCredentials();

        cred.getServer().setHost(hostname);
        cred.getServer().setPort(port);
        cred.getUser().setUsername(username);
        cred.getUser().setPassword(password);

        return cred;
    }


    /**
     * Creates the importation config linked to the user.
     *
     * @param hostname Name of the host.
     * @param port     Port used by OMERO.
     * @param username Username of the user.
     * @param password Password of the user.
     */
    private void createConfig(String hostname, int port, String username, String password) {
        config = new ome.formats.importer.ImportConfig();

        config.email.set("");
        config.sendFiles.set(true);
        config.sendReport.set(false);
        config.contOnError.set(false);
        config.debug.set(false);

        config.hostname.set(hostname);
        config.port.set(port);
        config.username.set(username);
        config.password.set(password);
    }


    /**
     * Connects the user to OMERO. Gets the SecurityContext and the BrowseFacility.
     *
     * @param cred User credential.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void connect(LoginCredentials cred) throws ServiceException, ExecutionException {
        try {
            this.user = gateway.connect(cred);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        }

        this.ctx = new SecurityContext(user.getGroupId());
        this.browse = gateway.getFacility(BrowseFacility.class);
    }


    /**
     * Disconnects the user
     */
    public void disconnect() {
        gateway.disconnect();
    }


    /**
     * Gets the project with the specified id from OMERO.
     *
     * @param id Id of the project.
     *
     * @return ProjectContainer containing the project.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     */
    public ProjectContainer getProject(Long id)
    throws ServiceException, AccessException, NoSuchElementException {
        Collection<ProjectData> projects;
        try {
            projects = browse.getProjects(ctx);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        for (ProjectData project : projects) {
            if (project.getId() == id) {
                return new ProjectContainer(project);
            }
        }
        throw new NoSuchElementException("Project " + id + " doesn't exist in this context");
    }


    /**
     * Gets all projects available from OMERO.
     *
     * @return Collection of ProjectContainer.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public Collection<ProjectContainer> getProjects() throws ServiceException, AccessException {
        Collection<ProjectContainer> projectsContainer = new ArrayList<>();
        Collection<ProjectData>      projects;
        try {
            projects = browse.getProjects(ctx);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        for (ProjectData project : projects) {
            projectsContainer.add(new ProjectContainer(project));
        }
        return projectsContainer;
    }


    /**
     * Gets all projects with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return Collection of ProjectContainer.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public Collection<ProjectContainer> getProjects(String name) throws ServiceException, AccessException {
        Collection<ProjectContainer> projectsContainer = new ArrayList<>();
        Collection<ProjectData>      projects;
        try {
            projects = browse.getProjects(ctx, name);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        for (ProjectData project : projects) {
            projectsContainer.add(new ProjectContainer(project));
        }

        return projectsContainer;
    }


    /**
     * Gets the dataset with the specified id from OMERO.
     *
     * @param id Id of the Dataset.
     *
     * @return ProjectContainer containing the project.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     */
    public DatasetContainer getDataset(Long id)
    throws ServiceException, AccessException, NoSuchElementException {
        Collection<DatasetData> datasets;
        try {
            datasets = browse.getDatasets(ctx);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        for (DatasetData dataset : datasets) {
            if (dataset.getId() == id) {
                return new DatasetContainer(dataset);
            }
        }
        throw new NoSuchElementException("Dataset " + id + " doesn't exist in this context");
    }


    /**
     * Gets all the datasets available from OMERO.
     *
     * @return Collection of DatasetContainer.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public Collection<DatasetContainer> getDatasets() throws ServiceException, AccessException {
        Collection<DatasetContainer> datasetContainer = new ArrayList<>();
        Collection<DatasetData>      datasets;
        try {
            datasets = browse.getDatasets(ctx);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        for (DatasetData dataset : datasets) {
            datasetContainer.add(new DatasetContainer(dataset));
        }
        return datasetContainer;
    }


    /**
     * Gets all datasets with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return Collection of DatasetContainer.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public Collection<DatasetContainer> getDatasets(String name) throws ServiceException, AccessException {
        Collection<DatasetContainer> datasetContainer = new ArrayList<>();
        Collection<DatasetData>      datasets;
        try {
            datasets = browse.getDatasets(ctx, name);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        for (DatasetData dataset : datasets) {
            datasetContainer.add(new DatasetContainer(dataset));
        }

        return datasetContainer;
    }


    /**
     * Transforms a collection of ImageData in a list of ImageContainer sorted by the ImageData id.
     *
     * @param images ImageData Collection.
     *
     * @return ImageContainer list sorted.
     */
    private List<ImageContainer> toImagesContainer(Collection<ImageData> images) {
        List<ImageContainer> imagesContainer = new ArrayList<>();

        for (ImageData image : images) {
            imagesContainer.add(new ImageContainer(image));
        }

        imagesContainer.sort(new SortImageContainer());

        return imagesContainer;
    }


    /**
     * Returns an ImageContainer that contains the image with the specified id from OMERO.
     *
     * @param imageId Id of the image.
     *
     * @return ImageContainer containing the image.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     */
    public ImageContainer getImage(Long imageId)
    throws ServiceException, AccessException, NoSuchElementException {
        try {
            return new ImageContainer(browse.getImage(ctx, imageId));
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }
    }


    /**
     * Gets all images available from OMERO.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageContainer> getImages() throws ServiceException, AccessException {
        Collection<ImageData> images;
        try {
            images = browse.getUserImages(ctx);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        return toImagesContainer(images);
    }


    /**
     * Gets all images with a certain from OMERO.
     *
     * @param name Name searched.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageContainer> getImages(String name) throws ServiceException, AccessException {
        List<ImageContainer>  selected = new ArrayList<>();
        Collection<ImageData> images;
        try {
            images = browse.getImages(ctx, name);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        for (ImageData image : images) {
            if (image.getName().equals(name)) {
                selected.add(new ImageContainer(image));
            }
        }

        return selected;
    }


    /**
     * Gets all images with a certain motif in their name from OMERO.
     *
     * @param motif Motif searched in an image name.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<ImageContainer> getImagesLike(String motif) throws ServiceException, AccessException {
        List<ImageContainer> images = getImages();
        final String         regexp = ".*" + motif + ".*";
        images.removeIf(image -> !image.getName().matches(regexp));
        return images;
    }


    /**
     * Gets all images tagged with a specified tag from OMERO.
     *
     * @param tag TagAnnotationContainer containing the tag researched.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws ServerError      Server error.
     */
    public List<ImageContainer> getImagesTagged(TagAnnotationContainer tag)
    throws ServiceException, AccessException, ServerError {
        List<ImageContainer> selected = new ArrayList<>();
        List<IObject> os = findByQuery("select link.parent " +
                                       "from ImageAnnotationLink link " +
                                       "where link.child = " +
                                       tag.getId());

        for (IObject o : os) {
            selected.add(getImage(o.getId().getValue()));
        }

        return selected;
    }


    /**
     * Gets all images tagged with a specified tag from OMERO.
     *
     * @param tagId Id of the tag researched.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws ServerError      Server error.
     */
    public List<ImageContainer> getImagesTagged(Long tagId)
    throws ServiceException, AccessException, ServerError {
        List<ImageContainer> selected = new ArrayList<>();
        List<IObject> os = findByQuery("select link.parent " +
                                       "from ImageAnnotationLink link " +
                                       "where link.child = " +
                                       tagId);

        for (IObject o : os) {
            selected.add(getImage(o.getId().getValue()));
        }

        return selected;
    }


    /**
     * Gets all images with a certain key
     *
     * @param key Name of the key researched.
     *
     * @return ImageContainer list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageContainer> getImagesKey(String key)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageContainer> selected = new ArrayList<>();
        List<ImageContainer> images   = getImages();

        for (ImageContainer image : images) {
            Collection<NamedValue> pairsKeyValue = image.getKeyValuePairs(this);
            for (NamedValue pairKeyValue : pairsKeyValue) {
                if (pairKeyValue.name.equals(key)) {
                    selected.add(image);
                    break;
                }
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
     * @return ImageContainer list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageContainer> getImagesPairKeyValue(String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<ImageContainer> selected = new ArrayList<>();
        List<ImageContainer> images   = getImages();
        for (ImageContainer image : images) {
            Collection<NamedValue> pairsKeyValue = image.getKeyValuePairs(this);
            for (NamedValue pairKeyValue : pairsKeyValue) {
                if (pairKeyValue.name.equals(key) && pairKeyValue.value.equals(value)) {
                    selected.add(image);
                    break;
                }
            }
        }

        return selected;
    }


    /**
     * Gets the client associated with the username in the parameters. The user calling this function needs to have
     * administrator rights. All action realized with the client returned will be considered as his.
     *
     * @param username Username of user.
     *
     * @return The client corresponding to the new user.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public Client sudoGetUser(String username) throws ServiceException, AccessException, ExecutionException {
        Client c = new Client();

        ExperimenterData sudoUser;
        try {
            sudoUser = getAdminFacility().lookupExperimenter(ctx, username);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        }

        SecurityContext sudoCtx = new SecurityContext(sudoUser.getGroupId());
        sudoCtx.setExperimenter(sudoUser);
        sudoCtx.sudo();

        c.gateway = this.gateway;
        c.ctx = sudoCtx;
        c.user = sudoUser;
        c.browse = this.browse;

        return c;
    }


    /**
     * Gets the list of TagAnnotationContainer available to the user
     *
     * @return list of TagAnnotationContainer.
     *
     * @throws ServerError      Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<TagAnnotationContainer> getTags() throws ServerError, ServiceException {
        List<TagAnnotationContainer> tags = new ArrayList<>();

        List<IObject> os;
        try {
            os = gateway.getQueryService(ctx).findAll(TagAnnotation.class.getSimpleName(), null);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (omero.ServerError se) {
            throw new ServerError("Server error", se);
        }

        for (IObject o : os) {
            TagAnnotationData tag = new TagAnnotationData((TagAnnotation) o);
            tags.add(new TagAnnotationContainer(tag));
        }

        tags.sort(new SortTagAnnotationContainer());
        return tags;
    }


    /**
     * Gets the list of TagAnnotationContainer with the specified name available to the user
     *
     * @param name Name of the tag searched.
     *
     * @return list of TagAnnotationContainer.
     *
     * @throws ServerError      Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<TagAnnotationContainer> getTags(String name) throws ServerError, ServiceException {
        List<TagAnnotationContainer> tags = getTags();
        tags.removeIf(tag -> !tag.getName().equals(name));
        tags.sort(new SortTagAnnotationContainer());
        return tags;
    }


    /**
     * Gets a specific tag from the OMERO database
     *
     * @param id Id of the tag.
     *
     * @return TagAnnotationContainer containing the specified tag.
     *
     * @throws ServerError      Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public TagAnnotationContainer getTag(Long id) throws ServerError, ServiceException {
        IObject o;
        try {
            o = gateway.getQueryService(ctx).find(TagAnnotation.class.getSimpleName(), id);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (omero.ServerError se) {
            throw new ServerError("Server error", se);
        }

        TagAnnotationData tag = new TagAnnotationData((TagAnnotation) o);
        tag.setNameSpace(tag.getContentAsString());

        return new TagAnnotationContainer(tag);
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
        IObject result;
        try {
            result = getDm().saveAndReturnObject(ctx, object);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
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
     * @throws ServerError          If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    void delete(IObject object)
    throws ServiceException, AccessException, ExecutionException, ServerError, InterruptedException {
        try {
            getDm().delete(ctx, object).loop(10, 500);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (DSAccessException ae) {
            throw new AccessException("Cannot access data", ae);
        } catch (LockTimeout lt) {
            throw new ServerError("Thread was interrupted", lt);
        }
    }


    /**
     * Deletes an image from OMERO
     *
     * @param image ImageContainer containing the image to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException Id not defined.
     * @throws ServerError              If the thread was interrupted.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void deleteImage(ImageContainer image) throws
                                                  ServiceException,
                                                  AccessException,
                                                  ExecutionException,
                                                  IllegalArgumentException,
                                                  ServerError,
                                                  InterruptedException {
        deleteImage(image.getId());
    }


    /**
     * Deletes an image from OMERO
     *
     * @param id Id of the image to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerError          If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    public void deleteImage(Long id)
    throws ServiceException, AccessException, ExecutionException, ServerError, InterruptedException {
        ImageI image = new ImageI(id, false);
        delete(image);
    }


    /**
     * Deletes a project from OMERO
     *
     * @param project ProjectContainer containing the project to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException Id not defined.
     * @throws ServerError              If the thread was interrupted.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void deleteProject(ProjectContainer project) throws
                                                        ServiceException,
                                                        AccessException,
                                                        ExecutionException,
                                                        IllegalArgumentException,
                                                        ServerError,
                                                        InterruptedException {
        if (project.getId() != null)
            deleteProject(project.getId());
        else
            throw new IllegalArgumentException("Project id is null");
    }


    /**
     * Deletes a project from OMERO
     *
     * @param id Id of the project to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerError          If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    public void deleteProject(Long id)
    throws ServiceException, AccessException, ExecutionException, ServerError, InterruptedException {
        ProjectI project = new ProjectI(id, false);
        delete(project);
    }


    /**
     * Deletes a dataset from OMERO
     *
     * @param dataset DatasetContainer containing the dataset to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException Id not defined.
     * @throws ServerError              If the thread was interrupted.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void deleteDataset(DatasetContainer dataset) throws
                                                        DSOutOfServiceException,
                                                        DSAccessException,
                                                        ExecutionException,
                                                        IllegalArgumentException,
                                                        ServerError,
                                                        InterruptedException {
        if (dataset.getId() != null)
            deleteDataset(dataset.getId());
        else
            throw new IllegalArgumentException("Dataset id is null");
    }


    /**
     * Deletes a dataset from OMERO
     *
     * @param id Id of the dataset to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerError          If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    public void deleteDataset(Long id)
    throws ServiceException, AccessException, ExecutionException, ServerError, InterruptedException {
        DatasetI dataset = new DatasetI(id, false);
        delete(dataset);
    }


    /**
     * Deletes a tag from OMERO
     *
     * @param tag TagContainer containing the tag to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException Id not defined.
     * @throws ServerError              If the thread was interrupted.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void deleteTag(TagAnnotationContainer tag) throws
                                                      ServiceException,
                                                      AccessException,
                                                      ExecutionException,
                                                      IllegalArgumentException,
                                                      ServerError,
                                                      InterruptedException {
        if (tag.getId() != null)
            deleteTag(tag.getId());
        else
            throw new IllegalArgumentException("Tag id is null");
    }


    /**
     * Deletes a tag from OMERO
     *
     * @param id Id of the tag to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerError          If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    public void deleteTag(Long id)
    throws ServiceException, AccessException, ExecutionException, ServerError, InterruptedException {
        TagAnnotationI tag = new TagAnnotationI(id, false);
        delete(tag);
    }


    /**
     * Deletes a ROI from OMERO
     *
     * @param roi ROIContainer containing the ROI to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException Id not defined.
     * @throws ServerError              If the thread was interrupted.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void deleteROI(ROIContainer roi) throws
                                            ServiceException,
                                            AccessException,
                                            ExecutionException,
                                            IllegalArgumentException,
                                            ServerError,
                                            InterruptedException {
        if (roi.getId() != null)
            deleteROI(roi.getId());
        else
            throw new IllegalArgumentException("ROI id is null");
    }


    /**
     * Deletes a ROI from OMERO
     *
     * @param id Id of the ROI to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerError          If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    public void deleteROI(Long id)
    throws ServiceException, AccessException, ExecutionException, ServerError, InterruptedException {
        RoiI roi = new RoiI(id, false);
        delete(roi);
    }


    /**
     * Deletes a table from OMERO
     *
     * @param table TableContainer containing the table to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException Id not defined.
     * @throws ServerError              If the thread was interrupted.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void deleteTable(TableContainer table)
    throws ServiceException, AccessException, ExecutionException, ServerError, InterruptedException {
        deleteTag(table.getId());
    }


    /**
     * Deletes a file from OMERO
     *
     * @param id Id of the file to delete.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerError          If the thread was interrupted.
     * @throws InterruptedException If block(long) does not return.
     */
    public void deleteFile(Long id)
    throws ServiceException, AccessException, ExecutionException, ServerError, InterruptedException {
        FileAnnotationI table = new FileAnnotationI(id, false);
        delete(table);
    }


    /**
     * Deletes a Folder from OMERO
     *
     * @param folder FolderContainer containing the folder to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException Id not defined.
     * @throws ServerError              If the thread was interrupted.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void deleteFolder(FolderContainer folder) throws
                                                     ServiceException,
                                                     AccessException,
                                                     ExecutionException,
                                                     IllegalArgumentException,
                                                     ServerError,
                                                     InterruptedException {
        folder.unlinkAllROI(this);
        delete(folder.getFolder().asIObject());
    }

}

