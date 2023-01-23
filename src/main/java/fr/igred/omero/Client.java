/*
 *  Copyright (C) 2020-2023 GReD
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


import fr.igred.omero.annotations.Table;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.Experimenter;
import fr.igred.omero.meta.Group;
import fr.igred.omero.repository.Dataset;
import fr.igred.omero.repository.Folder;
import fr.igred.omero.repository.Image;
import fr.igred.omero.repository.Plate;
import fr.igred.omero.repository.Project;
import fr.igred.omero.repository.Screen;
import fr.igred.omero.repository.Well;
import omero.RLong;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.WellData;
import omero.model.IObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.RemoteObject.distinct;
import static fr.igred.omero.RemoteObject.wrap;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;
import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndServer;


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
        super(null, null, null);
    }


    /**
     * Constructor of the Client class.
     *
     * @param gateway The gateway
     * @param ctx     The security context
     * @param user    The user
     */
    public Client(Gateway gateway, SecurityContext ctx, Experimenter user) {
        super(gateway, ctx, user);
    }


    /**
     * Gets the project with the specified id from OMERO.
     *
     * @param id ID of the project.
     *
     * @return Project containing the project.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public Project getProject(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Project> projects = getProjects(id);
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
    public List<Project> getProjects(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get projects with IDs: " + Arrays.toString(ids);
        Collection<ProjectData> projects = handleServiceAndAccess(getBrowseFacility(),
                                                                  bf -> bf.getProjects(getCtx(), Arrays.asList(ids)),
                                                                  error);
        return wrap(projects, Project::new);
    }


    /**
     * Gets all projects available from OMERO.
     *
     * @return Collection of Project.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Project> getProjects() throws ServiceException, AccessException, ExecutionException {
        Collection<ProjectData> projects = handleServiceAndAccess(getBrowseFacility(),
                                                                  bf -> bf.getProjects(getCtx()),
                                                                  "Cannot get projects");
        return wrap(projects, Project::new);
    }


    /**
     * Gets all projects with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return Collection of Project.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Project> getProjects(String name) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get projects with name: " + name;
        Collection<ProjectData> projects = handleServiceAndAccess(getBrowseFacility(),
                                                                  bf -> bf.getProjects(getCtx(), name),
                                                                  error);
        return wrap(projects, Project::new);
    }


    /**
     * Gets the dataset with the specified id from OMERO.
     *
     * @param id ID of the dataset.
     *
     * @return Project containing the project.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public Dataset getDataset(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Dataset> datasets = getDatasets(id);
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
    public List<Dataset> getDatasets(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get dataset with ID: " + Arrays.toString(ids);
        Collection<DatasetData> datasets = handleServiceAndAccess(getBrowseFacility(),
                                                                  bf -> bf.getDatasets(getCtx(), Arrays.asList(ids)),
                                                                  error);
        return wrap(datasets, Dataset::new);
    }


    /**
     * Gets all datasets available from OMERO.
     *
     * @return List of DatasetWrappers.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Dataset> getDatasets()
    throws ServiceException, AccessException, ServerException, ExecutionException {
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
     * @return Collection of Dataset.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Dataset> getDatasets(String name) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get datasets with name: " + name;
        Collection<DatasetData> datasets = handleServiceAndAccess(getBrowseFacility(),
                                                                  bf -> bf.getDatasets(getCtx(), name),
                                                                  error);
        return wrap(datasets, Dataset::new);
    }


    /**
     * Returns an Image that contains the image with the specified id from OMERO.
     *
     * @param id ID of the image.
     *
     * @return Image containing the image.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public Image getImage(Long id)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get image with ID: " + id;

        ImageData image = handleServiceAndAccess(getBrowseFacility(),
                                                 bf -> bf.getImage(getCtx(), id),
                                                 error);
        if (image == null) {
            throw new NoSuchElementException(String.format("Image %d doesn't exist in this context", id));
        }
        return new Image(image);
    }


    /**
     * Gets the images with the specified ids from OMERO
     *
     * @param ids Image IDs
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImages(Long... ids) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with IDs: " + Arrays.toString(ids);
        Collection<ImageData> images = handleServiceAndAccess(getBrowseFacility(),
                                                              bf -> bf.getImages(getCtx(), Arrays.asList(ids)),
                                                              error);
        return wrap(images, Image::new);
    }


    /**
     * Gets all images owned by the current user.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImages() throws ServiceException, AccessException, ExecutionException {
        Collection<ImageData> images = handleServiceAndAccess(getBrowseFacility(),
                                                              bf -> bf.getUserImages(getCtx()),
                                                              "Cannot get images");
        return wrap(images, Image::new);
    }


    /**
     * Gets all images with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImages(String name) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get images with name: " + name;
        Collection<ImageData> images = handleServiceAndAccess(getBrowseFacility(),
                                                              bf -> bf.getImages(getCtx(), name),
                                                              error);
        images.removeIf(image -> !image.getName().equals(name));
        return wrap(images, Image::new);
    }


    /**
     * Gets all images with the name specified inside projects and datasets with the given names.
     *
     * @param projectName Expected project name.
     * @param datasetName Expected dataset name.
     * @param name   Expected image name.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImages(String projectName, String datasetName, String name)
    throws ServiceException, AccessException, ExecutionException {
        List<Project> projects = getProjects(projectName);

        Collection<List<Image>> lists = new ArrayList<>(projects.size());
        for (Project project : projects) {
            lists.add(project.getImages(this, datasetName, name));
        }

        List<Image> images = lists.stream()
                                  .flatMap(Collection::stream)
                                  .sorted(Comparator.comparing(RemoteObject::getId))
                                  .collect(Collectors.toList());

        return distinct(images);
    }


    /**
     * Gets all images with a certain motif in their name from OMERO.
     *
     * @param motif Motif searched in an image name.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImagesLike(String motif) throws ServiceException, AccessException, ExecutionException {
        List<Image> images = getImages();
        String      regexp = ".*" + motif + ".*";
        images.removeIf(image -> !image.getName().matches(regexp));
        return images;
    }


    /**
     * Gets all images tagged with a specified tag from OMERO.
     *
     * @param tag TagAnnotation containing the tag researched.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImagesTagged(TagAnnotation tag)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        return tag.getImages(this);
    }


    /**
     * Gets all images tagged with a specified tag from OMERO.
     *
     * @param tagId Id of the tag researched.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImagesTagged(Long tagId)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        return getImagesTagged(getTag(tagId));
    }


    /**
     * Gets all images with a certain key
     *
     * @param key Name of the key researched.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImagesKey(String key)
    throws ServiceException, AccessException, ExecutionException {
        List<Image> images   = getImages();
        List<Image> selected = new ArrayList<>(images.size());
        for (Image image : images) {
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
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImagesPairKeyValue(String key, String value)
    throws ServiceException, AccessException, ExecutionException {
        List<Image> images   = getImages();
        List<Image> selected = new ArrayList<>(images.size());
        for (Image image : images) {
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
     * @param id ID of the screen.
     *
     * @return Screen containing the screen.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public Screen getScreen(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Screen> screens = getScreens(id);
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
    public List<Screen> getScreens(Long... ids) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get screens with IDs: " + Arrays.toString(ids);
        Collection<ScreenData> screens = handleServiceAndAccess(getBrowseFacility(),
                                                                bf -> bf.getScreens(getCtx(), Arrays.asList(ids)),
                                                                error);
        return wrap(screens, Screen::new);
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
    public List<Screen> getScreens() throws ServiceException, AccessException, ExecutionException {
        Collection<ScreenData> screens = handleServiceAndAccess(getBrowseFacility(),
                                                                bf -> bf.getScreens(getCtx()),
                                                                "Cannot get screens");
        return wrap(screens, Screen::new);
    }


    /**
     * Gets the plate with the specified id from OMERO.
     *
     * @param id ID of the plate.
     *
     * @return Plate containing the plate.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public Plate getPlate(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Plate> plates = getPlates(id);
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
    public List<Plate> getPlates(Long... ids) throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get plates with IDs: " + Arrays.toString(ids);
        Collection<PlateData> plates = handleServiceAndAccess(getBrowseFacility(),
                                                              bf -> bf.getPlates(getCtx(), Arrays.asList(ids)),
                                                              error);
        return wrap(plates, Plate::new);
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
    public List<Plate> getPlates() throws ServiceException, AccessException, ExecutionException {
        Collection<PlateData> plates = handleServiceAndAccess(getBrowseFacility(),
                                                              bf -> bf.getPlates(getCtx()),
                                                              "Cannot get plates");
        return wrap(plates, Plate::new);
    }


    /**
     * Gets the well with the specified id from OMERO.
     *
     * @param id ID of the well.
     *
     * @return Well containing the well.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with such id.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    public Well getWell(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Well> wells = getWells(id);
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
    public List<Well> getWells(Long... ids)
    throws ServiceException, AccessException, ExecutionException {
        String error = "Cannot get wells with IDs: " + Arrays.toString(ids);
        Collection<WellData> wells = handleServiceAndAccess(getBrowseFacility(),
                                                            bf -> bf.getWells(getCtx(), Arrays.asList(ids)),
                                                            error);
        return wrap(wells, Well::new);
    }


    /**
     * Gets all wells available from OMERO.
     *
     * @return List of Well.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    public List<Well> getWells()
    throws ServiceException, AccessException, ExecutionException, ServerException {
        Long[] ids = this.findByQuery("select w from Well w")
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getWells(ids);
    }


    /**
     * Gets the list of TagAnnotation available to the user
     *
     * @return list of TagAnnotation.
     *
     * @throws ServerException  Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<TagAnnotation> getTags() throws ServerException, ServiceException {
        List<IObject> os = handleServiceAndServer(getGateway(),
                                                  g -> g.getQueryService(getCtx())
                                                        .findAll(omero.model.TagAnnotation.class.getSimpleName(),
                                                                 null),
                                                  "Cannot get tags");
        return os.stream()
                 .map(omero.model.TagAnnotation.class::cast)
                 .map(TagAnnotationData::new)
                 .map(TagAnnotation::new)
                 .sorted(Comparator.comparing(RemoteObject::getId))
                 .collect(Collectors.toList());
    }


    /**
     * Gets the list of TagAnnotation with the specified name available to the user
     *
     * @param name Name of the tag searched.
     *
     * @return list of TagAnnotation.
     *
     * @throws ServerException  Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public List<TagAnnotation> getTags(String name) throws ServerException, ServiceException {
        List<TagAnnotation> tags = getTags();
        tags.removeIf(tag -> !tag.getName().equals(name));
        tags.sort(Comparator.comparing(RemoteObject::getId));
        return tags;
    }


    /**
     * Gets a specific tag from the OMERO database
     *
     * @param id Id of the tag.
     *
     * @return TagAnnotation containing the specified tag.
     *
     * @throws ServerException  Server error.
     * @throws ServiceException Cannot connect to OMERO.
     */
    public TagAnnotation getTag(Long id) throws ServerException, ServiceException {
        IObject o = handleServiceAndServer(getGateway(),
                                           g -> g.getQueryService(getCtx())
                                                 .find(omero.model.TagAnnotation.class.getSimpleName(),
                                                       id),
                                           "Cannot get tag ID: " + id);
        TagAnnotationData tag = new TagAnnotationData((omero.model.TagAnnotation) Objects.requireNonNull(o));
        tag.setNameSpace(tag.getContentAsString());

        return new TagAnnotation(tag);
    }


    /**
     * Deletes multiple objects from OMERO.
     *
     * @param objects The OMERO object.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    public void delete(Collection<? extends RemoteObject<?>> objects)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        for (RemoteObject<?> object : objects) {
            if (object instanceof Folder) {
                ((Folder) object).unlinkAllROI(this);
            }
        }
        if (!objects.isEmpty()) {
            delete(objects.stream().map(RemoteObject::asIObject).collect(Collectors.toList()));
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
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    public void delete(RemoteObject<?> object)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        if (object instanceof Folder) {
            ((Folder) object).unlinkAllROI(this);
        }
        delete(object.asIObject());
    }


    /**
     * Deletes a table from OMERO
     *
     * @param table Table containing the table to delete.
     *
     * @throws ServiceException         Cannot connect to OMERO.
     * @throws AccessException          Cannot access data.
     * @throws ExecutionException       A Facility can't be retrieved or instantiated.
     * @throws IllegalArgumentException ID not defined.
     * @throws ServerException          Server error.
     * @throws InterruptedException     If block(long) does not return.
     */
    public void delete(Table table)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        deleteFile(table.getId());
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
    public Experimenter getUser(String username)
    throws ExecutionException, ServiceException, AccessException {
        ExperimenterData experimenter = handleServiceAndAccess(getAdminFacility(),
                                                               a -> a.lookupExperimenter(getCtx(), username),
                                                               "Cannot retrieve user: " + username);
        if (experimenter != null) {
            return new Experimenter(experimenter);
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
    public Group getGroup(String groupName)
    throws ExecutionException, ServiceException, AccessException {
        GroupData group = handleServiceAndAccess(getAdminFacility(),
                                                 a -> a.lookupGroup(getCtx(), groupName),
                                                 "Cannot retrieve group: " + groupName);
        if (group != null) {
            return new Group(group);
        } else {
            throw new NoSuchElementException(String.format("Group not found: %s", groupName));
        }
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
    public Client sudo(String username) throws ServiceException, AccessException, ExecutionException {
        Experimenter sudoUser = getUser(username);

        SecurityContext context = new SecurityContext(sudoUser.getDefaultGroup().getId());
        context.setExperimenter(sudoUser.asDataObject());
        context.sudo();

        return new Client(this.getGateway(), context, sudoUser);
    }

}

