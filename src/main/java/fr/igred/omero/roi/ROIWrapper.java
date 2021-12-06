/*
 *  Copyright (C) 2020-2021 GReD
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
import fr.igred.omero.GenericObjectWrapper;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.PixelsWrapper.Bounds;
import fr.igred.omero.repository.PixelsWrapper.Coordinates;
import ij.gui.*;
import omero.ServerError;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.Roi;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrServer;
import static java.util.stream.Collectors.groupingBy;


/**
 * Class containing a ROIData
 * <p> Implements function using the ROIData contained
 */
public class ROIWrapper extends GenericObjectWrapper<ROIData> {

    /**
     * Default IJ property to store ROI local IDs / indices.
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
    public ROIWrapper(List<GenericShapeWrapper<?>> shapes) {
        super(new ROIData());

        for (GenericShapeWrapper<?> shape : shapes)
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


    /**
     * Checks the provided property.
     *
     * @param property The property where 4D ROI local ID is stored.
     *
     * @return The property, or the default value {@link #IJ_PROPERTY} (= {@value #IJ_PROPERTY}) if it is null or empty.
     */
    public static String checkProperty(String property) {
        if (property == null || property.trim().isEmpty()) return IJ_PROPERTY;
        else return property;
    }


    /**
     * Returns ID property corresponding to input local ID property (appends "_ID" to said property).
     *
     * @param property The property where 4D ROI local ID is stored, defaults to {@value #IJ_PROPERTY} if null or
     *                 empty.
     *
     * @return See above.
     */
    public static String ijIDProperty(String property) {
        property = checkProperty(property);
        return property + "_ID";
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs
     *
     * @param ijRois A list of ImageJ ROIs.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROIWrapper> fromImageJ(List<ij.gui.Roi> ijRois) {
        return fromImageJ(ijRois, IJ_PROPERTY);
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs
     *
     * @param ijRois   A list of ImageJ ROIs.
     * @param property The property where 4D ROI local ID is stored. Defaults to {@value #IJ_PROPERTY} if null or
     *                 empty.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROIWrapper> fromImageJ(List<ij.gui.Roi> ijRois, String property) {
        property = checkProperty(property);
        Map<Long, ROIWrapper> rois4D = new TreeMap<>();

        Map<Integer, ROIWrapper> shape2roi = new TreeMap<>();

        for (int i = 0; i < ijRois.size(); i++) {
            String value = ijRois.get(i).getProperty(property);
            if (value != null && value.matches("-?\\d+")) {
                long id = Long.parseLong(value);
                rois4D.computeIfAbsent(id, val -> new ROIWrapper());
                shape2roi.put(i, rois4D.get(id));
            } else {
                shape2roi.put(i, new ROIWrapper());
            }
        }

        for (Map.Entry<Integer, ROIWrapper> entry : shape2roi.entrySet()) {
            ij.gui.Roi ijRoi = ijRois.get(entry.getKey());
            ROIWrapper roi   = entry.getValue();
            roi.addShape(ijRoi);
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
    public static List<ij.gui.Roi> toImageJ(List<ROIWrapper> rois) {
        return toImageJ(rois, IJ_PROPERTY);
    }


    /**
     * Converts an OMERO list of ROIs to a list of ImageJ ROIs
     *
     * @param rois     A list of OMERO ROIs.
     * @param property The property where 4D ROI local ID will be stored. Defaults to {@value #IJ_PROPERTY} if null or
     *                 empty.
     *
     * @return The converted list of ImageJ ROIs.
     */
    public static List<ij.gui.Roi> toImageJ(List<ROIWrapper> rois, String property) {
        property = checkProperty(property);

        List<ij.gui.Roi> ijRois = new ArrayList<>();

        int index = 1;
        for (ROIWrapper roi : rois) {
            List<ij.gui.Roi> shapes = roi.toImageJ(property);
            for (ij.gui.Roi r : shapes) {
                r.setProperty(property, String.valueOf(index));
                if (rois.size() < 255) {
                    r.setGroup(index);
                }
            }
            ijRois.addAll(shapes);
            index++;
        }
        return ijRois;
    }


    /**
     * Changes the wrapped data.
     *
     * @param data The ROI data.
     */
    public void setData(ROIData data) {
        this.data = data;
    }


    /**
     * Adds ShapeData objects from a list of GenericShapeWrapper to the ROIData
     *
     * @param shapes List of GenericShapeWrapper.
     */
    public void addShapes(List<? extends GenericShapeWrapper<?>> shapes) {
        shapes.forEach(this::addShape);
    }


    /**
     * Adds a ShapeData from a GenericShapeWrapper to the ROIData
     *
     * @param shape GenericShapeWrapper to add.
     */
    public void addShape(GenericShapeWrapper<?> shape) {
        data.addShapeData(shape.asShapeData());
    }


    /**
     * Returns the list of shapes contained in the ROIData
     *
     * @return list of shape contained in the ROIData.
     */
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
    public void setImage(ImageWrapper image) {
        data.setImage(image.asImageData().asImage());
    }


    /**
     * Returns the ROIData contained.
     *
     * @return the {@link ROIData} contained.
     */
    public ROIData asROIData() {
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
     * @param client The client handling the connection.
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
        for (GenericShapeWrapper<?> shape : getShapes()) {
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
     * @param property The property where 4D ROI local ID will be stored.
     *
     * @return A list of ROIs.
     */
    public List<ij.gui.Roi> toImageJ(String property) {
        property = checkProperty(property);
        ShapeList shapes = getShapes();

        Map<String, List<GenericShapeWrapper<?>>> sameSlice = shapes.stream()
                                                                    .collect(groupingBy(GenericShapeWrapper::getCZT,
                                                                                        LinkedHashMap::new,
                                                                                        Collectors.toList()));
        sameSlice.values().removeIf(List::isEmpty);
        List<ij.gui.Roi> rois = new ArrayList<>(shapes.size());
        for (List<GenericShapeWrapper<?>> slice : sameSlice.values()) {
            GenericShapeWrapper<?> shape = slice.iterator().next();

            ij.gui.Roi roi = shape.toImageJ();
            if (slice.size() > 1) {
                ij.gui.Roi xor = slice.stream()
                                      .map(GenericShapeWrapper::toImageJ)
                                      .map(ShapeRoi::new)
                                      .reduce(ShapeRoi::xor)
                                      .map(ij.gui.Roi.class::cast)
                                      .orElse(roi);
                xor.setStrokeColor(roi.getStrokeColor());
                xor.setPosition(roi.getCPosition(), roi.getZPosition(), roi.getTPosition());
                roi = xor;
            }
            if (!shape.getText().equals("")) {
                roi.setName(shape.getText());
            } else {
                roi.setName(getId() + "-" + shape.getId());
            }
            roi.setProperty(ijIDProperty(property), String.valueOf(getId()));
            rois.add(roi);
        }
        return rois;
    }


    /**
     * Adds an ImageJ ROI to an OMERO ROI.
     *
     * @param ijRoi The ImageJ ROI.
     */
    private void addShape(ij.gui.Roi ijRoi) {
        final String ARROW = "Arrow";

        int c = Math.max(-1, ijRoi.getCPosition() - 1);
        int z = Math.max(-1, ijRoi.getZPosition() - 1);
        int t = Math.max(-1, ijRoi.getTPosition() - 1);

        GenericShapeWrapper<?> shape;
        if (ijRoi instanceof TextRoi) {
            String text = ((TextRoi) ijRoi).getText();

            double x = ijRoi.getBounds().getX();
            double y = ijRoi.getBounds().getY();

            shape = new TextWrapper(text, x, y);
            shape.setCZT(c, z, t);
            addShape(shape);
        } else if (ijRoi instanceof OvalRoi) {
            double x = ijRoi.getBounds().getX();
            double y = ijRoi.getBounds().getY();
            double w = ijRoi.getBounds().getWidth();
            double h = ijRoi.getBounds().getHeight();

            shape = new EllipseWrapper(x + w / 2, y + h / 2, w / 2, h / 2);
            shape.setText(ijRoi.getName());
            shape.setCZT(c, z, t);
            addShape(shape);
        } else if (ijRoi instanceof Arrow) {
            double x1 = ((Line) ijRoi).x1d;
            double x2 = ((Line) ijRoi).x2d;
            double y1 = ((Line) ijRoi).y1d;
            double y2 = ((Line) ijRoi).y2d;

            shape = new LineWrapper(x1, y1, x2, y2);
            shape.asShapeData().getShapeSettings().setMarkerEnd(ARROW);
            if (((Arrow) ijRoi).getDoubleHeaded()) {
                shape.asShapeData().getShapeSettings().setMarkerStart(ARROW);
            }
            shape.setText(ijRoi.getName());
            shape.setCZT(c, z, t);
            addShape(shape);
        } else if (ijRoi instanceof Line) {
            double x1 = ((Line) ijRoi).x1d;
            double x2 = ((Line) ijRoi).x2d;
            double y1 = ((Line) ijRoi).y1d;
            double y2 = ((Line) ijRoi).y2d;

            shape = new LineWrapper(x1, y1, x2, y2);
            shape.setText(ijRoi.getName());
            shape.setCZT(c, z, t);
            addShape(shape);
        } else if (ijRoi instanceof PointRoi) {
            int[] x = ijRoi.getPolygon().xpoints;
            int[] y = ijRoi.getPolygon().ypoints;

            List<PointWrapper> points = new LinkedList<>();
            IntStream.range(0, x.length)
                     .forEach(i -> points.add(new PointWrapper(x[i], y[i])));
            points.forEach(p -> p.setText(ijRoi.getName()));
            points.forEach(p -> p.setCZT(c, z, t));
            points.forEach(this::addShape);
        } else if (ijRoi instanceof PolygonRoi) {
            String type = ijRoi.getTypeAsString();

            int[] x = ijRoi.getPolygon().xpoints;
            int[] y = ijRoi.getPolygon().ypoints;

            List<Point2D.Double> points = new LinkedList<>();
            IntStream.range(0, x.length).forEach(i -> points.add(new Point2D.Double(x[i], y[i])));
            if (type.equals("Polyline") || type.equals("Freeline") || type.equals("Angle")) {
                shape = new PolylineWrapper(points);
            } else {
                shape = new PolygonWrapper(points);
            }
            shape.setText(ijRoi.getName());
            shape.setCZT(c, z, t);
            addShape(shape);
        } else if (ijRoi instanceof ShapeRoi) {
            ij.gui.Roi[] rois = ((ShapeRoi) ijRoi).getRois();
            IntStream.range(0, rois.length).forEach(i -> rois[i].setName(ijRoi.getName()));
            IntStream.range(0, rois.length).forEach(i -> rois[i].setPosition(ijRoi.getCPosition(),
                                                                             ijRoi.getZPosition(),
                                                                             ijRoi.getTPosition()));
            IntStream.range(0, rois.length).forEach(i -> addShape(rois[i]));
        } else if (ijRoi.getType() == ij.gui.Roi.RECTANGLE) {
            double x = ijRoi.getBounds().getX();
            double y = ijRoi.getBounds().getY();
            double w = ijRoi.getBounds().getWidth();
            double h = ijRoi.getBounds().getHeight();

            shape = new RectangleWrapper(x, y, w, h);

            shape.setText(ijRoi.getName());
            shape.setCZT(c, z, t);
            addShape(shape);
        }
    }

}