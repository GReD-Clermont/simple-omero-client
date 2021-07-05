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


import org.junit.Test;


public class ShapeErrorTest {


    @Test(expected = IllegalArgumentException.class)
    public void testPointNullCoordinates() {
        PointWrapper point = new PointWrapper();
        point.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testTextNullCoordinates() {
        TextWrapper text = new TextWrapper();
        text.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRectangleNullCoordinates() {
        RectangleWrapper rectangle = new RectangleWrapper();
        rectangle.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testMaskNullCoordinates() {
        MaskWrapper mask = new MaskWrapper();
        mask.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEllipseNullCoordinates() {
        EllipseWrapper ellipse = new EllipseWrapper();
        ellipse.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testLineNullCoordinates() {
        LineWrapper line = new LineWrapper();
        line.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testPointWrongCoordinates() {
        PointWrapper point       = new PointWrapper();
        double[]     coordinates = {2, 2, 4, 4};
        point.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testTextWrongCoordinates() {
        TextWrapper text        = new TextWrapper();
        double[]    coordinates = {2, 2, 4, 4};
        text.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRectangleWrongCoordinates() {
        RectangleWrapper rectangle   = new RectangleWrapper();
        double[]         coordinates = {2, 2};
        rectangle.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testMaskWrongCoordinates() {
        MaskWrapper mask        = new MaskWrapper();
        double[]    coordinates = {2, 2};
        mask.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEllipseWrongCoordinates() {
        EllipseWrapper ellipse     = new EllipseWrapper();
        double[]       coordinates = {2, 2};
        ellipse.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testLineWrongCoordinates() {
        LineWrapper line        = new LineWrapper();
        double[]    coordinates = {2, 2};
        line.setCoordinates(coordinates);
    }

}
