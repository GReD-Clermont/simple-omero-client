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


import fr.igred.omero.UserTest;
import fr.igred.omero.repository.ImageWrapper;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class ROITest extends UserTest {


    @Test
    public void testROI() throws Exception {
        ROIWrapper roiWrapper = new ROIWrapper();

        ImageWrapper image = client.getImage(IMAGE1.id);

        roiWrapper.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roiWrapper.addShape(rectangle);
        }

        image.saveROI(client, roiWrapper);

        List<ROIWrapper> rois = image.getROIs(client);

        assertEquals(1, rois.size());
        assertEquals(4, rois.get(0).getShapes().size());

        for (ROIWrapper roi : rois) {
            client.delete(roi);
        }

        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testROI2() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        List<GenericShapeWrapper<?>> shapes = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            shapes.add(rectangle);
        }

        ROIWrapper roiWrapper = new ROIWrapper(shapes);
        roiWrapper.setImage(image);
        image.saveROI(client, roiWrapper);

        List<ROIWrapper> rois = image.getROIs(client);

        assertEquals(1, rois.size());
        assertEquals(4, rois.get(0).getShapes().size());

        for (ROIWrapper roi : rois) {
            client.delete(roi);
        }

        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testRoiAddShapeAndDeleteIt() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        List<GenericShapeWrapper<?>> shapes = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            shapes.add(rectangle);
        }

        ROIWrapper roi = new ROIWrapper();
        roi.addShapes(shapes);
        roi.setImage(image);
        image.saveROI(client, roi);

        List<ROIWrapper> rois = image.getROIs(client);

        ROIWrapper updatedROI = rois.get(0);
        int        size       = updatedROI.getShapes().size();
        int        roiNumber  = rois.size();

        RectangleWrapper rectangle = new RectangleWrapper();
        rectangle.setCoordinates(2, 2, 8, 8);
        rectangle.setZ(2);
        rectangle.setT(2);
        rectangle.setC(2);

        updatedROI.addShape(rectangle);
        updatedROI.saveROI(client);

        List<ROIWrapper> rois2       = image.getROIs(client);
        ROIWrapper       updatedROI2 = rois2.get(0);
        assertEquals(size + 1, updatedROI2.getShapes().size());
        assertEquals(roiNumber, rois2.size());

        updatedROI2.deleteShape(updatedROI2.getShapes().size() - 1);
        updatedROI2.saveROI(client);

        List<ROIWrapper> rois3       = image.getROIs(client);
        ROIWrapper       updatedROI3 = rois3.get(0);

        assertEquals(size, updatedROI3.getShapes().size());
        assertEquals(roiNumber, rois3.size());

        for (ROIWrapper r : rois3) {
            client.delete(r);
        }

        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testROIAllShapes() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        PointWrapper point = new PointWrapper(1, 1);
        point.setCZT(0, 0, 0);

        TextWrapper text = new TextWrapper("Text", 2, 2);
        text.setCZT(0, 0, 1);

        RectangleWrapper rectangle = new RectangleWrapper(3, 3, 10, 10);
        rectangle.setCZT(0, 0, 2);

        MaskWrapper mask = new MaskWrapper();
        mask.setCoordinates(4, 4, 9, 9);
        mask.setCZT(1, 0, 0);

        EllipseWrapper ellipse = new EllipseWrapper(5, 5, 4, 4);
        ellipse.setCZT(1, 0, 1);

        LineWrapper line = new LineWrapper(0, 0, 10, 10);
        line.setCZT(1, 0, 2);

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
        roiWrapper.setImage(image);
        roiWrapper.addShape(point);
        roiWrapper.addShape(text);
        roiWrapper.addShape(rectangle);
        roiWrapper.addShape(mask);
        roiWrapper.addShape(ellipse);
        roiWrapper.addShape(line);
        roiWrapper.addShape(polyline);
        roiWrapper.addShape(polygon);
        image.saveROI(client, roiWrapper);

        List<ROIWrapper>       rois       = image.getROIs(client);
        ShapeList              shapes     = rois.get(0).getShapes();
        List<PointWrapper>     points     = shapes.getElementsOf(PointWrapper.class);
        List<TextWrapper>      texts      = shapes.getElementsOf(TextWrapper.class);
        List<RectangleWrapper> rectangles = shapes.getElementsOf(RectangleWrapper.class);
        List<MaskWrapper>      masks      = shapes.getElementsOf(MaskWrapper.class);
        List<EllipseWrapper>   ellipses   = shapes.getElementsOf(EllipseWrapper.class);
        List<LineWrapper>      lines      = shapes.getElementsOf(LineWrapper.class);
        List<PolylineWrapper>  polylines  = shapes.getElementsOf(PolylineWrapper.class);
        List<PolygonWrapper>   polygons   = shapes.getElementsOf(PolygonWrapper.class);

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

        for (ROIWrapper roi : rois) {
            client.delete(roi);
        }
        assertEquals(0, image.getROIs(client).size());
    }

}