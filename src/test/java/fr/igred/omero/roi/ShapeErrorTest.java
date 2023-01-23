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


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


class ShapeErrorTest {


    @Test
    void testPointNullCoordinates() {
        Point point = new Point();
        assertThrows(IllegalArgumentException.class, () -> point.setCoordinates(null));
    }


    @Test
    void testTextNullCoordinates() {
        Text text = new Text();
        assertThrows(IllegalArgumentException.class, () -> text.setCoordinates(null));
    }


    @Test
    void testRectangleNullCoordinates() {
        Rectangle rectangle = new Rectangle();
        assertThrows(IllegalArgumentException.class, () -> rectangle.setCoordinates(null));
    }


    @Test
    void testMaskNullCoordinates() {
        Mask mask = new Mask();
        assertThrows(IllegalArgumentException.class, () -> mask.setCoordinates(null));
    }


    @Test
    void testEllipseNullCoordinates() {
        Ellipse ellipse = new Ellipse();
        assertThrows(IllegalArgumentException.class, () -> ellipse.setCoordinates(null));
    }


    @Test
    void testLineNullCoordinates() {
        Line line = new Line();
        assertThrows(IllegalArgumentException.class, () -> line.setCoordinates(null));
    }


    @Test
    void testPointWrongCoordinates() {
        Point point       = new Point();
        double[]     coordinates = {2, 2, 4, 4};
        assertThrows(IllegalArgumentException.class, () -> point.setCoordinates(coordinates));
    }


    @Test
    void testTextWrongCoordinates() {
        Text text        = new Text();
        double[]    coordinates = {2, 2, 4, 4};
        assertThrows(IllegalArgumentException.class, () -> text.setCoordinates(coordinates));
    }


    @Test
    void testRectangleWrongCoordinates() {
        Rectangle rectangle   = new Rectangle();
        double[]         coordinates = {2, 2};
        assertThrows(IllegalArgumentException.class, () -> rectangle.setCoordinates(coordinates));
    }


    @Test
    void testMaskWrongCoordinates() {
        Mask mask        = new Mask();
        double[]    coordinates = {2, 2};
        assertThrows(IllegalArgumentException.class, () -> mask.setCoordinates(coordinates));
    }


    @Test
    void testEllipseWrongCoordinates() {
        Ellipse ellipse     = new Ellipse();
        double[]       coordinates = {2, 2};
        assertThrows(IllegalArgumentException.class, () -> ellipse.setCoordinates(coordinates));
    }


    @Test
    void testLineWrongCoordinates() {
        Line line        = new Line();
        double[]    coordinates = {2, 2};
        assertThrows(IllegalArgumentException.class, () -> line.setCoordinates(coordinates));
    }

}
