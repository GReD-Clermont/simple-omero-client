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

package fr.igred.omero.screen;


import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.RepositoryObjectWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ome.model.units.BigResult;
import omero.RLong;
import omero.gateway.model.PlateData;
import omero.gateway.model.WellData;
import omero.model.IObject;
import omero.model.Length;
import omero.model.enums.UnitsLength;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;


/**
 * Class containing a PlateData object.
 * <p> Wraps function calls to the PlateData contained.
 */
public class PlateWrapper extends RepositoryObjectWrapper<PlateData> {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "PlateAnnotationLink";


    /**
     * Constructor of the class PlateWrapper.
     *
     * @param plate The plate contained in the PlateWrapper.
     */
    public PlateWrapper(PlateData plate) {
        super(plate);
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
     * Gets the plate name.
     *
     * @return See above.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the plate.
     *
     * @param name The name of the plate. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Gets the plate description
     *
     * @return See above.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the plate.
     *
     * @param description The description of the plate.
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Retrieves the screens containing this plate.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ScreenWrapper> getScreens(Browser client)
    throws ServiceException, AccessException, ExecutionException {
        String query = "select link.parent from ScreenPlateLink as link" +
                       " where link.child=" + getId();
        List<IObject> os = client.findByQuery(query);
        return client.getScreens(os.stream()
                                   .map(IObject::getId)
                                   .map(RLong::getValue)
                                   .distinct()
                                   .toArray(Long[]::new));
    }


    /**
     * Returns the plate acquisitions related to this plate.
     *
     * @return See above.
     */
    public List<PlateAcquisitionWrapper> getPlateAcquisitions() {
        return wrap(data.getPlateAcquisitions(), PlateAcquisitionWrapper::new);
    }


    /**
     * Gets all wells in the plate available from OMERO.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<WellWrapper> getWells(Browser client)
    throws ServiceException, AccessException, ExecutionException {
        Collection<WellData> wells = call(client.getBrowseFacility(),
                                          bf -> bf.getWells(client.getCtx(),
                                                            data.getId()),
                                          "Cannot get wells from " + this);

        return wells.stream()
                    .map(WellWrapper::new)
                    .sorted(Comparator.comparing(WellWrapper::getRow)
                                      .thenComparing(WellWrapper::getColumn))
                    .collect(Collectors.toList());
    }


    /**
     * Returns the images contained in the wells of this plate.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImages(Browser client)
    throws ServiceException, AccessException, ExecutionException {
        return getWells(client).stream()
                               .map(WellWrapper::getImages)
                               .flatMap(Collection::stream)
                               .collect(toMap(ObjectWrapper::getId,
                                              i -> i, (i1, i2) -> i1))
                               .values()
                               .stream()
                               .sorted(Comparator.comparing(ObjectWrapper::getId))
                               .collect(Collectors.toList());
    }


    /**
     * Returns the index indicating how to label a column.
     *
     * @return See above.
     */
    public int getColumnSequenceIndex() {
        return data.getColumnSequenceIndex();
    }


    /**
     * Returns the index indicating how to label a row.
     *
     * @return See above.
     */
    public int getRowSequenceIndex() {
        return data.getRowSequenceIndex();
    }


    /**
     * Returns the currently selected field or {@code 0}.
     *
     * @return See above.
     */
    public int getDefaultSample() {
        return data.getDefaultSample();
    }


    /**
     * Sets the default sample.
     *
     * @param value The value to set.
     */
    public void setDefaultSample(int value) {
        data.setDefaultSample(value);
    }


    /**
     * Returns the status of the plate.
     *
     * @return See above.
     */
    public String getStatus() {
        return data.getStatus();
    }


    /**
     * Sets the status.
     *
     * @param value The value to set.
     */
    public void setStatus(String value) {
        data.setStatus(value);
    }


    /**
     * Returns the external identifier of the plate.
     *
     * @return See above.
     */
    public String getExternalIdentifier() {
        return data.getExternalIdentifier();
    }


    /**
     * Sets the external identifier.
     *
     * @param value The value to set.
     */
    public void setExternalIdentifier(String value) {
        data.setExternalIdentifier(value);
    }


    /**
     * Returns the type of plate e.g. A 384-Well Plate, 96-Well Plate.
     *
     * @return See above.
     */
    public String getPlateType() {
        return data.getPlateType();
    }


    /**
     * Returns the x-coordinate in 2D-space of the well.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getWellOriginX(UnitsLength unit) throws BigResult {
        return data.getWellOriginX(unit);
    }


    /**
     * Returns the y-coordinate in 2D-space of the well.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getWellOriginY(UnitsLength unit) throws BigResult {
        return data.getWellOriginY(unit);
    }


    /**
     * Reloads the plate from OMERO.
     *
     * @param browser The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void reload(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        data = call(browser.getBrowseFacility(),
                    bf -> bf.getPlates(browser.getCtx(), singletonList(getId()))
                            .iterator()
                            .next(),
                    "Cannot reload " + this);
    }

}
