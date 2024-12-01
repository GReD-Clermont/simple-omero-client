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

package fr.igred.omero.containers;


import fr.igred.omero.RepositoryObjectWrapper;
import fr.igred.omero.client.BasicBrowser;
import fr.igred.omero.client.BasicDataManager;
import fr.igred.omero.client.Client;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.RemoteObject.distinct;
import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.Collections.singletonList;


/**
 * Class containing a ProjectData object.
 * <p> Wraps function calls to the Project contained
 */
public class ProjectWrapper extends RepositoryObjectWrapper<ProjectData> implements Project {


    /**
     * Constructor of the ProjectWrapper class.
     *
     * @param project ProjectData to be contained.
     */
    public ProjectWrapper(ProjectData project) {
        super(project);
    }


    /**
     * Constructor of the ProjectWrapper class. Creates a new project and save it to OMERO.
     *
     * @param dm          The data manager.
     * @param name        Project name.
     * @param description Project description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ProjectWrapper(BasicDataManager dm, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        super(new ProjectData());
        data.setName(name);
        data.setDescription(description);
        super.saveAndUpdate(dm);
    }


    /**
     * Gets the ProjectData name
     *
     * @return ProjectData name.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the project.
     *
     * @param name The name of the project. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    @Override
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Gets the project description
     *
     * @return The project description.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the project.
     *
     * @param description The description of the project.
     */
    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Returns the type of annotation link for this object.
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Gets all the datasets in the project available from OMERO.
     *
     * @return Collection of DatasetWrapper.
     */
    @Override
    public List<Dataset> getDatasets() {
        return wrap(data.getDatasets(), DatasetWrapper::new);
    }


    /**
     * Creates a dataset and adds it to the project in OMERO.
     * <p>The project needs to be reloaded afterwards to list the new dataset.</p>
     *
     * @param dm          The data manager.
     * @param name        Dataset name.
     * @param description Dataset description.
     *
     * @return The object saved in OMERO.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public Dataset addDataset(BasicDataManager dm, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        Dataset dataset = new DatasetWrapper(name, description);
        dataset.saveAndUpdate(dm);
        return addDataset(dm, dataset);
    }


    /**
     * Removes a dataset from the project in OMERO.
     *
     * @param client  The client handling the connection.
     * @param dataset Dataset to remove.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    public void removeDataset(Client client, Dataset dataset)
    throws ServiceException, AccessException, ExecutionException, InterruptedException {
        removeLink(client, "ProjectDatasetLink", dataset.getId());
        reload(client);
    }


    /**
     * Gets all images in the project available from OMERO.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<Image> getImages(BasicBrowser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<Long> projectIds = singletonList(getId());
        Collection<ImageData> images = call(browser.getBrowseFacility(),
                                            bf -> bf.getImagesForProjects(browser.getCtx(),
                                                                          projectIds),
                                            "Cannot get images from " + this);
        return distinct(wrap(images, ImageWrapper::new));
    }


    /**
     * Reloads the project from OMERO.
     *
     * @param browser The data browser.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void reload(BasicBrowser browser)
    throws ServiceException, AccessException, ExecutionException {
        data = call(browser.getBrowseFacility(),
                    bf -> bf.getProjects(browser.getCtx(),
                                         singletonList(getId()))
                            .iterator()
                            .next(),
                    "Cannot reload " + this);
    }

}