/*
 *  Copyright (C) 2020-2025 GReD
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
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.Bounds;
import fr.igred.omero.util.Coordinates;
import ij.IJ;
import ij.gui.Roi;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Interface to handle ROIs on OMERO.
 */
public interface ROI extends Annotatable {

    /** Annotation link name for this type of object */
    String ANNOTATION_LINK = "RoiAnnotationLink";

    /** Default IJ property to store ROI local labels / indices. */
    String IJ_PROPERTY = "ROI";


    /**
     * Checks the provided property.
     *
     * @param property The property where the 4D ROI local index/label is stored.
     *
     * @return The property, or the default value {@link #IJ_PROPERTY} (= {@value IJ_PROPERTY}) if it is null or empty.
     */
    static String checkProperty(String property) {
        if (property == null || property.trim().isEmpty()) {
            return IJ_PROPERTY;
        } else {
            return property;
        }
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
     * @param property    The property used to store the 4D ROI local index/label. Defaults to {@value IJ_PROPERTY} if
     *                    null or empty.
     * @param constructor A constructor to create ROI instances.
     * @param converter   A function to convert an IJ Roi to a list of OMERO Shapes.
     *
     * @return The converted list of OMERO ROIs.
     */
    static List<ROI> fromImageJ(List<? extends ij.gui.Roi> ijRois,
                                String property,
                                Supplier<? extends ROI> constructor,
                                Function<? super Roi, ? extends List<? extends Shape>> converter) {
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
        return shape2roi.values()
                        .stream()
                        .distinct()
                        .collect(Collectors.toList());
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
     * @param property  The property used to store the 4D ROI local labels/IDs. Defaults to {@value IJ_PROPERTY} if null
     *                  or empty.
     * @param groupRois Whether ImageJ Rois belonging to the same OMERO ROI should be grouped or not.
     *
     * @return The converted list of ImageJ ROIs.
     */
    static List<Roi> toImageJ(Collection<? extends ROI> rois, String property, boolean groupRois) {
        property = checkProperty(property);
        final int maxGroups = 255;
        groupRois = groupRois && rois.size() < maxGroups && IJ.getVersion().compareTo("1.52t") >= 0;

        int nShapes = rois.stream()
                          .map(ROI::asDataObject)
                          .mapToInt(ROIData::getShapeCount)
                          .sum();

        List<Roi> ijRois = new ArrayList<>(nShapes);

        int index = 1;
        for (ROI roi : rois) {
            String name = roi.getName();

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
     * Adds ShapeData objects from a list of ShapeWrapper to the ROIData
     *
     * @param shapes List of ShapeWrapper.
     */
    default void addShapes(Iterable<? extends Shape> shapes) {
        shapes.forEach(this::addShape);
    }


    /**
     * Adds a ShapeData from a ShapeWrapper to the ROIData
     *
     * @param shape ShapeWrapper to add.
     */
    void addShape(Shape shape);


    /**
     * Returns the list of shapes contained in the ROIData.
     *
     * @return list of shape contained in the ROIData.
     */
    List<Shape> getShapes();


    /**
     * Sets the image linked to the ROI.
     *
     * @param image Image linked to the ROIData.
     */
    void setImage(Image image);


    /**
     * Deletes a ShapeData from the ROI.
     *
     * @param shape ShapeData to delete.
     */
    void deleteShape(ShapeData shape);


    /**
     * Deletes a shape from the ROI.
     *
     * @param pos Position of the ShapeData in the ShapeData list from the ROIData.
     *
     * @throws IndexOutOfBoundsException If pos is out of the ShapeData list bounds.
     */
    void deleteShape(int pos);


    /**
     * Saves the ROI.
     *
     * @param dm The data manager.
     *
     * @throws AccessException    Cannot access data.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void saveROI(DataManager dm)
    throws AccessException, ServiceException, ExecutionException;


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
     * Converts the ROI to a list of ImageJ ROIs.
     *
     * @return A list of ROIs.
     */
    default List<ij.gui.Roi> toImageJ() {
        return toImageJ(IJ_PROPERTY);
    }


    /**
     * Converts the ROI to a list of ImageJ ROIs.
     *
     * @param property The property where the 4D ROI local index will be stored.
     *
     * @return A list of ROIs.
     */
    List<Roi> toImageJ(String property);


    /**
     * Attach an {@link AnnotationData} to this object.
     *
     * @param dm         The data manager.
     * @param annotation The {@link AnnotationData}.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    default <A extends AnnotationData> void link(DataManager dm, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        RoiAnnotationLink link = new RoiAnnotationLinkI();
        link.setChild(annotation.asAnnotation());
        link.setParent((omero.model.Roi) asDataObject().asIObject());
        long id = ((RoiAnnotationLink) dm.save(link)).getChild().getId().getValue();
        annotation.setId(id);
    }

}
