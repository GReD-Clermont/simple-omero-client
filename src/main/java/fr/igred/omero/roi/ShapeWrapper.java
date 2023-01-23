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
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import omero.gateway.model.ShapeData;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Generic class containing a ShapeData (or a subclass) object.
 *
 * @param <T> Subclass of {@link ShapeData}
 */
public abstract class ShapeWrapper<T extends ShapeData> extends RemoteObjectWrapper<T> implements Shape<T> {

    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);


    /**
     * Constructor of the Shape class using a ShapeData.
     *
     * @param dataObject the shape
     */
    protected ShapeWrapper(T dataObject) {
        super(dataObject);
    }


    /**
     * Converts an IJ roi to a list of shapes.
     *
     * @param ijRoi An ImageJ ROI.
     *
     * @return A list of ShapeWrappers.
     */
    static ShapeList fromImageJ(ij.gui.Roi ijRoi) {
        ShapeList list = new ShapeList();
        int       type = ijRoi.getType();
        switch (type) {
            case Roi.FREEROI:
            case Roi.TRACED_ROI:
            case Roi.POLYGON:
                list.add(new PolygonWrapper(ijRoi));
                break;
            case Roi.FREELINE:
            case Roi.ANGLE:
            case Roi.POLYLINE:
                list.add(new PolylineWrapper(ijRoi));
                break;
            case Roi.LINE:
                list.add(new LineWrapper((ij.gui.Line) ijRoi));
                break;
            case Roi.OVAL:
                list.add(new EllipseWrapper(ijRoi));
                break;
            case Roi.POINT:
                list.addAll(PointWrapper.fromIJ(ijRoi));
                break;
            case Roi.COMPOSITE:
                List<ij.gui.Roi> rois = Arrays.asList(((ShapeRoi) ijRoi).getRois());
                rois.forEach(r -> r.setName(ijRoi.getName()));
                rois.forEach(r -> r.setPosition(ijRoi.getCPosition(),
                                                ijRoi.getZPosition(),
                                                ijRoi.getTPosition()));
                rois.stream().map(ShapeWrapper::fromImageJ).forEach(list::addAll);
                break;
            default:
                if (ijRoi instanceof TextRoi)
                    list.add(new TextWrapper((TextRoi) ijRoi));
                else
                    list.add(new RectangleWrapper(ijRoi));
                break;
        }
        return list;
    }


    /**
     * Copies details from an ImageJ ROI (position, stroke color, stroke width).
     *
     * @param ijRoi An ImageJ Roi.
     */
    protected void copy(ij.gui.Roi ijRoi) {
        data.setC(Math.max(-1, ijRoi.getCPosition() - 1));
        data.setZ(Math.max(-1, ijRoi.getZPosition() - 1));
        data.setT(Math.max(-1, ijRoi.getTPosition() - 1));
        LengthI size          = new LengthI(ijRoi.getStrokeWidth(), UnitsLength.POINT);
        Color   defaultStroke = Optional.ofNullable(Roi.getColor()).orElse(Color.YELLOW);
        Color   defaultFill   = Optional.ofNullable(Roi.getDefaultFillColor()).orElse(TRANSPARENT);
        Color   stroke        = Optional.ofNullable(ijRoi.getStrokeColor()).orElse(defaultStroke);
        Color   fill          = Optional.ofNullable(ijRoi.getFillColor()).orElse(defaultFill);
        data.getShapeSettings().setStrokeWidth(size);
        data.getShapeSettings().setStroke(stroke);
        data.getShapeSettings().setFill(fill);
    }


}
