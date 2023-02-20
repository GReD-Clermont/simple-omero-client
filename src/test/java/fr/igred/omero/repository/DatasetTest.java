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


import fr.igred.omero.RemoteObject;
import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import omero.gateway.model.TagAnnotationData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DatasetTest extends UserTest {


    @Test
    void testCreateDatasetAndDeleteIt1() throws Exception {
        String name = "To delete";

        Project project = client.getProject(PROJECT1.id);

        Long id = project.addDataset(client, name, "Dataset which will be deleted").getId();

        Dataset dataset = client.getDataset(id);

        assertEquals(name, dataset.getName());

        assertTrue(dataset.canLink());
        assertTrue(dataset.canAnnotate());
        assertTrue(dataset.canEdit());
        assertTrue(dataset.canDelete());
        assertTrue(dataset.canChgrp());
        assertFalse(dataset.canChown());

        client.delete(dataset);
        assertThrows(NoSuchElementException.class, () -> client.getDataset(id));
    }


    @Test
    void testCreateDatasetAndDeleteIt2() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        String description = "Dataset which will be deleted";

        Dataset dataset = new DatasetWrapper("To delete", description);

        long id = project.addDataset(client, dataset).getId();

        Dataset checkDataset = client.getDataset(id);
        client.delete(checkDataset);
        assertThrows(NoSuchElementException.class, () -> client.getDataset(id));
        assertEquals(description, checkDataset.getDescription());
    }


    @Test
    void testCopyDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client);

        Project project = client.getProject(2L);

        String name = "Will be deleted";

        Long id = project.addDataset(client, name, "Dataset which will be deleted").getId();

        Dataset newDataset = client.getDataset(id);

        newDataset.addImages(client, images);

        assertEquals(images.size(), newDataset.getImages(client).size());

        for (Image image : images) {
            newDataset.removeImage(client, image);
        }
        assertTrue(newDataset.getImages(client).isEmpty());

        client.delete(newDataset);

        dataset.refresh(client);
        List<Image> newImages = dataset.getImages(client);

        assertEquals(images.size(), newImages.size());
    }


    @Test
    void testDatasetBasic() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        assertEquals(DATASET1.name, dataset.getName());
        assertEquals(DATASET1.description, dataset.getDescription());
        assertEquals(1L, dataset.getId());
    }


    @Test
    void testAddTagToDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");

        dataset.addTag(client, tag);

        List<TagAnnotationWrapper> tags = dataset.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> endTags = dataset.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagToDataset2() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        dataset.addTag(client, "Dataset tag", "tag attached to a dataset");

        List<TagAnnotationWrapper> tags = client.getTags("Dataset tag");
        assertEquals(1, tags.size());

        client.delete(tags.get(0));
        List<TagAnnotationWrapper> endTags = client.getTags("Dataset tag");

        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagIdToDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        RemoteObject<TagAnnotationData>
                tag = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");

        long tagId = tag.getId();
        dataset.addTag(client, tagId);
        List<TagAnnotationWrapper> tags = dataset.getTags(client);
        client.delete(tag);
        assertThrows(NullPointerException.class, () -> client.getTag(tagId));
        assertEquals(1, tags.size());
    }


    @Test
    void testAddTagsToDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");

        dataset.addTags(client, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationWrapper> tags = dataset.getTags(client);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);
        List<TagAnnotationWrapper> endTags = dataset.getTags(client);

        assertEquals(4, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagsToDataset2() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");

        dataset.addTags(client, tag1, tag2, tag3, tag4);

        List<TagAnnotationWrapper> tags = dataset.getTags(client);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);
        List<TagAnnotationWrapper> endTags = dataset.getTags(client);

        assertEquals(4, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddAndRemoveTagFromDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Dataset tag", "tag attached to a dataset");

        dataset.addTag(client, tag);

        List<TagAnnotationWrapper> tags = dataset.getTags(client);
        dataset.unlink(client, tag);
        List<TagAnnotationWrapper> removed = dataset.getTags(client);
        client.delete(tag);

        assertEquals(1, tags.size());
        assertEquals(0, removed.size());
    }


    @Test
    void testGetProjects() throws Exception {
        assertEquals(PROJECT1.id, client.getImage(DATASET1.id).getProjects(client).get(0).getId());
    }


    @Test
    void testGetImagesInDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client);

        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesByNameInDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client, "image1.fake");

        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesLikeInDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImagesLike(client, ".fake");

        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesTaggedInDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImagesTagged(client, TAG1.id);

        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesTaggedInDataset2() throws Exception {
        TagAnnotationWrapper tag     = client.getTag(TAG2.id);
        Dataset              dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImagesTagged(client, tag);

        assertEquals(1, images.size());
    }


    @Test
    void testGetImagesKeyInDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImagesKey(client, "testKey1");

        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesPairKeyValueInDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImagesPairKeyValue(client, "testKey1", "testValue1");

        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesFromDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client);
        assertEquals(3, images.size());
    }


    @Test
    void testAddFileDataset() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        File file = createRandomFile("test_dataset.txt");
        long id   = dataset.addFile(client, file);
        removeFile(file);
        client.deleteFile(id);
        assertNotEquals(0L, id);
    }


    @Test
    void testSetName() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        String name  = dataset.getName();
        String name2 = "NewName";
        dataset.setName(name2);
        dataset.saveAndUpdate(client);
        assertEquals(name2, client.getDataset(DATASET1.id).getName());

        dataset.setName(name);
        dataset.saveAndUpdate(client);
        assertEquals(name, client.getDataset(DATASET1.id).getName());
    }


    @Test
    void testSetDescription() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        String description  = dataset.getDescription();
        String description2 = "NewName";
        dataset.setDescription(description2);
        dataset.saveAndUpdate(client);
        assertEquals(description2, client.getDataset(DATASET1.id).getDescription());

        dataset.setDescription(description);
        dataset.saveAndUpdate(client);
        assertEquals(description, client.getDataset(DATASET1.id).getDescription());
    }

}
