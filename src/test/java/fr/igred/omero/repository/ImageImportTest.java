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

package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static fr.igred.omero.repository.GenericRepositoryObjectWrapper.ReplacePolicy.DELETE;
import static fr.igred.omero.repository.GenericRepositoryObjectWrapper.ReplacePolicy.DELETE_ORPHANED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ImageImportTest extends UserTest {


    @Test
    void testImportImage() throws Exception {
        String filename1 = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake";
        String filename2 = "8bit-unsigned&pixelType=uint8&sizeZ=4&sizeC=5&sizeT=6&sizeX=512&sizeY=512.fake";

        File f1 = createFile(filename1);
        File f2 = createFile(filename2);

        DatasetWrapper dataset = client.getDataset(DATASET2.id);

        boolean imported = dataset.importImages(client, f1.getAbsolutePath(), f2.getAbsolutePath());

        removeFile(f1);
        removeFile(f2);

        List<ImageWrapper> images = dataset.getImages(client);
        client.delete(images);
        List<ImageWrapper> endImages = dataset.getImages(client);

        assertEquals(2, images.size());
        assertTrue(endImages.isEmpty());
        assertTrue(imported);
    }


    @Test
    void testReplaceAndDeleteImages() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&sizeX=512&sizeY=512.fake";

        DatasetWrapper dataset = new DatasetWrapper("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);
        File file      = createRandomFile("test_image.txt");

        List<Long>   ids1   = dataset.importImage(client, imageFile.getAbsolutePath());
        ImageWrapper image1 = client.getImage(ids1.get(0));
        image1.setDescription("This is");
        image1.saveAndUpdate(client);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "ReplaceTestTag1", "Copy annotations");
        image1.link(client, tag1);
        image1.addKeyValuePair(client, "Map", "ReplaceTest");

        long fileId = image1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        List<Long>   ids2   = dataset.importImage(client, imageFile.getAbsolutePath());
        ImageWrapper image2 = client.getImage(ids2.get(0));
        image2.setDescription("a test.");
        image2.saveAndUpdate(client);

        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "ReplaceTestTag2", "Copy annotations");
        image2.link(client, tag2);
        image2.link(client, image1.getFileAnnotations(client).get(0));
        image2.link(client, image1.getMapAnnotations(client).get(0));

        RectangleWrapper rectangle = new RectangleWrapper(3, 3, 2, 2);
        ROIWrapper       roi       = new ROIWrapper();
        roi.setImage(image2);
        roi.addShape(rectangle);
        roi = image2.saveROIs(client, roi).get(0);

        FolderWrapper roiFolder = new FolderWrapper(client, "ReplaceTestFolder");
        roiFolder.addROIs(client, image2, roi);
        FolderWrapper imgFolder = new FolderWrapper(client, "ReplaceTestImageFolder");
        imgFolder.addImages(client, image2);

        TableWrapper table = new TableWrapper(1, "ReplaceTestTable");
        table.setColumn(0, "Name", String.class);
        table.setRowCount(1);
        table.addRow("Annotation");
        image1.addTable(client, table);
        image2.addTable(client, table);

        List<Long>   ids3   = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath(), DELETE);
        ImageWrapper image3 = client.getImage(ids3.get(0));

        assertEquals(2, image3.getTags(client).size());
        assertEquals(2, image3.getTables(client).size());
        assertEquals(3, image3.getFileAnnotations(client).size());
        assertEquals(1, image3.getMapAnnotations(client).size());
        assertEquals(1, image3.getROIs(client).size());
        assertEquals(1, image3.getROIFolders(client).size());
        assertEquals(1, image3.getFolders(client).size());
        assertEquals("ReplaceTestTag1", image3.getTags(client).get(0).getName());
        assertEquals("ReplaceTestTag2", image3.getTags(client).get(1).getName());
        assertEquals("ReplaceTest", image3.getValues(client, "Map").get(0));
        assertEquals("ReplaceTestTable", image3.getTables(client).get(0).getName());
        //noinspection HardcodedLineSeparator
        assertEquals("This is\na test.", image3.getDescription());

        client.delete(image3.getMapAnnotations(client).get(0));
        removeFile(imageFile);

        List<ImageWrapper> images = dataset.getImages(client);

        client.delete(images);
        List<ImageWrapper> endImages = dataset.getImages(client);
        client.delete(dataset);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(table);
        client.deleteFile(fileId);
        client.delete(roi);
        client.delete(roiFolder);
        client.delete(imgFolder);

        assertEquals(1, images.size());
        assertTrue(endImages.isEmpty());
    }


    @Test
    void testReplaceAndUnlinkImages() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&sizeX=512&sizeY=512.fake";

        DatasetWrapper dataset = new DatasetWrapper("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);
        File file      = createRandomFile("test_image.txt");

        List<Long>   ids1   = dataset.importImage(client, imageFile.getAbsolutePath());
        ImageWrapper image1 = client.getImage(ids1.get(0));

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "ReplaceTestTag1", "Copy annotations");
        image1.link(client, tag1);
        image1.addKeyValuePair(client, "Map", "ReplaceTest");

        long fileId = image1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        List<Long>   ids2   = dataset.importImage(client, imageFile.getAbsolutePath());
        ImageWrapper image2 = client.getImage(ids2.get(0));
        image2.setDescription("A test.");
        image2.saveAndUpdate(client);

        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "ReplaceTestTag2", "Copy annotations");
        image2.link(client, tag2);
        image2.link(client, image1.getFileAnnotations(client).get(0));
        image2.link(client, image1.getMapAnnotations(client).get(0));

        RectangleWrapper rectangle = new RectangleWrapper(3, 3, 2, 2);
        ROIWrapper       roi       = new ROIWrapper();
        roi.setImage(image2);
        roi.addShape(rectangle);
        roi = image2.saveROIs(client, roi).get(0);

        FolderWrapper folder = new FolderWrapper(client, "ReplaceTestFolder");
        folder.addROIs(client, image2, roi);

        TableWrapper table = new TableWrapper(1, "ReplaceTestTable");
        table.setColumn(0, "Name", String.class);
        table.setRowCount(1);
        table.addRow("Annotation");
        image1.addTable(client, table);
        image2.addTable(client, table);

        List<Long>   ids3   = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath());
        ImageWrapper image3 = client.getImage(ids3.get(0));

        assertEquals(2, image3.getTags(client).size());
        assertEquals(2, image3.getTables(client).size());
        assertEquals(3, image3.getFileAnnotations(client).size());
        assertEquals(1, image3.getMapAnnotations(client).size());
        assertEquals(1, image3.getROIs(client).size());
        assertEquals(1, image3.getROIFolders(client).size());
        assertEquals("ReplaceTestTag1", image3.getTags(client).get(0).getName());
        assertEquals("ReplaceTestTag2", image3.getTags(client).get(1).getName());
        assertEquals("ReplaceTest", image3.getValues(client, "Map").get(0));
        assertEquals("ReplaceTestTable", image3.getTables(client).get(0).getName());
        assertEquals("A test.", image3.getDescription());

        client.delete(image3.getMapAnnotations(client).get(0));
        removeFile(imageFile);

        List<ImageWrapper> images = dataset.getImages(client);

        for (ImageWrapper image : images) {
            client.delete(image);
        }
        List<ImageWrapper> endImages = dataset.getImages(client);
        client.delete(dataset);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(table);
        client.deleteFile(fileId);
        client.delete(roi);
        client.delete(folder);
        client.delete(image1);
        client.delete(image2);

        assertEquals(1, images.size());
        assertTrue(endImages.isEmpty());
    }


    @Test
    void testReplaceImagesFileset1() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&series=2&sizeX=512&sizeY=512.fake";

        DatasetWrapper dataset = new DatasetWrapper("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);

        List<Long>         ids1    = dataset.importImage(client, imageFile.getAbsolutePath());
        List<ImageWrapper> images1 = dataset.getImages(client);

        List<Long>         ids2    = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath(), DELETE);
        List<ImageWrapper> images2 = dataset.getImages(client);

        removeFile(imageFile);

        assertEquals(2, ids1.size());
        assertEquals(2, ids2.size());
        assertEquals(ids1.size(), images1.size());
        assertEquals(ids2.size(), images2.size());
        assertTrue(client.getImages(ids1.get(0), ids1.get(1)).isEmpty());

        client.delete(images2);
        List<ImageWrapper> endImages = dataset.getImages(client);
        client.delete(dataset);

        assertTrue(endImages.isEmpty());
    }


    @Test
    void testReplaceImagesFileset2() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&series=2&sizeX=512&sizeY=512.fake";

        DatasetWrapper dataset = new DatasetWrapper("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);

        List<Long> ids1 = dataset.importImage(client, imageFile.getAbsolutePath());
        assertEquals(2, ids1.size());
        List<ImageWrapper> images1 = dataset.getImages(client);
        assertEquals(ids1.size(), images1.size());
        dataset.removeImage(client, images1.get(0));
        List<ImageWrapper> fsImages = images1.get(0).getFilesetImages(client);
        assertEquals(images1.size(), fsImages.size());
        assertTrue(ids1.contains(fsImages.get(0).getId()));
        assertTrue(ids1.contains(fsImages.get(1).getId()));

        List<Long> ids2 = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath(), DELETE);
        assertEquals(2, ids2.size());
        List<ImageWrapper> images2 = dataset.getImages(client);
        assertEquals(ids2.size(), images2.size());

        removeFile(imageFile);

        List<ImageWrapper> images3 = client.getImages(ids1.get(0), ids1.get(1));
        assertEquals(2, images3.size());
        assertFalse(images2.get(0).isOrphaned(client));
        assertTrue(images3.get(0).isOrphaned(client));

        client.delete(images1);
        client.delete(images2);
        List<ImageWrapper> endImages = dataset.getImages(client);
        client.delete(dataset);

        assertTrue(endImages.isEmpty());
    }


    @Test
    void testReplaceImagesFileset3() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&series=2&sizeX=512&sizeY=512.fake";

        DatasetWrapper dataset = new DatasetWrapper("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);

        List<Long> ids1 = dataset.importImage(client, imageFile.getAbsolutePath());
        assertEquals(2, ids1.size());
        List<ImageWrapper> images1 = dataset.getImages(client);
        assertEquals(ids1.size(), images1.size());
        dataset.removeImage(client, images1.get(0));
        List<ImageWrapper> fsImages = images1.get(0).getFilesetImages(client);
        assertEquals(images1.size(), fsImages.size());
        assertTrue(ids1.contains(fsImages.get(0).getId()));
        assertTrue(ids1.contains(fsImages.get(1).getId()));

        List<Long> ids2 = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath(), DELETE_ORPHANED);
        assertEquals(2, ids2.size());
        List<ImageWrapper> images2 = dataset.getImages(client);
        assertEquals(ids2.size(), images2.size());

        removeFile(imageFile);

        assertEquals(2, ids1.size());
        assertEquals(2, ids2.size());
        assertEquals(ids1.size(), images1.size());
        assertEquals(ids2.size(), images2.size());
        assertTrue(client.getImages(ids1.get(0), ids1.get(1)).isEmpty());

        client.delete(images2);
        List<ImageWrapper> endImages = dataset.getImages(client);
        client.delete(dataset);

        assertTrue(endImages.isEmpty());
    }


}
