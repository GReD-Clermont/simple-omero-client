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

package fr.igred.omero.roi;


import fr.igred.omero.ConnectionHandler;
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.Image;
import fr.igred.omero.util.Bounds;
import fr.igred.omero.util.Coordinates;
import ij.gui.ShapeRoi;
import omero.RString;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.Roi;
import omero.model._RoiOperationsNC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndServer;
import static java.util.stream.Collectors.groupingBy;


/**
 * Class containing a ROIData object.
 * <p> Wraps function calls to the ROIData contained.
 */
public class ROIWrapper extends ObjectWrapper<ROIData> implements ROI {


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
    public ROIWrapper(Iterable<? extends Shape<?>> shapes) {
        super(new ROIData());

        for (Shape<?> shape : shapes) {
            data.addShapeData(shape.asDataObject());
        }
    }


    /**
     * Constructor of the ROIWrapper class.
     *
     * @param data ROIData to be contained.
     */
    public ROIWrapper(ROIData data) {
        super(data);
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs.
     *
     * @param ijRois A list of ImageJ ROIs.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROI> fromImageJ(List<? extends ij.gui.Roi> ijRois) {
        return ROI.fromImageJ(ijRois, IJ_PROPERTY, ROIWrapper::new);
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs.
     *
     * @param ijRois   A list of ImageJ ROIs.
     * @param property The property where 4D ROI local ID is stored. Defaults to {@value IJ_PROPERTY} if null or empty.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROI> fromImageJ(List<? extends ij.gui.Roi> ijRois, String property) {
        return ROI.fromImageJ(ijRois, property, ROIWrapper::new);
    }


    /**
     * Gets the ROI name.
     *
     * @return The ROI name (can be null).
     */
    @Override
    public String getName() {
        RString name = ((_RoiOperationsNC) data.asIObject()).getName();
        return name != null ? name.getValue() : "";
    }


    /**
     * Sets the ROI name.
     *
     * @param name The ROI name.
     */
    @Override
    public void setName(String name) {
        ((_RoiOperationsNC) data.asIObject()).setName(omero.rtypes.rstring(name));
    }


    /**
     * Adds ShapeData objects from a list of Shapes to the ROIData
     *
     * @param shapes List of Shapes.
     */
    @Override
    public void addShapes(Iterable<? extends Shape<?>> shapes) {
        shapes.forEach(this::addShape);
    }


    /**
     * Adds a ShapeData from a Shape to the ROIData
     *
     * @param shape Shape to add.
     */
    @Override
    public void addShape(Shape<?> shape) {
        data.addShapeData(shape.asDataObject());
    }


    /**
     * Returns the list of shapes contained in the ROIData
     *
     * @return list of shape contained in the ROIData.
     */
    @Override
    public ShapeList getShapes() {
        ShapeList shapes = new ShapeList();
        data.getShapes().stream().sorted(Comparator.comparing(ShapeData::getId)).forEach(shapes::add);
        return shapes;
    }


    /**
     * Sets the image linked to the ROI.
     *
     * @param image Image linked to the ROIData.
     */
    @Override
    public void setImage(Image image) {
        data.setImage(image.asDataObject().asImage());
    }


    /**
     * Deletes a ShapeData from the ROIData.
     *
     * @param shape ShapeData to delete.
     */
    @Override
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
    @Override
    public void deleteShape(int pos) {
        data.removeShapeData(data.getShapes().get(pos));
    }


    /**
     * Saves the ROI.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    @Override
    public void saveROI(ConnectionHandler client) throws ServerException, ServiceException {
        String message = "Cannot save ROI";
        Roi roi = (Roi) handleServiceAndServer(client.getGateway(),
                                               g -> g.getUpdateService(client.getCtx())
                                                     .saveAndReturnObject(data.asIObject()),
                                               message);
        data = new ROIData(roi);
    }


    /**
     * Returns the 5D bounds containing the ROI.
     *
     * @return The 5D bounds.
     */
    @Override
    public Bounds getBounds() {
        int[] x = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] y = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] c = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] z = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] t = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (Shape<?> shape : getShapes()) {
            Rectangle box = shape.getBoundingBox();
            x[0] = Math.min(x[0], (int) box.getX());
            y[0] = Math.min(y[0], (int) box.getY());
            c[0] = Math.min(c[0], box.getC());
            z[0] = Math.min(z[0], box.getZ());
            t[0] = Math.min(t[0], box.getT());
            x[1] = Math.max(x[1], (int) (box.getX() + box.getWidth() - 1));
            y[1] = Math.max(y[1], (int) (box.getY() + box.getHeight() - 1));
            c[1] = Math.max(c[1], box.getC());
            z[1] = Math.max(z[1], box.getZ());
            t[1] = Math.max(t[1], box.getT());
        }
        Coordinates start = new Coordinates(x[0], y[0], c[0], z[0], t[0]);
        Coordinates end   = new Coordinates(x[1], y[1], c[1], z[1], t[1]);
        return new Bounds(start, end);
    }


    /**
     * Convert ROI to ImageJ list of ROIs.
     *
     * @return A list of ROIs.
     */
    @Override
    public List<ij.gui.Roi> toImageJ() {
        return this.toImageJ(IJ_PROPERTY);
    }


    /**
     * Convert ROI to ImageJ list of ROIs.
     *
     * @param property The property where 4D ROI local ID will be stored.
     *
     * @return A list of ROIs.
     */
    @Override
    public List<ij.gui.Roi> toImageJ(String property) {
        property = ROI.checkProperty(property);
        ShapeList shapes = getShapes();

        Map<String, List<Shape<?>>> sameSlice = shapes.stream()
                                                      .collect(groupingBy(Shape::getCZT,
                                                                          LinkedHashMap::new,
                                                                          Collectors.toList()));
        sameSlice.values().removeIf(List::isEmpty);
        List<ij.gui.Roi> rois = new ArrayList<>(shapes.size());
        for (List<Shape<?>> slice : sameSlice.values()) {
            Shape<?> shape = slice.iterator().next();

            ij.gui.Roi roi = shape.toImageJ();
            String     txt = shape.getText();
            if (slice.size() > 1) {
                ij.gui.Roi xor = slice.stream()
                                      .map(Shape::toImageJ)
                                      .map(ShapeRoi::new)
                                      .reduce(ShapeRoi::xor)
                                      .map(ij.gui.Roi.class::cast)
                                      .orElse(roi);
                xor.setStrokeColor(roi.getStrokeColor());
                xor.setPosition(roi.getCPosition(), roi.getZPosition(), roi.getTPosition());
                roi = xor;
            }
            if (txt.isEmpty()) {
                roi.setName(String.format("%d-%d", getId(), shape.getId()));
            } else {
                roi.setName(txt);
            }
            roi.setProperty(ROI.ijIDProperty(property), String.valueOf(getId()));
            rois.add(roi);
        }
        return rois;
    }

}