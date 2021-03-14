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

package fr.igred.omero;


import fr.igred.omero.metadata.ShapeContainer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import omero.gateway.model.ShapeData;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


public class ShapeTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName Name of the test case.
     */
    public ShapeTest(String testName) {
        super(testName);
    }


    /**
     * @return the suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(ShapeTest.class);
    }


    public void testPointShapeContainer() {
        ShapeContainer point = new ShapeContainer(ShapeContainer.POINT);

        double[] pointCoordinates = {0, 0};
        double[] lineCoordinates  = {2, 2, 4, 4};

        point.setPointCoordinates(pointCoordinates[0], pointCoordinates[1]);
        point.setLineCoordinates(2, 2, 4, 4);
        point.setCoordinates(null);
        point.setCoordinates(lineCoordinates);
        point.setText("Point");

        double[] checkCoordinates = point.getCoordinates();

        double difference = 0;
        for (int i = 0; i < 2; i++)
            difference += Math.abs(checkCoordinates[i] - pointCoordinates[i]);

        assertEquals(0, difference, 0.001);
        assertEquals("Point", point.getText());
    }


    public void testTextShapeContainer() {
        ShapeContainer text = new ShapeContainer(ShapeContainer.TEXT);

        double[] textCoordinates      = {1, 1};
        double[] rectangleCoordinates = {2, 2, 5, 5};

        text.setPointCoordinates(textCoordinates[0], textCoordinates[1]);
        text.setCoordinates(rectangleCoordinates);
        text.setText("Text");
        text.setFontSize(25);
        double fontSize = text.getFontSize();

        double[] checkCoordinates = text.getCoordinates();

        double difference = 0;
        for (int i = 0; i < 2; i++)
            difference += Math.abs(checkCoordinates[i] - textCoordinates[i]);

        assertEquals(0, difference, 0.001);
        assertEquals(25, fontSize, 0.001);
        assertEquals("Text", text.getText());
    }


    public void testRectangleShapeContainer() {
        ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);

        double[] pointCoordinates     = {0, 0};
        double[] rectangleCoordinates = {2, 2, 5, 5};

        rectangle.setCoordinates(rectangleCoordinates);
        rectangle.setCoordinates(pointCoordinates);
        rectangle.setPointCoordinates(0, 0);

        double[] checkCoordinates = rectangle.getCoordinates();

        double difference = 0;
        for (int i = 0; i < 4; i++)
            difference += Math.abs(checkCoordinates[i] - rectangleCoordinates[i]);

        int[][] maskValues = new int[10][10];
        rectangle.setMask(maskValues);

        int[][] checkValues = rectangle.getMask();

        assertNull(checkValues);
        assertEquals(0, difference, 0.001);
    }


    public void testRectangleShapeContainerCZT() {
        ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);

        double[] rectangleCoordinates = {2, 2, 5, 5};
        int      c                    = 1;
        int      z                    = 2;
        int      t                    = 3;

        rectangle.setCoordinates(rectangleCoordinates);
        rectangle.setEllipseCoordinates(9, 11, 5, 10);
        rectangle.setText("Rectangle");
        rectangle.setC(c);
        rectangle.setZ(z);
        rectangle.setT(t);

        double[] checkCoordinates = rectangle.getCoordinates();

        double difference = 0;
        for (int i = 0; i < 4; i++)
            difference += Math.abs(checkCoordinates[i] - rectangleCoordinates[i]);

        int c2 = rectangle.getC();
        int z2 = rectangle.getZ();
        int t2 = rectangle.getT();
        difference += Math.abs(c2 - c) + Math.abs(z2 - z) + Math.abs(t2 - t);

        assertEquals(0, difference, 0.001);
        assertEquals("Rectangle", rectangle.getText());
    }


    public void testMaskShapeContainer() {
        ShapeContainer mask = new ShapeContainer(ShapeContainer.MASK);

        double[] maskCoordinates = {3, 3, 10, 10};
        mask.setCoordinates(maskCoordinates);

        double[] pointCoordinates = {0, 0};
        mask.setEllipseCoordinates(6, 6, 20, 20);
        mask.setCoordinates(pointCoordinates);

        double[] checkCoordinates = mask.getCoordinates();

        double difference = 0;
        for (int i = 0; i < 4; i++)
            difference += Math.abs(checkCoordinates[i] - maskCoordinates[i]);

        assertEquals(0, difference, 0.001);
    }


    public void testValuesMaskShapeContainer() {
        ShapeContainer mask = new ShapeContainer(ShapeContainer.MASK);
        mask.setRectangleCoordinates(3, 3, 10, 10);
        mask.setText("Mask");

        int[][] maskValues = new int[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                maskValues[i][j] = i >= 5 ? 1 : 0;
            }
        }
        mask.setMask(maskValues);

        int[][] checkValues = mask.getMask();

        int difference = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                difference += Math.abs(maskValues[i][j] - checkValues[i][j]);
            }
        }

        assertEquals(0, difference);
        assertEquals("Mask", mask.getText());
    }


    public void testEllipseShapeContainer() {
        ShapeContainer ellipse = new ShapeContainer(ShapeContainer.ELLIPSE);

        double[] ellipseCoordinates = {9, 11, 5, 10};
        ellipse.setEllipseCoordinates(ellipseCoordinates[0],
                                      ellipseCoordinates[1],
                                      ellipseCoordinates[2],
                                      ellipseCoordinates[3]);
        ellipse.setRectangleCoordinates(3, 3, 4, 9);
        ellipse.setText("Ellipse");

        double[] pointCoordinates = {0, 0};
        ellipse.setCoordinates(pointCoordinates);

        double[] checkCoordinates = ellipse.getCoordinates();

        double difference = 0;
        for (int i = 0; i < 4; i++)
            difference += Math.abs(checkCoordinates[i] - ellipseCoordinates[i]);

        assertEquals(0, difference, 0.001);
        assertEquals("Ellipse", ellipse.getText());
    }


    public void testLineShapeContainer() {
        ShapeContainer line = new ShapeContainer(ShapeContainer.LINE);

        double[] lineCoordinates = {3, 3, 10, 10};
        line.setLineCoordinates(lineCoordinates[0],
                                lineCoordinates[1],
                                lineCoordinates[2],
                                lineCoordinates[3]);
        line.setText("Line");

        double[] pointCoordinates = {0, 0};
        line.setCoordinates(pointCoordinates);

        double[] checkCoordinates = line.getCoordinates();

        double difference = 0;
        for (int i = 0; i < 4; i++)
            difference += Math.abs(checkCoordinates[i] - lineCoordinates[i]);

        assertEquals(0, difference, 0.001);
        assertEquals("Line", line.getText());
    }


    public void testPointsLineShapeContainer() {
        ShapeContainer       line   = new ShapeContainer(ShapeContainer.LINE);
        List<Point2D.Double> points = new ArrayList<>();

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        double[] lineCoordinates = {3, 3, 10, 10};
        line.setLineCoordinates(lineCoordinates[0],
                                lineCoordinates[1],
                                lineCoordinates[2],
                                lineCoordinates[3]);
        line.setPoints(points);
        List<Point2D.Double> points2 = line.getPoints();

        double[] checkCoordinates = line.getCoordinates();

        double difference = 0;
        for (int i = 0; i < 4; i++)
            difference += Math.abs(checkCoordinates[i] - lineCoordinates[i]);


        assertNull(points2);
        assertEquals(0, difference, 0.001);
    }


    public void testPolylineShapeContainer() {
        ShapeContainer       polyline = new ShapeContainer(ShapeContainer.POLYLINE);
        List<Point2D.Double> points   = new ArrayList<>();

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        polyline.setText("Polyline");
        polyline.setPoints(points);
        List<Point2D.Double> points2 = polyline.getPoints();

        assertEquals(points, points2);
        assertEquals("Polyline", polyline.getText());
    }


    public void testCoordinatesPolylineShapeContainer() {
        ShapeContainer       polyline = new ShapeContainer(ShapeContainer.POLYLINE);
        List<Point2D.Double> points   = new ArrayList<>();

        polyline.setLineCoordinates(3, 3, 10, 10);

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        polyline.setPoints(points);

        List<Point2D.Double> points2 = polyline.getPoints();

        double[] polylineCoordinates = polyline.getCoordinates();

        assertNull(polylineCoordinates);
        assertEquals(points, points2);
    }


    public void testPolygonShapeContainer() {
        ShapeContainer       polygon = new ShapeContainer(ShapeContainer.POLYGON);
        List<Point2D.Double> points  = new ArrayList<>();

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        polygon.setPoints(points);
        polygon.setText("Polygon");

        List<Point2D.Double> points2 = polygon.getPoints();

        assertEquals(points, points2);
        assertEquals("Polygon", polygon.getText());
    }


    public void testEmptyShapeData() {
        ShapeContainer empty = new ShapeContainer((ShapeData) null);
        List<Point2D.Double> points           = new ArrayList<>();
        double[]             pointCoordinates = {1, 1};

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        empty.setPointCoordinates(0, 0);
        empty.setCoordinates(pointCoordinates);
        empty.setPoints(points);
        empty.setText("Empty");

        assertNull(empty.getText());
        assertNull(empty.getPoints());
        assertNull(empty.getCoordinates());
    }

}
