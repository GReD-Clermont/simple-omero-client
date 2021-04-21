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

package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.metadata.ChannelWrapper;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.ImageCalculator;
import ij.process.ImageStatistics;
import loci.plugins.BF;
import omero.gateway.model.MapAnnotationData;
import omero.model.NamedValue;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;


public class ImageTest extends UserTest {


    @Test
    public void testImportImage() throws Exception {
        File f = new File("./8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");
        if (!f.createNewFile())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be created.");

        File f2 = new File("./8bit-unsigned&pixelType=uint8&sizeZ=4&sizeC=5&sizeT=6&sizeX=512&sizeY=512.fake");
        if (!f2.createNewFile())
            System.err.println("\"" + f2.getCanonicalPath() + "\" could not be created.");

        DatasetWrapper dataset = client.getDataset(2L);

        dataset.importImages(client,
                             "./8bit-unsigned&pixelType=uint8&sizeZ=5&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake",
                             "./8bit-unsigned&pixelType=uint8&sizeZ=4&sizeC=5&sizeT=6&sizeX=512&sizeY=512.fake");

        if (!f.delete())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be deleted.");

        if (!f2.delete())
            System.err.println("\"" + f2.getCanonicalPath() + "\" could not be deleted.");

        List<ImageWrapper> images = dataset.getImages(client);

        assertEquals(2, images.size());

        for (ImageWrapper image : images) {
            client.deleteImage(image);
        }

        images = dataset.getImages(client);

        assert (images.isEmpty());
    }


    @Test
    public void testPairKeyValue() throws Exception {
        File f = new File("./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");
        if (!f.createNewFile())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be created.");

        DatasetWrapper dataset = client.getDataset(2L);

        dataset.importImages(client,
                             "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");

        if (!f.delete())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be deleted.");

        List<ImageWrapper> images = dataset.getImages(client);

        ImageWrapper image = images.get(0);

        List<NamedValue> result1 = new ArrayList<>();
        result1.add(new NamedValue("Test result1", "Value Test"));
        result1.add(new NamedValue("Test2 result1", "Value Test2"));

        List<NamedValue> result2 = new ArrayList<>();
        result2.add(new NamedValue("Test result2", "Value Test"));
        result2.add(new NamedValue("Test2 result2", "Value Test2"));

        MapAnnotationWrapper mapAnnotation1 = new MapAnnotationWrapper(result1);

        MapAnnotationData mapData2 = new MapAnnotationData();
        mapData2.setContent(result2);
        MapAnnotationWrapper mapAnnotation2 = new MapAnnotationWrapper(mapData2);

        assertEquals(result1, mapAnnotation1.getContent());

        image.addMapAnnotation(client, mapAnnotation1);
        image.addMapAnnotation(client, mapAnnotation2);

        List<NamedValue> result = image.getKeyValuePairs(client);

        assertEquals(4, result.size());
        assertEquals("Value Test", image.getValue(client, "Test result1"));

        client.deleteImage(image);
    }


    @Test
    public void testPairKeyValue2() throws Exception {
        File f = new File("./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");
        if (!f.createNewFile())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be created.");

        DatasetWrapper dataset = client.getDataset(2L);

        dataset.importImages(client,
                             "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");

        if (!f.delete())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be deleted.");

        List<ImageWrapper> images = dataset.getImages(client);

        ImageWrapper image = images.get(0);

        List<NamedValue> result = new ArrayList<>();
        result.add(new NamedValue("Test result1", "Value Test"));
        result.add(new NamedValue("Test2 result1", "Value Test2"));

        MapAnnotationWrapper mapAnnotation = new MapAnnotationWrapper();
        mapAnnotation.setContent(result);

        image.addMapAnnotation(client, mapAnnotation);

        List<NamedValue> results = image.getKeyValuePairs(client);

        assertEquals(2, results.size());
        assertEquals("Value Test", image.getValue(client, "Test result1"));

        client.deleteImage(image);
    }


    @Test
    public void testPairKeyValue3() throws Exception {
        boolean exception = false;
        File f =
                new File("./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");
        if (!f.createNewFile())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be created.");

        DatasetWrapper dataset = client.getDataset(2L);

        dataset.importImages(client,
                             "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");

        if (!f.delete())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be deleted.");

        List<ImageWrapper> images = dataset.getImages(client);

        ImageWrapper image = images.get(0);

        image.addPairKeyValue(client, "Test result1", "Value Test");
        image.addPairKeyValue(client, "Test result2", "Value Test2");

        List<NamedValue> results = image.getKeyValuePairs(client);

        assertEquals(2, results.size());
        try {
            image.getValue(client, "Nonexistent value");
        } catch (Exception e) {
            exception = true;
        }
        client.deleteImage(image);
        assertTrue(exception);
    }


    @Test
    public void testGetImageInfo() throws Exception {
        ImageWrapper image = client.getImage(1L);
        assertEquals("image1.fake", image.getName());
        assertNull(image.getDescription());
        assertEquals(1L, image.getId().longValue());
    }


    @Test
    public void testGetImageTag() throws Exception {
        ImageWrapper image = client.getImage(1L);

        List<TagAnnotationWrapper> tags = image.getTags(client);
        assertEquals(2, tags.size());
    }


    @Test
    public void testGetImageSize() throws Exception {
        ImageWrapper  image  = client.getImage(1L);
        PixelsWrapper pixels = image.getPixels();
        assertEquals(512, pixels.getSizeX());
        assertEquals(512, pixels.getSizeY());
        assertEquals(5, pixels.getSizeC());
        assertEquals(3, pixels.getSizeZ());
        assertEquals(7, pixels.getSizeT());
    }


    @Test
    public void testGetRawData() throws Exception {
        ImageWrapper     image  = client.getImage(1L);
        PixelsWrapper    pixels = image.getPixels();
        double[][][][][] value  = pixels.getAllPixels(client);

        assertEquals(pixels.getSizeX(), value[0][0][0][0].length);
        assertEquals(pixels.getSizeY(), value[0][0][0].length);
        assertEquals(pixels.getSizeC(), value[0][0].length);
        assertEquals(pixels.getSizeZ(), value[0].length);
        assertEquals(pixels.getSizeT(), value.length);
    }


    @Test
    public void testGetRawData2() throws Exception {
        ImageWrapper  image  = client.getImage(1L);
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
    public void testGetRawDataBound() throws Exception {
        ImageWrapper  image  = client.getImage(1L);
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
    public void testGetRawDataBoundError() throws Exception {
        ImageWrapper  image  = client.getImage(1L);
        PixelsWrapper pixels = image.getPixels();

        int[] xBound = {511, 513};
        int[] yBound = {0, 2};
        int[] cBound = {0, 2};
        int[] zBound = {0, 2};
        int[] tBound = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBound, yBound, cBound, zBound, tBound);
        assertNotEquals(xBound[1] - xBound[0] + 1, value[0][0][0][0].length);
    }


    @Test
    public void testGetRawDataBoundErrorNegative() throws Exception {
        ImageWrapper  image  = client.getImage(1L);
        PixelsWrapper pixels = image.getPixels();

        int[] xBound = {-1, 1};
        int[] yBound = {0, 2};
        int[] cBound = {0, 2};
        int[] zBound = {0, 2};
        int[] tBound = {0, 2};
        try {
            double[][][][][] value = pixels.getAllPixels(client, xBound, yBound, cBound, zBound, tBound);
            assertNotEquals(3, value[0][0][0][0].length);
        } catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void testToImagePlusBound() throws Exception {
        int[] xBound = {0, 2};
        int[] yBound = {0, 2};
        int[] cBound = {0, 2};
        int[] zBound = {0, 2};
        int[] tBound = {0, 2};

        Random random = new Random();
        xBound[0] = random.nextInt(500);
        yBound[0] = random.nextInt(500);
        cBound[0] = random.nextInt(3);
        tBound[0] = random.nextInt(5);
        xBound[1] = random.nextInt(507 - xBound[0]) + xBound[0] + 5;
        yBound[1] = random.nextInt(507 - yBound[0]) + yBound[0] + 5;
        cBound[1] = random.nextInt(3 - cBound[0]) + cBound[0] + 2;
        tBound[1] = random.nextInt(5 - tBound[0]) + tBound[0] + 2;

        String fake     = "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake";
        File   fakeFile = new File(fake);

        if (!fakeFile.createNewFile())
            System.err.println("\"" + fakeFile.getCanonicalPath() + "\" could not be created.");

        ImagePlus reference = BF.openImagePlus(fake)[0];

        if (!fakeFile.delete())
            System.err.println("\"" + fakeFile.getCanonicalPath() + "\" could not be deleted.");

        Duplicator duplicator = new Duplicator();
        reference.setRoi(xBound[0], yBound[0], xBound[1] - xBound[0] + 1, yBound[1] - yBound[0] + 1);
        ImagePlus crop = duplicator.run(reference,
                                        cBound[0] + 1, cBound[1] + 1,
                                        zBound[0] + 1, zBound[1] + 1,
                                        tBound[0] + 1, tBound[1] + 1);

        ImageWrapper image = client.getImage(1L);

        ImagePlus imp = image.toImagePlus(client, xBound, yBound, cBound, zBound, tBound);

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus       difference = calculator.run("difference create stack", crop, imp);
        ImageStatistics stats      = difference.getStatistics();

        assertEquals(0.5, imp.getCalibration().pixelHeight, 0.001);
        assertEquals(0.5, imp.getCalibration().pixelWidth, 0.001);
        assertEquals(1.0, imp.getCalibration().pixelDepth, 0.001);
        assertEquals("MICROMETER", imp.getCalibration().getUnit());
        assertEquals(0, (int) stats.max);
    }


    @Test
    public void testToImagePlus() throws Exception {
        String fake     = "8bit-unsigned&pixelType=uint8&sizeZ=2&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake";
        File   fakeFile = new File(fake);

        if (!fakeFile.createNewFile())
            System.err.println("\"" + fakeFile.getCanonicalPath() + "\" could not be created.");

        ImagePlus reference = BF.openImagePlus(fake)[0];

        if (!fakeFile.delete())
            System.err.println("\"" + fakeFile.getCanonicalPath() + "\" could not be deleted.");

        ImageWrapper image = client.getImage(3L);

        ImagePlus imp = image.toImagePlus(client);

        ImageCalculator calculator = new ImageCalculator();
        ImagePlus       difference = calculator.run("difference create stack", reference, imp);
        ImageStatistics stats      = difference.getStatistics();

        assertEquals(0, (int) stats.max);
    }


    @Test
    public void testGetImageChannel() throws Exception {
        ImageWrapper image = client.getImage(1L);
        assertEquals("0", image.getChannelName(client, 0));
    }


    @Test
    public void testGetImageChannelError() throws Exception {
        ImageWrapper image = client.getImage(1L);
        try {
            image.getChannelName(client, 6);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void testAddTagToImage() throws Exception {
        ImageWrapper image = client.getImage(3L);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "image tag", "tag attached to an image");

        image.addTag(client, tag);

        List<TagAnnotationWrapper> tags = image.getTags(client);

        assertEquals(1, tags.size());

        client.deleteTag(tag);

        tags = image.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagToImage2() throws Exception {
        ImageWrapper image = client.getImage(3L);

        image.addTag(client, "image tag", "tag attached to an image");

        List<TagAnnotationWrapper> tags = client.getTags("image tag");
        assertEquals(1, tags.size());

        client.deleteTag(tags.get(0).getId());

        tags = client.getTags("image tag");

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagIdToImage() throws Exception {
        ImageWrapper image = client.getImage(3L);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "image tag", "tag attached to an image");

        image.addTag(client, tag.getId());

        List<TagAnnotationWrapper> tags = image.getTags(client);

        assertEquals(1, tags.size());

        client.deleteTag(tag);

        tags = image.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagsToImage() throws Exception {
        ImageWrapper image = client.getImage(3L);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Image tag", "tag attached to an image");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Image tag", "tag attached to an image");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Image tag", "tag attached to an image");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Image tag", "tag attached to an image");

        image.addTags(client, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationWrapper> tags = image.getTags(client);

        assertEquals(4, tags.size());

        client.deleteTag(tag1);
        client.deleteTag(tag2);
        client.deleteTag(tag3);
        client.deleteTag(tag4);

        tags = image.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagsToImage2() throws Exception {
        ImageWrapper image = client.getImage(3L);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Image tag", "tag attached to an image");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Image tag", "tag attached to an image");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Image tag", "tag attached to an image");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Image tag", "tag attached to an image");

        image.addTags(client, tag1, tag2, tag3, tag4);

        List<TagAnnotationWrapper> tags = image.getTags(client);

        assertEquals(4, tags.size());

        client.deleteTag(tag1);
        client.deleteTag(tag2);
        client.deleteTag(tag3);
        client.deleteTag(tag4);

        tags = image.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testImageOrder() throws Exception {
        List<ImageWrapper> images = client.getImages();
        for (int i = 1; i < images.size(); i++) {
            assertTrue(images.get(i - 1).getId() <= images.get(i).getId());
        }
    }


    @Test
    public void testAddFileImage() throws Exception {
        ImageWrapper image = client.getImage(1L);

        File file = new File("./test.txt");
        if (!file.createNewFile())
            System.err.println("\"" + file.getCanonicalPath() + "\" could not be created.");

        byte[] array = new byte[2 * 262144 + 20];
        new Random().nextBytes(array);
        String generatedString = new String(array, StandardCharsets.UTF_8);
        try (PrintStream out = new PrintStream(new FileOutputStream("./test.txt"))) {
            out.print(generatedString);
        }

        Long id = image.addFile(client, file);
        if (!file.delete())
            System.err.println("\"" + file.getCanonicalPath() + "\" could not be deleted.");

        client.deleteFile(id);

        assertNotEquals(0L, id.longValue());
    }


    @Test
    public void testGetCreated() throws Exception {
        Date created = new Date(client.getImage(1L).getCreated().getTime());
        Date now     = new Date(System.currentTimeMillis());

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

        assertEquals(fmt.format(now), fmt.format(created));
    }


    @Test
    public void testGetAcquisitionDate() throws Exception {
        Date acquired = new Date(client.getImage(1L).getAcquisitionDate().getTime());

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        assertEquals("2020-04-01_20-04-01", fmt.format(acquired));
    }


    @Test
    public void testGetChannel() throws Exception {
        ChannelWrapper channel = client.getImage(1L).getChannels(client).get(0);
        assertEquals(0, channel.getIndex());
        channel.setName("Foo channel");
        assertEquals("Foo channel", channel.getName());
    }


    @Test
    public void testSetDescription() throws Exception {
        ImageWrapper image = client.getImage(1L);
        image.setDescription("Foo");
        assertEquals("Foo", image.getDescription());
    }


    @Test
    public void testSetName() throws Exception {
        ImageWrapper image = client.getImage(1L);
        String       name  = image.getName();
        image.setName("Foo image");
        assertEquals("Foo image", image.getName());
        image.setName(name);
        assertEquals(name, image.getName());
    }

}
