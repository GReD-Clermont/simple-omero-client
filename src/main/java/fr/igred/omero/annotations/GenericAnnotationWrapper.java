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

package fr.igred.omero.annotations;


import fr.igred.omero.Client;
import fr.igred.omero.GenericObjectWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import omero.gateway.model.AnnotationData;
import omero.model.IObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Generic class containing an AnnotationData (or a subclass) object.
 *
 * @param <T> Subclass of {@link AnnotationData}
 */
public abstract class GenericAnnotationWrapper<T extends AnnotationData> extends GenericObjectWrapper<T> {


    /**
     * Constructor of the GenericAnnotationWrapper class.
     *
     * @param annotation Annotation to be contained.
     */
    protected GenericAnnotationWrapper(T annotation) {
        super(annotation);
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
     * Gets all projects with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return ProjectWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ProjectWrapper> getProjects(Client client)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
        List<IObject> os = getLinks(client, "ProjectAnnotationLink");

        List<ProjectWrapper> selected = new ArrayList<>(os.size());
        for (IObject o : os) {
            selected.add(client.getProject(o.getId().getValue()));
        }
        selected.sort(new SortById<>());

        return selected;
    }


    /**
     * Gets all datasets with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return DatasetWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<DatasetWrapper> getDatasets(Client client)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
        List<IObject> os = getLinks(client, "DatasetAnnotationLink");

        List<DatasetWrapper> selected = new ArrayList<>(os.size());
        for (IObject o : os) {
            selected.add(client.getDataset(o.getId().getValue()));
        }
        selected.sort(new SortById<>());

        return selected;
    }


    /**
     * Gets all images with this tag from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return ImageWrapper list.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws OMEROServerError   Server error.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImages(Client client)
    throws ServiceException, AccessException, OMEROServerError, ExecutionException {
        List<IObject> os = getLinks(client, "ImageAnnotationLink");

        List<ImageWrapper> selected = new ArrayList<>(os.size());
        for (IObject o : os) {
            selected.add(client.getImage(o.getId().getValue()));
        }
        selected.sort(new SortById<>());

        return selected;
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
     * @throws OMEROServerError Server error.
     */
    private List<IObject> getLinks(Client client, String linkType)
    throws ServiceException, OMEROServerError {
        return client.findByQuery("select link.parent from " + linkType +
                                  " link where link.child = " + getId());
    }

}
