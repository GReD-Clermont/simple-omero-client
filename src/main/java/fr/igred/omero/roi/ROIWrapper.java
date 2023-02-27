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
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.util.Wrapper;
import omero.RString;
import omero.gateway.model.ROIData;
import omero.gateway.model.ShapeData;
import omero.model.Roi;
import omero.model._RoiOperationsNC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Class containing a ROIData object.
 * <p> Wraps function calls to the ROIData contained.
 */
public class ROIWrapper extends AnnotatableWrapper<ROIData> implements ROI {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "RoiAnnotationLink";


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
     * Converts a collection of ImageJ ROIs to a list of OMERO ROIs
     *
     * @param ijRois A collection of ImageJ ROIs.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROI> fromImageJ(Collection<? extends ij.gui.Roi> ijRois) {
        return ROI.fromImageJ(ijRois, ROIWrapper::new, ShapeWrapper::fromImageJ);
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
    public static List<ROI> fromImageJ(Collection<? extends ij.gui.Roi> ijRois, String property) {
        return ROI.fromImageJ(ijRois, property, ROIWrapper::new, ShapeWrapper::fromImageJ);
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
     * Adds a Shape to the ROI.
     *
     * @param shape Shape to add.
     */
    @Override
    public void addShape(Shape shape) {
        data.addShapeData(shape.asDataObject());
    }


    /**
     * Returns the list of shapes contained in the ROI.
     *
     * @return See above.
     */
    @Override
    public List<Shape> getShapes() {
        List<ShapeData> shapeData = data.getShapes();
        List<Shape>     shapes    = new ArrayList<>(shapeData.size());
        shapeData.stream().sorted(Comparator.comparing(ShapeData::getId)).forEach(s -> shapes.add(Wrapper.wrap(s)));
        return shapes;
    }


    /**
     * Sets the image linked to the ROI.
     *
     * @param image Image linked to the ROI.
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
     * @param pos Position of the ShapeData in the ShapeData list from the ROI.
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
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void saveROI(DataManager dm) throws ServiceException, AccessException, ExecutionException {
        Roi roi = (Roi) dm.save(data.asIObject());
        data = new ROIData(roi);
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