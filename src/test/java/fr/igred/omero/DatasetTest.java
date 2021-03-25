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


import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ProjectContainer;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.Assert.*;


public class DatasetTest extends UserTest {


    @Test
    public void testCreateDatasetAndDeleteIt1() throws Exception {
        boolean exception = false;
        String  name      = "To delete";

        ProjectContainer project = client.getProject(2L);

        Long id = project.addDataset(client, name, "Dataset which will be deleted").getId();

        DatasetContainer dataset = client.getDataset(id);

        assertEquals(dataset.getName(), name);

        client.deleteDataset(dataset);

        try {
            client.getDataset(id);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testCreateDatasetAndDeleteIt2() throws Exception {
        boolean exception = false;

        ProjectContainer project = client.getProject(2L);

        String description = "Dataset which will be deleted";

        DatasetContainer dataset = new DatasetContainer("To delete", description);

        Long id = project.addDataset(client, dataset).getId();

        dataset = client.getDataset(id);

        assertEquals(dataset.getDescription(), description);

        client.deleteDataset(dataset);

        try {
            client.getDataset(id);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testCopyDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(client);

        ProjectContainer project = client.getProject(3L);

        String name = "Will be deleted";

        Long id = project.addDataset(client, name, "Dataset which will be deleted").getId();

        DatasetContainer newDataset = client.getDataset(id);

        newDataset.addImages(client, images);

        assertEquals(images.size(), newDataset.getImages(client).size());

        client.deleteDataset(newDataset);

        List<ImageContainer> newImages = dataset.getImages(client);

        assertEquals(images.size(), newImages.size());
    }


    @Test
    public void testDatasetBasic() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        assertEquals("TestDataset", dataset.getName());
        assertEquals("description", dataset.getDescription());
        assertEquals(1L, dataset.getId().longValue());
    }


    @Test
    public void testAddTagToDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        TagAnnotationContainer tag = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");

        dataset.addTag(client, tag);

        List<TagAnnotationContainer> tags = dataset.getTags(client);

        assertEquals(1, tags.size());

        client.deleteTag(tag);

        tags = dataset.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagToDataset2() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        dataset.addTag(client, "Dataset tag", "tag attached to a dataset");

        List<TagAnnotationContainer> tags = client.getTags("Dataset tag");
        assertEquals(1, tags.size());

        client.deleteTag(tags.get(0).getId());

        tags = client.getTags("Dataset tag");

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagIdToDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        TagAnnotationContainer tag = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");

        dataset.addTag(client, tag.getId());

        List<TagAnnotationContainer> tags = dataset.getTags(client);

        assertEquals(1, tags.size());

        client.deleteTag(tag);

        tags = dataset.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagsToDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");

        dataset.addTags(client, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationContainer> tags = dataset.getTags(client);

        assertEquals(4, tags.size());

        client.deleteTag(tag1);
        client.deleteTag(tag2);
        client.deleteTag(tag3);
        client.deleteTag(tag4);

        tags = dataset.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagsToDataset2() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(client, "Dataset tag", "tag attached to a dataset");

        dataset.addTags(client, tag1, tag2, tag3, tag4);

        List<TagAnnotationContainer> tags = dataset.getTags(client);

        assertEquals(4, tags.size());

        client.deleteTag(tag1);
        client.deleteTag(tag2);
        client.deleteTag(tag3);
        client.deleteTag(tag4);

        tags = dataset.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testGetImagesInDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(client);

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesByNameInDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(client, "image1.fake");

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesLikeInDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesLike(client, ".fake");

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesTaggedInDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesTagged(client, 1L);

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesTaggedInDataset2() throws Exception {
        TagAnnotationContainer tag     = client.getTag(2L);
        DatasetContainer       dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesTagged(client, tag);

        assertEquals(1, images.size());
    }


    @Test
    public void testGetImagesKeyInDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesKey(client, "testKey1");

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesPairKeyValueInDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesPairKeyValue(client, "testKey1", "testValue1");

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesFromDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(client);
        assertEquals(3, images.size());
    }


    @Test
    public void testAddFileDataset() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        File file = new File("./test.txt");
        if (!file.createNewFile())
            System.err.println("\"" + file.getCanonicalPath() + "\" could not be created.");

        byte[] array = new byte[2*262144+20];
        new Random().nextBytes(array);
        String generatedString = new String(array, StandardCharsets.UTF_8);
        try (PrintStream out = new PrintStream(new FileOutputStream("./test.txt"))) {
            out.print(generatedString);
        }

        Long id = dataset.addFile(client, file);
        if (!file.delete())
            System.err.println("\"" + file.getCanonicalPath() + "\" could not be deleted.");

        client.deleteFile(id);

        assertNotEquals(0L, id.longValue());
    }

}
