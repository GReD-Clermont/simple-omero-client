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


import fr.igred.omero.RemoteObjectWrapper;
import omero.gateway.model.ROIData;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


/**
 * Class containing a ROIData object.
 * <p> Wraps function calls to the ROIData contained.
 */
public class ROIWrapper extends RemoteObjectWrapper<ROIData> implements ROI {


    /**
     * Constructor of the ROI class.
     */
    public ROIWrapper() {
        super(new ROIData());
    }


    /**
     * Constructor of the ROI class.
     *
     * @param shapes List of shapes to add to the ROIData.
     */
    public ROIWrapper(Iterable<? extends Shape<?>> shapes) {
        super(new ROIData());

        for (Shape<?> shape : shapes) {
            data.addShapeData(shape.asDataObject());
        }
    }


    /**
     * Constructor of the ROI class.
     *
     * @param dataObject ROIData to be contained.
     */
    public ROIWrapper(ROIData dataObject) {
        super(dataObject);
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs
     *
     * @param ijRois A list of ImageJ ROIs.
     *
     * @return The converted list of OMERO ROIs.
     */
    static List<ROI> fromImageJ(List<? extends ij.gui.Roi> ijRois) {
        return fromImageJ(ijRois, IJ_PROPERTY);
    }


    /**
     * Converts an ImageJ list of ROIs to a list of OMERO ROIs
     *
     * @param ijRois   A list of ImageJ ROIs.
     * @param property The property where 4D ROI local ID is stored. Defaults to {@value IJ_PROPERTY} if null or empty.
     *
     * @return The converted list of OMERO ROIs.
     */
    public static List<ROI> fromImageJ(List<? extends ij.gui.Roi> ijRois, String property) {
        property = ROI.checkProperty(property);
        Map<String, ROIWrapper> rois4D = new TreeMap<>();

        Map<Integer, ROIWrapper> shape2roi = new TreeMap<>();

        for (int i = 0; i < ijRois.size(); i++) {
            String value = ijRois.get(i).getProperty(property);
            if (value != null && !value.trim().isEmpty()) {
                rois4D.computeIfAbsent(value, val -> new ROIWrapper());
                shape2roi.put(i, rois4D.get(value));
            } else {
                shape2roi.put(i, new ROIWrapper());
            }
        }

        rois4D.forEach((name, roi) -> roi.setName(name));

        for (Map.Entry<Integer, ROIWrapper> entry : shape2roi.entrySet()) {
            ij.gui.Roi ijRoi = ijRois.get(entry.getKey());
            ROIWrapper roi   = entry.getValue();
            roi.addShape(ijRoi);
        }
        return shape2roi.values().stream().distinct().collect(Collectors.toList());
    }


    /**
     * Changes the wrapped data.
     *
     * @param roi The ROI data.
     */
    @Override
    public void replace(ROIData roi) {
        this.data = roi;
    }


    /**
     * Adds an ImageJ ROI to an OMERO ROI.
     *
     * @param ijRoi The ImageJ ROI.
     */
    private void addShape(ij.gui.Roi ijRoi) {
        addShapes(ShapeWrapper.fromImageJ(ijRoi));
    }

}