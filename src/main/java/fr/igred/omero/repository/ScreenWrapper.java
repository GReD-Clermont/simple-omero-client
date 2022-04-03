/*
 *  Copyright (C) 2020-2022 GReD
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
package fr.igred.omero.repository;


import omero.gateway.model.ScreenData;

import java.util.List;


public class ScreenWrapper extends GenericRepositoryObjectWrapper<ScreenData> {

    public static final String ANNOTATION_LINK = "ScreenAnnotationLink";


    /**
     * Constructor of the class ScreenWrapper.
     *
     * @param screen The ScreenData contained in the ScreenWrapper.
     */
    public ScreenWrapper(ScreenData screen) {
        super(screen);
    }


    /**
     * Returns the type of annotation link for this object
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
     * @return the ScreenData contained.
     */
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

}
