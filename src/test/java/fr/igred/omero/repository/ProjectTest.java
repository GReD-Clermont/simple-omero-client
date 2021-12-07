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
import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ProjectTest extends UserTest {


    @Test
    public void testGetDatasetFromProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        List<DatasetWrapper> datasets = project.getDatasets();

        assertEquals(2, datasets.size());
    }


    @Test
    public void testGetDatasetFromProject2() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        List<DatasetWrapper> datasets = project.getDatasets("TestDataset");

        assertEquals(1, datasets.size());
    }


    @Test
    public void testAddAndRemoveDataset() throws Exception {
        boolean exception = false;

        ProjectWrapper project = client.getProject(1L);
        int initialSize = project.getDatasets().size();

        String         description = "Dataset which will be deleted";
        DatasetWrapper dataset     = new DatasetWrapper("To delete", description);

        Long id = project.addDataset(client, dataset).getId();

        DatasetWrapper checkDataset = client.getDataset(id);
        assertEquals(description, checkDataset.getDescription());
        assertEquals(initialSize + 1, project.getDatasets().size());

        project.removeDataset(client, checkDataset);
        List<DatasetWrapper> datasets = project.getDatasets();
        datasets.removeIf(d -> d.getId() != checkDataset.getId());
        assertEquals(0, datasets.size());

        client.delete(checkDataset);
        try {
            client.getDataset(id);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testAddTagToProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.addTag(client, tag);

        List<TagAnnotationWrapper> tags = project.getTags(client);

        assertEquals(1, tags.size());

        client.delete(tag);

        tags = project.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagToProject2() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        project.addTag(client, "test", "test");

        List<TagAnnotationWrapper> tags = client.getTags("test");
        assertEquals(1, tags.size());

        client.delete(tags.get(0));

        tags = client.getTags("test");
        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagIdToProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.addTag(client, tag.getId());

        List<TagAnnotationWrapper> tags = project.getTags(client);
        assertEquals(1, tags.size());

        client.delete(tag);

        tags = project.getTags(client);
        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagsToProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.addTags(client, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationWrapper> tags = project.getTags(client);

        assertEquals(4, tags.size());

        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);

        tags = project.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagsToProject2() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.addTags(client, tag1, tag2, tag3, tag4);

        List<TagAnnotationWrapper> tags = project.getTags(client);

        assertEquals(4, tags.size());

        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);

        tags = project.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddAndRemoveTagFromProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        project.addTag(client, tag);
        List<TagAnnotationWrapper> tags = project.getTags(client);
        project.unlink(client, tag);
        List<TagAnnotationWrapper> removedTags = project.getTags(client);
        client.delete(tag);
        assertEquals(1, tags.size());
        assertEquals(0, removedTags.size());
    }


    @Test
    public void testGetImagesInProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        List<ImageWrapper> images = project.getImages(client);

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesByNameInProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        List<ImageWrapper> images = project.getImages(client, "image1.fake");

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesLikeInProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        List<ImageWrapper> images = project.getImagesLike(client, ".fake");

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesTaggedInProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        List<ImageWrapper> images = project.getImagesTagged(client, 1L);

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesTaggedInProject2() throws Exception {
        TagAnnotationWrapper tag     = client.getTag(2L);
        ProjectWrapper       project = client.getProject(1L);

        List<ImageWrapper> images = project.getImagesTagged(client, tag);

        assertEquals(1, images.size());
    }


    @Test
    public void testGetImagesKeyInProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        List<ImageWrapper> images = project.getImagesKey(client, "testKey1");

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesPairKeyValueInProject() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        List<ImageWrapper> images = project.getImagesPairKeyValue(client, "testKey1", "testValue1");

        assertEquals(2, images.size());
    }


    @Test
    public void testSetName() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        String name  = project.getName();
        String name2 = "NewName";
        project.setName(name2);
        project.saveAndUpdate(client);
        assertEquals(name2, client.getProject(1L).getName());

        project.setName(name);
        project.saveAndUpdate(client);
        assertEquals(name, client.getProject(1L).getName());
    }


    @Test
    public void testSetDescription() throws Exception {
        ProjectWrapper project = client.getProject(1L);

        String description = project.getDescription();

        String description2 = "NewName";
        project.setDescription(description2);
        project.saveAndUpdate(client);
        assertEquals(description2, client.getProject(1L).getDescription());

        project.setDescription(description);
        project.saveAndUpdate(client);
        assertEquals(description, client.getProject(1L).getDescription());
    }

}