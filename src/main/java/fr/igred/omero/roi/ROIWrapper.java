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


import fr.igred.omero.client.Client;
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.core.PixelsWrapper.Bounds;
import fr.igred.omero.core.PixelsWrapper.Coordinates;
import ij.gui.ShapeRoi;
import omero.RString;
import omero.ServerError;
import omero.gateway.exception.DSOutOfServiceException;
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
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrServer;
import static java.util.stream.Collectors.groupingBy;


/**
 * Class containing a ROIData object.
 * <p> Wraps function calls to the ROIData contained.
 */
public class ROIWrapper extends ObjectWrapper<ROIData> {

    /**
     * Default IJ property to store ROI local labels / indices.
     */
    public static final String IJ_PROPERTY = "ROI";


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
    public ROIWrapper(Iterable<? extends ShapeWrapper<?>> shapes) {
        super(new ROIData());

        for (ShapeWrapper<?> shape : shapes) {
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
     * Checks the provided property.
     *
     * @param property The property where the 4D ROI local index/label is stored.
     *
     * @return The property, or the default value {@link #IJ_PROPERTY} (= {@value IJ_PROPERTY}) if it is null or empty.
     */
    public static String checkProperty(String property) {
        if (property == null || property.trim().isEmpty()) return IJ_PROPERTY;
        else return property;
    }


    /**
     * Returns the ID property corresponding to the input local index/label property (appends "_ID" to said property).
     *
     * @param property The property where the 4D ROI local index/label is stored. Defaults to {@value IJ_PROPERTY} if
     *                 null or empty.
     *
     * @return See above.
     */
    public static String ijIDProperty(String property) {
        property = checkProperty(property);
        return property + "_ID";
    }


    /**
     * Returns the ID property corresponding to the input local index/label property (appends "_NAME" to said
     * property).
     *
     * @param property The property where the 4D ROI local index/label is stored. Defaults to {@value IJ_PROPERTY} if
     *                 null or empty.
     *
     * @return See above.
     */
    static String ijNameProperty(String property) {
        property = checkProperty(property);
        return property + "_NAME";
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs
     *
     * @param ijRois A list of ImageJ ROIs.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROIWrapper> fromImageJ(List<? extends ij.gui.Roi> ijRois) {
        return fromImageJ(ijRois, IJ_PROPERTY);
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs
     *
     * @param ijRois   A list of ImageJ ROIs.
     * @param property The property used to store the 4D ROI local index/label. Defaults to {@value IJ_PROPERTY} if null
     *                 or empty.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROIWrapper> fromImageJ(List<? extends ij.gui.Roi> ijRois, String property) {
        return fromImageJ(ijRois, property, ROIWrapper::new, ShapeWrapper::fromImageJ);
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs using the provided constructor and shape converter.
     *
     * @param ijRois      A list of ImageJ ROIs.
     * @param property    The property used to store the 4D ROI local index/label. Defaults to {@value IJ_PROPERTY} if
     *                    null or empty.
     * @param constructor A constructor to create ROI instances.
     * @param converter   A function to convert an IJ Roi to a list of OMERO Shapes.
     *
     * @return The converted list of OMERO ROIs.
     */
    private static List<ROIWrapper> fromImageJ(List<? extends ij.gui.Roi> ijRois,
                                               String property,
                                               Supplier<? extends ROIWrapper> constructor,
                                               Function<? super ij.gui.Roi, ? extends List<? extends ShapeWrapper<?>>> converter) {
        property = checkProperty(property);

        Map<String, ROIWrapper> rois4D = new TreeMap<>();
        Map<String, String>     names  = new TreeMap<>();

        Map<Integer, ROIWrapper> shape2roi = new TreeMap<>();

        for (int i = 0; i < ijRois.size(); i++) {
            String value = ijRois.get(i).getProperty(property);
            String name  = ijRois.get(i).getProperty(ijNameProperty(property));

            ROIWrapper roi;
            if (value != null && !value.trim().isEmpty()) {
                roi = rois4D.computeIfAbsent(value, v -> constructor.get());
                names.putIfAbsent(value, name);
            } else {
                roi = constructor.get();
                roi.setName(name);
            }
            shape2roi.put(i, roi);
        }
        rois4D.forEach((id, roi) -> roi.setName(names.get(id)));
        shape2roi.forEach((key, value) -> value.addShapes(converter.apply(ijRois.get(key))));
        return shape2roi.values().stream().distinct().collect(Collectors.toList());
    }


    /**
     * Converts an OMERO list of ROIs to a list of ImageJ ROIs
     *
     * @param rois A list of OMERO ROIs.
     *
     * @return The converted list of ImageJ ROIs.
     */
    public static List<ij.gui.Roi> toImageJ(Collection<? extends ROIWrapper> rois) {
        return toImageJ(rois, IJ_PROPERTY);
    }


    /**
     * Converts an OMERO list of ROIs to a list of ImageJ ROIs
     *
     * @param rois     A list of OMERO ROIs.
     * @param property The property used to store the 4D ROI local index/label. Defaults to {@value IJ_PROPERTY} if null
     *                 or empty.
     *
     * @return The converted list of ImageJ ROIs.
     */
    public static List<ij.gui.Roi> toImageJ(Collection<? extends ROIWrapper> rois, String property) {
        return toImageJ(rois, property, true);
    }


    /**
     * Converts an OMERO list of ROIs to a list of ImageJ ROIs
     *
     * @param rois      A list of OMERO ROIs.
     * @param property  The property used to store the 4D ROI local labels/IDs. Defaults to {@value IJ_PROPERTY} if null
     *                  or empty.
     * @param groupRois Whether ImageJ Rois belonging to the same OMERO ROI should be grouped or not.
     *
     * @return The converted list of ImageJ ROIs.
     */
    public static List<ij.gui.Roi> toImageJ(Collection<? extends ROIWrapper> rois, String property, boolean groupRois) {
        property = checkProperty(property);
        final int maxGroups = 255;
        groupRois = groupRois && rois.size() < maxGroups;

        int nShapes = rois.stream().map(ObjectWrapper::asDataObject).mapToInt(ROIData::getShapeCount).sum();

        List<ij.gui.Roi> ijRois = new ArrayList<>(nShapes);

        int index = 1;
        for (ROIWrapper roi : rois) {
            String name = roi.getName();

            List<ij.gui.Roi> shapes = roi.toImageJ(property);
            for (ij.gui.Roi r : shapes) {
                r.setProperty(property, String.valueOf(index));
                r.setProperty(ijNameProperty(property), name);
                if (groupRois) {
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
    public String getName() {
        RString name = ((_RoiOperationsNC) data.asIObject()).getName();
        return name != null ? name.getValue() : "";
    }


    /**
     * Sets the ROI name.
     *
     * @param name The ROI name.
     */
    public void setName(String name) {
        ((_RoiOperationsNC) data.asIObject()).setName(omero.rtypes.rstring(name));
    }


    /**
     * Adds ShapeData objects from a list of ShapeWrapper to the ROIData
     *
     * @param shapes List of ShapeWrapper.
     */
    public void addShapes(Iterable<? extends ShapeWrapper<?>> shapes) {
        shapes.forEach(this::addShape);
    }


    /**
     * Adds a ShapeData from a ShapeWrapper to the ROIData
     *
     * @param shape ShapeWrapper to add.
     */
    public void addShape(ShapeWrapper<?> shape) {
        data.addShapeData(shape.asDataObject());
    }


    /**
     * Returns the list of shapes contained in the ROIData.
     *
     * @return list of shape contained in the ROIData.
     */
    public ShapeList getShapes() {
        List<ShapeData> shapeData = data.getShapes();
        ShapeList       shapes    = new ShapeList(shapeData.size());
        shapeData.stream().sorted(Comparator.comparing(ShapeData::getId)).forEach(shapes::add);
        return shapes;
    }


    /**
     * Sets the image linked to the ROI.
     *
     * @param image Image linked to the ROIData.
     */
    public void setImage(ImageWrapper image) {
        data.setImage(image.asDataObject().asImage());
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
    public void deleteShape(int pos) {
        data.removeShapeData(data.getShapes().get(pos));
    }


    /**
     * Saves the ROI.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException Server error.
     */
    public void saveROI(Client client) throws ServerException, ServiceException {
        try {
            Roi roi = (Roi) client.getGateway().getUpdateService(client.getCtx()).saveAndReturnObject(data.asIObject());
            data = new ROIData(roi);
        } catch (DSOutOfServiceException | ServerError e) {
            handleServiceOrServer(e, "Cannot save ROI");
        }
    }


    /**
     * Returns the 5D bounds containing the ROI.
     *
     * @return The 5D bounds.
     */
    public Bounds getBounds() {
        int[] x = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] y = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] c = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] z = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] t = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (ShapeWrapper<?> shape : getShapes()) {
            RectangleWrapper box = shape.getBoundingBox();
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
    public List<ij.gui.Roi> toImageJ() {
        return this.toImageJ(IJ_PROPERTY);
    }


    /**
     * Convert ROI to ImageJ list of ROIs.
     *
     * @param property The property where the 4D ROI local index will be stored.
     *
     * @return A list of ROIs.
     */
    public List<ij.gui.Roi> toImageJ(String property) {
        property = checkProperty(property);
        ShapeList shapes = getShapes();

        Map<String, List<ShapeWrapper<?>>> sameSlice = shapes.stream()
                                                             .collect(groupingBy(ShapeWrapper::getCZT,
                                                                                 LinkedHashMap::new,
                                                                                 Collectors.toList()));
        sameSlice.values().removeIf(List::isEmpty);
        List<ij.gui.Roi> rois = new ArrayList<>(shapes.size());
        for (List<ShapeWrapper<?>> slice : sameSlice.values()) {
            ShapeWrapper<?> shape = slice.iterator().next();

            ij.gui.Roi roi = shape.toImageJ();
            String     txt = shape.getText();
            if (slice.size() > 1) {
                ij.gui.Roi xor = slice.stream()
                                      .map(ShapeWrapper::toImageJ)
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