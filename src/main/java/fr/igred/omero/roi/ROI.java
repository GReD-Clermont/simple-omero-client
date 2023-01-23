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

package fr.igred.omero.roi;


import fr.igred.omero.Client;
import fr.igred.omero.RemoteObject;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndServer;
import static java.util.stream.Collectors.groupingBy;


/**
 * Interface to handle ROIs on OMERO.
 */
public interface ROI extends RemoteObject<ROIData> {

    /**
     * Default IJ property to store ROI local IDs / indices.
     */
    String IJ_PROPERTY = "ROI";


    /**
     * Checks the provided property.
     *
     * @param property The property where 4D ROI local ID is stored.
     *
     * @return The property, or the default value {@link #IJ_PROPERTY} (= {@value IJ_PROPERTY}) if it is null or empty.
     */
    static String checkProperty(String property) {
        if (property == null || property.trim().isEmpty()) return IJ_PROPERTY;
        else return property;
    }


    /**
     * Returns ID property corresponding to input local ID property (appends "_ID" to said property).
     *
     * @param property The property where 4D ROI local ID is stored, defaults to {@value IJ_PROPERTY} if null or empty.
     *
     * @return See above.
     */
    static String ijIDProperty(String property) {
        property = checkProperty(property);
        return property + "_ID";
    }


    /**
     * Converts an OMERO list of ROIs to a list of ImageJ ROIs
     *
     * @param rois A list of OMERO ROIs.
     *
     * @return The converted list of ImageJ ROIs.
     */
    static List<ij.gui.Roi> toImageJ(Collection<? extends ROI> rois) {
        return toImageJ(rois, IJ_PROPERTY);
    }


    /**
     * Converts an OMERO list of ROIs to a list of ImageJ ROIs
     *
     * @param rois     A list of OMERO ROIs.
     * @param property The property used to store 4D ROI local IDs. Defaults to {@value IJ_PROPERTY} if null or empty.
     *
     * @return The converted list of ImageJ ROIs.
     */
    static List<ij.gui.Roi> toImageJ(Collection<? extends ROI> rois, String property) {
        property = checkProperty(property);
        final int maxGroups = 255;

        int nShapes = rois.stream().map(ROI::asDataObject).mapToInt(ROIData::getShapeCount).sum();

        List<ij.gui.Roi> ijRois = new ArrayList<>(nShapes);

        int index = 1;
        for (ROI roi : rois) {
            String name = roi.getName();
            if (name.trim().isEmpty()) {
                name = "SOC_INDEX_" + index;
            }
            List<ij.gui.Roi> shapes = roi.toImageJ(property);
            for (ij.gui.Roi r : shapes) {
                r.setProperty(property, name);
                if (rois.size() < maxGroups) {
                    r.setGroup(index);
                }
            }
            ijRois.addAll(shapes);
            index++;
        }
        return ijRois;
    }


    /**
     * Gets the ROI name.
     *
     * @return The ROI name (can be null).
     */
    default String getName() {
        RString name = ((_RoiOperationsNC) asIObject()).getName();
        return name != null ? name.getValue() : "";
    }


    /**
     * Sets the ROI name.
     *
     * @param name The ROI name.
     */
    default void setName(String name) {
        ((_RoiOperationsNC) asIObject()).setName(omero.rtypes.rstring(name));
    }


    /**
     * Replaces this ROI by the data provided.
     *
     * @param roi The ROI data.
     */
    void replace(ROIData roi);


    /**
     * Adds ShapeData objects from a list of Shape to the ROIData
     *
     * @param shapes List of Shape.
     */
    default void addShapes(Iterable<? extends Shape<?>> shapes) {
        shapes.forEach(this::addShape);
    }


    /**
     * Adds a ShapeData from a Shape to the ROIData
     *
     * @param shape Shape to add.
     */
    default void addShape(Shape<?> shape) {
        asDataObject().addShapeData(shape.asDataObject());
    }


    /**
     * Returns the list of shapes contained in the ROIData
     *
     * @return list of shape contained in the ROIData.
     */
    default ShapeList getShapes() {
        ShapeList shapes = new ShapeList();
        asDataObject().getShapes().stream().sorted(Comparator.comparing(ShapeData::getId)).forEach(shapes::add);
        return shapes;
    }


    /**
     * Sets the image linked to the ROI.
     *
     * @param image Image linked to the ROIData.
     */
    default void setImage(Image image) {
        asDataObject().setImage(image.asDataObject().asImage());
    }


    /**
     * Deletes a ShapeData from the ROIData.
     *
     * @param shape ShapeData to delete.
     */
    default void deleteShape(ShapeData shape) {
        asDataObject().removeShapeData(shape);
    }


    /**
     * Deletes a ShapeData from the ROIData.
     *
     * @param pos Position of the ShapeData in the ShapeData list from the ROIData.
     *
     * @throws IndexOutOfBoundsException If pos is out of the ShapeData list bounds.
     */
    default void deleteShape(int pos) {
        asDataObject().removeShapeData(asDataObject().getShapes().get(pos));
    }


    /**
     * Saves the ROI.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    default void saveROI(Client client) throws ServerException, ServiceException {
        String message = "Cannot save ROI";
        Roi roi = (Roi) handleServiceAndServer(client.getGateway(),
                                               g -> g.getUpdateService(client.getCtx())
                                                     .saveAndReturnObject(asIObject()),
                                               message);
        replace(new ROIData(roi));
    }


    /**
     * Returns the 5D bounds containing the ROI.
     *
     * @return The 5D bounds.
     */
    default Bounds getBounds() {
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
    default List<ij.gui.Roi> toImageJ() {
        return this.toImageJ(IJ_PROPERTY);
    }


    /**
     * Convert ROI to ImageJ list of ROIs.
     *
     * @param property The property where 4D ROI local ID will be stored.
     *
     * @return A list of ROIs.
     */
    default List<ij.gui.Roi> toImageJ(String property) {
        property = checkProperty(property);
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
            roi.setProperty(ijIDProperty(property), String.valueOf(getId()));
            rois.add(roi);
        }
        return rois;
    }

}
