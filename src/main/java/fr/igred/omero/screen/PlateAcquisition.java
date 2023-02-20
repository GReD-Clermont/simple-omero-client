/*
 *  Copyright (C) 2020-2023 GReD
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


import fr.igred.omero.HCSLinked;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.RepositoryObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.PlateAcquisitionData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.RemoteObject.flatten;


/**
 * Interface to handle Plate Acquisitions on OMERO.
 */
public interface PlateAcquisition extends RepositoryObject<PlateAcquisitionData>, HCSLinked<PlateAcquisitionData> {


    /**
     * Sets the name of the plate acquisition.
     *
     * @param name The name of the plate acquisition. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Sets the description of the plate acquisition.
     *
     * @param description The description of the plate acquisition.
     */
    void setDescription(String description);


    /**
     * Returns the label associated to the plate acquisition.
     *
     * @return See above.
     */
    String getLabel();


    /**
     * Returns the ID of the reference plate.
     *
     * @return See above.
     */
    long getRefPlateId();


    /**
     * Sets the ID of the plate this plate acquisition is for.
     *
     * @param refPlateId The value to set.
     */
    void setRefPlateId(long refPlateId);


    /**
     * Returns the time when the first image was collected.
     *
     * @return See above.
     */
    Timestamp getStartTime();


    /**
     * Returns the time when the last image was collected.
     *
     * @return See above.
     */
    Timestamp getEndTime();


    /**
     * Returns the maximum number of fields in any well.
     *
     * @return See above.
     */
    int getMaximumFieldCount();


    /**
     * Retrieves the screens containing the parent plates.
     *
     * @param browser The data browser.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    @Override
    default List<Screen> getScreens(Browser browser)
    throws ServiceException, AccessException, ExecutionException, ServerException {
        List<Plate>              plates  = getPlates(browser);
        Collection<List<Screen>> screens = new ArrayList<>(plates.size());
        for (Plate p : plates) {
            screens.add(p.getScreens(browser));
        }
        return flatten(screens);
    }


    /**
     * Returns the (updated) parent plate as a singleton list.
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
    default List<Plate> getPlates(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return browser.getPlates(getRefPlateId());
    }


    /**
     * Returns this plate acquisitions as a singleton list.
     *
     * @param browser The data browser (unused).
     *
     * @return See above.
     */
    @Override
    default List<PlateAcquisition> getPlateAcquisitions(Browser browser) {
        return Collections.singletonList(this);
    }


    /**
     * Retrieves the wells contained in the parent plate.
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
    default List<Well> getWells(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return getPlates(browser).iterator().next().getWells(browser);
    }


    /**
     * Retrieves the images contained in the wells in the parent plate.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default List<Image> getImages(Browser browser)
    throws ServiceException, AccessException, ExecutionException {
        return getWells(browser).stream()
                                .map(Well::getImages)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toMap(RemoteObject::getId, i -> i, (i1, i2) -> i1))
                                .values()
                                .stream()
                                .sorted(Comparator.comparing(RemoteObject::getId))
                                .collect(Collectors.toList());
    }

}
