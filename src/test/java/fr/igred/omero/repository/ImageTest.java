/*
 *  Copyright (C) 2020-2022 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General License for more details.
 * You should have received a copy of the GNU General License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.roi.EllipseWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.process.ImageStatistics;
import loci.plugins.BF;
import omero.gateway.model.MapAnnotationData;
import omero.model.NamedValue;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static fr.igred.omero.repository.GenericRepositoryObjectWrapper.ReplacePolicy.DELETE;
import static fr.igred.omero.repository.GenericRepositoryObjectWrapper.ReplacePolicy.DELETE_ORPHANED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


class ImageTest extends UserTest {


    @Test
    void testGetDatasets() throws Exception {
        assertEquals(DATASET1.id, client.getImage(IMAGE1.id).getDatasets(client).get(0).getId());
    }


    @Test
    void testGetProjects() throws Exception {
        assertEquals(PROJECT1.id, client.getImage(IMAGE1.id).getProjects(client).get(0).getId());
    }


    @Test
    void testGetScreens() throws Exception {
        final long id = 5L;
        assertEquals(SCREEN1.id, client.getImage(id).getScreens(client).get(0).getId());
    }


    @Test
    void testGetPlates() throws Exception {
        final long id = 5L;
        assertEquals(PLATE1.id, client.getImage(id).getPlates(client).get(0).getId());
    }


    @Test
    void testGetWells() throws Exception {
        final long  wellId = 1L;
        WellWrapper well   = client.getWell(wellId);

        long imageId = well.getWellSamples().get(0).getImage().getId();
        assertEquals(wellId, client.getImage(imageId).getWells(client).get(0).getId());
    }


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
    void testPairKeyValue() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake";

        File f = createFile(filename);

        DatasetWrapper dataset = client.getDataset(DATASET2.id);

        List<Long> newIDs = dataset.importImage(client, f.getAbsolutePath());
        assertEquals(1, newIDs.size());

        removeFile(f);

        List<ImageWrapper> images = dataset.getImages(client);

        ImageWrapper image = images.get(0);

        List<NamedValue> result1 = new ArrayList<>(2);
        result1.add(new NamedValue("Test result1", "Value Test"));
        result1.add(new NamedValue("Test2 result1", "Value Test2"));

        Collection<NamedValue> result2 = new ArrayList<>(2);
        result2.add(new NamedValue("Test result2", "Value Test"));
        result2.add(new NamedValue("Test2 result2", "Value Test2"));

        MapAnnotationWrapper mapAnnotation1 = new MapAnnotationWrapper(result1);

        MapAnnotationData mapData2 = new MapAnnotationData();
        mapData2.setContent(result2);
        MapAnnotationWrapper mapAnnotation2 = new MapAnnotationWrapper(mapData2);

        assertEquals(result1, mapAnnotation1.getContent());

        image.addMapAnnotation(client, mapAnnotation1);
        image.addMapAnnotation(client, mapAnnotation2);

        Map<String, String> result = image.getKeyValuePairs(client);

        assertEquals(4, result.size());
        assertEquals("Value Test", image.getValue(client, "Test result1"));

        client.delete(image);
    }


    @Test
    void testPairKeyValue2() throws Exception {
        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake";

        File f = createFile(filename);

        DatasetWrapper dataset = client.getDataset(DATASET2.id);
        dataset.importImages(client, f.getAbsolutePath());
        removeFile(f);
        List<ImageWrapper> images = dataset.getImages(client);

        ImageWrapper image = images.get(0);

        List<NamedValue> result = new ArrayList<>(2);
        result.add(new NamedValue("Test result1", "Value Test"));
        result.add(new NamedValue("Test2 result1", "Value Test2"));

        MapAnnotationWrapper mapAnnotation = new MapAnnotationWrapper();
        mapAnnotation.setContent(result);

        image.addMapAnnotation(client, mapAnnotation);

        Map<String, String> results = image.getKeyValuePairs(client);

        assertEquals(2, results.size());
        assertEquals("Value Test", image.getValue(client, "Test result1"));

        client.delete(image);
    }


    @Test
    void testPairKeyValue3() throws Exception {
        boolean exception = false;

        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake";

        File f = createFile(filename);

        DatasetWrapper dataset = client.getDataset(DATASET2.id);
        dataset.importImages(client, f.getAbsolutePath());
        removeFile(f);
        List<ImageWrapper> images = dataset.getImages(client);

        ImageWrapper image = images.get(0);

        image.addPairKeyValue(client, "Test result1", "Value Test");
        image.addPairKeyValue(client, "Test result2", "Value Test2");

        Map<String, String> results = image.getKeyValuePairs(client);

        assertEquals(2, results.size());
        try {
            image.getValue(client, "Nonexistent value");
        } catch (Exception e) {
            exception = true;
        }
        client.delete(image);
        assertTrue(exception);
    }


    @Test
    void testGetImageInfo() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        assertEquals(IMAGE1.name, image.getName());
        assertNull(image.getDescription());
        assertEquals(1L, image.getId());
    }


    @Test
    void testGetImageTag() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        List<TagAnnotationWrapper> tags = image.getTags(client);
        assertEquals(2, tags.size());
    }


    @Test
    void testGetImageSize() throws Exception {
        final int sizeXY = 512;
        final int sizeC  = 5;
        final int sizeZ  = 3;
        final int sizeT  = 7;

        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();

        assertEquals(sizeXY, pixels.getSizeX());
        assertEquals(sizeXY, pixels.getSizeY());
        assertEquals(sizeC, pixels.getSizeC());
        assertEquals(sizeZ, pixels.getSizeZ());
        assertEquals(sizeT, pixels.getSizeT());
    }


    @Test
    void testGetRawData() throws Exception {
        ImageWrapper     image  = client.getImage(IMAGE1.id);
        PixelsWrapper    pixels = image.getPixels();
        double[][][][][] value  = pixels.getAllPixels(client);

        assertEquals(pixels.getSizeX(), value[0][0][0][0].length);
        assertEquals(pixels.getSizeY(), value[0][0][0].length);
        assertEquals(pixels.getSizeC(), value[0][0].length);
        assertEquals(pixels.getSizeZ(), value[0].length);
        assertEquals(pixels.getSizeT(), value.length);
    }


    @Test
    void testGetRawData2() throws Exception {
        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();
        byte[][][][]  value  = pixels.getRawPixels(client, 1);

        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        int sizeZ = pixels.getSizeZ();
        int sizeC = pixels.getSizeC();
        int sizeT = pixels.getSizeT();

        assertEquals(sizeX * sizeY, value[0][0][0].length);
        assertEquals(sizeC, value[0][0].length);
        assertEquals(sizeZ, value[0].length);
        assertEquals(sizeT, value.length);
    }


    @Test
    void testGetRawDataBound() throws Exception {
        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();

        int[] xBound = {0, 2};
        int[] yBound = {0, 2};
        int[] cBound = {0, 2};
        int[] zBound = {0, 2};
        int[] tBound = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBound, yBound, cBound, zBound, tBound);

        assertEquals(3, value[0][0][0][0].length);
        assertEquals(3, value[0][0][0].length);
        assertEquals(3, value[0][0].length);
        assertEquals(3, value[0].length);
        assertEquals(3, value.length);
    }


    @Test
    void testGetRawDataBoundError() throws Exception {
        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();

        final int[] xBound = {511, 513};
        final int[] yBound = {0, 2};
        final int[] cBound = {0, 2};
        final int[] zBound = {0, 2};
        final int[] tBound = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBound, yBound, cBound, zBound, tBound);
        assertNotEquals(xBound[1] - xBound[0] + 1, value[0][0][0][0].length);
    }


    @Test
    void testGetRawDataBoundErrorNegative() throws Exception {
        boolean success = true;

        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();

        int[] xBound = {-1, 1};
        int[] yBound = {0, 2};
        int[] cBound = {0, 2};
        int[] zBound = {0, 2};
        int[] tBound = {0, 2};
        try {
            double[][][][][] value = pixels.getAllPixels(client, xBound, yBound, cBound, zBound, tBound);
            success = false;
            assertNotEquals(3, value[0][0][0][0].length);
        } catch (Exception e) {
            assertTrue(success);
        }
    }


    @Test
    void testToImagePlusBound() throws Exception {
        final int    lowXY   = 500;
        final int    highXY  = 507;
        final double pixSize = 0.5;

        int[] xBound = {0, 2};
        int[] yBound = {0, 2};
        int[] cBound = {0, 2};
        int[] zBound = {0, 2};
        int[] tBound = {0, 2};

        Random random = new SecureRandom();
        xBound[0] = random.nextInt(lowXY);
        yBound[0] = random.nextInt(lowXY);
        cBound[0] = random.nextInt(3);
        tBound[0] = random.nextInt(5);
        xBound[1] = random.nextInt(highXY - xBound[0]) + xBound[0] + 5;
        yBound[1] = random.nextInt(highXY - yBound[0]) + yBound[0] + 5;
        cBound[1] = random.nextInt(3 - cBound[0]) + cBound[0] + 2;
        tBound[1] = random.nextInt(5 - tBound[0]) + tBound[0] + 2;

        String fake     = "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake";
        File   fakeFile = createFile(fake);

        ImagePlus reference = BF.openImagePlus(fake)[0];
        removeFile(fakeFile);

        Duplicator duplicator = new Duplicator();
        reference.setRoi(xBound[0], yBound[0], xBound[1] - xBound[0] + 1, yBound[1] - yBound[0] + 1);
        ImagePlus crop = duplicator.run(reference,
                                        cBound[0] + 1, cBound[1] + 1,
                                        zBound[0] + 1, zBound[1] + 1,
                                        tBound[0] + 1, tBound[1] + 1);

        ImageWrapper image = client.getImage(IMAGE1.id);

        ImagePlus imp = image.toImagePlus(client, xBound, yBound, cBound, zBound, tBound);

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus       difference = calculator.run("difference create stack", crop, imp);
        ImageStatistics stats      = difference.getStatistics();

        assertEquals(pixSize, imp.getCalibration().pixelHeight, Double.MIN_VALUE);
        assertEquals(pixSize, imp.getCalibration().pixelWidth, Double.MIN_VALUE);
        assertEquals(1.0, imp.getCalibration().pixelDepth, Double.MIN_VALUE);
        assertEquals("µm", imp.getCalibration().getUnit());
        assertEquals(0, (int) stats.max);
    }


    @Test
    void testToImagePlus() throws Exception {
        String fake     = "8bit-unsigned&pixelType=uint8&sizeZ=2&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake";
        File   fakeFile = createFile(fake);

        ImagePlus reference = BF.openImagePlus(fake)[0];
        removeFile(fakeFile);

        ImageWrapper image = client.getImage(IMAGE2.id);

        ImagePlus imp = image.toImagePlus(client);

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus       difference = calculator.run("difference create stack", reference, imp);
        ImageStatistics stats      = difference.getStatistics();

        assertEquals(0, (int) stats.max);
    }


    @Test
    void testGetImageChannel() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        assertEquals("0", image.getChannelName(client, 0));
    }


    @Test
    void testGetImageChannelError() throws Exception {
        boolean success = true;

        ImageWrapper image = client.getImage(IMAGE1.id);
        try {
            image.getChannelName(client, 6);
            success = false;
            fail();
        } catch (Exception e) {
            assertTrue(success);
        }
    }


    @Test
    void testAddTagToImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE2.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "image tag", "tag attached to an image");

        image.addTag(client, tag);

        List<TagAnnotationWrapper> tags = image.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> endTags = image.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagToImage2() throws Exception {
        ImageWrapper image = client.getImage(IMAGE2.id);

        image.addTag(client, "image tag", "tag attached to an image");

        List<TagAnnotationWrapper> tags = client.getTags("image tag");
        client.delete(tags.get(0));
        List<TagAnnotationWrapper> endTags = client.getTags("image tag");

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagIdToImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE2.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "image tag", "tag attached to an image");

        image.addTag(client, tag.getId());

        List<TagAnnotationWrapper> tags = image.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> endTags = image.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagsToImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE2.id);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Image tag 1", "tag attached to an image");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Image tag 2", "tag attached to an image");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Image tag 3", "tag attached to an image");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Image tag 4", "tag attached to an image");

        image.addTags(client, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());
        List<TagAnnotationWrapper> tags = image.getTags(client);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);
        List<TagAnnotationWrapper> endTags = image.getTags(client);

        assertEquals(4, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagsToImage2() throws Exception {
        ImageWrapper image = client.getImage(IMAGE2.id);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Image tag 1", "tag attached to an image");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Image tag 2", "tag attached to an image");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Image tag 3", "tag attached to an image");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Image tag 4", "tag attached to an image");

        image.addTags(client, tag1, tag2, tag3, tag4);
        List<TagAnnotationWrapper> tags = image.getTags(client);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);
        List<TagAnnotationWrapper> endTags = image.getTags(client);

        assertEquals(4, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddAndRemoveTagFromImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE2.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to an image");

        image.addTag(client, tag);

        List<TagAnnotationWrapper> tags = image.getTags(client);
        image.unlink(client, tag);
        List<TagAnnotationWrapper> removed = image.getTags(client);
        client.delete(tag);

        assertEquals(1, tags.size());
        assertEquals(0, removed.size());
    }


    @Test
    void testImageOrder() throws Exception {
        List<ImageWrapper> images = client.getImages();
        for (int i = 1; i < images.size(); i++) {
            assertTrue(images.get(i - 1).getId() <= images.get(i).getId());
        }
    }


    @Test
    void testAddFileImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        File file = createRandomFile("test_image.txt");
        long id   = image.addFile(client, file);

        List<FileAnnotationWrapper> files = image.getFileAnnotations(client);
        for (FileAnnotationWrapper f : files) {
            if (f.getId() == id) {
                assertEquals(file.getName(), f.getFileName());
                assertEquals("txt", f.getFileFormat());
                assertEquals("text/plain", f.getOriginalMimetype());
                assertEquals("text/plain", f.getServerFileMimetype());
                assertEquals("Plain Text Document", f.getFileKind());
                assertEquals(file.getParent() + File.separator, f.getContentAsString());
                assertEquals(file.getParent() + File.separator, f.getFilePath());
                assertFalse(f.isMovieFile());

                File uploadedFile = f.getFile(client, "." + File.separator + "uploaded.txt");

                List<String> expectedLines = Files.readAllLines(file.toPath());
                List<String> lines         = Files.readAllLines(uploadedFile.toPath());
                assertEquals(expectedLines.size(), lines.size());
                for (int i = 0; i < expectedLines.size(); i++) {
                    assertEquals(expectedLines.get(i), lines.get(i));
                }
                removeFile(uploadedFile);
            }
        }

        client.deleteFile(id);
        removeFile(file);

        assertNotEquals(0L, id);
    }


    @Test
    void testGetCreated() throws Exception {
        LocalDate created = client.getImage(IMAGE1.id).getCreated().toLocalDateTime().toLocalDate();
        LocalDate now     = LocalDate.now();

        assertEquals(now, created);
    }


    @Test
    void testGetAcquisitionDate() throws Exception {
        LocalDateTime     acq = client.getImage(IMAGE1.id).getAcquisitionDate().toLocalDateTime();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        assertEquals("2020-04-01_20-04-01", dtf.format(acq));
    }


    @Test
    void testGetChannel() throws Exception {
        ChannelWrapper channel = client.getImage(IMAGE1.id).getChannels(client).get(0);
        assertEquals(0, channel.getIndex());
        channel.setName("Foo channel");
        assertEquals("Foo channel", channel.getName());
    }


    @Test
    void testSetDescription() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        String description  = image.getDescription();
        String description2 = "Foo";
        image.setDescription(description2);
        image.saveAndUpdate(client);
        assertEquals(description2, client.getImage(IMAGE1.id).getDescription());
        image.setDescription(description);
        image.saveAndUpdate(client);
        assertEquals(description, client.getImage(IMAGE1.id).getDescription());
    }


    @Test
    void testSetName() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        String name  = image.getName();
        String name2 = "Foo image";
        image.setName(name2);
        image.saveAndUpdate(client);
        assertEquals(name2, client.getImage(IMAGE1.id).getName());
        image.setName(name);
        image.saveAndUpdate(client);
        assertEquals(name, image.getName());
    }


    @Test
    void testGetCropFromROI() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        final RectangleWrapper rectangle = new RectangleWrapper(30, 30, 20, 20);
        rectangle.setCZT(1, 1, 2);

        final EllipseWrapper ellipse = new EllipseWrapper(50, 50, 20, 40);
        ellipse.setCZT(1, 0, 1);

        final int[] xBound = {30, 69};
        final int[] yBound = {10, 89};
        final int[] cBound = {1, 1};
        final int[] zBound = {0, 1};
        final int[] tBound = {1, 2};

        ROIWrapper roiWrapper = new ROIWrapper();
        roiWrapper.setImage(image);
        roiWrapper.addShape(rectangle);
        roiWrapper.addShape(ellipse);

        ImagePlus imp1 = image.toImagePlus(client, roiWrapper);
        ImagePlus imp2 = image.toImagePlus(client, xBound, yBound, cBound, zBound, tBound);

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus       difference = calculator.run("difference create stack", imp1, imp2);
        ImageStatistics stats      = difference.getStatistics();

        assertEquals(0, (int) stats.max);
        assertEquals(imp1.getWidth(), imp2.getWidth());
        assertEquals(imp1.getHeight(), imp2.getHeight());
        assertEquals(imp1.getNChannels(), imp2.getNChannels());
        assertEquals(imp1.getNSlices(), imp2.getNSlices());
        assertEquals(imp1.getNFrames(), imp2.getNFrames());
    }


    @Test
    void testGetThumbnail() throws Exception {
        final int size = 96;

        ImageWrapper  image     = client.getImage(IMAGE1.id);
        BufferedImage thumbnail = image.getThumbnail(client, size);
        assertNotNull(thumbnail);
        assertEquals(size, thumbnail.getWidth());
        assertEquals(size, thumbnail.getHeight());
    }


    @Test
    void testDownload() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        List<File>   files = image.download(client, ".");
        assertEquals(2, files.size());
        assertTrue(files.get(0).exists());
        Files.deleteIfExists(files.get(0).toPath());
        Files.deleteIfExists(files.get(1).toPath());
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
        image1.addTag(client, tag1);
        image1.addPairKeyValue(client, "Map", "ReplaceTest");

        long fileId = image1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        List<Long>   ids2   = dataset.importImage(client, imageFile.getAbsolutePath());
        ImageWrapper image2 = client.getImage(ids2.get(0));
        image2.setDescription("a test.");
        image2.saveAndUpdate(client);

        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "ReplaceTestTag2", "Copy annotations");
        image2.addTag(client, tag2);
        image2.addFileAnnotation(client, image1.getFileAnnotations(client).get(0));
        image2.addMapAnnotation(client, image1.getMapAnnotations(client).get(0));

        final RectangleWrapper rectangle = new RectangleWrapper(30, 30, 20, 20);
        ROIWrapper             roi       = new ROIWrapper();
        roi.setImage(image2);
        roi.addShape(rectangle);
        image2.saveROI(client, roi);

        FolderWrapper folder = new FolderWrapper(client, "ReplaceTestFolder");
        folder.setImage(image2);
        folder.addROI(client, roi);

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
        assertEquals(1, image3.getFolders(client).size());
        assertEquals("ReplaceTestTag1", image3.getTags(client).get(0).getName());
        assertEquals("ReplaceTestTag2", image3.getTags(client).get(1).getName());
        assertEquals("ReplaceTest", image3.getValue(client, "Map"));
        assertEquals("ReplaceTestTable", image3.getTables(client).get(0).getName());
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
        client.delete(folder);

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
        image1.addTag(client, tag1);
        image1.addPairKeyValue(client, "Map", "ReplaceTest");

        long fileId = image1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        List<Long>   ids2   = dataset.importImage(client, imageFile.getAbsolutePath());
        ImageWrapper image2 = client.getImage(ids2.get(0));
        image2.setDescription("A test.");
        image2.saveAndUpdate(client);

        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "ReplaceTestTag2", "Copy annotations");
        image2.addTag(client, tag2);
        image2.addFileAnnotation(client, image1.getFileAnnotations(client).get(0));
        image2.addMapAnnotation(client, image1.getMapAnnotations(client).get(0));

        final RectangleWrapper rectangle = new RectangleWrapper(30, 30, 20, 20);
        ROIWrapper             roi       = new ROIWrapper();
        roi.setImage(image2);
        roi.addShape(rectangle);
        image2.saveROI(client, roi);

        FolderWrapper folder = new FolderWrapper(client, "ReplaceTestFolder");
        folder.setImage(image2);
        folder.addROI(client, roi);

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
        assertEquals(1, image3.getFolders(client).size());
        assertEquals("ReplaceTestTag1", image3.getTags(client).get(0).getName());
        assertEquals("ReplaceTestTag2", image3.getTags(client).get(1).getName());
        assertEquals("ReplaceTest", image3.getValue(client, "Map"));
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
