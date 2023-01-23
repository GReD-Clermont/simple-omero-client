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

package fr.igred.omero.annotations;


import fr.igred.omero.Client;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.Dataset;
import fr.igred.omero.repository.Image;
import fr.igred.omero.repository.Plate;
import fr.igred.omero.repository.Project;
import fr.igred.omero.repository.Screen;
import fr.igred.omero.repository.Well;
import omero.RLong;
import omero.gateway.model.AnnotationData;
import omero.model.IObject;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Generic class containing an AnnotationData (or a subclass) object.
 *
 * @param <T> Subclass of {@link AnnotationData}
 */
public abstract class Annotation<T extends AnnotationData> extends RemoteObject<T> {


    /**
     * Constructor of the Annotation class.
     *
     * @param dataObject Annotation to be contained.
     */
    protected Annotation(T dataObject) {
        super(dataObject);
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
     * @throws ServerException  Server error.
     */
    public int countAnnotationLinks(Client client) throws ServiceException, ServerException {
        return client.findByQuery("select link.parent from ome.model.IAnnotationLink link " +
                                  "where link.child.id=" + getId()).size();
    }


    /**
     * Gets all projects with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Project list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Project> getProjects(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        List<IObject> os  = getLinks(client, linkType(Project.class, Annotation.class));
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getProjects(ids);
    }


    /**
     * Gets all datasets with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Dataset list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Dataset> getDatasets(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        List<IObject> os  = getLinks(client, linkType(Dataset.class, Annotation.class));
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getDatasets(ids);
    }


    /**
     * Gets all images with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Image list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Image> getImages(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        List<IObject> os  = getLinks(client, linkType(Image.class, Annotation.class));
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getImages(ids);
    }


    /**
     * Gets all screens with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Screen list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Screen> getScreens(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        List<IObject> os  = getLinks(client, linkType(Screen.class, Annotation.class));
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getScreens(ids);
    }


    /**
     * Gets all plates with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Plate list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Plate> getPlates(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        List<IObject> os  = getLinks(client, linkType(Plate.class, Annotation.class));
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getPlates(ids);
    }


    /**
     * Gets all wells with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return Well list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ServerException    Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<Well> getWells(Client client)
    throws ServiceException, AccessException, ServerException, ExecutionException {
        List<IObject> os  = getLinks(client, linkType(Well.class, Annotation.class));
        Long[]        ids = os.stream().map(IObject::getId).map(RLong::getValue).sorted().toArray(Long[]::new);
        return client.getWells(ids);
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
     * @throws ServerException  Server error.
     */
    private List<IObject> getLinks(Client client, String linkType)
    throws ServiceException, ServerException {
        return client.findByQuery("select link.parent from " + linkType +
                                  " link where link.child = " + getId());
    }

}
