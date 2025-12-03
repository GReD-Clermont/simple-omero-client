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


import fr.igred.omero.BasicTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


class ShapeErrorTest extends BasicTest {


    @Test
    void testPointNullCoordinates() {
        Punctual point = new PointWrapper();
        assertThrows(IllegalArgumentException.class,
                     () -> point.setCoordinates(null));
    }


    @Test
    void testTextNullCoordinates() {
        Punctual text = new TextWrapper();
        assertThrows(IllegalArgumentException.class,
                     () -> text.setCoordinates(null));
    }


    @Test
    void testRectangleNullCoordinates() {
        Rectangular rectangle = new RectangleWrapper();
        assertThrows(IllegalArgumentException.class,
                     () -> rectangle.setCoordinates(null));
    }


    @Test
    void testMaskNullCoordinates() {
        Rectangular mask = new MaskWrapper();
        assertThrows(IllegalArgumentException.class,
                     () -> mask.setCoordinates(null));
    }


    @Test
    void testEllipseNullCoordinates() {
        Ellipse ellipse = new EllipseWrapper();
        assertThrows(IllegalArgumentException.class,
                     () -> ellipse.setCoordinates(null));
    }


    @Test
    void testLineNullCoordinates() {
        Line line = new LineWrapper();
        assertThrows(IllegalArgumentException.class,
                     () -> line.setCoordinates(null));
    }


    @Test
    void testPointWrongCoordinates() {
        Punctual point       = new PointWrapper();
        double[] coordinates = {2, 2, 4, 4};
        assertThrows(IllegalArgumentException.class,
                     () -> point.setCoordinates(coordinates));
    }


    @Test
    void testTextWrongCoordinates() {
        Punctual text        = new TextWrapper();
        double[] coordinates = {2, 2, 4, 4};
        assertThrows(IllegalArgumentException.class,
                     () -> text.setCoordinates(coordinates));
    }


    @Test
    void testRectangleWrongCoordinates() {
        Rectangular rectangle   = new RectangleWrapper();
        double[]    coordinates = {2, 2};
        assertThrows(IllegalArgumentException.class,
                     () -> rectangle.setCoordinates(coordinates));
    }


    @Test
    void testMaskWrongCoordinates() {
        Rectangular mask        = new MaskWrapper();
        double[]    coordinates = {2, 2};
        assertThrows(IllegalArgumentException.class,
                     () -> mask.setCoordinates(coordinates));
    }


    @Test
    void testEllipseWrongCoordinates() {
        Ellipse  ellipse     = new EllipseWrapper();
        double[] coordinates = {2, 2};
        assertThrows(IllegalArgumentException.class,
                     () -> ellipse.setCoordinates(coordinates));
    }


    @Test
    void testLineWrongCoordinates() {
        Line     line        = new LineWrapper();
        double[] coordinates = {2, 2};
        assertThrows(IllegalArgumentException.class,
                     () -> line.setCoordinates(coordinates));
    }

}
