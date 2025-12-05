/*
 *  Copyright (C) 2020-2025 GReD
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
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.screen.PlateAcquisition;
import fr.igred.omero.screen.PlateAcquisitionWrapper;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.PlateAcquisitionData;
import omero.model.IObject;

import java.sql.Timestamp;
import java.util.List;
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

}
