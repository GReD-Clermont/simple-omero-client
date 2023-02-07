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
import ij.gui.Roi;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.IObject;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Interface to handle ROIs on OMERO.
 */
public interface ROI extends Annotatable<ROIData> {

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
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs using the provided constructor.
     *
     * @param ijRois      A list of ImageJ ROIs.
     * @param constructor A constructor to create ROI instances.
     *
     * @return The converted list of OMERO ROIs.
     */
    static List<ROI> fromImageJ(List<? extends Roi> ijRois, Supplier<? extends ROI> constructor) {
        return fromImageJ(ijRois, IJ_PROPERTY, constructor);
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs.
     *
     * @param ijRois      A list of ImageJ ROIs.
     * @param property    The property where 4D ROI local ID is stored. Defaults to {@value IJ_PROPERTY} if null or
     *                    empty.
     * @param constructor A constructor to create ROI instances.
     *
     * @return The converted list of OMERO ROIs.
     */
    static List<ROI> fromImageJ(List<? extends Roi> ijRois, String property, Supplier<? extends ROI> constructor) {
        property = checkProperty(property);
        Map<String, ROI> rois4D = new TreeMap<>();

        Map<Integer, ROI> shape2roi = new TreeMap<>();

        for (int i = 0; i < ijRois.size(); i++) {
            String value = ijRois.get(i).getProperty(property);
            if (value != null && !value.trim().isEmpty()) {
                rois4D.computeIfAbsent(value, val -> constructor.get());
                shape2roi.put(i, rois4D.get(value));
            } else {
                shape2roi.put(i, constructor.get());
            }
        }

        rois4D.forEach((name, roi) -> roi.setName(name));

        for (Map.Entry<Integer, ROI> entry : shape2roi.entrySet()) {
            Roi ijRoi = ijRois.get(entry.getKey());
            ROI roi   = entry.getValue();
            roi.addShapes(ShapeWrapper.fromImageJ(ijRoi));
        }
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
     * @param property The property used to store 4D ROI local IDs. Defaults to {@value IJ_PROPERTY} if null or empty.
     *
     * @return The converted list of ImageJ ROIs.
     */
    static List<Roi> toImageJ(Collection<? extends ROI> rois, String property) {
        property = checkProperty(property);
        final int maxGroups = 255;

        int nShapes = rois.stream().map(ROI::asDataObject).mapToInt(ROIData::getShapeCount).sum();

        List<Roi> ijRois = new ArrayList<>(nShapes);

        int index = 1;
        for (ROI roi : rois) {
            String name = roi.getName();
            if (name.trim().isEmpty()) {
                name = "SOC_INDEX_" + index;
            }
            List<Roi> shapes = roi.toImageJ(property);
            for (Roi r : shapes) {
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
     * @param shapes List of ShapeWrapper.
     */
    void addShapes(Iterable<? extends Shape<?>> shapes);


    /**
     * Adds a Shape to the ROI.
     *
     * @param shape ShapeWrapper to add.
     */
    void addShape(Shape<?> shape);


    /**
     * Returns the list of shapes contained in the ROI.
     *
     * @return See above.
     */
    ShapeList getShapes();


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
    Bounds getBounds();


    /**
     * Convert ROI to ImageJ list of ROIs.
     *
     * @return A list of ROIs.
     */
    List<Roi> toImageJ();


    /**
     * Convert ROI to ImageJ list of ROIs.
     *
     * @param property The property where 4D ROI local ID will be stored.
     *
     * @return A list of ROIs.
     */
    List<Roi> toImageJ(String property);


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
    default <A extends Annotation<?>> void link(DataManager dm, A annotation)
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
    default <A extends Annotation<?>> void unlink(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException, ServerException, InterruptedException {
        List<IObject> os = client.findByQuery("select link from RoiAnnotationLink as link" +
                                              " where link.parent = " + getId() +
                                              " and link.child = " + annotation.getId());
        client.delete(os.iterator().next());
    }

}
