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


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.Table;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.Rectangle;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static fr.igred.omero.repository.RepositoryObject.ReplacePolicy.DELETE;
import static fr.igred.omero.repository.RepositoryObject.ReplacePolicy.DELETE_ORPHANED;
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

        Dataset dataset = client.getDataset(DATASET2.id);

        boolean imported = dataset.importImages(client, f1.getAbsolutePath(), f2.getAbsolutePath());

        removeFile(f1);
        removeFile(f2);

        List<Image> images = dataset.getImages(client);
        client.delete(images);
        List<Image> endImages = dataset.getImages(client);

        assertEquals(2, images.size());
        assertTrue(endImages.isEmpty());
        assertTrue(imported);
    }


    @Test
    void testReplaceAndDeleteImages() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&sizeX=512&sizeY=512.fake";

        Dataset dataset = new Dataset("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);
        File file      = createRandomFile("test_image.txt");

        List<Long> ids1   = dataset.importImage(client, imageFile.getAbsolutePath());
        Image      image1 = client.getImage(ids1.get(0));
        image1.setDescription("This is");
        image1.saveAndUpdate(client);

        TagAnnotation tag1 = new TagAnnotation(client, "ReplaceTestTag1", "Copy annotations");
        image1.addTag(client, tag1);
        image1.addPairKeyValue(client, "Map", "ReplaceTest");

        long fileId = image1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        List<Long> ids2   = dataset.importImage(client, imageFile.getAbsolutePath());
        Image      image2 = client.getImage(ids2.get(0));
        image2.setDescription("a test.");
        image2.saveAndUpdate(client);

        TagAnnotation tag2 = new TagAnnotation(client, "ReplaceTestTag2", "Copy annotations");
        image2.addTag(client, tag2);
        image2.addFileAnnotation(client, image1.getFileAnnotations(client).get(0));
        image2.addMapAnnotation(client, image1.getMapAnnotations(client).get(0));

        Rectangle rectangle = new Rectangle(3, 3, 2, 2);
        ROI       roi       = new ROI();
        roi.setImage(image2);
        roi.addShape(rectangle);
        image2.saveROI(client, roi);

        Folder folder = new Folder(client, "ReplaceTestFolder");
        folder.setImage(image2);
        folder.addROI(client, roi);

        Table table = new Table(1, "ReplaceTestTable");
        table.setColumn(0, "Name", String.class);
        table.setRowCount(1);
        table.addRow("Annotation");
        image1.addTable(client, table);
        image2.addTable(client, table);

        List<Long> ids3   = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath(), DELETE);
        Image      image3 = client.getImage(ids3.get(0));

        assertEquals(2, image3.getTags(client).size());
        assertEquals(2, image3.getTables(client).size());
        assertEquals(3, image3.getFileAnnotations(client).size());
        assertEquals(1, image3.getMapAnnotations(client).size());
        assertEquals(1, image3.getROIs(client).size());
        assertEquals(1, image3.getFolders(client).size());
        assertEquals("ReplaceTestTag1", image3.getTags(client).get(0).getName());
        assertEquals("ReplaceTestTag2", image3.getTags(client).get(1).getName());
        assertEquals("ReplaceTest", image3.getValue(client, "Map"));
        assertEquals("ReplaceTestTable", image3.getTables(client).get(0).getName());
        //noinspection HardcodedLineSeparator
        assertEquals("This is\na test.", image3.getDescription());

        client.delete(image3.getMapAnnotations(client).get(0));
        removeFile(imageFile);

        List<Image> images = dataset.getImages(client);

        client.delete(images);
        List<Image> endImages = dataset.getImages(client);
        client.delete(dataset);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(table);
        client.deleteFile(fileId);
        client.delete(roi);
        client.delete(folder);

        assertEquals(1, images.size());
        assertTrue(endImages.isEmpty());
    }


    @Test
    void testReplaceAndUnlinkImages() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&sizeX=512&sizeY=512.fake";

        Dataset dataset = new Dataset("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);
        File file      = createRandomFile("test_image.txt");

        List<Long> ids1   = dataset.importImage(client, imageFile.getAbsolutePath());
        Image      image1 = client.getImage(ids1.get(0));

        TagAnnotation tag1 = new TagAnnotation(client, "ReplaceTestTag1", "Copy annotations");
        image1.addTag(client, tag1);
        image1.addPairKeyValue(client, "Map", "ReplaceTest");

        long fileId = image1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        List<Long> ids2   = dataset.importImage(client, imageFile.getAbsolutePath());
        Image      image2 = client.getImage(ids2.get(0));
        image2.setDescription("A test.");
        image2.saveAndUpdate(client);

        TagAnnotation tag2 = new TagAnnotation(client, "ReplaceTestTag2", "Copy annotations");
        image2.addTag(client, tag2);
        image2.addFileAnnotation(client, image1.getFileAnnotations(client).get(0));
        image2.addMapAnnotation(client, image1.getMapAnnotations(client).get(0));

        Rectangle rectangle = new Rectangle(3, 3, 2, 2);
        ROI       roi       = new ROI();
        roi.setImage(image2);
        roi.addShape(rectangle);
        image2.saveROI(client, roi);

        Folder folder = new Folder(client, "ReplaceTestFolder");
        folder.setImage(image2);
        folder.addROI(client, roi);

        Table table = new Table(1, "ReplaceTestTable");
        table.setColumn(0, "Name", String.class);
        table.setRowCount(1);
        table.addRow("Annotation");
        image1.addTable(client, table);
        image2.addTable(client, table);

        List<Long> ids3   = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath());
        Image      image3 = client.getImage(ids3.get(0));

        assertEquals(2, image3.getTags(client).size());
        assertEquals(2, image3.getTables(client).size());
        assertEquals(3, image3.getFileAnnotations(client).size());
        assertEquals(1, image3.getMapAnnotations(client).size());
        assertEquals(1, image3.getROIs(client).size());
        assertEquals(1, image3.getFolders(client).size());
        assertEquals("ReplaceTestTag1", image3.getTags(client).get(0).getName());
        assertEquals("ReplaceTestTag2", image3.getTags(client).get(1).getName());
        assertEquals("ReplaceTest", image3.getValue(client, "Map"));
        assertEquals("ReplaceTestTable", image3.getTables(client).get(0).getName());
        assertEquals("A test.", image3.getDescription());

        client.delete(image3.getMapAnnotations(client).get(0));
        removeFile(imageFile);

        List<Image> images = dataset.getImages(client);

        for (Image image : images) {
            client.delete(image);
        }
        List<Image> endImages = dataset.getImages(client);
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

        Dataset dataset = new Dataset("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);

        List<Long>  ids1    = dataset.importImage(client, imageFile.getAbsolutePath());
        List<Image> images1 = dataset.getImages(client);

        List<Long>  ids2    = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath(), DELETE);
        List<Image> images2 = dataset.getImages(client);

        removeFile(imageFile);

        assertEquals(2, ids1.size());
        assertEquals(2, ids2.size());
        assertEquals(ids1.size(), images1.size());
        assertEquals(ids2.size(), images2.size());
        assertTrue(client.getImages(ids1.get(0), ids1.get(1)).isEmpty());

        client.delete(images2);
        List<Image> endImages = dataset.getImages(client);
        client.delete(dataset);

        assertTrue(endImages.isEmpty());
    }


    @Test
    void testReplaceImagesFileset2() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&series=2&sizeX=512&sizeY=512.fake";

        Dataset dataset = new Dataset("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);

        List<Long> ids1 = dataset.importImage(client, imageFile.getAbsolutePath());
        assertEquals(2, ids1.size());
        List<Image> images1 = dataset.getImages(client);
        assertEquals(ids1.size(), images1.size());
        dataset.removeImage(client, images1.get(0));
        List<Image> fsImages = images1.get(0).getFilesetImages(client);
        assertEquals(images1.size(), fsImages.size());
        assertTrue(ids1.contains(fsImages.get(0).getId()));
        assertTrue(ids1.contains(fsImages.get(1).getId()));

        List<Long> ids2 = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath(), DELETE);
        assertEquals(2, ids2.size());
        List<Image> images2 = dataset.getImages(client);
        assertEquals(ids2.size(), images2.size());

        removeFile(imageFile);

        List<Image> images3 = client.getImages(ids1.get(0), ids1.get(1));
        assertEquals(2, images3.size());
        assertFalse(images2.get(0).isOrphaned(client));
        assertTrue(images3.get(0).isOrphaned(client));

        client.delete(images1);
        client.delete(images2);
        List<Image> endImages = dataset.getImages(client);
        client.delete(dataset);

        assertTrue(endImages.isEmpty());
    }


    @Test
    void testReplaceImagesFileset3() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&series=2&sizeX=512&sizeY=512.fake";

        Dataset dataset = new Dataset("Test Import & Replace", "");
        client.getProject(PROJECT1.id).addDataset(client, dataset);

        File imageFile = createFile(filename);

        List<Long> ids1 = dataset.importImage(client, imageFile.getAbsolutePath());
        assertEquals(2, ids1.size());
        List<Image> images1 = dataset.getImages(client);
        assertEquals(ids1.size(), images1.size());
        dataset.removeImage(client, images1.get(0));
        List<Image> fsImages = images1.get(0).getFilesetImages(client);
        assertEquals(images1.size(), fsImages.size());
        assertTrue(ids1.contains(fsImages.get(0).getId()));
        assertTrue(ids1.contains(fsImages.get(1).getId()));

        List<Long> ids2 = dataset.importAndReplaceImages(client, imageFile.getAbsolutePath(), DELETE_ORPHANED);
        assertEquals(2, ids2.size());
        List<Image> images2 = dataset.getImages(client);
        assertEquals(ids2.size(), images2.size());

        removeFile(imageFile);

        assertEquals(2, ids1.size());
        assertEquals(2, ids2.size());
        assertEquals(ids1.size(), images1.size());
        assertEquals(ids2.size(), images2.size());
        assertTrue(client.getImages(ids1.get(0), ids1.get(1)).isEmpty());

        client.delete(images2);
        List<Image> endImages = dataset.getImages(client);
        client.delete(dataset);

        assertTrue(endImages.isEmpty());
    }


}
