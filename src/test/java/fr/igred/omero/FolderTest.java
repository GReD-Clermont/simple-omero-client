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


import fr.igred.omero.metadata.ROIContainer;
import fr.igred.omero.metadata.ShapeContainer;
import loci.common.DebugTools;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class FolderTest extends BasicTest {


    @Test
    public void testFolder1() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        FolderContainer folder = new FolderContainer(root, "Test1");
        try {
            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(0, 0, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            ROIContainer roi = new ROIContainer();
            roi.addShape(rectangle);
            roi.saveROI(root);

            folder.addROI(root, roi);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void testFolder2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(3L);

        FolderContainer folder = new FolderContainer(root, "Test");
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROIContainer roi = new ROIContainer();

            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(root);

            folder.addROI(root, roi);
        }

        folder = image.getFolder(root, folder.getId());
        List<ROIContainer> rois = folder.getROIs(root);
        assertEquals(8, rois.size());
        assertEquals("Test", folder.getName());
        assertEquals(8, image.getROIs(root).size());

        for (ROIContainer roi : rois) {
            root.deleteROI(roi);
        }

        rois = folder.getROIs(root);
        assertEquals(0, rois.size());
        assertEquals(0, image.getROIs(root).size());

        root.deleteFolder(folder);

        try {
            image.getFolder(root, folder.getId());
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void testFolder3() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        FolderContainer folder = new FolderContainer(root, "Test");
        folder.setImage(3L);

        for (int i = 0; i < 8; i++) {
            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            ROIContainer roi = new ROIContainer();
            roi.addShape(rectangle);
            roi.saveROI(root);

            folder.addROI(root, roi);
        }

        List<ROIContainer> rois = folder.getROIs(root);
        assertEquals(8, rois.size());

        folder.unlinkROI(root, rois.get(0));
        root.deleteROI(rois.get(0));
        rois = folder.getROIs(root);
        assertEquals(7, rois.size());

        root.deleteFolder(folder);

        for (ROIContainer roi : rois) {
            root.deleteROI(roi);
        }
    }


    @Test
    public void testFolder4() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(3L);

        FolderContainer folder = new FolderContainer(root, "Test1");
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROIContainer roi = new ROIContainer();

            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(root);

            folder.addROI(root, roi);
        }

        folder = new FolderContainer(root, "Test2");
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROIContainer roi = new ROIContainer();

            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 5, 5);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(root);

            folder.addROI(root, roi);
        }

        List<FolderContainer> folders = image.getFolders(root);
        assertEquals(2, folders.size());
        assertEquals(16, image.getROIs(root).size());

        for (FolderContainer RoiFolder : folders) {
            root.deleteFolder(RoiFolder);
        }

        folders = image.getFolders(root);
        assertEquals(0, folders.size());
        assertEquals(16, image.getROIs(root).size());

        List<ROIContainer> rois = image.getROIs(root);
        for (ROIContainer roi : rois) {
            root.deleteROI(roi);
        }
        assertEquals(0, image.getROIs(root).size());
    }

}
