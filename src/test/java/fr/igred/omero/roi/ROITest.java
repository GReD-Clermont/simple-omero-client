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


import fr.igred.omero.UserTest;
import fr.igred.omero.repository.ImageWrapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class ROITest extends UserTest {


    @Test
    public void testROI() throws Exception {
        ROIWrapper roiWrapper = new ROIWrapper();

        ImageWrapper image = client.getImage(1L);

        roiWrapper.setImage(image);

        for (int i = 0; i < 4; i++) {
            ShapeWrapper rectangle = new ShapeWrapper(ShapeWrapper.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
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
            client.deleteROI(roi);
        }

        rois = image.getROIs(client);

        assertEquals(0, rois.size());
    }


    @Test
    public void testROI2() throws Exception {
        ImageWrapper image = client.getImage(1L);

        List<ShapeWrapper> shapes = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            ShapeWrapper rectangle = new ShapeWrapper(ShapeWrapper.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
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
            client.deleteROI(roi);
        }

        rois = image.getROIs(client);

        assertEquals(0, rois.size());
    }


    @Test
    public void testRoiAddShapeAndDeleteIt() throws Exception {
        ImageWrapper image = client.getImage(1L);

        List<ShapeWrapper> shapes = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            ShapeWrapper rectangle = new ShapeWrapper(ShapeWrapper.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
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

        roi = rois.get(0);
        int size      = roi.getShapes().size();
        int ROINumber = rois.size();

        ShapeWrapper rectangle = new ShapeWrapper(ShapeWrapper.RECTANGLE);
        rectangle.setRectangleCoordinates(2, 2, 8, 8);
        rectangle.setZ(2);
        rectangle.setT(2);
        rectangle.setC(2);

        roi.addShape(rectangle);
        roi.saveROI(client);

        rois = image.getROIs(client);
        roi = rois.get(0);
        assertEquals(size + 1, roi.getShapes().size());
        assertEquals(ROINumber, rois.size());

        roi.deleteShape(roi.getShapes().size() - 1);
        roi.saveROI(client);

        rois = image.getROIs(client);
        roi = rois.get(0);

        assertEquals(size, roi.getShapes().size());
        assertEquals(ROINumber, rois.size());
    }

}