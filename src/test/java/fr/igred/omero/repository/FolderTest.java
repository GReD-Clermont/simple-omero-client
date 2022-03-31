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

package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;


public class FolderTest extends UserTest {


    @Test(expected = NoSuchElementException.class)
    public void testGetDeletedFolder() throws Exception {
        ImageWrapper image = client.getImage(3L);

        FolderWrapper folder = new FolderWrapper(client, "Test");
        folder.setImage(image);
        folder = image.getFolder(client, folder.getId());
        client.delete(folder);
        image.getFolder(client, folder.getId());
    }


    @Test
    public void testFolder1() throws Exception {
        ImageWrapper image = client.getImage(3L);

        FolderWrapper folder = new FolderWrapper(client, "Test");
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROIWrapper roi = new ROIWrapper();

            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        folder = image.getFolder(client, folder.getId());
        List<ROIWrapper> rois = folder.getROIs(client);
        assertEquals(8, rois.size());
        assertEquals("Test", folder.getName());
        assertEquals(8, image.getROIs(client).size());

        for (ROIWrapper roi : rois) {
            client.delete(roi);
        }

        rois = folder.getROIs(client);
        assertEquals(0, rois.size());
        assertEquals(0, image.getROIs(client).size());

        client.delete(folder);
    }


    @Test
    public void testFolder2() throws Exception {
        FolderWrapper folder = new FolderWrapper(client, "Test");
        folder.setImage(3L);

        for (int i = 0; i < 8; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            ROIWrapper roi = new ROIWrapper();
            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        List<ROIWrapper> rois = folder.getROIs(client);
        assertEquals(8, rois.size());

        folder.unlinkROI(client, rois.get(0));
        client.delete(rois.get(0));
        rois = folder.getROIs(client);
        assertEquals(7, rois.size());

        client.delete(folder);

        for (ROIWrapper roi : rois) {
            client.delete(roi);
        }
    }


    @Test
    public void testFolder3() throws Exception {
        ImageWrapper image = client.getImage(3L);

        FolderWrapper folder = new FolderWrapper(client, "Test1");
        folder.setDescription("Test 1");
        folder.saveAndUpdate(client);
        assertEquals("Test1", folder.getName());
        assertEquals("Test 1", folder.getDescription());
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROIWrapper roi = new ROIWrapper();

            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        folder = new FolderWrapper(client, "Test");
        folder.setName("Test2");
        folder.saveAndUpdate(client);
        assertEquals("Test2", folder.getName());
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROIWrapper roi = new ROIWrapper();

            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 5, 5);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        List<FolderWrapper> folders = image.getFolders(client);
        assertEquals(2, folders.size());
        assertEquals(16, image.getROIs(client).size());

        for (FolderWrapper RoiFolder : folders) {
            client.delete(RoiFolder);
        }

        folders = image.getFolders(client);
        assertEquals(0, folders.size());
        assertEquals(16, image.getROIs(client).size());

        List<ROIWrapper> rois = image.getROIs(client);
        for (ROIWrapper roi : rois) {
            client.delete(roi);
        }

        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testAddAndRemoveTagFromFolder() throws Exception {
        FolderWrapper folder = new FolderWrapper(client, "Test1");

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a folder");

        folder.addTag(client, tag);

        List<TagAnnotationWrapper> tags = folder.getTags(client);
        assertEquals(1, tags.size());
        folder.unlink(client, tags.get(0));

        List<TagAnnotationWrapper> removed = folder.getTags(client);
        assertEquals(0, removed.size());

        client.delete(tag);
        client.delete(folder);
    }

}
