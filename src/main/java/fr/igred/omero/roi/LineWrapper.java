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


import ij.gui.Arrow;
import omero.gateway.model.LineData;


/**
 * Class containing an LineData.
 * <p> Wraps function calls to the LineData contained.
 */
public class LineWrapper extends ShapeWrapper<LineData> implements Line {


    /**
     * Constructor of the Line class using a LineData.
     *
     * @param dataObject the shape
     */
    public LineWrapper(LineData dataObject) {
        super(dataObject);
    }


    /**
     * Constructor of the Rectangle class using a new empty LineData.
     */
    public LineWrapper() {
        this(new LineData());
    }


    /**
     * Constructor of the Line class using an ImageJ Line ROI.
     *
     * @param line An ImageJ Line ROI.
     */
    public LineWrapper(ij.gui.Line line) {
        this(line.x1d, line.y1d, line.x2d, line.y2d);
        data.setText(line.getName());

        if (line instanceof Arrow) {
            data.getShapeSettings().setMarkerEnd(ARROW);
            if (((Arrow) line).getDoubleHeaded()) {
                data.getShapeSettings().setMarkerStart(ARROW);
            }
        }
        super.copy(line);
    }


    /**
     * Constructor of the Rectangle class using a new LineData.
     *
     * @param x1 x1-coordinate of the shape.
     * @param y1 y1-coordinate of the shape.
     * @param x2 x2-coordinate of the shape.
     * @param y2 y2-coordinate of the shape.
     */
    public LineWrapper(double x1, double y1, double x2, double y2) {
        this(new LineData(x1, y1, x2, y2));
    }


}
