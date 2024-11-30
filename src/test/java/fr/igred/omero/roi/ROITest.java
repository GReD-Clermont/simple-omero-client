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


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.core.Image;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;


class ROITest extends UserTest {


    @Test
    void testAddTagToROI() throws Exception {
        ROI roiWrapper = new ROIWrapper();

        Image image = client.getImage(IMAGE1.id);

        for (int i = 0; i < 4; i++) {
            Rectangle rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roiWrapper.addShape(rectangle);
        }

        roiWrapper = image.saveROIs(client, singletonList(roiWrapper)).get(0);
        roiWrapper.addTag(client, "ROI Tag", "ROI tag test");

        List<TagAnnotation> tags = roiWrapper.getTags(client);
        roiWrapper.unlink(client, tags.get(0));
        List<TagAnnotation> checkTags = roiWrapper.getTags(client);
        client.delete(tags);
        client.delete(roiWrapper);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testAddTagToShape() throws Exception {
        ROI roiWrapper = new ROIWrapper();

        Image image     = client.getImage(IMAGE1.id);
        Shape rectangle = new RectangleWrapper();
        roiWrapper.addShape(rectangle);

        roiWrapper = image.saveROIs(client, roiWrapper).get(0);
        Shape shape = roiWrapper.getShapes().get(0);
        shape.addTag(client, "Shape tag", "Shape tag test");

        List<TagAnnotation> tags = shape.getTags(client);
        shape.unlink(client, tags.get(0));
        List<TagAnnotation> checkTags = roiWrapper.getTags(client);
        client.delete(tags);
        client.delete(roiWrapper);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testROI() throws Exception {
        ROI roiWrapper = new ROIWrapper();

        Image image = client.getImage(IMAGE1.id);

        roiWrapper.setImage(image);

        for (int i = 0; i < 4; i++) {
            Rectangle rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roiWrapper.addShape(rectangle);
        }

        image.saveROIs(client, roiWrapper);

        List<ROI> rois = image.getROIs(client);

        assertEquals(1, rois.size());
        assertEquals(4, rois.get(0).getShapes().size());

        for (ROI roi : rois) {
            client.delete(roi);
        }

        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testROI2() throws Exception {
        Image image = client.getImage(IMAGE1.id);

        List<Shape> shapes = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            Rectangle rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            shapes.add(rectangle);
        }

        ROI roiWrapper = new ROIWrapper(shapes);
        roiWrapper.setImage(image);
        image.saveROIs(client, roiWrapper);

        List<ROI> rois = image.getROIs(client);

        assertEquals(1, rois.size());
        assertEquals(4, rois.get(0).getShapes().size());

        for (ROI roi : rois) {
            client.delete(roi);
        }

        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testRoiAddShapeAndDeleteIt() throws Exception {
        Image image = client.getImage(IMAGE1.id);

        Collection<Shape> shapes = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            Rectangle rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            shapes.add(rectangle);
        }

        ROI roi = new ROIWrapper();
        roi.addShapes(shapes);
        roi.setImage(image);
        image.saveROIs(client, roi);

        List<ROI> rois = image.getROIs(client);

        ROI updatedROI = rois.get(0);
        int size       = updatedROI.getShapes().size();
        int roiNumber  = rois.size();

        Rectangle rectangle = new RectangleWrapper();
        rectangle.setCoordinates(2, 2, 8, 8);
        rectangle.setZ(2);
        rectangle.setT(2);
        rectangle.setC(2);

        updatedROI.addShape(rectangle);
        updatedROI.saveROI(client);

        List<ROI> rois2       = image.getROIs(client);
        ROI       updatedROI2 = rois2.get(0);
        assertEquals(size + 1, updatedROI2.getShapes().size());
        assertEquals(roiNumber, rois2.size());

        updatedROI2.deleteShape(updatedROI2.getShapes().size() - 1);
        updatedROI2.saveROI(client);

        List<ROI> rois3       = image.getROIs(client);
        ROI       updatedROI3 = rois3.get(0);

        assertEquals(size, updatedROI3.getShapes().size());
        assertEquals(roiNumber, rois3.size());

        for (ROI r : rois3) {
            client.delete(r);
        }

        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testROIAllShapes() throws Exception {
        Image image = client.getImage(IMAGE1.id);

        Point point = new PointWrapper(1, 1);
        point.setCZT(0, 0, 0);

        Text text = new TextWrapper("Text", 2, 2);
        text.setCZT(0, 0, 1);

        Rectangle rectangle = new RectangleWrapper(3, 3, 10, 10);
        rectangle.setCZT(0, 0, 2);

        Mask mask = new MaskWrapper();
        mask.setCoordinates(4, 4, 9, 9);
        mask.setCZT(1, 0, 0);

        Ellipse ellipse = new EllipseWrapper(5, 5, 4, 4);
        ellipse.setCZT(1, 0, 1);

        Line line = new LineWrapper(0, 0, 10, 10);
        line.setCZT(1, 0, 2);

        List<Point2D.Double> points2D = new ArrayList<>(3);

        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(3, 0);
        Point2D.Double p3 = new Point2D.Double(3, 4);
        points2D.add(p1);
        points2D.add(p2);
        points2D.add(p3);

        Polyline polyline = new PolylineWrapper(points2D);
        polyline.setCZT(1, 1, 0);

        Polygon polygon = new PolygonWrapper(points2D);
        polygon.setCZT(1, 1, 1);

        ROI roiWrapper = new ROIWrapper();
        roiWrapper.setImage(image);
        roiWrapper.addShape(point);
        roiWrapper.addShape(text);
        roiWrapper.addShape(rectangle);
        roiWrapper.addShape(mask);
        roiWrapper.addShape(ellipse);
        roiWrapper.addShape(line);
        roiWrapper.addShape(polyline);
        roiWrapper.addShape(polygon);
        image.saveROIs(client, roiWrapper);

        List<ROI>       rois       = image.getROIs(client);
        ShapeList       shapes     = rois.get(0).getShapes();
        List<Point>     points     = shapes.getElementsOf(Point.class);
        List<Text>      texts      = shapes.getElementsOf(Text.class);
        List<Rectangle> rectangles = shapes.getElementsOf(Rectangle.class);
        List<Mask>      masks      = shapes.getElementsOf(Mask.class);
        List<Ellipse>   ellipses   = shapes.getElementsOf(Ellipse.class);
        List<Line>      lines      = shapes.getElementsOf(Line.class);
        List<Polyline>  polylines  = shapes.getElementsOf(Polyline.class);
        List<Polygon>   polygons   = shapes.getElementsOf(Polygon.class);

        assertEquals(1, rois.size());
        assertEquals(8, shapes.size());
        assertEquals(1, points.size());
        assertEquals(1, texts.size());
        assertEquals(1, rectangles.size());
        assertEquals(1, masks.size());
        assertEquals(1, ellipses.size());
        assertEquals(1, lines.size());
        assertEquals(1, polylines.size());
        assertEquals(1, polygons.size());

        for (ROI roi : rois) {
            client.delete(roi);
        }
        assertEquals(0, image.getROIs(client).size());
    }

}