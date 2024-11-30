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

package fr.igred.omero.annotations;


import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.containers.Folder;
import fr.igred.omero.containers.Project;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.screen.Plate;
import fr.igred.omero.screen.PlateAcquisition;
import fr.igred.omero.screen.PlateAcquisitionWrapper;
import fr.igred.omero.screen.Screen;
import fr.igred.omero.screen.Well;
import omero.RLong;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.PlateAcquisitionData;
import omero.model.IObject;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Generic class containing an AnnotationData (or a subclass) object.
 *
 * @param <T> Subclass of {@link AnnotationData}
 */
public abstract class AnnotationWrapper<T extends AnnotationData> extends ObjectWrapper<T> implements Annotation {


    /**
     * Constructor of the AnnotationWrapper class.
     *
     * @param a The AnnotationData to wrap.
     */
    protected AnnotationWrapper(T a) {
        super(a);
    }


    /**
     * Retrieves the {@link AnnotationData} namespace of the underlying {@link AnnotationData} instance.
     *
     * @return See above.
     */
    @Override
    public String getNameSpace() {
        return data.getNameSpace();
    }


    /**
     * Sets the name space of the underlying {@link AnnotationData} instance.
     *
     * @param name The value to set.
     */
    @Override
    public void setNameSpace(String name) {
        data.setNameSpace(name);
    }


    /**
     * Returns the time when the annotation was last modified.
     *
     * @return See above.
     */
    @Override
    public Timestamp getLastModified() {
        return data.getLastModified();
    }


    /**
     * Retrieves the {@link AnnotationData#getDescription() description} of the underlying {@link AnnotationData}
     * instance.
     *
     * @return See above
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the underlying {@link AnnotationData} instance.
     *
     * @param description The description
     */
    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Gets all projects with this annotation from OMERO.
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
    public List<Project> getProjects(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os = getLinks(browser, Project.ANNOTATION_LINK);
        Long[] ids = os.stream()
                       .map(IObject::getId)
                       .map(RLong::getValue)
                       .sorted()
                       .toArray(Long[]::new);
        return browser.getProjects(ids);
    }


    /**
     * Gets all datasets with this annotation from OMERO.
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
    public List<Dataset> getDatasets(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os = getLinks(browser, Dataset.ANNOTATION_LINK);
        Long[] ids = os.stream()
                       .map(IObject::getId)
                       .map(RLong::getValue)
                       .sorted()
                       .toArray(Long[]::new);
        return browser.getDatasets(ids);
    }


    /**
     * Gets all images with this annotation from OMERO.
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
    public List<Image> getImages(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os = getLinks(browser, Image.ANNOTATION_LINK);
        Long[] ids = os.stream()
                       .map(IObject::getId)
                       .map(RLong::getValue)
                       .sorted()
                       .toArray(Long[]::new);
        return browser.getImages(ids);
    }


    /**
     * Gets all screens with this annotation from OMERO.
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
    public List<Screen> getScreens(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os = getLinks(browser, Screen.ANNOTATION_LINK);
        Long[] ids = os.stream()
                       .map(IObject::getId)
                       .map(RLong::getValue)
                       .sorted()
                       .toArray(Long[]::new);
        return browser.getScreens(ids);
    }


    /**
     * Gets all plates with this annotation from OMERO.
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
    public List<Plate> getPlates(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os = getLinks(browser, Plate.ANNOTATION_LINK);
        Long[] ids = os.stream()
                       .map(IObject::getId)
                       .map(RLong::getValue)
                       .sorted()
                       .toArray(Long[]::new);
        return browser.getPlates(ids);
    }


    /**
     * Gets all plate acquisitions with this annotation from OMERO.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    @Override
    public List<PlateAcquisition> getPlateAcquisitions(Browser browser)
    throws ServiceException, AccessException {
        List<IObject> os = getLinks(browser,
                                    PlateAcquisition.ANNOTATION_LINK);
        return os.stream()
                 .map(omero.model.PlateAcquisition.class::cast)
                 .map(PlateAcquisitionData::new)
                 .map(PlateAcquisitionWrapper::new)
                 .collect(Collectors.toList());
    }


    /**
     * Gets all wells with this annotation from OMERO.
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
    public List<Well> getWells(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os = getLinks(browser, Well.ANNOTATION_LINK);
        Long[] ids = os.stream()
                       .map(IObject::getId)
                       .map(RLong::getValue)
                       .sorted()
                       .toArray(Long[]::new);
        return browser.getWells(ids);
    }


    /**
     * Gets all folders with this annotation from OMERO.
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
    public List<Folder> getFolders(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os = getLinks(browser, Folder.ANNOTATION_LINK);
        Long[] ids = os.stream()
                       .map(IObject::getId)
                       .map(RLong::getValue)
                       .sorted()
                       .toArray(Long[]::new);
        return browser.getFolders(ids);
    }


    /**
     * Retrieves all links of the given type.
     *
     * @param browser  The data browser.
     * @param linkType The link type.
     *
     * @return The list of linked objects.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    private List<IObject> getLinks(Browser browser, String linkType)
    throws ServiceException, AccessException {
        return browser.findByQuery("select link.parent from " + linkType +
                                   " link where link.child = " + getId());
    }

}
