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


import fr.igred.omero.BasicTest;
import omero.gateway.model.EllipseData;
import omero.gateway.model.LineData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PointData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.TextData;
import org.junit.Test;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class ShapeTest extends BasicTest {


    @Test
    public void testPoint() {
        final String text  = "Point";
        PointWrapper point = new PointWrapper();

        double[] pointCoordinates = {25, 25};

        point.setCoordinates(pointCoordinates);
        point.setText(text);

        double[] checkCoordinates = point.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 2; i++)
            differences += Math.abs(checkCoordinates[i] - pointCoordinates[i]);

        assertEquals(0, differences, 0.001);
        assertEquals(text, point.getText());
    }


    @Test
    public void testText() {
        final String value = "Point";
        TextWrapper  text  = new TextWrapper();

        double[] textCoordinates = {1, 1};
        text.setCoordinates(textCoordinates);

        text.setText(value);
        text.setFontSize(25);
        double fontSize = text.getFontSize();

        double[] checkCoordinates = text.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 2; i++)
            differences += Math.abs(checkCoordinates[i] - textCoordinates[i]);

        assertEquals(0, differences, 0.001);
        assertEquals(25, fontSize, 0.001);
        assertEquals(value, text.getText());
    }


    @Test
    public void testRectangle() {
        RectangleWrapper rectangle = new RectangleWrapper();

        double[] rectangleCoordinates = {2, 2, 5, 5};
        rectangle.setCoordinates(rectangleCoordinates);

        double[] checkCoordinates = rectangle.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(checkCoordinates[i] - rectangleCoordinates[i]);

        assertEquals(0, differences, 0.001);
    }


    @Test
    public void testRectangleCZT() {
        final String     text      = "Rectangle";
        RectangleWrapper rectangle = new RectangleWrapper();

        double[] rectangleCoordinates = {2, 2, 5, 5};
        int      c                    = 1;
        int      z                    = 2;
        int      t                    = 3;

        rectangle.setCoordinates(rectangleCoordinates);
        rectangle.setText(text);
        rectangle.setC(c);
        rectangle.setZ(z);
        rectangle.setT(t);

        double[] checkCoordinates = rectangle.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(checkCoordinates[i] - rectangleCoordinates[i]);

        int c2 = rectangle.getC();
        int z2 = rectangle.getZ();
        int t2 = rectangle.getT();
        differences += Math.abs(c2 - c) + Math.abs(z2 - z) + Math.abs(t2 - t);

        assertEquals(0, differences, 0.001);
        assertEquals(text, rectangle.getText());
    }


    @Test
    public void testMask() {
        MaskWrapper mask = new MaskWrapper();

        double[] maskCoordinates = {3, 3, 10, 10};
        mask.setCoordinates(maskCoordinates);

        double[] checkCoordinates = mask.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(checkCoordinates[i] - maskCoordinates[i]);

        assertEquals(0, differences, 0.001);
    }


    @Test
    public void testValuesMask() {
        final String text = "Mask";
        MaskWrapper  mask = new MaskWrapper();
        mask.setCoordinates(3, 3, 10, 10);
        mask.setText(text);

        int[][] maskValues = new int[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                maskValues[i][j] = i >= 5 ? 1 : 0;
            }
        }
        mask.setMask(maskValues);

        int[][] checkValues = mask.getMaskAsBinaryArray();

        int differences = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                differences += Math.abs(maskValues[i][j] - checkValues[i][j]);
            }
        }

        assertEquals(0, differences);
        assertEquals(text, mask.getText());
    }


    @Test
    public void testEllipse() {
        final String   text    = "Ellipse";
        EllipseWrapper ellipse = new EllipseWrapper();

        Color stroke = Color.BLUE;
        ellipse.setStroke(stroke);

        double[] ellipseCoordinates = {9, 11, 5, 10};
        ellipse.setCoordinates(ellipseCoordinates[0],
                               ellipseCoordinates[1],
                               ellipseCoordinates[2],
                               ellipseCoordinates[3]);
        ellipse.setText(text);

        double[] checkCoordinates = ellipse.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(checkCoordinates[i] - ellipseCoordinates[i]);

        assertEquals(0, differences, 0.001);
        assertEquals(stroke, ellipse.getStroke());
        assertEquals(text, ellipse.getText());
    }


    @Test
    public void testLine() {
        final String text = "Line";
        LineWrapper  line = new LineWrapper();

        double[] lineCoordinates = {3, 3, 10, 10};
        line.setCoordinates(lineCoordinates[0],
                            lineCoordinates[1],
                            lineCoordinates[2],
                            lineCoordinates[3]);
        line.setText(text);

        double[] checkCoordinates = line.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(checkCoordinates[i] - lineCoordinates[i]);

        assertEquals(0, differences, 0.001);
        assertEquals(text, line.getText());
    }


    @Test
    public void testPointsLine() {
        LineWrapper line = new LineWrapper();

        double[] lineCoordinates = {3, 3, 10, 10};
        line.setCoordinates(lineCoordinates[0],
                            lineCoordinates[1],
                            lineCoordinates[2],
                            lineCoordinates[3]);

        double[] checkCoordinates = line.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(checkCoordinates[i] - lineCoordinates[i]);

        assertEquals(0, differences, 0.001);
    }


    @Test
    public void testPolyline() {
        final String         text     = "Polyline";
        PolylineWrapper      polyline = new PolylineWrapper();
        List<Point2D.Double> points   = new ArrayList<>();

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        polyline.setText(text);
        polyline.setPoints(points);
        List<Point2D.Double> points2 = polyline.getPoints();

        assertEquals(points, points2);
        assertEquals(text, polyline.getText());
    }


    @Test
    public void testPolygon() {
        final String         text    = "Polygon";
        PolygonWrapper       polygon = new PolygonWrapper();
        List<Point2D.Double> points  = new ArrayList<>();

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        polygon.setPoints(points);
        polygon.setText(text);

        List<Point2D.Double> points2 = polygon.getPoints();

        assertEquals(points, points2);
        assertEquals(text, polygon.getText());
    }


    @Test
    public void testPointConstructor() {
        PointWrapper point1 = new PointWrapper(0, 0);
        PointWrapper point2 = new PointWrapper(new PointData(25, 25));

        double[] pointCoordinates = {25, 25};
        point1.setCoordinates(pointCoordinates);

        double[] coordinates1 = point1.getCoordinates();
        double[] coordinates2 = point2.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 2; i++)
            differences += Math.abs(coordinates1[i] - coordinates2[i]);

        assertEquals(0, differences, 0.001);
    }


    @Test
    public void testTextConstructor() {
        TextWrapper text1 = new TextWrapper("Text1", 0, 0);
        TextWrapper text2 = new TextWrapper(new TextData("Text1", 25, 25));

        double[] textCoordinates = {25, 25};
        text1.setCoordinates(textCoordinates);

        double[] coordinates1 = text1.getCoordinates();
        double[] coordinates2 = text2.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 2; i++)
            differences += Math.abs(coordinates1[i] - coordinates2[i]);

        assertEquals(0, differences, 0.001);
        assertEquals(text1.getText(), text2.getText());
    }


    @Test
    public void testLineConstructor() {
        LineWrapper line1 = new LineWrapper(0, 0, 0, 0);
        LineWrapper line2 = new LineWrapper(new LineData(25, 25, 50, 50));

        double[] lineCoordinates = {25, 25, 50, 50};
        line1.setCoordinates(lineCoordinates);

        double[] coordinates1 = line1.getCoordinates();
        double[] coordinates2 = line2.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(coordinates1[i] - coordinates2[i]);

        assertEquals(0, differences, 0.001);
    }


    @Test
    public void testRectangleConstructor() {
        RectangleWrapper r1 = new RectangleWrapper(0, 0, 0, 0);
        RectangleWrapper r2 = new RectangleWrapper(new RectangleData(25, 25, 50, 50));

        double[] rectangleCoordinates = {25, 25, 50, 50};
        r1.setCoordinates(rectangleCoordinates);

        double[] coordinates1 = r1.getCoordinates();
        double[] coordinates2 = r2.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(coordinates1[i] - coordinates2[i]);

        assertEquals(0, differences, 0.001);
    }


    @Test
    public void testMaskConstructor() {
        byte[] maskValues = new byte[25];
        for (int i = 0; i < 25; i++) {
            maskValues[i] = (byte) (i >= 12 ? 1 : 0);
        }

        MaskWrapper m1 = new MaskWrapper(10, 10, 5, 5, maskValues);
        MaskWrapper m2 = new MaskWrapper(new MaskData(0, 0, 5, 5, maskValues));
        m1.setCoordinates(0, 0, 5, 5);

        double[] coordinates1 = m1.getCoordinates();
        double[] coordinates2 = m2.getCoordinates();

        byte[] checkValues1 = m1.getMask();
        byte[] checkValues2 = m2.getMask();

        int differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(coordinates1[i] - coordinates2[i]);
        for (int i = 0; i < 25; i++)
            differences += Math.abs(checkValues2[i] - checkValues1[i]);

        assertEquals(0, differences, 0.001);
    }


    @Test
    public void testEllipseConstructor() {
        EllipseWrapper e1 = new EllipseWrapper(0, 0, 0, 0);
        EllipseWrapper e2 = new EllipseWrapper(new EllipseData(25, 25, 50, 50));

        double[] ellipseCoordinates = {25, 25, 50, 50};
        e1.setCoordinates(ellipseCoordinates);

        double[] coordinates1 = e1.getCoordinates();
        double[] coordinates2 = e2.getCoordinates();

        double differences = 0;
        for (int i = 0; i < 4; i++)
            differences += Math.abs(coordinates1[i] - coordinates2[i]);

        assertEquals(0, differences, 0.001);
    }


    @Test
    public void testPolylineConstructor() {
        List<Point2D.Double> points = new ArrayList<>();

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        PolylineWrapper      polyline = new PolylineWrapper(points);
        List<Point2D.Double> points2  = polyline.getPoints();

        assertEquals(points, points2);
    }


    @Test
    public void testPolygonConstructor() {
        List<Point2D.Double> points = new ArrayList<>();

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        PolygonWrapper       polygon = new PolygonWrapper(points);
        List<Point2D.Double> points2 = polygon.getPoints();

        assertEquals(points, points2);
    }


    @Test
    public void testAWTRectangle() {
        RectangleWrapper shape     = new RectangleWrapper(25, 26, 27, 28);
        Rectangle2D      awtShape  = new Rectangle2D.Double(25, 26, 27, 28);
        java.awt.Shape   awtShape2 = shape.toAWTShape();
        assertEquals(awtShape, awtShape2);
    }


    @Test
    public void testAWTMask() {
        byte[] maskValues = new byte[25];
        for (int i = 0; i < 25; i++) {
            maskValues[i] = (byte) (i >= 12 ? 1 : 0);
        }

        MaskWrapper    shape     = new MaskWrapper(25, 26, 27, 28, maskValues);
        Rectangle2D    awtShape  = new Rectangle2D.Double(25, 26, 27, 28);
        java.awt.Shape awtShape2 = shape.toAWTShape();
        assertEquals(awtShape, awtShape2);
    }


    @Test
    public void testAWTEllipse() {
        EllipseWrapper shape     = new EllipseWrapper(28, 27, 26, 25);
        Ellipse2D      awtShape  = new Ellipse2D.Double(2, 2, 52, 50);
        java.awt.Shape awtShape2 = shape.toAWTShape();
        assertEquals(awtShape, awtShape2);
    }


    @Test
    public void testAWTLine() {
        LineWrapper shape     = new LineWrapper(0, 1, 2, 3);
        Line2D      awtShape  = new Line2D.Double(0, 1, 2, 3);
        Line2D      awtShape2 = (Line2D) shape.toAWTShape();
        assertEquals(awtShape.getP1(), awtShape2.getP1());
        assertEquals(awtShape.getP2(), awtShape2.getP2());
    }


    @Test
    public void testAWTPolygon() {
        List<Point2D.Double> points = new ArrayList<>();

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        PolygonWrapper shape = new PolygonWrapper(points);

        Path2D awtShape = new Path2D.Double();
        awtShape.moveTo(p1.x, p1.y);
        awtShape.lineTo(p2.x, p2.y);
        awtShape.lineTo(p3.x, p3.y);
        awtShape.closePath();

        java.awt.Shape awtShape2 = shape.toAWTShape();

        PathIterator pi  = awtShape.getPathIterator(null);
        PathIterator pi2 = awtShape2.getPathIterator(null);

        while (!pi.isDone() && !pi2.isDone()) {
            double[] pos1 = new double[2];
            double[] pos2 = new double[2];
            assertEquals(pi.currentSegment(pos1), pi.currentSegment(pos2));
            assertEquals(pos1[0], pos2[0], 0.01);
            assertEquals(pos1[1], pos2[1], 0.01);
            pi.next();
            pi2.next();
        }
    }


    @Test
    public void testAWTPolyline() {
        List<Point2D.Double> points = new ArrayList<>();

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points.add(p1);
        points.add(p2);
        points.add(p3);

        PolylineWrapper shape = new PolylineWrapper(points);

        Path2D awtShape = new Path2D.Double();
        awtShape.moveTo(p1.x, p1.y);
        awtShape.lineTo(p2.x, p2.y);
        awtShape.lineTo(p3.x, p3.y);
        awtShape.closePath();

        java.awt.Shape awtShape2 = shape.toAWTShape();

        PathIterator pi  = awtShape.getPathIterator(null);
        PathIterator pi2 = awtShape2.getPathIterator(null);

        while (!pi.isDone() && !pi2.isDone()) {
            double[] pos1 = new double[2];
            double[] pos2 = new double[2];
            assertEquals(pi.currentSegment(pos1), pi.currentSegment(pos2));
            assertEquals(pos1[0], pos2[0], 0.01);
            assertEquals(pos1[1], pos2[1], 0.01);
            pi.next();
            pi2.next();
        }
    }


    @Test
    public void testAWTPoint() {
        PointWrapper shape = new PointWrapper(1, 2);

        Path2D awtShape = new Path2D.Double();
        awtShape.moveTo(1, 2);

        java.awt.Shape awtShape2 = shape.toAWTShape();

        PathIterator pi  = awtShape.getPathIterator(null);
        PathIterator pi2 = awtShape2.getPathIterator(null);

        while (!pi.isDone() && !pi2.isDone()) {
            double[] pos1 = new double[2];
            double[] pos2 = new double[2];
            assertEquals(pi.currentSegment(pos1), pi.currentSegment(pos2));
            assertEquals(pos1[0], pos2[0], 0.01);
            assertEquals(pos1[1], pos2[1], 0.01);
            pi.next();
            pi2.next();
        }
    }


    @Test
    public void testAWTText() {
        TextWrapper shape = new TextWrapper("Text", 1, 2);

        Path2D awtShape = new Path2D.Double();
        awtShape.moveTo(1, 2);

        java.awt.Shape awtShape2 = shape.toAWTShape();

        PathIterator pi  = awtShape.getPathIterator(null);
        PathIterator pi2 = awtShape2.getPathIterator(null);

        while (!pi.isDone() && !pi2.isDone()) {
            double[] pos1 = new double[2];
            double[] pos2 = new double[2];
            assertEquals(pi.currentSegment(pos1), pi.currentSegment(pos2));
            assertEquals(pos1[0], pos2[0], 0.01);
            assertEquals(pos1[1], pos2[1], 0.01);
            pi.next();
            pi2.next();
        }
    }


    @Test
    public void testAffineTransform() {
        RectangleWrapper shape = new RectangleWrapper(1, 2, 3, 4);
        shape.setTransform(1, 2, 3, 4, 5, 6);

        AffineTransform transform = new AffineTransform(1, 2, 3, 4, 5, 6);
        assertEquals(transform, shape.toAWTTransform());
    }


    @Test
    public void testNoAffineTransform() {
        RectangleWrapper shape = new RectangleWrapper(1, 2, 3, 4);

        java.awt.Shape awtShape = new Rectangle2D.Double(1, 2, 3, 4);
        assertEquals(awtShape, shape.createTransformedAWTShape());
    }


    @Test
    public void testBoundingBox() {
        RectangleWrapper shape = new RectangleWrapper(1, 2, 3, 4);
        RectangleWrapper box   = new RectangleWrapper(-6, 1, 4, 3);

        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.PI / 2);
        double[] a = new double[6];
        transform.getMatrix(a);
        shape.setTransform(a[0], a[1], a[2], a[3], a[4], a[5]);

        double[] coordinates1 = box.getCoordinates();
        double[] coordinates2 = shape.getBoundingBox().getCoordinates();

        for (int i = 0; i < 4; i++) {
            assertEquals(coordinates1[i], coordinates2[i], 0.01);
        }
    }


    @Test
    public void testBoundingBox2() {
        EllipseWrapper   shape = new EllipseWrapper(50, 50, 20, 40);
        RectangleWrapper box   = new RectangleWrapper(30, 10, 40, 80);

        double[] coordinates1 = box.getCoordinates();
        double[] coordinates2 = shape.getBoundingBox().getCoordinates();

        for (int i = 0; i < 4; i++) {
            assertEquals(coordinates1[i], coordinates2[i], 0.01);
        }
    }

}
