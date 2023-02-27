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


import fr.igred.omero.Annotatable;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.core.Image;
import fr.igred.omero.util.Bounds;
import fr.igred.omero.util.Coordinates;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.IObject;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


/**
 * Interface to handle ROIs on OMERO.
 */
public interface ROI extends Annotatable {

    /**
     * Default IJ property to store ROI local IDs / indices.
     */
    String IJ_PROPERTY = "ROI";


    /**
     * Checks the provided property.
     *
     * @param property The property where the 4D ROI local ID is stored.
     *
     * @return The property, or the default value {@link #IJ_PROPERTY} (= {@value IJ_PROPERTY}) if it is null or empty.
     */
    static String checkProperty(String property) {
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
    static String ijIDProperty(String property) {
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
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs using the provided constructor and shape converter.
     *
     * @param ijRois      A list of ImageJ ROIs.
     * @param constructor A constructor to create ROI instances.
     * @param converter   A function to convert an IJ Roi to a list of OMERO Shapes.
     *
     * @return The converted list of OMERO ROIs.
     */
    static List<ROI> fromImageJ(List<? extends Roi> ijRois,
                                Supplier<? extends ROI> constructor,
                                Function<? super Roi, Iterable<? extends Shape>> converter) {
        return fromImageJ(ijRois, IJ_PROPERTY, constructor, converter);
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
    static List<ROI> fromImageJ(List<? extends Roi> ijRois,
                                String property,
                                Supplier<? extends ROI> constructor,
                                Function<? super Roi, Iterable<? extends Shape>> converter) {
        property = checkProperty(property);

        Map<String, ROI>    rois4D = new TreeMap<>();
        Map<String, String> names  = new TreeMap<>();

        Map<Integer, ROI> shape2roi = new TreeMap<>();

        for (int i = 0; i < ijRois.size(); i++) {
            String value = ijRois.get(i).getProperty(property);
            String name  = ijRois.get(i).getProperty(ijNameProperty(property));

            ROI roi;
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
    static List<Roi> toImageJ(Collection<? extends ROI> rois) {
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
    static List<Roi> toImageJ(Collection<? extends ROI> rois, String property) {
        return toImageJ(rois, property, true);
    }


    /**
     * Converts an OMERO list of ROIs to a list of ImageJ ROIs
     *
     * @param rois      A list of OMERO ROIs.
     * @param property  The property used to store the 4D ROI local label/index. Defaults to {@value IJ_PROPERTY} if
     *                  null or empty.
     * @param groupRois Whether ImageJ Rois belonging to the same OMERO ROI should be grouped or not.
     *
     * @return The converted list of ImageJ ROIs.
     */
    static List<Roi> toImageJ(Collection<? extends ROI> rois, String property, boolean groupRois) {
        property = checkProperty(property);
        final int maxGroups = 255;
        groupRois = groupRois && rois.size() < maxGroups;

        int nShapes = rois.stream()
                          .map(ROI::asDataObject)
                          .mapToInt(ROIData::getShapeCount)
                          .sum();

        List<Roi> ijRois = new ArrayList<>(nShapes);

        int index = 1;
        for (ROI roi : rois) {
            String    name   = roi.getName();
            List<Roi> shapes = roi.toImageJ(property);
            for (Roi r : shapes) {
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
     * Returns a ROIData corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    ROIData asDataObject();


    /**
     * Gets the ROI name.
     *
     * @return The ROI name (can be null).
     */
    String getName();


    /**
     * Sets the ROI name.
     *
     * @param name The ROI name.
     */
    void setName(String name);


    /**
     * Adds shape objects from a list of shapes to the ROI.
     *
     * @param shapes List of Shapes.
     */
    default void addShapes(Iterable<? extends Shape> shapes) {
        shapes.forEach(this::addShape);
    }


    /**
     * Adds a Shape to the ROI.
     *
     * @param shape Shape to add.
     */
    void addShape(Shape shape);


    /**
     * Returns the list of shapes contained in the ROI.
     *
     * @return See above.
     */
    List<Shape> getShapes();


    /**
     * Sets the image linked to the ROI.
     *
     * @param image Image linked to the ROI.
     */
    void setImage(Image image);


    /**
     * Deletes a shape from the ROI.
     *
     * @param shape ShapeData to delete.
     */
    void deleteShape(ShapeData shape);


    /**
     * Deletes a shape from the ROI.
     *
     * @param pos Position of the ShapeData in the ShapeData list from the ROI.
     *
     * @throws IndexOutOfBoundsException If pos is out of the ShapeData list bounds.
     */
    void deleteShape(int pos);


    /**
     * Saves the ROI.
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    void saveROI(ConnectionHandler client) throws ServerException, ServiceException;


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
        for (Shape shape : getShapes()) {
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
    default List<Roi> toImageJ() {
        return this.toImageJ(IJ_PROPERTY);
    }


    /**
     * Converts a ROI to a list of ImageJ ROIs.
     *
     * @param property The property where the 4D ROI local index will be stored.
     *
     * @return A list of ROIs.
     */
    default List<Roi> toImageJ(String property) {
        property = checkProperty(property);
        List<Shape> shapes = getShapes();

        Map<String, List<Shape>> sameSlice = shapes.stream()
                                                   .collect(groupingBy(Shape::getCZT,
                                                                       LinkedHashMap::new,
                                                                       Collectors.toList()));
        sameSlice.values().removeIf(List::isEmpty);
        List<ij.gui.Roi> rois = new ArrayList<>(shapes.size());
        for (List<Shape> slice : sameSlice.values()) {
            Shape shape = slice.iterator().next();

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


    /**
     * Adds an annotation to the object in OMERO, if possible.
     *
     * @param dm         The data manager.
     * @param annotation Annotation to be added.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default <A extends Annotation> void link(DataManager dm, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        RoiAnnotationLink link = new RoiAnnotationLinkI();
        link.setChild(annotation.asDataObject().asAnnotation());
        link.setParent((omero.model.Roi) asIObject());
        dm.save(link);
    }


    /**
     * Unlinks the given annotation from the current object.
     *
     * @param client     The client handling the connection.
     * @param annotation An annotation.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException     Cannot connect to OMERO.
     * @throws AccessException      Cannot access data.
     * @throws ExecutionException   A Facility can't be retrieved or instantiated.
     * @throws ServerException      Server error.
     * @throws InterruptedException If block(long) does not return.
     */
    @Override
    default <A extends Annotation> void unlink(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        List<IObject> os = client.findByQuery("select link from RoiAnnotationLink as link" +
                                              " where link.parent = " + getId() +
                                              " and link.child = " + annotation.getId());
        client.delete(os.iterator().next());
    }

}
