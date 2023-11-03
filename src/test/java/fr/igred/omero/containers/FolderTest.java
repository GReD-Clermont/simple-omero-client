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

package fr.igred.omero.containers;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FolderTest extends UserTest {


    @Test
    void testImageFolderLinks() throws Exception {
        FolderWrapper parent = new FolderWrapper(client, "Parent");
        parent.addImages(client, client.getImage(IMAGE1.id), client.getImage(IMAGE2.id));
        parent.reload(client);
        parent.addImages(client, client.getImage(IMAGE1.id), client.getImage(IMAGE2.id));
        parent.reload(client);

        ImageWrapper        image   = client.getImage(IMAGE2.id);
        List<FolderWrapper> folders = image.getFolders(client);
        List<ImageWrapper>  images  = parent.getImages(client);

        client.delete(folders);

        assertEquals(2, images.size());
        assertEquals(IMAGE1.id, images.get(0).getId());
        assertEquals(1, folders.size());
        assertEquals(parent.getId(), folders.get(0).getId());
    }


    @Test
    void testHierarchyFolders() throws Exception {
        FolderWrapper parent = new FolderWrapper(client, "Parent");
        FolderWrapper child1 = new FolderWrapper(client, "Child 1");
        FolderWrapper child2 = new FolderWrapper(client, "Child 2");
        parent.addChild(child1);
        parent.saveAndUpdate(client);
        child2.setParent(parent);
        child2.addChildren(Collections.singletonList(child1));
        child2.saveAndUpdate(client);

        parent.reload(client);
        List<FolderWrapper> children  = parent.getChildren();
        List<FolderWrapper> children2 = child2.getChildren();

        client.delete(parent);
        client.delete(child1);
        client.delete(child2);

        assertEquals(2, children.size());
        assertEquals(child2.getId(), children.get(1).getId());
        assertEquals(parent.getId(), children.get(1).getParent().getId());
        assertEquals(1, children2.size());
        assertEquals(child1.getId(), children.get(0).getId());
    }


    @Test
    void testTagFolder() throws Exception {
        FolderWrapper        folder = new FolderWrapper(client, "Test");
        TagAnnotationWrapper tag    = new TagAnnotationWrapper(client, "Folder test", "Folder tag");
        folder.link(client, tag);

        List<FolderWrapper>        folders = tag.getFolders(client);
        List<TagAnnotationWrapper> tags    = folder.getTags(client);

        client.delete(tag);
        client.delete(folder);

        assertEquals(1, folders.size());
        assertEquals(folders.get(0).getId(), folder.getId());
        assertEquals(1, tags.size());
        assertEquals(tags.get(0).getId(), tag.getId());
    }


    @Test
    void testGetDeletedFolder() throws Exception {
        FolderWrapper folder = new FolderWrapper(client, "Test");
        assertEquals(1, client.getFolders().size());
        assertEquals(1, client.getFolders(client.getUser()).size());

        long id = folder.getId();
        client.delete(folder);
        assertThrows(NoSuchElementException.class, () -> client.getFolder(id));
    }


    @Test
    void testFolder1() throws Exception {
        ImageWrapper image = client.getImage(IMAGE2.id);

        FolderWrapper folder = new FolderWrapper(client, "Test");

        for (int i = 0; i < 8; i++) {
            ROIWrapper roi = new ROIWrapper();

            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROIs(client, image, roi);
        }

        folder = client.getFolder(folder.getId());
        List<ROIWrapper> rois = folder.getROIs(client, image);
        assertEquals(8, rois.size());
        assertEquals("Test", folder.getName());
        assertEquals(8, image.getROIs(client).size());

        folder.unlinkAllROIs(client, image);
        int nImgROIs    = image.getROIs(client).size();
        int nFolderROIs = folder.getROIs(client, image).size();

        client.delete(rois);

        assertEquals(8, nImgROIs);
        assertEquals(0, nFolderROIs);
        assertEquals(0, folder.getROIs(client, image).size());
        assertEquals(0, image.getROIs(client).size());

        client.delete(folder);
    }


    @Test
    void testFolder2() throws Exception {
        long imageId = IMAGE2.id;

        FolderWrapper folder = new FolderWrapper(client, "Test");

        for (int i = 0; i < 8; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            ROIWrapper roi = new ROIWrapper();
            roi.addShape(rectangle);
            roi.saveROI(client);

            folder.addROIs(client, imageId, roi);
        }

        List<ROIWrapper> rois = folder.getROIs(client, imageId);
        assertEquals(8, rois.size());

        folder.unlinkROIs(client, rois.get(0));
        client.delete(rois.get(0));
        List<ROIWrapper> updatedROIs = folder.getROIs(client, imageId);
        assertEquals(7, updatedROIs.size());

        client.delete(folder);

        for (ROIWrapper roi : updatedROIs) {
            client.delete(roi);
        }
    }


    @Test
    void testFolder3() throws Exception {
        final int nImages = 16;

        ImageWrapper image = client.getImage(IMAGE2.id);

        FolderWrapper folder1 = new FolderWrapper(client, "Test1");
        folder1.setDescription("Test 1");
        folder1.saveAndUpdate(client);
        assertEquals("Test1", folder1.getName());
        assertEquals("Test 1", folder1.getDescription());

        for (int i = 0; i < 8; i++) {
            ROIWrapper roi = new ROIWrapper();

            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder1.addROIs(client, image, roi);
        }

        FolderWrapper folder2 = new FolderWrapper(client, "Test");
        folder2.setName("Test2");
        folder2.saveAndUpdate(client);
        assertEquals("Test2", folder2.getName());

        for (int i = 0; i < 8; i++) {
            ROIWrapper roi = new ROIWrapper();

            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 5, 5);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
            roi.saveROI(client);

            folder2.addROIs(client, image, roi);
        }

        List<FolderWrapper> folders = image.getROIFolders(client);
        assertEquals(2, folders.size());
        assertEquals(nImages, image.getROIs(client).size());

        for (FolderWrapper f : folders) {
            f.reload(client);
        }
        client.delete(folders);

        assertEquals(0, image.getROIFolders(client).size());
        assertEquals(nImages, image.getROIs(client).size());

        List<ROIWrapper> rois = image.getROIs(client);
        for (ROIWrapper roi : rois) {
            client.delete(roi);
        }

        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testAddAndRemoveTagFromFolder() throws Exception {
        FolderWrapper folder = new FolderWrapper(client, "Test1");

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a folder");

        folder.link(client, tag);

        List<TagAnnotationWrapper> tags = folder.getTags(client);
        assertEquals(1, tags.size());
        folder.unlink(client, tags);

        List<TagAnnotationWrapper> removed = folder.getTags(client);
        assertEquals(0, removed.size());

        client.delete(tag);
        client.delete(folder);
    }

}
