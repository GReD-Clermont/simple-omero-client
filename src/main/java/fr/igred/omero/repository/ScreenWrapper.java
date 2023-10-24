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

package fr.igred.omero.repository;


import fr.igred.omero.Browser;
import fr.igred.omero.Client;
import fr.igred.omero.GatewayWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.ScreenData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;


/**
 * Class containing a ScreenData object.
 * <p> Wraps function calls to the ScreenData contained.
 */
public class ScreenWrapper extends GenericRepositoryObjectWrapper<ScreenData> {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "ScreenAnnotationLink";


    /**
     * Constructor of the ScreenWrapper class. Creates a new screen and saves it to OMERO.
     *
     * @param client      The client handling the connection.
     * @param name        Screen name.
     * @param description Screen description.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public ScreenWrapper(Client client, String name, String description)
    throws ServiceException, AccessException, ExecutionException {
        super(new ScreenData());
        data.setName(name);
        data.setDescription(description);
        super.saveAndUpdate(client);
    }


    /**
     * Constructor of the class ScreenWrapper.
     *
     * @param screen The ScreenData contained in the ScreenWrapper.
     */
    public ScreenWrapper(ScreenData screen) {
        super(screen);
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
     * Gets the screen name.
     *
     * @return See above.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the screen.
     *
     * @param name The name of the screen. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * @return See above.
     *
     * @deprecated Returns the ScreenData contained. Use {@link #asDataObject()} instead.
     */
    @Deprecated
    public ScreenData asScreenData() {
        return data;
    }


    /**
     * Gets the screen description
     *
     * @return See above.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the screen.
     *
     * @param description The description of the screen.
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Returns the plates contained in this screen.
     *
     * @return See above.
     */
    public List<PlateWrapper> getPlates() {
        return wrap(data.getPlates(), PlateWrapper::new);
    }


    /**
     * Returns the plates contained in this screen, with the specified name.
     *
     * @param name The expected plates name.
     *
     * @return See above.
     */
    public List<PlateWrapper> getPlates(String name) {
        List<PlateWrapper> plates = getPlates();
        plates.removeIf(plate -> !plate.getName().equals(name));
        return plates;
    }


    /**
     * Returns the plate acquisitions linked to this object, either directly, or through parents/children.
     *
     * @param client The client handling the connection.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<PlateAcquisitionWrapper> getPlateAcquisitions(Client client)
    throws ServiceException, AccessException, ExecutionException {
        reload(client);
        return getPlates().stream()
                          .map(PlateWrapper::getPlateAcquisitions)
                          .flatMap(Collection::stream)
                          .collect(toMap(GenericRepositoryObjectWrapper::getId,
                                         p -> p, (p1, p2) -> p1))
                          .values()
                          .stream()
                          .sorted(comparing(GenericRepositoryObjectWrapper::getId))
                          .collect(Collectors.toList());
    }


    /**
     * Retrieves the wells linked to this object, either directly, or through parents/children.
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
        List<PlateWrapper>            plates = getPlates();
        Collection<List<WellWrapper>> wells  = new ArrayList<>(plates.size());
        for (PlateWrapper p : plates) {
            wells.add(p.getWells(client));
        }
        return flatten(wells);
    }


    /**
     * Retrieves the images contained in this screen.
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImages(Client client)
    throws ServiceException, AccessException, ExecutionException {
        List<PlateWrapper>             plates = getPlates();
        Collection<List<ImageWrapper>> images = new ArrayList<>(plates.size());
        for (PlateWrapper p : plates) {
            images.add(p.getImages(client));
        }
        return flatten(images);
    }


    /**
     * Returns the description of the protocol.
     *
     * @return See above.
     */
    public String getProtocolDescription() {
        return data.getProtocolDescription();
    }


    /**
     * Sets the description of the protocol.
     *
     * @param value The value to set.
     */
    public void setProtocolDescription(String value) {
        data.setProtocolDescription(value);
    }


    /**
     * Returns the identifier of the protocol.
     *
     * @return See above.
     */
    public String getProtocolIdentifier() {
        return data.getProtocolIdentifier();
    }


    /**
     * Sets the identifier of the protocol.
     *
     * @param value The value to set.
     */
    public void setProtocolIdentifier(String value) {
        data.setProtocolIdentifier(value);
    }


    /**
     * Returns the description of the reagent set.
     *
     * @return See above.
     */
    public String getReagentSetDescription() {
        return data.getReagentSetDescripion();
    }


    /**
     * Sets the identifier of the reagent.
     *
     * @param value The value to set.
     */
    public void setReagentSetDescription(String value) {
        data.setReagentSetDescripion(value);
    }


    /**
     * Returns the identifier of the Reagent set.
     *
     * @return See above.
     */
    public String getReagentSetIdentifier() {
        return data.getReagentSetIdentifier();
    }


    /**
     * Sets the identifier of the reagent.
     *
     * @param value The value to set.
     */
    public void setReagentSetIdentifier(String value) {
        data.setReagentSetIdentifier(value);
    }


    /**
     * Reloads the screen from OMERO.
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
                    bf -> bf.getScreens(browser.getCtx(),
                                        singletonList(getId()))
                            .iterator()
                            .next(),
                    "Cannot reload " + this);
    }


    /**
     * Imports all images candidates in the paths to the screen in OMERO.
     *
     * @param client The client handling the connection.
     * @param paths  Paths to the image files on the computer.
     *
     * @return If the import did not exit because of an error.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot read file.
     */
    public boolean importImages(GatewayWrapper client, String... paths)
    throws ServiceException, AccessException, IOException {
        return importImages(client, 1, paths);
    }


    /**
     * Imports all images candidates in the paths to the screen in OMERO.
     *
     * @param client  The client handling the connection.
     * @param threads The number of threads (same value used for filesets and uploads).
     * @param paths   Paths to the image files on the computer.
     *
     * @return If the import did not exit because of an error.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot read file.
     */
    public boolean importImages(GatewayWrapper client, int threads, String... paths)
    throws ServiceException, AccessException, IOException {
        return importImages(client, data, threads, paths);
    }


    /**
     * Imports one image file to the screen in OMERO.
     *
     * @param client The client handling the connection.
     * @param path   Path to the image file on the computer.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot read file.
     */
    public List<Long> importImage(GatewayWrapper client, String path)
    throws ServiceException, AccessException, IOException {
        return importImage(client, data, path);
    }

}
