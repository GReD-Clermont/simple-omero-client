/*
 *  Copyright (C) 2020 GReD
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


import fr.igred.omero.ObjectWrapper;
import ome.model.units.BigResult;
import omero.gateway.model.ShapeData;
import omero.gateway.model.PointData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.EllipseData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.MaskData;
import omero.gateway.model.TextData;
import omero.gateway.model.LineData;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class containing a ShapeData
 * <p> Implements functions using the ShapeData contained
 */
public class ShapeWrapper<T extends ShapeData> extends ObjectWrapper<T> {


    /**
     * Constructor of the ShapeWrapper class using a ShapeData.
     *
     * @param shape the shape
     */
    public ShapeWrapper(T shape) {
        super(shape);
    }


    /**
     * Gets the ShapeData object contained.
     *
     * @return the shape.
     */
    public T getShape() {
        return data;
    }


    /**
     * Gets the channel.
     *
     * @return the channel. -1 if the shape applies to all channels of the image.
     */
    public int getC() {
        return this.data.getC();
    }


    /**
     * Sets the channel.
     *
     * @param c the channel. Pass -1 to remove z value, i. e. shape applies to all channels of the image.
     */
    public void setC(int c) {
        this.data.setC(c);
    }


    /**
     * Gets the z-section.
     *
     * @return the z-section. -1 if the shape applies to all z-sections of the image.
     */
    public int getZ() {
        return this.data.getZ();
    }


    /**
     * Sets the z-section.
     *
     * @param z the z-section. Pass -1 to remove z value, i. e. shape applies to all z-sections of the image.
     */
    public void setZ(int z) {
        this.data.setZ(z);
    }


    /**
     * Sets the time-point.
     *
     * @return the time-point. -1 if the shape applies to all time-points of the image.
     */
    public int getT() {
        return this.data.getT();
    }


    /**
     * Sets the time-point.
     *
     * @param t the time-point. Pass -1 to remove t value, i. e. shape applies to all time-points of the image.
     */
    public void setT(int t) {
        this.data.setT(t);
    }


    /**
     * Gets ShapeData font size
     *
     * @return The font size (in typography points)
     */
    public double getFontSize() {
        double fontSize = Double.NaN;
        try {
            fontSize = data.getShapeSettings().getFontSize(UnitsLength.POINT).getValue();
        } catch (BigResult bigResult) {
            Logger.getLogger(getClass().getName())
                  .log(Level.WARNING, "Error while getting font size from ShapeData.", bigResult);
        }
        return fontSize;
    }


    /**
     * Sets ShapeData font size
     *
     * @param value The font size (in typography points)
     */
    public void setFontSize(double value) {
        LengthI size = new LengthI(value, UnitsLength.POINT);
        data.getShapeSettings().setFontSize(size);
    }


    /**
     * Sets ShapeData stroke color
     *
     * @return The stroke color
     */
    public Color getStroke() {
        return data.getShapeSettings().getStroke();
    }


    /**
     * Sets ShapeData stroke color
     *
     * @param color The stroke color
     */
    public void setStroke(Color color) {
        data.getShapeSettings().setStroke(color);
    }


    /**
     * Gets the text on the ShapeData.
     *
     * @return the text
     */
    public String getText() {
        String text = null;
        if (PointData.class.equals(data.getClass())) {
            text = ((PointData) this.data).getText();
        } else if (LineData.class.equals(data.getClass())) {
            text = ((LineData) this.data).getText();
        } else if (PolylineData.class.equals(data.getClass())) {
            text = ((PolylineData) this.data).getText();
        } else if (RectangleData.class.equals(data.getClass())) {
            text = ((RectangleData) this.data).getText();
        } else if (PolygonData.class.equals(data.getClass())) {
            text = ((PolygonData) this.data).getText();
        } else if (EllipseData.class.equals(data.getClass())) {
            text = ((EllipseData) this.data).getText();
        } else if (MaskData.class.equals(data.getClass())) {
            text = ((MaskData) this.data).getText();
        } else if (TextData.class.equals(data.getClass())) {
            text = ((TextData) this.data).getText();
        }
        return text;
    }


    /**
     * Sets the text on the ShapeData.
     *
     * @param text the text
     */
    public void setText(String text) {
        if (PointData.class.equals(data.getClass())) {
            ((PointData) this.data).setText(text);
        } else if (LineData.class.equals(data.getClass())) {
            ((LineData) this.data).setText(text);
        } else if (PolylineData.class.equals(data.getClass())) {
            ((PolylineData) this.data).setText(text);
        } else if (RectangleData.class.equals(data.getClass())) {
            ((RectangleData) this.data).setText(text);
        } else if (PolygonData.class.equals(data.getClass())) {
            ((PolygonData) this.data).setText(text);
        } else if (EllipseData.class.equals(data.getClass())) {
            ((EllipseData) this.data).setText(text);
        } else if (MaskData.class.equals(data.getClass())) {
            ((MaskData) this.data).setText(text);
        } else if (TextData.class.equals(data.getClass())) {
            ((TextData) this.data).setText(text);
        }
    }

}
