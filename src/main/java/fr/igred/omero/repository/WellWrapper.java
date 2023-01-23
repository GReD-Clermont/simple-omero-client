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

package fr.igred.omero.repository;


import omero.gateway.model.WellData;

import java.util.List;


/**
 * Class containing a WellData object.
 * <p> Wraps function calls to the WellData contained.
 */
public class WellWrapper extends RepositoryObjectWrapper<WellData> implements Well {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "WellAnnotationLink";


    /**
     * Constructor of the class Well.
     *
     * @param dataObject The WellData contained in the Well.
     */
    public WellWrapper(WellData dataObject) {
        super(dataObject);
    }


    /**
     * Converts a number to a suit of letters (e.g. 1 to A or 28 to AB).
     *
     * @param number The number.
     *
     * @return The corresponding identifier
     */
    private static String identifier(int number) {
        final int     alphabetSize = 26;
        final int     charOffset   = 64; // 'A' - 1
        int           temp;
        StringBuilder letters      = new StringBuilder(3);
        while (number > 0) {
            temp = number % alphabetSize;
            letters.append((char) (temp + charOffset));
            number = (number - temp) / alphabetSize;
        }
        return letters.toString();
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
     * Gets the object name.
     *
     * @return See above.
     */
    @Override
    public String getName() {
        return String.format("Well %s-%d", identifier(getRow() + 1), getColumn() + 1);
    }


    /**
     * Returns the well samples linked to the well.
     *
     * @return See above.
     */
    @Override
    public List<WellSample> getWellSamples() {
        return wrap(asDataObject().getWellSamples(), WellSampleWrapper::new,
                    w -> w.getImage().asDataObject().getSeries());
    }


    /**
     * Returns the plate containing this Well.
     *
     * @return See above.
     */
    @Override
    public Plate getPlate() {
        return new PlateWrapper(asDataObject().getPlate());
    }

}
