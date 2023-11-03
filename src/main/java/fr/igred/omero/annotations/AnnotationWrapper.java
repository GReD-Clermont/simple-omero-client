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


import fr.igred.omero.Client;
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.FolderWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.PlateAcquisitionWrapper;
import fr.igred.omero.repository.PlateWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.omero.repository.ScreenWrapper;
import fr.igred.omero.repository.WellWrapper;
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
public abstract class AnnotationWrapper<T extends AnnotationData> extends ObjectWrapper<T> {


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
    public String getNameSpace() {
        return data.getNameSpace();
    }


    /**
     * Sets the name space of the underlying {@link AnnotationData} instance.
     *
     * @param name The value to set.
     */
    public void setNameSpace(String name) {
        data.setNameSpace(name);
    }


    /**
     * Returns the time when the annotation was last modified.
     *
     * @return See above.
     */
    public Timestamp getLastModified() {
        return data.getLastModified();
    }


    /**
     * Retrieves the {@link AnnotationData#getDescription() description} of the underlying {@link AnnotationData}
     * instance.
     *
     * @return See above
     */
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the underlying {@link AnnotationData} instance.
     *
     * @param description The description
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Returns the number of annotations links for this object.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public int countAnnotationLinks(Client client)
    throws ServiceException, AccessException {
        return client.findByQuery("select link.parent from ome.model.IAnnotationLink link " +
                                  "where link.child.id=" + getId()).size();
    }


    /**
     * Gets all projects with this annotation from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ProjectWrapper> getProjects(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os  = getLinks(client, ProjectWrapper.ANNOTATION_LINK);
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getProjects(ids);
    }


    /**
     * Gets all datasets with this annotation from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<DatasetWrapper> getDatasets(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os  = getLinks(client, DatasetWrapper.ANNOTATION_LINK);
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getDatasets(ids);
    }


    /**
     * Gets all images with this annotation from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImages(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os  = getLinks(client, ImageWrapper.ANNOTATION_LINK);
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getImages(ids);
    }


    /**
     * Gets all screens with this annotation from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ScreenWrapper> getScreens(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os  = getLinks(client, ScreenWrapper.ANNOTATION_LINK);
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getScreens(ids);
    }


    /**
     * Gets all plates with this annotation from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<PlateWrapper> getPlates(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os  = getLinks(client, PlateWrapper.ANNOTATION_LINK);
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getPlates(ids);
    }


    /**
     * Gets all plate acquisitions with this annotation from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public List<PlateAcquisitionWrapper> getPlateAcquisitions(Client client)
    throws ServiceException, AccessException {
        List<IObject> os = getLinks(client, PlateAcquisitionWrapper.ANNOTATION_LINK);
        return os.stream()
                 .map(o -> new PlateAcquisitionWrapper(new PlateAcquisitionData((omero.model.PlateAcquisition) o)))
                 .collect(Collectors.toList());
    }


    /**
     * Gets all wells with this annotation from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<WellWrapper> getWells(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os  = getLinks(client, WellWrapper.ANNOTATION_LINK);
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getWells(ids);
    }


    /**
     * Gets all folders with this annotation from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<FolderWrapper> getFolders(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> os  = getLinks(client, FolderWrapper.ANNOTATION_LINK);
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.loadFolders(ids);
    }


    /**
     * Retrieves all links of the given type.
     *
     * @param client   The client handling the connection.
     * @param linkType The link type.
     *
     * @return The list of linked objects.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    private List<IObject> getLinks(Client client, String linkType)
    throws ServiceException, AccessException {
        return client.findByQuery("select link.parent from " + linkType +
                                  " link where link.child = " + getId());
    }

}
