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
import fr.igred.omero.Client;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.Image;
import fr.igred.omero.repository.PixelsWrapper;
import ij.IJ;
import ij.gui.Roi;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Interface to handle ROIs on OMERO.
 */
public interface ROI extends Annotatable {

    /**
     * Default IJ property to store ROI local labels / indices.
     */
    String IJ_PROPERTY = "ROI";


    /**
     * Checks the provided property.
     *
     * @param property The property where the 4D ROI local index/label is stored.
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
     * @param ijRois      A collection of ImageJ ROIs.
     * @param constructor A constructor to create ROI instances.
     * @param converter   A function to convert an IJ Roi to a list of OMERO Shapes.
     *
     * @return The converted list of OMERO ROIs.
     */
    static List<ROI> fromImageJ(Collection<? extends Roi> ijRois,
                                Supplier<? extends ROI> constructor,
                                Function<? super Roi, Iterable<? extends Shape>> converter) {
        return fromImageJ(ijRois, IJ_PROPERTY, constructor, converter);
    }


    /**
     * Converts a collection of ImageJ ROIs to a list of OMERO ROIs using the provided constructor and shape converter.
     *
     * @param ijRois      A collection of ImageJ ROIs.
     * @param property    The property used to store the 4D ROI local index/label. Defaults to {@value IJ_PROPERTY} if
     *                    null or empty.
     * @param constructor A constructor to create ROI instances.
     * @param converter   A function to convert an IJ Roi to a list of OMERO Shapes.
     * @param <T>         The ROI type.
     *
     * @return The converted list of OMERO ROIs.
     */
    static <T extends ROI> List<T> fromImageJ(Collection<? extends Roi> ijRois,
                                              String property,
                                              Supplier<? extends T> constructor,
                                              Function<? super Roi, ? extends Iterable<? extends Shape>> converter) {
        property = checkProperty(property);
        int nRois = ijRois.size();

        Map<String, T>      rois4D = new HashMap<>(nRois);
        Map<String, String> names  = new HashMap<>(nRois);

        Map<Roi, T> shape2roi = new HashMap<>(nRois);

        for (Roi ijRoi : ijRois) {
            String value = ijRoi.getProperty(property);
            String name  = ijRoi.getProperty(ijNameProperty(property));

            T roi;
            if (value != null && !value.trim().isEmpty()) {
                roi = rois4D.computeIfAbsent(value, v -> constructor.get());
                names.putIfAbsent(value, name);
            } else {
                roi = constructor.get();
                roi.setName(name);
            }
            shape2roi.put(ijRoi, roi);
        }
        rois4D.forEach((id, roi) -> roi.setName(names.get(id)));
        shape2roi.forEach((key, value) -> value.addShapes(converter.apply(key)));
        return shape2roi.values()
                        .stream()
                        .sorted(Comparator.comparing(RemoteObject::getId))
                        .distinct()
                        .collect(Collectors.toList());
    }


    /**
     * Converts a collection of OMERO ROIs to a list of ImageJ ROIs.
     *
     * @param rois A collection of OMERO ROIs.
     *
     * @return The converted list of ImageJ ROIs.
     */
    static List<Roi> toImageJ(Collection<? extends ROI> rois) {
        return toImageJ(rois, IJ_PROPERTY);
    }


    /**
     * Converts a collection of OMERO ROIs to a list of ImageJ ROIs.
     *
     * @param rois     A collection of OMERO ROIs.
     * @param property The property used to store the 4D ROI local index/label. Defaults to {@value IJ_PROPERTY} if null
     *                 or empty.
     *
     * @return The converted list of ImageJ ROIs.
     */
    static List<Roi> toImageJ(Collection<? extends ROI> rois, String property) {
        return toImageJ(rois, property, true);
    }


    /**
     * Converts a collection of OMERO ROIs to a list of ImageJ ROIs.
     *
     * @param rois      A collection of OMERO ROIs.
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
     * @param shapes List of Shape.
     */
    void addShapes(Iterable<? extends Shape> shapes);


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
    List<? extends Shape> getShapes();


    /**
     * Sets the image linked to the ROI.
     *
     * @param image Image linked to the ROI.
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
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void saveROI(Client client)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Returns the 5D bounds containing the ROI.
     *
     * @return The 5D bounds.
     */
    PixelsWrapper.Bounds getBounds();


    /**
     * Converts the ROI to a list of ImageJ ROIs.
     *
     * @return A list of ROIs.
     */
    List<Roi> toImageJ();


    /**
     * Converts the ROI to a list of ImageJ ROIs.
     *
     * @param property The property where the 4D ROI local index will be stored.
     *
     * @return A list of ROIs.
     */
    List<Roi> toImageJ(String property);

}