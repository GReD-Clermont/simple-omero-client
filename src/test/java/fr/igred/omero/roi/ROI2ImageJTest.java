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


import fr.igred.omero.BasicTest;
import ij.gui.Arrow;
import ij.gui.EllipseRoi;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.igred.omero.RemoteObject.getElementsOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class ROI2ImageJTest extends BasicTest {


    @Test
    void testROIsFromImageJ() {
        Collection<Roi> rois = new ArrayList<>(11);

        float[] x1 = {0.0f, 3.0f, 3.0f};
        float[] y1 = {0.0f, 0.0f, 4.0f};
        float[] x2 = {0.0f, 0.0f, 4.0f};
        float[] y2 = {0.0f, 3.0f, 3.0f};

        Roi rectangle = new Roi(1.0, 2.0, 3.0, 4.0);
        rectangle.setName("rectangle");
        rectangle.setPosition(1, 2, 3);
        rectangle.setProperty("ROI", "24");
        rectangle.setProperty("ROI_NAME", "");
        rois.add(rectangle);

        TextRoi textRoi = new TextRoi(3.0, 4.0, "Text");
        textRoi.setName("text");
        textRoi.setProperty("ROI", "24");
        textRoi.setProperty("ROI_NAME", "text");
        rois.add(textRoi);

        OvalRoi ovalRoi = new OvalRoi(4.0, 5.0, 6.0, 7.0);
        ovalRoi.setName("oval");
        ovalRoi.setPosition(1, 0, 3);
        ovalRoi.setProperty("ROI", " ");
        rois.add(ovalRoi);

        Arrow arrow = new Arrow(2.0, 3.0, 3.0, 4.0);
        arrow.setDoubleHeaded(true);
        arrow.setName("arrow");
        rois.add(arrow);

        ij.gui.Line line = new ij.gui.Line(4.0, 3.0, 2.0, 1.0);
        rois.add(line);

        PointRoi pointRoi = new PointRoi(x1, y1);
        pointRoi.setProperty("TEST", "24");
        rois.add(pointRoi);

        PolygonRoi polylineRoi = new PolygonRoi(x2, y2, Roi.POLYLINE);
        polylineRoi.setPosition(1, 1, 2);
        polylineRoi.setProperty("ROI", "23");
        polylineRoi.setProperty("ROI_NAME", "23");
        rois.add(polylineRoi);

        PolygonRoi polygonRoi = new PolygonRoi(x2, y2, Roi.POLYGON);
        polygonRoi.setPosition(1, 1, 1);
        polygonRoi.setProperty("ROI", "23");
        polygonRoi.setProperty("ROI_NAME", "233");
        rois.add(polygonRoi);

        EllipseRoi ellipseRoi = new EllipseRoi(0.0, 0.0, 5.0, 5.0, 0.5);
        ellipseRoi.setPosition(1, 1, 1);
        rois.add(ellipseRoi);

        ShapeRoi shapeRoi = new ShapeRoi(new Ellipse2D.Double(0.0, 5.0, 5.0, 10.0));
        shapeRoi.setPosition(1, 3, 1);
        rois.add(shapeRoi);

        rois.add(ellipseRoi);

        List<ROI> omeroROIs = ROI.fromImageJ(rois);

        assertEquals(8, omeroROIs.size());
        assertEquals(1, omeroROIs.stream().filter(r -> "text".equals(r.getName())).count());
        assertEquals(1, omeroROIs.stream().filter(r -> "23".equals(r.getName())).count());
        assertEquals(0, omeroROIs.stream().filter(r -> "233".equals(r.getName())).count());
        assertEquals(0, omeroROIs.stream().filter(r -> "invalid".equals(r.getName())).count());
    }


    @Test
    void testROItoImageJ() {
        final int nRois = 11;

        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.PI / 4);

        Point point = new Point(1, 1);
        point.setCZT(0, 0, 0);

        Text text = new Text("Text", 2, 2);
        text.setCZT(0, 0, 1);

        Rectangle rectangle = new Rectangle(3, 3, 10, 10);
        rectangle.setCZT(0, 0, 2);

        Rectangle rectangle2 = new Rectangle(3, 3, 10, 10);
        rectangle2.setCZT(0, 0, 2);
        rectangle.setTransform(transform);

        Mask mask = new Mask();
        mask.setCoordinates(4, 4, 9, 9);
        mask.setCZT(1, 0, 0);
        mask.setTransform(transform);

        Ellipse ellipse = new Ellipse(5, 5, 4, 4);
        ellipse.setCZT(1, 0, 1);

        Ellipse ellipse2 = new Ellipse(5, 5, 4, 4);
        ellipse2.setCZT(1, 0, 2);
        ellipse2.setTransform(transform);

        Line line = new Line(0, 0, 10, 10);
        line.setCZT(1, 0, 3);
        line.asDataObject().getShapeSettings().setMarkerStart(Line.ARROW);

        Line line2 = new Line(0, 0, 10, 10);
        line2.setCZT(1, 0, 4);
        line2.setTransform(transform);

        Line line3 = new Line(2, 2, 3, 4);
        line3.setCZT(1, 0, 5);
        line3.asDataObject().getShapeSettings().setMarkerStart(Line.ARROW);
        line3.asDataObject().getShapeSettings().setMarkerEnd(Line.ARROW);

        List<Point2D.Double> points2D = new ArrayList<>(3);

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points2D.add(p1);
        points2D.add(p2);
        points2D.add(p3);

        Polyline polyline = new Polyline(points2D);
        polyline.setCZT(1, 1, 0);

        Polygon polygon = new Polygon(points2D);
        polygon.setCZT(1, 1, 1);

        ROI roiWrapper1 = new ROI();
        roiWrapper1.addShape(point);
        roiWrapper1.addShape(text);
        roiWrapper1.addShape(rectangle);
        roiWrapper1.addShape(rectangle2);
        roiWrapper1.addShape(mask);
        roiWrapper1.addShape(ellipse);
        roiWrapper1.setName("2");

        ROI roiWrapper2 = new ROI();
        roiWrapper2.addShape(ellipse2);
        roiWrapper2.addShape(line);
        roiWrapper2.addShape(line2);
        roiWrapper2.addShape(line3);
        roiWrapper2.addShape(polyline);
        roiWrapper2.addShape(polygon);

        Collection<ROI> rois = new ArrayList<>(2);
        rois.add(roiWrapper1);
        rois.add(roiWrapper2);

        List<Roi> ijRois = ROI.toImageJ(rois);

        assertEquals(nRois, ijRois.size());
        assertEquals("2", ijRois.get(0).getProperty("ROI_NAME"));
        assertEquals("2", ijRois.get(nRois - 1).getProperty("ROI"));
        assertNull(ijRois.get(nRois - 1).getProperty("ROI_NAME"));
    }


    @Test
    void convertEllipse() {
        Ellipse ellipse = new Ellipse(3, 4, 10, 8);
        ellipse.setCZT(0, 0, 2);

        OvalRoi ijEllipse = (OvalRoi) ellipse.toImageJ();
        assertEquals(ellipse.toAWTShape().getBounds(), ijEllipse.getBounds());

        Collection<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijEllipse);

        ROI roi = ROI.fromImageJ(roiList).get(0);

        Ellipse newEllipse = getElementsOf(roi.getShapes(), Ellipse.class).get(0);

        assertEquals(ellipse.getX(), newEllipse.getX(), Double.MIN_VALUE);
        assertEquals(ellipse.getY(), newEllipse.getY(), Double.MIN_VALUE);
        assertEquals(ellipse.getRadiusX(), newEllipse.getRadiusX(), Double.MIN_VALUE);
        assertEquals(ellipse.getRadiusY(), newEllipse.getRadiusY(), Double.MIN_VALUE);
        assertEquals(ellipse.getC(), newEllipse.getC());
        assertEquals(ellipse.getZ(), newEllipse.getZ());
        assertEquals(ellipse.getT(), newEllipse.getT());
    }


    @Test
    void convertRectangle() {
        Rectangle rectangle = new Rectangle(3, 3, 10, 10);
        rectangle.setCZT(0, 0, 2);

        Roi ijRectangle = rectangle.toImageJ();
        assertEquals(rectangle.toAWTShape().getBounds(), ijRectangle.getBounds());

        Collection<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijRectangle);
        ROI roi = ROI.fromImageJ(roiList, null).get(0);

        Rectangle newRectangle = getElementsOf(roi.getShapes(), Rectangle.class).get(0);

        assertEquals(rectangle.getX(), newRectangle.getX(), Double.MIN_VALUE);
        assertEquals(rectangle.getY(), newRectangle.getY(), Double.MIN_VALUE);
        assertEquals(rectangle.getWidth(), newRectangle.getWidth(), Double.MIN_VALUE);
        assertEquals(rectangle.getHeight(), newRectangle.getHeight(), Double.MIN_VALUE);
        assertEquals(rectangle.getC(), newRectangle.getC());
        assertEquals(rectangle.getZ(), newRectangle.getZ());
        assertEquals(rectangle.getT(), newRectangle.getT());
    }


    @Test
    void convertArrow() {
        Line arrow = new Line(3, 3, 10, 10);
        arrow.setCZT(0, 0, 2);
        arrow.asDataObject().getShapeSettings().setMarkerStart(Line.ARROW);
        arrow.setFill(new Color(0, 0, 0, 0));

        Arrow ijArrow = (Arrow) arrow.toImageJ();
        assertEquals(arrow.getX1(), ijArrow.x2d, Double.MIN_VALUE);
        assertEquals(arrow.getY1(), ijArrow.y2d, Double.MIN_VALUE);
        assertEquals(arrow.getX2(), ijArrow.x1d, Double.MIN_VALUE);
        assertEquals(arrow.getY2(), ijArrow.y1d, Double.MIN_VALUE);
        assertNull(ijArrow.getFillColor());

        Collection<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijArrow);
        ROI roi = ROI.fromImageJ(roiList, "").get(0);

        fr.igred.omero.roi.Line newArrow = getElementsOf(roi.getShapes(), Line.class).get(0);

        assertEquals(arrow.getX1(), newArrow.getX2(), Double.MIN_VALUE);
        assertEquals(arrow.getY1(), newArrow.getY2(), Double.MIN_VALUE);
        assertEquals(arrow.getX2(), newArrow.getX1(), Double.MIN_VALUE);
        assertEquals(arrow.getY2(), newArrow.getY1(), Double.MIN_VALUE);
        assertEquals(arrow.getC(), newArrow.getC());
        assertEquals(arrow.getZ(), newArrow.getZ());
        assertEquals(arrow.getT(), newArrow.getT());
        assertEquals(arrow.asDataObject().getShapeSettings().getMarkerStart(),
                     newArrow.asDataObject().getShapeSettings().getMarkerEnd());
    }


    @Test
    void convertLine() {
        Line line = new Line(3, 3, 10, 10);
        line.setCZT(0, 0, 2);
        line.setFill(Color.BLUE);

        ij.gui.Line ijLine = (ij.gui.Line) line.toImageJ();
        assertEquals(line.getX1(), ijLine.x1d, Double.MIN_VALUE);
        assertEquals(line.getY1(), ijLine.y1d, Double.MIN_VALUE);
        assertEquals(line.getX2(), ijLine.x2d, Double.MIN_VALUE);
        assertEquals(line.getY2(), ijLine.y2d, Double.MIN_VALUE);
        assertEquals(Color.BLUE, line.getFill());

        Collection<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijLine);
        ROI roi = ROI.fromImageJ(roiList, ROI.IJ_PROPERTY).get(0);

        fr.igred.omero.roi.Line newLine = getElementsOf(roi.getShapes(), Line.class).get(0);

        assertEquals(line.getX1(), newLine.getX1(), Double.MIN_VALUE);
        assertEquals(line.getY1(), newLine.getY1(), Double.MIN_VALUE);
        assertEquals(line.getX2(), newLine.getX2(), Double.MIN_VALUE);
        assertEquals(line.getY2(), newLine.getY2(), Double.MIN_VALUE);
        assertEquals(line.getC(), newLine.getC());
        assertEquals(line.getZ(), newLine.getZ());
        assertEquals(line.getT(), newLine.getT());
    }


    @Test
    void convertMask() {
        Mask mask = new Mask();
        mask.setCoordinates(3, 3, 10, 10);
        mask.setCZT(0, 0, 2);

        Roi ijRectangle = mask.toImageJ();
        assertEquals(mask.toAWTShape().getBounds(), ijRectangle.getBounds());

        Collection<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijRectangle);
        ROI roi = ROI.fromImageJ(roiList).get(0);

        Rectangle newRectangle = getElementsOf(roi.getShapes(), Rectangle.class).get(0);

        assertEquals(mask.getX(), newRectangle.getX(), Double.MIN_VALUE);
        assertEquals(mask.getY(), newRectangle.getY(), Double.MIN_VALUE);
        assertEquals(mask.getWidth(), newRectangle.getWidth(), Double.MIN_VALUE);
        assertEquals(mask.getHeight(), newRectangle.getHeight(), Double.MIN_VALUE);
        assertEquals(mask.getC(), newRectangle.getC());
        assertEquals(mask.getZ(), newRectangle.getZ());
        assertEquals(mask.getT(), newRectangle.getT());
    }


    @Test
    void convertPoint() {
        Point point = new Point();
        point.setCoordinates(3, 3);
        point.setCZT(0, 0, 2);

        PointRoi ijPoint = (PointRoi) point.toImageJ();
        assertEquals(point.getX(), ijPoint.getXBase(), Double.MIN_VALUE);
        assertEquals(point.getY(), ijPoint.getYBase(), Double.MIN_VALUE);

        Collection<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijPoint);
        ROI roi = ROI.fromImageJ(roiList).get(0);

        Point newPoint = getElementsOf(roi.getShapes(), Point.class).get(0);

        assertEquals(point.getX(), newPoint.getX(), Double.MIN_VALUE);
        assertEquals(point.getY(), newPoint.getY(), Double.MIN_VALUE);
        assertEquals(point.getC(), newPoint.getC());
        assertEquals(point.getZ(), newPoint.getZ());
        assertEquals(point.getT(), newPoint.getT());
    }


    @Test
    void convertText() {
        //noinspection HardcodedLineSeparator
        Pattern c = Pattern.compile("\r", Pattern.LITERAL); // Oddly, IJ adds \r

        Text text = new Text();
        text.setCoordinates(3, 3);
        text.setText("Text");
        text.setCZT(0, 0, 2);

        TextRoi ijPoint = (TextRoi) text.toImageJ();
        assertEquals(text.getX(), ijPoint.getXBase(), Double.MIN_VALUE);
        assertEquals(text.getY(), ijPoint.getYBase(), Double.MIN_VALUE);
        assertEquals(text.getText(), c.matcher(ijPoint.getText().trim()).replaceAll(Matcher.quoteReplacement("")));

        Collection<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijPoint);
        ROI roi = ROI.fromImageJ(roiList).get(0);

        Text newText = getElementsOf(roi.getShapes(), Text.class).get(0);

        assertEquals(text.getX(), newText.getX(), Double.MIN_VALUE);
        assertEquals(text.getY(), newText.getY(), Double.MIN_VALUE);
        assertEquals(text.getC(), newText.getC());
        assertEquals(text.getZ(), newText.getZ());
        assertEquals(text.getT(), newText.getT());
        assertEquals(text.getText(), c.matcher(newText.getText().trim()).replaceAll(Matcher.quoteReplacement("")));
    }


    @Test
    void convertPolygon() {
        List<Point2D.Double> points2D = new ArrayList<>(3);

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points2D.add(p1);
        points2D.add(p2);
        points2D.add(p3);

        Polygon polygon = new Polygon(points2D);
        polygon.setCZT(0, 0, 2);

        Roi ijPolygon = polygon.toImageJ();
        assertEquals(polygon.toAWTShape().getBounds(), ijPolygon.getBounds());

        Collection<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijPolygon);
        ROI roi = ROI.fromImageJ(roiList).get(0);

        Polygon newPolygon = getElementsOf(roi.getShapes(), Polygon.class).get(0);

        assertEquals(polygon.getPoints(), newPolygon.getPoints());
        assertEquals(polygon.getC(), newPolygon.getC());
        assertEquals(polygon.getZ(), newPolygon.getZ());
        assertEquals(polygon.getT(), newPolygon.getT());
    }


    @Test
    void convertPolyline() {
        List<Point2D.Double> points2D = new ArrayList<>(3);

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points2D.add(p1);
        points2D.add(p2);
        points2D.add(p3);

        Polyline polyline = new Polyline(points2D);
        polyline.setCZT(0, 0, 2);

        Roi ijPolyline = polyline.toImageJ();
        // Compare to getFloatPolygon().getBounds() because polyline bounds are different for ij 1.53h+
        assertEquals(polyline.toAWTShape().getBounds(), ijPolyline.getFloatPolygon().getBounds());

        Collection<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijPolyline);
        ROI roi = ROI.fromImageJ(roiList).get(0);

        Polyline newPolyline = getElementsOf(roi.getShapes(), Polyline.class).get(0);

        assertEquals(polyline.getPoints(), newPolyline.getPoints());
        assertEquals(polyline.getC(), newPolyline.getC());
        assertEquals(polyline.getZ(), newPolyline.getZ());
        assertEquals(polyline.getT(), newPolyline.getT());
    }

}
