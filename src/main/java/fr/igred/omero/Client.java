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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import fr.igred.omero.metadata.ROIContainer;
import fr.igred.omero.metadata.TableContainer;
import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ProjectContainer;
import fr.igred.omero.sort.SortImageContainer;
import fr.igred.omero.sort.SortTagAnnotationContainer;
import ome.formats.importer.ImportConfig;
import omero.LockTimeout;
import omero.ServerError;
import omero.api.IQueryPrx;
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


/**
 * Basic class, contain the gateway, the security context, and multiple facility.
 * 
 * Allow the connexion of user to connect to OMERO and browse through all the data accessible to the user.
 */
public class Client {
    //User
    private ExperimenterData    user;

    //Gateway linking the code to OMERO, only linked to one group.
    private Gateway             gateway;

    //Security context of the user, contains the permissions of the user in this group.
    private SecurityContext     ctx;

    private BrowseFacility      browse;
    private DataManagerFacility dm;
    private MetadataFacility    metadata;
    private ImportConfig        config;
    private ROIFacility         roifac;
    private TablesFacility      fac;
    private AdminFacility       admin;
    private RawDataFacility     rdf;
    private IQueryPrx           qs;
    /**
     * Return the security context of the user.
     * Contains the permissions of the user in the group.
     * 
     * @return ctx, the security context
     */
    public SecurityContext getCtx()
    {
        return ctx;
    }

    /**
     * Return the BrowseFacility linked to the gateway.
     * The BrowseFacility is used to access the data from OMERO.
     * 
     * @return browse
     */
    public BrowseFacility getBrowseFacility()
    {
        return browse;
    }

    /**
     * Return the DataManagerFacility linked to the gateway.
     * Permet de gérer la sauvergarde de données sur OMERO depuis le code.
     * 
     * @return dm
     * 
     * @throws ExecutionException If the DataManagerFacility can't be retrieved or instantiated
     */
    public DataManagerFacility getDm()
        throws
            ExecutionException
    {
        if(dm == null)
            dm = gateway.getFacility(DataManagerFacility.class);
        return dm;
    }

    /**
     * Return MetadataFacility linked to the gateway.
     * The MetadataFacility is used to manipulate metadata from OMERO.
     * 
     * @return metadata
     * 
     * @throws ExecutionException If the MetadataFacility can't be retrieved or instantiated
     */
    public MetadataFacility getMetadata()
        throws
            ExecutionException
    {
        if(metadata == null)
            metadata = gateway.getFacility(MetadataFacility.class);

        return metadata;
    }

    /**
     * Return ROIFacility linked to the gateway.
     * The ROIFacility is used to manipulate ROI from OMERO.
     * 
     * @return roifac
     * 
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated
     */
    public ROIFacility getRoiFacility()
        throws
            ExecutionException
    {
        if(roifac == null)
            roifac = gateway.getFacility(ROIFacility.class);

        return roifac;
    }

    /**
     * Return TablesFacility linked to the gateway.
     * The v is used to manipulate table from OMERO.
     * 
     * @return fac
     * 
     * @throws ExecutionException If the TablesFacility can't be retrieved or instantiated
     */
    public TablesFacility getTablesFacilitity()
        throws
            ExecutionException
    {
        if(fac == null)
            fac = gateway.getFacility(TablesFacility.class);

        return fac;
    }

    /**
     * Return AdminFacility linked to the gateway.
     * The AdminFacility is used to use admin specific function.
     * 
     * @return admin
     * 
     * @throws ExecutionException If the AdminFacility can't be retrieved or instantiated
     */
    public AdminFacility getAdminFacility()
        throws
            ExecutionException
    {
        if(admin == null)
            admin = gateway.getFacility(AdminFacility.class);

        return admin;
    }

    /**
     * Return ExecutionException linked to the gateway.
     * The ExecutionException is used to access the raw data of the images.
     * 
     * @return rdf
     * 
     * @throws ExecutionException If the ExecutionException can't be retrieved or instantiated
     */
    public RawDataFacility getRdf()
        throws
            ExecutionException
    {
        if(rdf == null)
            rdf = gateway.getFacility(RawDataFacility.class);
        
        return rdf;
    }

    public IQueryPrx getQueryService()
        throws DSOutOfServiceException
    {
        if(qs == null)
            qs = gateway.getQueryService(ctx);

        return qs;
    }

    /**
     * Return the importation config for the user.
     * 
     * @return config
     */
    public ImportConfig getConfig()
    {
        return config;
    }

    /**
     * Return the user id.
     * 
     * @return id
     */
    public Long getId()
    {
        return user.getId();
    }

    public Long getGroupId()
    {
        return user.getGroupId();
    }

    public Gateway getGateway()
    {
        return gateway;
    }




    /**
     * Connect the user to OMERO.
     * Use the argument to connect to the gateway.
     * Connect to the group specified in the argument.
     * 
     * 
     * @param hostname name of the host
     * @param port     port used by OMERO
     * @param username username of the user
     * @param password password of the user
     * @param groupID  id of the group to connect
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public void connect(String hostname, 
                        int    port, 
                        String username, 
                        String password, 
                        Long   groupID)
        throws 
            DSOutOfServiceException,
            ExecutionException
    {
        LoginCredentials cred = createCred(hostname, port, username, password);

        cred.setGroupID(groupID);

        createConfig(hostname, port, username, password);

        connect(cred);
    }

    /**
     * Connect the user to OMERO.
     * Use the argument to connect to the gateway.
     * Connect to the default group of the user.
     * 
     * 
     * @param hostname name of the host
     * @param port     port used by OMERO
     * @param username username of the user
     * @param password password of the user
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public void connect(String hostname, 
                        int    port, 
                        String username, 
                        String password)
        throws 
            DSOutOfServiceException,
            ExecutionException
    {
        LoginCredentials cred = createCred(hostname, port, username, password);

        createConfig(hostname, port, username, password);

        connect(cred);
    }

    /**
     * Create the credential used to log the user to OMERO.
     * 
     * @param hostname name of the host
     * @param port     port used by OMERO
     * @param username username of the user
     * @param password password of the user
     */
    private LoginCredentials createCred(String hostname, 
                                        int    port, 
                                        String username, 
                                        String password)
    {
        LoginCredentials cred = new LoginCredentials();

        cred.getServer().setHost(hostname);
        cred.getServer().setPort(port);
        cred.getUser().setUsername(username);
        cred.getUser().setPassword(password);

        return cred;
    }

    /**
     * Create the importation config linked to the user.
     * 
     * @param hostname name of the host
     * @param port     port used by OMERO
     * @param username username of the user
     * @param password password of the user
     */
    private void createConfig(String hostname, 
                              int    port, 
                              String username, 
                              String password)
    {
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
     * Connect the user to OMERO.
     * Get the SecurityContext and the BrowseFacility.
     * 
     * @param cred user credential 
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO  
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public void connect(LoginCredentials cred)
        throws 
            DSOutOfServiceException,
            ExecutionException
    {
        this.user = gateway.connect(cred);

        this.ctx = new SecurityContext(user.getGroupId());
        this.browse = gateway.getFacility(BrowseFacility.class);
    }
    
    /**
     * Disconnect the user
     */
    public void disconnect()
    {
        gateway.disconnect();
    }




    /**
     * Get the project with the specified id from OMERO.
     * 
     * @param id Id of the project 
     * 
     * @return ProjectContainer containing the project
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws NoSuchElementException  No element with such id
     */
    public ProjectContainer getProject(Long id)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            NoSuchElementException
    {
        Collection<ProjectData> projects = browse.getProjects(ctx);

        for(ProjectData project : projects)
        {
            if (project.getId() == id)
            {
                return new ProjectContainer(project);
            }
        }
        throw new NoSuchElementException("Project " + id + " doesn't exist in this context");
    }

    /**
     * Get all projects available from OMERO.
     * 
     * @return Collection of ProjectContainer
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public Collection<ProjectContainer> getProjects()
        throws 
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<ProjectData> projects =  browse.getProjects(ctx);
        
        Collection<ProjectContainer> projectsContainer = new ArrayList<ProjectContainer>(projects.size());

        for(ProjectData project : projects)
        {
            projectsContainer.add(new ProjectContainer(project));
        }
        return projectsContainer;
    }

    /**
     * Get all projects with a certain name from OMERO.
     * 
     * @param name Name searched
     * 
     * @return Collection of ProjectContainer.
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public Collection<ProjectContainer> getProjects(String name)
        throws 
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<ProjectData> projects =  browse.getProjects(ctx, name);
        
        Collection<ProjectContainer> projectsContainer = new ArrayList<ProjectContainer>(projects.size());

        for (ProjectData project : projects) 
        { 
            projectsContainer.add(new ProjectContainer(project));
        }
        
        return projectsContainer;
    }





    /**
     * Get the dataset with the specified id from OMERO.
     * 
     * @param id Id of the Dataset
     * 
     * @return ProjectContainer containing the project
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws NoSuchElementException  No element with such id
     */
    public DatasetContainer getDataset(Long id)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            NoSuchElementException
    {
        Collection<DatasetData> datasets = browse.getDatasets(ctx);

        for(DatasetData dataset : datasets)
        {
            if (dataset.getId() == id)
            {
                return new DatasetContainer(dataset);
            }
        }
        throw new NoSuchElementException("Dataset " + id + " doesn't exist in this context");
    }

    /**
     * Get all the datasets available from OMERO.
     * 
     * @return Collection of DatasetContainer.
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public Collection<DatasetContainer> getDatasets()
        throws 
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<DatasetData> datasets =  browse.getDatasets(ctx);
        
        Collection<DatasetContainer> datasetContainer = new ArrayList<DatasetContainer>(datasets.size());

        for(DatasetData dataset : datasets)
        {
            datasetContainer.add(new DatasetContainer(dataset));
        }
        return datasetContainer;
    }

    /**
     * Get all datasets with a certain name from OMERO.
     * 
     * @param name Name searched
     * 
     * @return Collection of DatasetContainer.
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public Collection<DatasetContainer> getDatasets(String name)
        throws 
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<DatasetData> datasets =  browse.getDatasets(ctx, name);
        
        Collection<DatasetContainer> datasetContainer = new ArrayList<DatasetContainer>(datasets.size());

        for (DatasetData dataset : datasets) 
        { 
            datasetContainer.add(new DatasetContainer(dataset));
        }
        
        return datasetContainer;
    }




    /**
     * Transform a collection of ImageData in a list of ImageContainer sorted by the ImageData id.
     * 
     * @param images ImageData Collection 
     * 
     * @return ImageContainer list sorted
     */
    private List<ImageContainer> toImagesContainer(Collection<ImageData> images)
    {
        List<ImageContainer> imagesContainer = new ArrayList<ImageContainer>(images.size());

        for(ImageData image : images)
        {
            imagesContainer.add(new ImageContainer(image));
        }

        Collections.sort(imagesContainer, new SortImageContainer());

        return imagesContainer;
    }

    /**
     * Return an ImageContainer that contains the image with the specified id from OMERO.
     * 
     * @param imageId Id of the image
     * 
     * @return ImageContainer containing the image
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws NoSuchElementException  No element with such id
     */
    public ImageContainer getImage(Long imageId)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            NoSuchElementException
    {
        ImageContainer image = new ImageContainer(browse.getImage(ctx, imageId));

        return image;
    }

    /**
     * Get all images available from OMERO.
     * 
     * @return ImageContainer list
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public List<ImageContainer> getImages()
        throws 
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<ImageData> images = browse.getUserImages(ctx);

        return toImagesContainer(images);
    }

    /**
     * Get all images with a certain from OMERO.
     * 
     * @param name Name searched
     * 
     * @return ImageContainer list
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public List<ImageContainer> getImages(String name)
        throws 
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<ImageData> images = browse.getImages(ctx, name);

        Collection<ImageData> selected = new ArrayList<ImageData>(images.size());

        for(ImageData image : images) {
            if(image.getName().equals(name)) {
                selected.add(image);
            }
        }

        return toImagesContainer(selected);
    }

    /**
     * Get all images with a certain motif in their name from OMERO.
     * 
     * @param motif Motif searched in an Image name
     * 
     * @return ImageContainer list
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     */
    public List<ImageContainer> getImagesLike(String motif)
        throws 
            DSOutOfServiceException,
            DSAccessException
    {
        Collection<ImageData> images = browse.getUserImages(ctx);

        motif = ".*" + motif + ".*";

        Collection<ImageData> selected = new ArrayList<ImageData>(images.size());

        for(ImageData image : images) {
            if(image.getName().matches(motif)) {
                selected.add(image); 
            }
        }
        return toImagesContainer(selected);
    }

    /**
     * Get all images tagged with a specified tag from OMERO.
     * 
     * @param tag TagAnnotationContainer containing the tag researched
     * 
     * @return ImageContainer list
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public List<ImageContainer> getImagesTagged(TagAnnotationContainer tag)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            ServerError
    {
        List<IObject> os = getQueryService().findAllByQuery("select link.parent " +
                                                            "from ImageAnnotationLink link " + 
                                                            "where link.child = " + 
                                                            tag.getId(), null);

        Collection<ImageData> selected = new ArrayList<ImageData>();

        for(IObject o : os) {
            ImageData image = browse.getImage(ctx, o.getId().getValue());
            selected.add(image);
        }
        return toImagesContainer(selected);
    }

    /**
     * Get all images tagged with a specified tag from OMERO.
     * 
     * @param tagId Id of the tag researched
     * 
     * @return ImageContainer list
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public List<ImageContainer> getImagesTagged(Long tagId)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            ServerError
    {
        List<IObject> os = getQueryService().findAllByQuery("select link.parent " +
                                                            "from ImageAnnotationLink link " + 
                                                            "where link.child = " + 
                                                            tagId, null);

        Collection<ImageData> selected = new ArrayList<ImageData>();

        for(IObject o : os) {
            ImageData image = browse.getImage(ctx, o.getId().getValue());
            selected.add(image);
        }
        return toImagesContainer(selected);
    }

    /**
     * Get all images with a certain key
     * 
     * @param key Name of the key researched
     * 
     * @return ImageContainer list
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public List<ImageContainer> getImagesKey(String key)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<ImageData> images = browse.getUserImages(ctx);

        Collection<ImageData> selected = new ArrayList<ImageData>(images.size());

        for(ImageData image : images)
        {
            ImageContainer imageContainer = new ImageContainer(image);
            
            Collection<NamedValue> pairsKeyValue = imageContainer.getKeyValuePairs(this);

            for(NamedValue pairKeyValue : pairsKeyValue)
            {
                if(pairKeyValue.name.equals(key))
                {
                    selected.add(image);
                    break;
                }
            }
        }

        return toImagesContainer(selected);
    }

    /**
     * Get all images with a certain key value pair from OMERO
     * 
     * @param key   Name of the key researched
     * @param value Value associated with the key
     * 
     * @return ImageContainer list
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public List<ImageContainer> getImagesPairKeyValue(String key, 
                                                      String value)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Collection<ImageData> images = browse.getUserImages(ctx);

        Collection<ImageData> selected = new ArrayList<ImageData>(images.size());

        for(ImageData image : images)
        {
            ImageContainer imageContainer = new ImageContainer(image);
            
            Collection<NamedValue> pairsKeyValue = imageContainer.getKeyValuePairs(this);

            for(NamedValue pairKeyValue : pairsKeyValue)
            {
                if(pairKeyValue.name.equals(key) && pairKeyValue.value.equals(value))
                {
                    selected.add(image);
                    break;
                }
            }
        }

        return toImagesContainer(selected);
    }





    /**
     * Get the client associated with the username in the parameters.
     * The user calling this function needs to have administrator rights.
     * All action realized with the client returned will be considered as his.
     * 
     * @param username Username of user
     * 
     * @return The client corresponding to the new user
     * 
     * @throws DSOutOfServiceException Cannot connect to OMERO
     * @throws DSAccessException       Cannot access data
     * @throws ExecutionException      A Facility can't be retrieved or instancied
     */
    public Client SudoGetUser(String username)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException
    {
        Client c = new Client();

        ExperimenterData sudoUser = getAdminFacility().lookupExperimenter(ctx, username);

        SecurityContext sudoCtx = new SecurityContext(sudoUser.getGroupId());
        sudoCtx.setExperimenter(sudoUser);
        sudoCtx.sudo();

        c.gateway = this.gateway;
        c.ctx     = sudoCtx;
        c.user    = sudoUser;
        c.browse  = this.browse;

        return c;
    }




    /**
     * Get the list of TagAnnotationContainer available to the user
     * 
     * @return list of TagAnnotationContainer
     * 
     * @throws ServerError
     * @throws DSOutOfServiceException Cannot connect to OMERO
     */
    public List<TagAnnotationContainer> getTags()
        throws 
            ServerError,
            DSOutOfServiceException
    {
        IQueryPrx qs = gateway.getQueryService(ctx);
        List<IObject> os = qs.findAll(TagAnnotation.class.getSimpleName(), null);
        List<TagAnnotationContainer> tags = new ArrayList<TagAnnotationContainer>(os.size());

        for(IObject o : os) {
            TagAnnotationData tag = new TagAnnotationData((TagAnnotation) o);
            tags.add(new TagAnnotationContainer(tag));
        }
        
        Collections.sort(tags, new SortTagAnnotationContainer());
        return tags;
    }

    /**
     * Get the list of TagAnnotationContainer with the specified name available to the user
     * 
     * @param name Name of the tag searched
     * 
     * @return list of TagAnnotationContainer
     * 
     * @throws ServerError Server error
     * @throws DSOutOfServiceException Cannot connect to OMERO
     */
    public List<TagAnnotationContainer> getTags(String name)
        throws 
            ServerError,
            DSOutOfServiceException
    {
        IQueryPrx qs = gateway.getQueryService(ctx);
        List<IObject> os = qs.findAll(TagAnnotation.class.getSimpleName(), null);
        List<TagAnnotationContainer> tags = new ArrayList<TagAnnotationContainer>(os.size());

        for(IObject o : os) {
            TagAnnotationData tag = new TagAnnotationData((TagAnnotation) o);
            tag.setNameSpace(tag.getContentAsString());

            if(tag.getNameSpace().equals(name))
                tags.add(new TagAnnotationContainer(tag));
        }

        Collections.sort(tags, new SortTagAnnotationContainer());
        return tags;
    }

    /**
     * Get a specific tag from the OMERO database
     * 
     * @param id Id of the tag 
     * 
     * @return TagAnnotationContainer containng the specified tag
     * 
     * @throws ServerError
     * @throws DSOutOfServiceException Cannot connect to OMERO
     */
    public TagAnnotationContainer getTag(Long id)
        throws 
            ServerError,
            DSOutOfServiceException
    {
        IQueryPrx qs = gateway.getQueryService(ctx);
        IObject o = qs.find(TagAnnotation.class.getSimpleName(), id);

        TagAnnotationData tag = new TagAnnotationData((TagAnnotation) o);
        tag.setNameSpace(tag.getContentAsString());

        return new TagAnnotationContainer(tag);
    }

    


    /**
     * Delete an image from OMERO
     * 
     * @param image ImageContainer containing the image to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws IllegalArgumentException Id not defined
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteImage(ImageContainer image)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            IllegalArgumentException,
            LockTimeout,
            InterruptedException 
    {
        deleteImage(image.getId());
    }

    /**
     * Delete an image from OMERO
     * 
     * @param id Id of the image to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteImage(Long id)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            LockTimeout,
            InterruptedException 
    {
        ImageI image = new ImageI(id, false);

        getDm().delete(ctx, image).loop(10, 500);
    }

    /**
     * Delete a project from OMERO
     * 
     * @param project ProjectContainer containing the project to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws IllegalArgumentException Id not defined
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteProject(ProjectContainer project)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            IllegalArgumentException,
            LockTimeout,
            InterruptedException 
    {
        if(project.getId() != null)
            deleteProject(project.getId());
        else
            throw new IllegalArgumentException("Project id is null");
    }

    /**
     * Delete a project from OMERO
     * 
     * @param id Id of the project to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteProject(Long id)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            LockTimeout,
            InterruptedException 
    {
        ProjectI project = new ProjectI(id, false);

        getDm().delete(ctx, project).loop(10, 500);
    }

    /**
     * Delete a dataset from OMERO
     * 
     * @param dataset DatasetContainer containing the dataset to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws IllegalArgumentException Id not defined
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteDataset(DatasetContainer dataset)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            IllegalArgumentException,
            LockTimeout,
            InterruptedException 
    {
        if(dataset.getId() != null)
            deleteDataset(dataset.getId());
        else
            throw new IllegalArgumentException("Dataset id is null");
    }

    /**
     * Delete a dataset from OMERO
     * 
     * @param id Id of the dataset to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteDataset(Long id)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            LockTimeout,
            InterruptedException 
    {
        DatasetI dataset = new DatasetI(id, false);

        getDm().delete(ctx, dataset).loop(10, 500);
    }

    /**
     * Delete a tag from OMERO
     * 
     * @param tag TagContainer containing the tag to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws IllegalArgumentException Id not defined
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteTag(TagAnnotationContainer tag)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            IllegalArgumentException,
            LockTimeout,
            InterruptedException 
    {
        if(tag.getId() != null)
            deleteTag(tag.getId());
        else
            throw new IllegalArgumentException("Tag id is null");
    }

    /**
     * Delete a tag from OMERO
     * 
     * @param id Id of the tag to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteTag(Long id)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            LockTimeout,
            InterruptedException 
    {
        TagAnnotationI tag = new TagAnnotationI(id, false);

        getDm().delete(ctx, tag).loop(10, 500);
    }

    /**
     * Delete a ROI from OMERO
     * 
     * @param roi ROIContainer containing the ROI to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws IllegalArgumentException Id not defined
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteROI(ROIContainer roi)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            IllegalArgumentException,
            LockTimeout,
            InterruptedException 
    {
        if(roi.getId() != null)
            deleteROI(roi.getId());
        else
            throw new IllegalArgumentException("ROI id is null");
    }

    /**
     * Delete a ROI from OMERO
     * 
     * @param id Id of the ROI to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteROI(Long id)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            LockTimeout,
            InterruptedException 
    {
        RoiI roi = new RoiI(id, false);

        getDm().delete(ctx, roi).loop(10, 500);
    }

    /**
     * Delete a table from OMERO
     * 
     * @param table TableContainer containing the table to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws IllegalArgumentException Id not defined
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteTable(TableContainer table)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            LockTimeout,
            InterruptedException 
    {
        deleteTag(table.getId());
    }

    /**
     * Delete a table from OMERO
     * 
     * @param id Id of the table to delete
     * 
     * @throws DSOutOfServiceException  Cannot connect to OMERO
     * @throws DSAccessException        Cannot access data
     * @throws ExecutionException       A Facility can't be retrieved or instancied
     * @throws LockTimeout              If the thread was interrupted
     * @throws InterruptedException     If block(long) does not return
     */
    public void deleteFile(Long id)
        throws 
            DSOutOfServiceException,
            DSAccessException,
            ExecutionException,
            LockTimeout,
            InterruptedException 
    {
        FileAnnotationI table = new FileAnnotationI(id, false);

        getDm().delete(ctx, table).loop(10, 500);
    }




    /**
     * Constructor of the Client class.
     * Initialize the gateway.
     */
    public Client()
    {
        gateway = new Gateway(new SimpleLogger());
    }
}

