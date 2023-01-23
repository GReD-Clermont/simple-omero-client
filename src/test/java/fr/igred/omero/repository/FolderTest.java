/*
 *  Copyright (C) 2020-2023 GReD
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


import fr.igred.omero.RemoteObject;
import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.Rectangle;
import fr.igred.omero.roi.RectangleWrapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FolderTest extends UserTest {


    @Test
    void testGetDeletedFolder() throws Exception {
        Image image = client.getImage(IMAGE2.id);

        RemoteObject<?> folder = new FolderWrapper(client, "Test");
        long            id     = folder.getId();
        client.delete(folder);
        assertThrows(NoSuchElementException.class, () -> image.getFolder(client, id));
    }


    @Test
    void testFolder1() throws Exception {
        Image image = client.getImage(IMAGE2.id);

        Folder folder = new FolderWrapper(client, "Test");
        folder.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROI roi = new ROIWrapper();

            Rectangle rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        folder = image.getFolder(client, folder.getId());
        List<ROI> rois = folder.getROIs(client);
        assertEquals(8, rois.size());
        assertEquals("Test", folder.getName());
        assertEquals(8, image.getROIs(client).size());

        for (ROI roi : rois) {
            client.delete(roi);
        }

        assertEquals(0, folder.getROIs(client).size());
        assertEquals(0, image.getROIs(client).size());

        client.delete(folder);
    }


    @Test
    void testFolder2() throws Exception {
        Folder folder = new FolderWrapper(client, "Test");
        folder.setImage(IMAGE2.id);

        for (int i = 0; i < 8; i++) {
            Rectangle rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            ROI roi = new ROIWrapper();
            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROI(client, roi);
        }

        List<ROI> rois = folder.getROIs(client);
        assertEquals(8, rois.size());

        folder.unlinkROI(client, rois.get(0));
        client.delete(rois.get(0));
        List<ROI> updatedROIs = folder.getROIs(client);
        assertEquals(7, updatedROIs.size());

        client.delete(folder);

        for (ROI roi : updatedROIs) {
            client.delete(roi);
        }
    }


    @Test
    void testFolder3() throws Exception {
        final int nImages = 16;

        Image image = client.getImage(IMAGE2.id);

        Folder folder1 = new FolderWrapper(client, "Test1");
        folder1.setDescription("Test 1");
        folder1.saveAndUpdate(client);
        assertEquals("Test1", folder1.getName());
        assertEquals("Test 1", folder1.getDescription());
        folder1.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROI roi = new ROIWrapper();

            Rectangle rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder1.addROI(client, roi);
        }

        Folder folder2 = new FolderWrapper(client, "Test");
        folder2.setName("Test2");
        folder2.saveAndUpdate(client);
        assertEquals("Test2", folder2.getName());
        folder2.setImage(image);

        for (int i = 0; i < 8; i++) {
            ROI roi = new ROIWrapper();

            Rectangle rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 5, 5);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder2.addROI(client, roi);
        }

        List<Folder> folders = image.getFolders(client);
        assertEquals(2, folders.size());
        assertEquals(nImages, image.getROIs(client).size());

        client.delete(folders);

        assertEquals(0, image.getFolders(client).size());
        assertEquals(nImages, image.getROIs(client).size());

        List<ROI> rois = image.getROIs(client);
        for (ROI roi : rois) {
            client.delete(roi);
        }

        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testAddAndRemoveTagFromFolder() throws Exception {
        RepositoryObject<?> folder = new FolderWrapper(client, "Test1");

        TagAnnotation tag = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a folder");

        folder.addTag(client, tag);

        List<TagAnnotation> tags = folder.getTags(client);
        assertEquals(1, tags.size());
        folder.unlink(client, tags.get(0));

        List<TagAnnotation> removed = folder.getTags(client);
        assertEquals(0, removed.size());

        client.delete(tag);
        client.delete(folder);
    }

}
