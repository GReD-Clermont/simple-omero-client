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
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.roi.EllipseWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.process.ImageStatistics;
import loci.plugins.BF;
import omero.constants.metadata.NSCLIENTMAPANNOTATION;
import omero.gateway.model.MapAnnotationData;
import omero.model.NamedValue;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
    void testGetKeyValuePair1() throws Exception {
        ImageWrapper        image = client.getImage(IMAGE1.id);
        Map<String, String> pairs = image.getKeyValuePairs(client);
        assertEquals(2, pairs.size());
    }


    @Test
    void testGetKeyValuePair2() throws Exception {
        ImageWrapper               image       = client.getImage(IMAGE1.id);
        List<MapAnnotationWrapper> annotations = image.getMapAnnotations(client);
        assertEquals(1, annotations.size());
    }


    @Test
    void testGetValue() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        String       value = image.getValue(client, "testKey1");
        assertEquals("testValue1", value);
    }


    @Test
    void testGetValueWrongKey() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        assertThrows(NoSuchElementException.class, () -> image.getValue(client, "testKey"));
    }


    @Test
    void testAddKeyValuePair1() throws Exception {
        final long   imageId = 4L;
        ImageWrapper image   = client.getImage(imageId);

        String name1  = "Test result1";
        String name2  = "Test2 result1";
        String value1 = "Value Test";
        String value2 = "Value Test2";

        List<NamedValue> values = new ArrayList<>(2);
        values.add(new NamedValue(name1, value1));
        values.add(new NamedValue(name2, value2));

        MapAnnotationWrapper mapAnnotation = new MapAnnotationWrapper(values);
        image.link(client, mapAnnotation);

        Map<String, String> pairs = image.getKeyValuePairs(client);

        String value = image.getValue(client, name1);

        client.delete(image.getMapAnnotations(client));

        assertEquals(values, mapAnnotation.getContent());
        assertEquals(2, pairs.size());
        assertEquals(value1, value);
    }


    @Test
    void testAddKeyValuePair2() throws Exception {
        final long   imageId = 4L;
        ImageWrapper image   = client.getImage(imageId);

        String name1  = "Test result2";
        String name2  = "Test2 result2";
        String value1 = "Value Test";
        String value2 = "Value Test2";

        Collection<NamedValue> values = new ArrayList<>(2);
        values.add(new NamedValue(name1, value1));
        values.add(new NamedValue(name2, value2));

        MapAnnotationData mapData = new MapAnnotationData();
        mapData.setContent(values);

        MapAnnotationWrapper mapAnnotation = new MapAnnotationWrapper(mapData);
        image.link(client, mapAnnotation);

        Map<String, String> pairs = image.getKeyValuePairs(client);

        String value = image.getValue(client, name2);

        client.delete(image.getMapAnnotations(client));

        assertEquals(values, mapAnnotation.getContent());
        assertEquals(2, pairs.size());
        assertEquals(value2, value);
    }


    @Test
    void testAddKeyValuePair3() throws Exception {
        final long   imageId = 4L;
        ImageWrapper image   = client.getImage(imageId);

        String name1  = "Test result3";
        String name2  = "Test2 result3";
        String value1 = "Value Test";
        String value2 = "Value Test2";

        List<NamedValue> values = new ArrayList<>(2);
        values.add(new NamedValue(name1, value1));
        values.add(new NamedValue(name2, value2));

        MapAnnotationWrapper mapAnnotation = new MapAnnotationWrapper();
        mapAnnotation.setContent(values);
        image.link(client, mapAnnotation);

        List<MapAnnotationWrapper> maps = image.getMapAnnotations(client);

        String value = image.getValue(client, name1);

        client.delete(image.getMapAnnotations(client));

        assertEquals(values, mapAnnotation.getContent());
        assertEquals(1, maps.size());
        assertEquals(value1, value);
    }


    @Test
    void testAddKeyValuePair4() throws Exception {
        final long   imageId = 4L;
        ImageWrapper image   = client.getImage(imageId);

        String key1   = "Test result4";
        String key2   = "Test2 result4";
        String value1 = "Value Test";
        String value2 = "Value Test2";

        Timestamp ts = Timestamp.from(Instant.now());
        image.addKeyValuePair(client, key1, value1);
        image.addKeyValuePair(client, key2, value2);

        List<MapAnnotationWrapper> maps = image.getMapAnnotations(client);

        String value = image.getValue(client, key1);
        client.delete(maps);

        assertEquals(2, maps.size());
        assertEquals(NSCLIENTMAPANNOTATION.value, maps.get(0).getNameSpace());
        assertEquals(0, (maps.get(0).getLastModified().getTime() - ts.getTime()) / 1000);
        assertEquals(value1, value);
    }


    @Test
    void testGetImageInfo() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        assertEquals(IMAGE1.name, image.getName());
        assertNull(image.getDescription());
        assertEquals(1L, image.getId());
    }


    @Test
    void testGetImageTags() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        List<TagAnnotationWrapper> tags = image.getTags(client);
        assertEquals(2, tags.size());
    }


    @Test
    void testToImagePlusBound() throws Exception {
        final int    lowXY    = 500;
        final int    highXY   = 507;
        final double pixSize  = 0.5;
        final double pixDepth = 1.5;
        final double deltaT   = 150;
        final double xyOrigin = 100;
        final double zOrigin  = 20;

        int[] xBounds = {0, 2};
        int[] yBounds = {0, 2};
        int[] cBounds = {0, 2};
        int[] zBounds = {0, 2};
        int[] tBounds = {0, 2};

        xBounds[0] = SECURE_RANDOM.nextInt(lowXY);
        yBounds[0] = SECURE_RANDOM.nextInt(lowXY);
        cBounds[0] = SECURE_RANDOM.nextInt(3);
        tBounds[0] = SECURE_RANDOM.nextInt(5);
        xBounds[1] = SECURE_RANDOM.nextInt(highXY - xBounds[0]) + xBounds[0] + 5;
        yBounds[1] = SECURE_RANDOM.nextInt(highXY - yBounds[0]) + yBounds[0] + 5;
        cBounds[1] = SECURE_RANDOM.nextInt(3 - cBounds[0]) + cBounds[0] + 2;
        tBounds[1] = SECURE_RANDOM.nextInt(5 - tBounds[0]) + tBounds[0] + 2;

        String fake     = "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake";
        File   fakeFile = createFile(fake);

        ImagePlus reference = BF.openImagePlus(fake)[0];
        removeFile(fakeFile);

        Duplicator duplicator = new Duplicator();
        reference.setRoi(xBounds[0], yBounds[0], xBounds[1] - xBounds[0] + 1, yBounds[1] - yBounds[0] + 1);
        ImagePlus crop = duplicator.run(reference,
                                        cBounds[0] + 1, cBounds[1] + 1,
                                        zBounds[0] + 1, zBounds[1] + 1,
                                        tBounds[0] + 1, tBounds[1] + 1);

        ImageWrapper image = client.getImage(IMAGE1.id);

        ImagePlus imp = image.toImagePlus(client, xBounds, yBounds, cBounds, zBounds, tBounds);

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus       difference = calculator.run("difference create stack", crop, imp);
        ImageStatistics stats      = difference.getStatistics();

        assertEquals(pixSize, imp.getCalibration().pixelHeight, Double.MIN_VALUE);
        assertEquals(pixSize, imp.getCalibration().pixelWidth, Double.MIN_VALUE);
        assertEquals(pixDepth, imp.getCalibration().pixelDepth, Double.MIN_VALUE);
        // Round numbers because rounding errors happen when converting units
        assertEquals(deltaT, imp.getCalibration().frameInterval, DOUBLE_PRECISION * deltaT);
        assertEquals(xyOrigin, imp.getCalibration().xOrigin, DOUBLE_PRECISION * xyOrigin);
        assertEquals(xyOrigin, imp.getCalibration().yOrigin, DOUBLE_PRECISION * xyOrigin);
        assertEquals(zOrigin, imp.getCalibration().zOrigin, DOUBLE_PRECISION * zOrigin);
        assertEquals("µm", imp.getCalibration().getUnit());
        assertEquals("µm", imp.getCalibration().getZUnit());
        assertEquals("ms", imp.getCalibration().getTimeUnit());
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
        ImageWrapper image = client.getImage(IMAGE1.id);
        assertThrows(IndexOutOfBoundsException.class, () -> image.getChannelName(client, 6));
    }


    @Test
    void testAddTagToImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE2.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "image tag", "tag attached to an image");

        image.link(client, tag);

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

        image.linkIfNotLinked(client, tag1, tag2, tag3, tag4);
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
    void testAddNewTagsToImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        TagAnnotationWrapper tag1 = client.getTag(TAG1.id);
        TagAnnotationWrapper tag2 = client.getTag(TAG2.id);
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Image tag 1", "tag attached to an image");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Image tag 2", "tag attached to an image");

        image.linkIfNotLinked(client, tag1, tag2, tag3, tag4);
        List<TagAnnotationWrapper> tags = image.getTags(client);
        client.delete(tag3);
        client.delete(tag4);
        List<TagAnnotationWrapper> endTags = image.getTags(client);

        assertTrue(image.isLinked(client, tag1));
        assertEquals(4, tags.size());
        assertEquals(2, endTags.size());
    }


    @Test
    void testAddAndRemoveTagFromImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE2.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to an image");

        image.link(client, tag);

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
        for (FileAnnotationWrapper fileAnn : files) {
            if (fileAnn.getId() == id) {
                assertEquals(file.getName(), fileAnn.getFileName());
                assertEquals("txt", fileAnn.getFileFormat());
                assertEquals("text/plain", fileAnn.getOriginalMimetype());
                assertEquals("text/plain", fileAnn.getServerFileMimetype());
                assertEquals("Plain Text Document", fileAnn.getFileKind());
                assertEquals(file.getParent() + File.separator, fileAnn.getContentAsString());
                assertEquals(file.getParent() + File.separator, fileAnn.getFilePath());
                assertFalse(fileAnn.isMovieFile());

                String tmpdir       = Files.createTempDirectory(null).toString();
                File   uploadedFile = fileAnn.getFile(client, tmpdir + File.separator + "uploaded.txt");

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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());

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

        RectangleWrapper rectangle = new RectangleWrapper(30, 30, 20, 20);
        rectangle.setCZT(1, 1, 2);

        EllipseWrapper ellipse = new EllipseWrapper(50, 50, 20, 40);
        ellipse.setCZT(1, 0, 1);

        int[] xBounds = {30, 69};
        int[] yBounds = {10, 89};
        int[] cBounds = {1, 1};
        int[] zBounds = {0, 1};
        int[] tBounds = {1, 2};

        ROIWrapper roiWrapper = new ROIWrapper();
        roiWrapper.setImage(image);
        roiWrapper.addShape(rectangle);
        roiWrapper.addShape(ellipse);

        ImagePlus imp1 = image.toImagePlus(client, roiWrapper);
        ImagePlus imp2 = image.toImagePlus(client, xBounds, yBounds, cBounds, zBounds, tBounds);

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


}
