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
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class FolderTest extends BasicTest {


    @Test
    public void testFolder1() throws Exception {
        boolean exception = false;
        Client  client    = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        FolderContainer folder = new FolderContainer(client, "Test1");
        try {
            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(0, 0, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            ROIContainer roi = new ROIContainer();
            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        } catch (Exception e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test
    public void testFolder2() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        ImageContainer image = client.getImage(3L);

        FolderContainer folder = new FolderContainer(client, "Test");
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROIContainer roi = new ROIContainer();

            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        folder = image.getFolder(client, folder.getId());
        List<ROIContainer> rois = folder.getROIs(client);
        assertEquals(8, rois.size());
        assertEquals("Test", folder.getName());
        assertEquals(8, image.getROIs(client).size());

        for (ROIContainer roi : rois) {
            client.deleteROI(roi);
        }

        rois = folder.getROIs(client);
        assertEquals(0, rois.size());
        assertEquals(0, image.getROIs(client).size());

        client.deleteFolder(folder);

        try {
            image.getFolder(client, folder.getId());
            fail();
        } catch (Exception e) {
            assertTrue(true);
            client.disconnect();
        }
    }


    @Test
    public void testFolder3() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        FolderContainer folder = new FolderContainer(client, "Test");
        folder.setImage(3L);

        for (int i = 0; i < 8; i++) {
            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            ROIContainer roi = new ROIContainer();
            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        List<ROIContainer> rois = folder.getROIs(client);
        assertEquals(8, rois.size());

        folder.unlinkROI(client, rois.get(0));
        client.deleteROI(rois.get(0));
        rois = folder.getROIs(client);
        assertEquals(7, rois.size());

        client.deleteFolder(folder);

        for (ROIContainer roi : rois) {
            client.deleteROI(roi);
        }
        client.disconnect();
    }


    @Test
    public void testFolder4() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        ImageContainer image = client.getImage(3L);

        FolderContainer folder = new FolderContainer(client, "Test1");
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROIContainer roi = new ROIContainer();

            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        folder = new FolderContainer(client, "Test2");
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROIContainer roi = new ROIContainer();

            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 5, 5);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        List<FolderContainer> folders = image.getFolders(client);
        assertEquals(2, folders.size());
        assertEquals(16, image.getROIs(client).size());

        for (FolderContainer RoiFolder : folders) {
            client.deleteFolder(RoiFolder);
        }

        folders = image.getFolders(client);
        assertEquals(0, folders.size());
        assertEquals(16, image.getROIs(client).size());

        List<ROIContainer> rois = image.getROIs(client);
        for (ROIContainer roi : rois) {
            client.deleteROI(roi);
        }
        client.disconnect();

        assertEquals(0, image.getROIs(client).size());
        client.disconnect();
    }

}
