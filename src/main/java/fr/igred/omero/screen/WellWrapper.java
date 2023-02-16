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


import fr.igred.omero.RepositoryObjectWrapper;
import fr.igred.omero.client.Browser;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.WellData;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static fr.igred.omero.util.Wrapper.wrap;


/**
 * Class containing a WellData object.
 * <p> Wraps function calls to the WellData contained.
 */
public class WellWrapper extends RepositoryObjectWrapper<WellData> implements Well {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "WellAnnotationLink";


    /**
     * Constructor of the class WellWrapper.
     *
     * @param well The WellData contained in the WellWrapper.
     */
    public WellWrapper(WellData well) {
        super(well);
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
     * Gets the object description
     *
     * @return See above.
     */
    @Override
    public String getDescription() {
        return data.getExternalDescription();
    }


    /**
     * Returns the well samples linked to the well.
     *
     * @return See above.
     */
    @Override
    public List<WellSample> getWellSamples() {
        return wrap(data.getWellSamples(), WellSampleWrapper::new, w -> w.getImage().asDataObject().getSeries());
    }


    /**
     * Returns the plate containing this Well.
     *
     * @return See above.
     */
    @Override
    public Plate getPlate() {
        return new PlateWrapper(data.getPlate());
    }


    /**
     * Returns the column used to indicate the location of the well on the grid.
     *
     * @return See above.
     */
    @Override
    public Integer getColumn() {
        return data.getColumn();
    }


    /**
     * Returns the row used to indicate the location of the well on the grid.
     *
     * @return See above.
     */
    @Override
    public Integer getRow() {
        return data.getRow();
    }


    /**
     * Returns the status of the well.
     *
     * @return See above.
     */
    @Override
    public String getStatus() {
        return data.getStatus();
    }


    /**
     * Sets the status of the well.
     *
     * @param status The status of the well.
     */
    @Override
    public void setStatus(String status) {
        data.setStatus(status);
    }


    /**
     * Returns a human-readable identifier for the screening status e.g. empty, positive control, etc.
     *
     * @return See above.
     */
    @Override
    public String getWellType() {
        return data.getWellType();
    }


    /**
     * Sets a human-readable identifier for the screening status e.g. empty, positive control, etc.
     *
     * @param type The value to set.
     */
    @Override
    public void setWellType(String type) {
        data.setWellType(type);
    }


    /**
     * Returns the red component of the color associated to the well, or {@code -1}.
     *
     * @return See above.
     */
    @Override
    public int getRed() {
        return data.getRed();
    }


    /**
     * Sets the red component of the color associated to the well.
     *
     * @param red The value to set.
     */
    @Override
    public void setRed(Integer red) {
        data.setRed(red);
    }


    /**
     * Returns the green component of the color associated to the well, or {@code -1}.
     *
     * @return See above.
     */
    @Override
    public int getGreen() {
        return data.getGreen();
    }


    /**
     * Sets the green component of the color associated to the well.
     *
     * @param green The value to set.
     */
    @Override
    public void setGreen(Integer green) {
        data.setGreen(green);
    }


    /**
     * Returns the blue component of the color associated to the well, or {@code -1}.
     *
     * @return See above.
     */
    @Override
    public int getBlue() {
        return data.getBlue();
    }


    /**
     * Sets the blue component of the color associated to the well.
     *
     * @param blue The value to set.
     */
    @Override
    public void setBlue(Integer blue) {
        data.setBlue(blue);
    }


    /**
     * Returns the alpha component of the color associated to the well, or {@code -1}.
     *
     * @return See above.
     */
    @Override
    public int getAlpha() {
        return data.getAlpha();
    }


    /**
     * Sets the alpha component of the color associated to the well.
     *
     * @param alpha The value to set.
     */
    @Override
    public void setAlpha(Integer alpha) {
        data.setAlpha(alpha);
    }


    /**
     * Retrieves the screens containing this well.
     *
     * @param browser The data browser.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     * @throws ServerException    Server error.
     */
    @Override
    public List<Screen> getScreens(Browser browser)
    throws AccessException, ServiceException, ExecutionException, ServerException {
        return getPlate().getScreens(browser);
    }


    /**
     * Returns the plate containing this well.
     *
     * @param browser The data browser.
     *
     * @return See above.
     */
    @Override
    public List<Plate> getPlates(Browser browser) throws AccessException, ServiceException, ExecutionException {
        return browser.getPlates(getPlate().getId());
    }


    /**
     * Returns the plate acquisitions linked to the plate containing this well.
     *
     * @param browser The data browser.
     *
     * @return See above.
     */
    @Override
    public List<PlateAcquisition> getPlateAcquisitions(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        return getPlate().getPlateAcquisitions(browser);
    }


    /**
     * Retrieves the wells in this well.
     *
     * @param browser The data browser (unused).
     *
     * @return See above.
     */
    @Override
    public List<Well> getWells(Browser browser) {
        return Collections.singletonList(this);
    }


    /**
     * Retrieves the images contained in this well.
     *
     * @return See above.
     */
    @Override
    public List<Image> getImages() {
        return getWellSamples().stream().map(WellSample::getImage).collect(Collectors.toList());
    }


    /**
     * Retrieves the images contained in this well.
     *
     * @param browser The data browser.
     *
     * @return See above.
     */
    @Override
    public List<Image> getImages(Browser browser) throws AccessException, ServiceException, ExecutionException {
        return browser.getWell(getId()).getImages();
    }

}
