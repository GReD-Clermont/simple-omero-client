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

package fr.igred.omero.metadata;


import fr.igred.omero.Client;
import fr.igred.omero.ImageContainer;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.exception.ServerError;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.Roi;

import java.util.ArrayList;
import java.util.List;


/**
 * Class containing a ROIData
 * <p> Implements function using the ROIData contained
 */
public class ROIContainer {

    /** ROI contained in the ROIContainer */
    ROIData data;


    /**
     * Constructor of the ROIContainer class.
     */
    public ROIContainer() {
        data = new ROIData();
    }


    /**
     * Constructor of the ROIContainer class.
     *
     * @param shapes List of shapes to add to the ROIData.
     */
    public ROIContainer(List<ShapeContainer> shapes) {
        data = new ROIData();

        for (ShapeContainer shape : shapes)
            addShape(shape);
    }


    /**
     * Constructor of the ROIContainer class.
     *
     * @param data ROIData to be contained.
     */
    public ROIContainer(ROIData data) {
        this.data = data;
    }


    /**
     * Gets the ROIData id.
     *
     * @return the {@link ROIData} ID.
     */
    public Long getId() {
        return data.getId();
    }


    public void setData(ROIData data) {
        this.data = data;
    }


    /**
     * Adds ShapeData objects from a list of ShapeContainer to the ROIData
     *
     * @param shapes List of ShapeContainer.
     */
    public void addShapes(List<ShapeContainer> shapes) {
        for (ShapeContainer shape : shapes)
            addShape(shape);
    }


    /**
     * Adds a ShapeData from a ShapeContainer to the ROIData
     *
     * @param shape ShapeContainer to add.
     */
    public void addShape(ShapeContainer shape) {
        data.addShapeData(shape.getShape());
    }


    /**
     * Returns the list of shape contained in the ROIData
     *
     * @return list of shape contained in the ROIData.
     */
    public List<ShapeContainer> getShapes() {
        List<ShapeContainer> shapes = new ArrayList<>();
        for (ShapeData shape : data.getShapes()) {
            shapes.add(new ShapeContainer(shape));
        }
        return shapes;
    }


    /**
     * Sets the image linked to the ROI.
     *
     * @param image Image linked to the ROIData.
     */
    public void setImage(ImageContainer image) {
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
     * @throws ServerError      Server error.
     */
    public void saveROI(Client client) throws ServerError, ServiceException {
        try {
            Roi roi = (Roi) client.getGateway().getUpdateService(client.getCtx()).saveAndReturnObject(data.asIObject());
            data = new ROIData(roi);
        } catch (DSOutOfServiceException oos) {
            throw new ServiceException("Cannot connect to OMERO", oos, oos.getConnectionStatus());
        } catch (omero.ServerError se) {
            throw new ServerError("Server error", se);
        }
    }

}