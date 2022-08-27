/*
 *  Copyright (C) 2020-2022 GReD
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
import ij.gui.Arrow;
import ij.gui.EllipseRoi;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import org.junit.Test;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;


public class ROI2ImageJTest extends BasicTest {


    @Test
    public void testROIsFromImageJ() {
        final List<Roi> rois = new ArrayList<>(11);

        final float[] x1 = {0.0f, 3.0f, 3.0f};
        final float[] y1 = {0.0f, 0.0f, 4.0f};
        final float[] x2 = {0.0f, 0.0f, 4.0f};
        final float[] y2 = {0.0f, 3.0f, 3.0f};

        final Roi rectangle = new Roi(1.0, 2.0, 3.0, 4.0);
        rectangle.setName("rectangle");
        rectangle.setPosition(1, 2, 3);
        rectangle.setProperty("ROI", "24");
        rois.add(rectangle);

        final TextRoi textRoi = new TextRoi(3.0, 4.0, "Text");
        textRoi.setName("text");
        textRoi.setProperty("ROI", "invalid");
        rois.add(textRoi);

        final OvalRoi ovalRoi = new OvalRoi(4.0, 5.0, 6.0, 7.0);
        ovalRoi.setName("oval");
        ovalRoi.setPosition(1, 0, 3);
        ovalRoi.setProperty("ROI", "24");
        rois.add(ovalRoi);

        final Arrow arrow = new Arrow(2.0, 3.0, 3.0, 4.0);
        arrow.setDoubleHeaded(true);
        arrow.setName("arrow");
        rois.add(arrow);

        final Line line = new Line(4.0, 3.0, 2.0, 1.0);
        rois.add(line);

        final PointRoi pointRoi = new PointRoi(x1, y1);
        pointRoi.setProperty("TEST", "24");
        rois.add(pointRoi);

        final PolygonRoi polylineRoi = new PolygonRoi(x2, y2, Roi.POLYLINE);
        polylineRoi.setPosition(1, 1, 2);
        polylineRoi.setProperty("ROI", "23");
        rois.add(polylineRoi);

        final PolygonRoi polygonRoi = new PolygonRoi(x2, y2, Roi.POLYGON);
        polygonRoi.setPosition(1, 1, 1);
        polygonRoi.setProperty("ROI", "23");
        rois.add(polygonRoi);

        final EllipseRoi ellipseRoi = new EllipseRoi(0.0, 0.0, 5.0, 5.0, 0.5);
        ellipseRoi.setPosition(1, 1, 1);
        rois.add(ellipseRoi);

        final ShapeRoi shapeRoi = new ShapeRoi(new Ellipse2D.Double(0.0, 5.0, 5.0, 10.0));
        shapeRoi.setPosition(1, 3, 1);
        rois.add(shapeRoi);

        rois.add(ellipseRoi);

        List<ROIWrapper> omeroROIs = ROIWrapper.fromImageJ(rois);

        assertEquals(9, omeroROIs.size());
    }


    @Test
    public void testROItoImageJ() {
        final int nRois = 11;

        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.PI / 4);

        PointWrapper point = new PointWrapper(1, 1);
        point.setCZT(0, 0, 0);

        TextWrapper text = new TextWrapper("Text", 2, 2);
        text.setCZT(0, 0, 1);

        RectangleWrapper rectangle = new RectangleWrapper(3, 3, 10, 10);
        rectangle.setCZT(0, 0, 2);

        RectangleWrapper rectangle2 = new RectangleWrapper(3, 3, 10, 10);
        rectangle2.setCZT(0, 0, 2);
        rectangle.setTransform(transform);

        MaskWrapper mask = new MaskWrapper();
        mask.setCoordinates(4, 4, 9, 9);
        mask.setCZT(1, 0, 0);
        mask.setTransform(transform);

        EllipseWrapper ellipse = new EllipseWrapper(5, 5, 4, 4);
        ellipse.setCZT(1, 0, 1);

        EllipseWrapper ellipse2 = new EllipseWrapper(5, 5, 4, 4);
        ellipse2.setCZT(1, 0, 2);
        ellipse2.setTransform(transform);

        LineWrapper line = new LineWrapper(0, 0, 10, 10);
        line.setCZT(1, 0, 3);
        line.asShapeData().getShapeSettings().setMarkerStart(LineWrapper.ARROW);

        LineWrapper line2 = new LineWrapper(0, 0, 10, 10);
        line2.setCZT(1, 0, 4);
        line2.setTransform(transform);

        LineWrapper line3 = new LineWrapper(2, 2, 3, 4);
        line3.setCZT(1, 0, 5);
        line3.asShapeData().getShapeSettings().setMarkerStart(LineWrapper.ARROW);
        line3.asShapeData().getShapeSettings().setMarkerEnd(LineWrapper.ARROW);

        List<Point2D.Double> points2D = new ArrayList<>(3);

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points2D.add(p1);
        points2D.add(p2);
        points2D.add(p3);

        PolylineWrapper polyline = new PolylineWrapper(points2D);
        polyline.setCZT(1, 1, 0);

        PolygonWrapper polygon = new PolygonWrapper(points2D);
        polygon.setCZT(1, 1, 1);

        ROIWrapper roiWrapper = new ROIWrapper();
        roiWrapper.addShape(point);
        roiWrapper.addShape(text);
        roiWrapper.addShape(rectangle);
        roiWrapper.addShape(rectangle2);
        roiWrapper.addShape(mask);
        roiWrapper.addShape(ellipse);
        roiWrapper.addShape(ellipse2);
        roiWrapper.addShape(line);
        roiWrapper.addShape(line2);
        roiWrapper.addShape(line3);
        roiWrapper.addShape(polyline);
        roiWrapper.addShape(polygon);

        List<ROIWrapper> rois = Collections.singletonList(roiWrapper);

        List<Roi> ijRois = ROIWrapper.toImageJ(rois);

        assertEquals(nRois, ijRois.size());
    }


    @Test
    public void convertEllipse() {
        EllipseWrapper ellipse = new EllipseWrapper(3, 4, 10, 8);
        ellipse.setCZT(0, 0, 2);

        OvalRoi ijEllipse = (OvalRoi) ellipse.toImageJ();
        assertEquals(ellipse.toAWTShape().getBounds(), ijEllipse.getBounds());

        List<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijEllipse);

        ROIWrapper roi = ROIWrapper.fromImageJ(roiList).get(0);

        EllipseWrapper newEllipse = roi.getShapes().getElementsOf(EllipseWrapper.class).get(0);

        assertEquals(ellipse.getX(), newEllipse.getX(), Double.MIN_VALUE);
        assertEquals(ellipse.getY(), newEllipse.getY(), Double.MIN_VALUE);
        assertEquals(ellipse.getRadiusX(), newEllipse.getRadiusX(), Double.MIN_VALUE);
        assertEquals(ellipse.getRadiusY(), newEllipse.getRadiusY(), Double.MIN_VALUE);
        assertEquals(ellipse.getC(), newEllipse.getC());
        assertEquals(ellipse.getZ(), newEllipse.getZ());
        assertEquals(ellipse.getT(), newEllipse.getT());
    }


    @Test
    public void convertRectangle() {
        RectangleWrapper rectangle = new RectangleWrapper(3, 3, 10, 10);
        rectangle.setCZT(0, 0, 2);

        Roi ijRectangle = rectangle.toImageJ();
        assertEquals(rectangle.toAWTShape().getBounds(), ijRectangle.getBounds());

        List<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijRectangle);
        ROIWrapper roi = ROIWrapper.fromImageJ(roiList, null).get(0);

        RectangleWrapper newRectangle = roi.getShapes().getElementsOf(RectangleWrapper.class).get(0);

        assertEquals(rectangle.getX(), newRectangle.getX(), Double.MIN_VALUE);
        assertEquals(rectangle.getY(), newRectangle.getY(), Double.MIN_VALUE);
        assertEquals(rectangle.getWidth(), newRectangle.getWidth(), Double.MIN_VALUE);
        assertEquals(rectangle.getHeight(), newRectangle.getHeight(), Double.MIN_VALUE);
        assertEquals(rectangle.getC(), newRectangle.getC());
        assertEquals(rectangle.getZ(), newRectangle.getZ());
        assertEquals(rectangle.getT(), newRectangle.getT());
    }


    @Test
    public void convertArrow() {
        LineWrapper arrow = new LineWrapper(3, 3, 10, 10);
        arrow.setCZT(0, 0, 2);
        arrow.asShapeData().getShapeSettings().setMarkerStart(LineWrapper.ARROW);

        Arrow ijArrow = (Arrow) arrow.toImageJ();
        assertEquals(arrow.getX1(), ijArrow.x2d, Double.MIN_VALUE);
        assertEquals(arrow.getY1(), ijArrow.y2d, Double.MIN_VALUE);
        assertEquals(arrow.getX2(), ijArrow.x1d, Double.MIN_VALUE);
        assertEquals(arrow.getY2(), ijArrow.y1d, Double.MIN_VALUE);

        List<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijArrow);
        ROIWrapper roi = ROIWrapper.fromImageJ(roiList, "").get(0);

        LineWrapper newArrow = roi.getShapes().getElementsOf(LineWrapper.class).get(0);

        assertEquals(arrow.getX1(), newArrow.getX2(), Double.MIN_VALUE);
        assertEquals(arrow.getY1(), newArrow.getY2(), Double.MIN_VALUE);
        assertEquals(arrow.getX2(), newArrow.getX1(), Double.MIN_VALUE);
        assertEquals(arrow.getY2(), newArrow.getY1(), Double.MIN_VALUE);
        assertEquals(arrow.getC(), newArrow.getC());
        assertEquals(arrow.getZ(), newArrow.getZ());
        assertEquals(arrow.getT(), newArrow.getT());
        assertEquals(arrow.asShapeData().getShapeSettings().getMarkerStart(),
                     newArrow.asShapeData().getShapeSettings().getMarkerEnd());
    }


    @Test
    public void convertLine() {
        final LineWrapper line = new LineWrapper(3, 3, 10, 10);
        line.setCZT(0, 0, 2);

        Line ijLine = (Line) line.toImageJ();
        assertEquals(line.getX1(), ijLine.x1d, Double.MIN_VALUE);
        assertEquals(line.getY1(), ijLine.y1d, Double.MIN_VALUE);
        assertEquals(line.getX2(), ijLine.x2d, Double.MIN_VALUE);
        assertEquals(line.getY2(), ijLine.y2d, Double.MIN_VALUE);

        List<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijLine);
        ROIWrapper roi = ROIWrapper.fromImageJ(roiList, ROIWrapper.IJ_PROPERTY).get(0);

        LineWrapper newLine = roi.getShapes().getElementsOf(LineWrapper.class).get(0);

        assertEquals(line.getX1(), newLine.getX1(), Double.MIN_VALUE);
        assertEquals(line.getY1(), newLine.getY1(), Double.MIN_VALUE);
        assertEquals(line.getX2(), newLine.getX2(), Double.MIN_VALUE);
        assertEquals(line.getY2(), newLine.getY2(), Double.MIN_VALUE);
        assertEquals(line.getC(), newLine.getC());
        assertEquals(line.getZ(), newLine.getZ());
        assertEquals(line.getT(), newLine.getT());
    }


    @Test
    public void convertMask() {
        MaskWrapper mask = new MaskWrapper();
        mask.setCoordinates(3, 3, 10, 10);
        mask.setCZT(0, 0, 2);

        Roi ijRectangle = mask.toImageJ();
        assertEquals(mask.toAWTShape().getBounds(), ijRectangle.getBounds());

        List<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijRectangle);
        ROIWrapper roi = ROIWrapper.fromImageJ(roiList).get(0);

        RectangleWrapper newRectangle = roi.getShapes().getElementsOf(RectangleWrapper.class).get(0);

        assertEquals(mask.getX(), newRectangle.getX(), Double.MIN_VALUE);
        assertEquals(mask.getY(), newRectangle.getY(), Double.MIN_VALUE);
        assertEquals(mask.getWidth(), newRectangle.getWidth(), Double.MIN_VALUE);
        assertEquals(mask.getHeight(), newRectangle.getHeight(), Double.MIN_VALUE);
        assertEquals(mask.getC(), newRectangle.getC());
        assertEquals(mask.getZ(), newRectangle.getZ());
        assertEquals(mask.getT(), newRectangle.getT());
    }


    @Test
    public void convertPoint() {
        PointWrapper point = new PointWrapper();
        point.setCoordinates(3, 3);
        point.setCZT(0, 0, 2);

        PointRoi ijPoint = (PointRoi) point.toImageJ();
        assertEquals(point.getX(), ijPoint.getXBase(), Double.MIN_VALUE);
        assertEquals(point.getY(), ijPoint.getYBase(), Double.MIN_VALUE);

        List<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijPoint);
        ROIWrapper roi = ROIWrapper.fromImageJ(roiList).get(0);

        PointWrapper newPoint = roi.getShapes().getElementsOf(PointWrapper.class).get(0);

        assertEquals(point.getX(), newPoint.getX(), Double.MIN_VALUE);
        assertEquals(point.getY(), newPoint.getY(), Double.MIN_VALUE);
        assertEquals(point.getC(), newPoint.getC());
        assertEquals(point.getZ(), newPoint.getZ());
        assertEquals(point.getT(), newPoint.getT());
    }


    @Test
    public void convertText() {
        //noinspection HardcodedLineSeparator
        final Pattern c = Pattern.compile("\r", Pattern.LITERAL); // Oddly, IJ adds \r

        TextWrapper text = new TextWrapper();
        text.setCoordinates(3, 3);
        text.setText("Text");
        text.setCZT(0, 0, 2);

        TextRoi ijPoint = (TextRoi) text.toImageJ();
        assertEquals(text.getX(), ijPoint.getXBase(), Double.MIN_VALUE);
        assertEquals(text.getY(), ijPoint.getYBase(), Double.MIN_VALUE);
        assertEquals(text.getText(), c.matcher(ijPoint.getText().trim()).replaceAll(Matcher.quoteReplacement("")));

        List<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijPoint);
        ROIWrapper roi = ROIWrapper.fromImageJ(roiList).get(0);

        TextWrapper newText = roi.getShapes().getElementsOf(TextWrapper.class).get(0);

        assertEquals(text.getX(), newText.getX(), Double.MIN_VALUE);
        assertEquals(text.getY(), newText.getY(), Double.MIN_VALUE);
        assertEquals(text.getC(), newText.getC());
        assertEquals(text.getZ(), newText.getZ());
        assertEquals(text.getT(), newText.getT());
        assertEquals(text.getText(), c.matcher(newText.getText().trim()).replaceAll(Matcher.quoteReplacement("")));
    }


    @Test
    public void convertPolygon() {
        List<Point2D.Double> points2D = new ArrayList<>(3);

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points2D.add(p1);
        points2D.add(p2);
        points2D.add(p3);

        PolygonWrapper polygon = new PolygonWrapper(points2D);
        polygon.setCZT(0, 0, 2);

        Roi ijPolygon = polygon.toImageJ();
        assertEquals(polygon.toAWTShape().getBounds(), ijPolygon.getBounds());

        List<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijPolygon);
        ROIWrapper roi = ROIWrapper.fromImageJ(roiList).get(0);

        PolygonWrapper newPolygon = roi.getShapes().getElementsOf(PolygonWrapper.class).get(0);

        assertEquals(polygon.getPoints(), newPolygon.getPoints());
        assertEquals(polygon.getC(), newPolygon.getC());
        assertEquals(polygon.getZ(), newPolygon.getZ());
        assertEquals(polygon.getT(), newPolygon.getT());
    }


    @Test
    public void convertPolyline() {
        List<Point2D.Double> points2D = new ArrayList<>(3);

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points2D.add(p1);
        points2D.add(p2);
        points2D.add(p3);

        PolylineWrapper polyline = new PolylineWrapper(points2D);
        polyline.setCZT(0, 0, 2);

        Roi ijPolyline = polyline.toImageJ();
        // Compare to getPolygon().getBounds() because polyline bounds are different for ij 1.53h+
        assertEquals(polyline.toAWTShape().getBounds(), ijPolyline.getPolygon().getBounds());

        List<Roi> roiList = new ArrayList<>(1);
        roiList.add(ijPolyline);
        ROIWrapper roi = ROIWrapper.fromImageJ(roiList).get(0);

        PolylineWrapper newPolyline = roi.getShapes().getElementsOf(PolylineWrapper.class).get(0);

        assertEquals(polyline.getPoints(), newPolyline.getPoints());
        assertEquals(polyline.getC(), newPolyline.getC());
        assertEquals(polyline.getZ(), newPolyline.getZ());
        assertEquals(polyline.getT(), newPolyline.getT());
    }

}
