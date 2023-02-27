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


import fr.igred.omero.AnnotatableWrapper;
import fr.igred.omero.client.Client;
import fr.igred.omero.ObjectWrapper;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.util.Bounds;
import fr.igred.omero.util.Coordinates;
import ij.IJ;
import ij.gui.ShapeRoi;
import omero.RString;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model._RoiOperationsNC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


/**
 * Class containing a ROIData object.
 * <p> Wraps function calls to the ROIData contained.
 */
public class ROIWrapper extends AnnotatableWrapper<ROIData> {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "RoiAnnotationLink";

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
     * @param roi The ROIData to wrap.
     */
    public ROIWrapper(ROIData roi) {
        super(roi);
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
    public static String ijNameProperty(String property) {
        property = checkProperty(property);
        return property + "_NAME";
    }


    /**
     * Converts a collection of ImageJ ROIs to a list of OMERO ROIs
     *
     * @param ijRois A collection of ImageJ ROIs.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROIWrapper> fromImageJ(Collection<? extends ij.gui.Roi> ijRois) {
        return fromImageJ(ijRois, IJ_PROPERTY);
    }


    /**
     * Converts a collection of ImageJ ROIs to a list of OMERO ROIs
     *
     * @param ijRois   A collection of ImageJ ROIs.
     * @param property The property used to store the 4D ROI local index/label. Defaults to {@value IJ_PROPERTY} if null
     *                 or empty.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROIWrapper> fromImageJ(Collection<? extends ij.gui.Roi> ijRois, String property) {
        return fromImageJ(ijRois, property, ROIWrapper::new, ShapeWrapper::fromImageJ);
    }


    /**
     * Converts a collection of ImageJ ROIs to a list of OMERO ROIs using the provided constructor and shape converter.
     *
     * @param ijRois      A collection of ImageJ ROIs.
     * @param property    The property used to store the 4D ROI local index/label. Defaults to {@value IJ_PROPERTY} if
     *                    null or empty.
     * @param constructor A constructor to create ROI instances.
     * @param converter   A function to convert an IJ Roi to a list of OMERO Shapes.
     *
     * @return The converted list of OMERO ROIs.
     */
    private static List<ROIWrapper> fromImageJ(Collection<? extends ij.gui.Roi> ijRois,
                                               String property,
                                               Supplier<? extends ROIWrapper> constructor,
                                               Function<? super ij.gui.Roi, ? extends List<? extends ShapeWrapper<?>>> converter) {
        property = checkProperty(property);
        int nRois = ijRois.size();

        Map<String, ROIWrapper> rois4D = new HashMap<>(nRois);
        Map<String, String>     names  = new HashMap<>(nRois);

        Map<ij.gui.Roi, ROIWrapper> shape2roi = new HashMap<>(nRois);

        for (ij.gui.Roi ijRoi : ijRois) {
            String value = ijRoi.getProperty(property);
            String name  = ijRoi.getProperty(ijNameProperty(property));

            ROIWrapper roi;
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
                        .sorted(Comparator.comparing(ObjectWrapper::getId))
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
    public static List<ij.gui.Roi> toImageJ(Collection<? extends ROIWrapper> rois) {
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
    public static List<ij.gui.Roi> toImageJ(Collection<? extends ROIWrapper> rois, String property) {
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
    public static List<ij.gui.Roi> toImageJ(Collection<? extends ROIWrapper> rois, String property, boolean groupRois) {
        property = checkProperty(property);
        final int maxGroups = 255;
        groupRois = groupRois && rois.size() < maxGroups && IJ.getVersion().compareTo("1.52t") >= 0;

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
     * Deletes a ShapeData from the ROI.
     *
     * @param shape ShapeData to delete.
     */
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
    public void saveROI(Client client) throws ServerException, ServiceException {
        Roi roi = (Roi) ExceptionHandler.of(client.getGateway(),
                                            g -> g.getUpdateService(client.getCtx())
                                                  .saveAndReturnObject(data.asIObject()))
                                        .handleServiceOrServer("Cannot save ROI")
                                        .get();
        data = new ROIData(roi);
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
     * Converts the ROI to a list of ImageJ ROIs.
     *
     * @return A list of ROIs.
     */
    public List<ij.gui.Roi> toImageJ() {
        return this.toImageJ(IJ_PROPERTY);
    }


    /**
     * Converts the ROI to a list of ImageJ ROIs.
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


    /**
     * Returns the type of annotation link for this object.
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Attach an {@link AnnotationData} to this object.
     *
     * @param <A>        The type of the annotation.
     * @param dm         The client handling the connection.
     * @param annotation The {@link AnnotationData}.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    protected <A extends AnnotationData> void link(DataManager dm, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        RoiAnnotationLink link = new RoiAnnotationLinkI();
        link.setChild(annotation.asAnnotation());
        link.setParent((Roi) data.asIObject());
        dm.save(link);
    }

}