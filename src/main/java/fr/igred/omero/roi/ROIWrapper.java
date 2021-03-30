/*
 *  Copyright (C) 2020 GReD
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

package fr.igred.omero.roi;


import fr.igred.omero.Client;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.exception.OMEROServerError;
import omero.ServerError;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.Roi;

import java.util.ArrayList;
import java.util.List;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrServer;


/**
 * Class containing a ROIData
 * <p> Implements function using the ROIData contained
 */
public class ROIWrapper extends ObjectWrapper<ROIData> {

    /**
     * Constructor of the ROIWrapper class.
     */
    public ROIWrapper() {
        super(new ROIData());
    }


    /**
     * Constructor of the ROIWrapper class.
     *
     * @param shapes List of shapes to add to the ROIData.
     */
    public ROIWrapper(List<ShapeWrapper> shapes) {
        super(new ROIData());

        for (ShapeWrapper shape : shapes)
            addShape(shape);
    }


    /**
     * Constructor of the ROIWrapper class.
     *
     * @param data ROIData to be contained.
     */
    public ROIWrapper(ROIData data) {
        super(data);
    }


    public void setData(ROIData data) {
        this.data = data;
    }


    /**
     * Adds ShapeData objects from a list of ShapeWrapper to the ROIData
     *
     * @param shapes List of ShapeWrapper.
     */
    public void addShapes(List<ShapeWrapper> shapes) {
        for (ShapeWrapper shape : shapes)
            addShape(shape);
    }


    /**
     * Adds a ShapeData from a ShapeWrapper to the ROIData
     *
     * @param shape ShapeWrapper to add.
     */
    public void addShape(ShapeWrapper shape) {
        data.addShapeData(shape.getShape());
    }


    /**
     * Returns the list of shape contained in the ROIData
     *
     * @return list of shape contained in the ROIData.
     */
    public List<ShapeWrapper> getShapes() {
        List<ShapeWrapper> shapes = new ArrayList<>();
        for (ShapeData shape : data.getShapes()) {
            shapes.add(new ShapeWrapper(shape));
        }
        return shapes;
    }


    /**
     * Sets the image linked to the ROI.
     *
     * @param image Image linked to the ROIData.
     */
    public void setImage(ImageWrapper image) {
        data.setImage(image.getImage().asImage());
    }


    /**
     * Returns the ROIData contained.
     *
     * @return the {@link ROIData} contained.
     */
    public ROIData getROI() {
        return data;
    }


    /**
     * Deletes a ShapeData from the ROIData.
     *
     * @param shape ShapeData to delete.
     */
    public void deleteShape(ShapeData shape) {
        data.removeShapeData(shape);
    }


    /**
     * Deletes a ShapeData from the ROIData.
     *
     * @param pos Position of the ShapeData in the ShapeData list from the ROIData.
     *
     * @throws IndexOutOfBoundsException If pos is out of the ShapeData list bounds.
     */
    public void deleteShape(int pos) throws IndexOutOfBoundsException {
        data.removeShapeData(data.getShapes().get(pos));
    }


    /**
     * Saves the ROI.
     *
     * @param client The user.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws OMEROServerError Server error.
     */
    public void saveROI(Client client) throws OMEROServerError, ServiceException {
        try {
            Roi roi = (Roi) client.getGateway().getUpdateService(client.getCtx()).saveAndReturnObject(data.asIObject());
            data = new ROIData(roi);
        } catch (DSOutOfServiceException | ServerError e) {
            handleServiceOrServer(e, "Cannot save ROI");
        }
    }

}