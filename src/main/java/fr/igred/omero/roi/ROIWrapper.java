/*
 *  Copyright (C) 2020-2024 GReD
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


import fr.igred.omero.AnnotatableWrapper;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.Wrapper;
import ij.gui.PointRoi;
import ij.gui.ShapeRoi;
import omero.RString;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.Roi;
import omero.model._RoiOperationsNC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


/**
 * Class containing a ROIData object.
 * <p> Wraps function calls to the ROIData contained.
 */
public class ROIWrapper extends AnnotatableWrapper<ROIData> implements ROI {


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
    public ROIWrapper(Iterable<? extends Shape> shapes) {
        super(new ROIData());

        for (Shape shape : shapes) {
            data.addShapeData(shape.asDataObject());
        }
    }


    /**
     * Constructor of the ROIWrapper class.
     *
     * @param roi The ROIData to wrap.
     */
    public ROIWrapper(ROIData roi) {
        super(roi);
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs
     *
     * @param ijRois A list of ImageJ ROIs.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROI> fromImageJ(List<? extends ij.gui.Roi> ijRois) {
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
    public static List<ROI> fromImageJ(List<? extends ij.gui.Roi> ijRois, String property) {
        return ROI.fromImageJ(ijRois, property,
                              ROIWrapper::new,
                              ShapeWrapper::fromImageJ);
    }


    /**
     * Combines a list of ROIs into a single ROI.
     * <p>{@code SHAPE_ID} property is the concatenation of shape IDs.</p>
     *
     * @param rois The ROIs to combine (must contain at least 1 element).
     *
     * @return See above.
     */
    private static ij.gui.Roi xor(Collection<? extends ij.gui.Roi> rois) {
        String idProperty = Shape.IJ_ID_PROPERTY;
        String shapeIDs = rois.stream()
                              .map(r -> r.getProperty(idProperty))
                              .collect(Collectors.joining(","));

        ij.gui.Roi roi = rois.iterator().next();
        if (rois.size() > 1) {
            ij.gui.Roi xor = rois.stream()
                                 .map(ShapeRoi::new)
                                 .reduce(ShapeRoi::xor)
                                 .map(ij.gui.Roi.class::cast)
                                 .orElse(roi);
            xor.setStrokeColor(roi.getStrokeColor());
            xor.setFillColor(roi.getFillColor());
            xor.setPosition(roi.getCPosition(), roi.getZPosition(), roi.getTPosition());
            xor.setName(roi.getName());
            xor.setProperty(idProperty, shapeIDs);
            roi = xor;
        }
        return roi;
    }


    /**
     * Combines a list of points into a single PointRoi.
     *
     * @param points The points to combine (must contain at least 1 element).
     *
     * @return See above.
     */
    private static PointRoi combine(Collection<? extends PointRoi> points) {
        String idProperty = Shape.IJ_ID_PROPERTY;
        String shapeIDs = points.stream()
                                .map(p -> p.getProperty(idProperty))
                                .collect(Collectors.joining(","));

        PointRoi point = points.iterator().next();
        points.stream()
              .skip(1)
              .forEachOrdered(p -> point.addPoint(p.getXBase(), p.getYBase()));
        point.setProperty(idProperty, shapeIDs);
        return point;
    }


    /**
     * Converts a shape to an ImageJ ROI and adds a name if there is none.
     *
     * @param shape The shape to convert.
     *
     * @return See above.
     */
    private ij.gui.Roi shapeToIJRoiWithName(Shape shape) {
        ij.gui.Roi roi         = shape.toImageJ();
        String     name        = roi.getName();
        String     defaultName = String.format("%d-%d", getId(), shape.getId());
        if (name.isEmpty()) {
            roi.setName(defaultName);
        }
        return roi;
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
     * Adds a ShapeData from a ShapeWrapper to the ROIData
     *
     * @param shape ShapeWrapper to add.
     */
    @Override
    public void addShape(Shape shape) {
        data.addShapeData(shape.asDataObject());
    }


    /**
     * Returns the list of shapes contained in the ROIData.
     *
     * @return list of shape contained in the ROIData.
     */
    @Override
    public List<Shape> getShapes() {
        return wrap(data.getShapes(), Wrapper::wrap);
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
     * Deletes a ShapeData from the ROI.
     *
     * @param shape ShapeData to delete.
     */
    @Override
    public void deleteShape(ShapeData shape) {
        data.removeShapeData(shape);
    }


    /**
     * Deletes a shape from the ROI.
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
     * @param dm The data manager.
     *
     * @throws AccessException    Cannot access data.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void saveROI(DataManager dm)
    throws AccessException, ServiceException, ExecutionException {
        Roi roi = (Roi) dm.save(data.asIObject());
        data = new ROIData(roi);
    }


    /**
     * Converts the ROI to a list of ImageJ ROIs.
     *
     * @param property The property where the 4D ROI local index will be stored.
     *
     * @return A list of ROIs.
     */
    @Override
    public List<ij.gui.Roi> toImageJ(String property) {
        property = ROI.checkProperty(property);
        String ijIDProperty   = ROI.ijIDProperty(property);
        String ijNameProperty = ROI.ijNameProperty(property);
        String roiID          = String.valueOf(getId());

        List<Shape> shapes = getShapes();

        Map<String, List<Shape>> sameSlice = shapes.stream()
                                                   .collect(groupingBy(Shape::getCZT,
                                                                       LinkedHashMap::new,
                                                                       Collectors.toList()));
        sameSlice.values().removeIf(List::isEmpty);
        List<ij.gui.Roi> rois = new ArrayList<>(shapes.size());
        for (List<Shape> slice : sameSlice.values()) {
            // Handle 2D shapes using XOR (Rectangles, Ellipses and Polygons)
            List<ij.gui.Roi> toXOR = slice.stream()
                                          .filter(s -> (s instanceof Rectangle)
                                                       || (s instanceof Ellipse)
                                                       || (s instanceof Polygon))
                                          .map(this::shapeToIJRoiWithName)
                                          .collect(Collectors.toList());
            if (!toXOR.isEmpty()) {
                rois.add(xor(toXOR));
            }

            // Handle points by combining them
            List<PointRoi> points = slice.stream()
                                         .filter(Point.class::isInstance)
                                         .map(this::shapeToIJRoiWithName)
                                         .map(PointRoi.class::cast)
                                         .collect(Collectors.toList());
            if (!points.isEmpty()) {
                rois.add(combine(points));
            }

            // Simply convert and add the others
            slice.stream()
                 .filter(s -> !(s instanceof Rectangle)
                              && !(s instanceof Ellipse)
                              && !(s instanceof Polygon)
                              && !(s instanceof Point))
                 .map(this::shapeToIJRoiWithName)
                 .forEachOrdered(rois::add);

            // Add properties
            rois.forEach(r -> r.setProperty(ijIDProperty, roiID));
            rois.forEach(r -> r.setProperty(ijNameProperty, getName()));
        }
        return rois;
    }


    /**
     * Returns the type of annotation link for this object.
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }

}