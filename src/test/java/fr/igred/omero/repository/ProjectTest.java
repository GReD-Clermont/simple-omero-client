/*
 *  Copyright (C) 2020-2022 GReD
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

import static org.junit.Assert.assertEquals;


public class ProjectTest extends UserTest {


    @Test
    public void testGetDatasetFromProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<DatasetWrapper> datasets = project.getDatasets();

        assertEquals(2, datasets.size());
    }


    @Test
    public void testGetDatasetFromProject2() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<DatasetWrapper> datasets = project.getDatasets(DATASET1.name);

        assertEquals(1, datasets.size());
    }


    @Test
    public void testAddAndRemoveDataset() throws Exception {
        ProjectWrapper project = new ProjectWrapper(client, "To delete", "");
        DatasetWrapper dataset = client.getDataset(DATASET2.id);

        int initialSize = project.getDatasets().size();
        project.addDataset(client, dataset);
        int size = project.getDatasets().size();

        project.removeDataset(client, dataset);
        List<DatasetWrapper> datasets = project.getDatasets();
        datasets.removeIf(d -> d.getId() != dataset.getId());
        int newSize = datasets.size();

        client.delete(project);

        assertEquals(initialSize + 1, size);
        assertEquals(0, newSize);
    }


    @Test
    public void testAddTagToProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.addTag(client, tag);
        List<TagAnnotationWrapper> tags = project.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> endTags = project.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    public void testAddTagToProject2() throws Exception {
        final String name        = "test";
        final String description = "test";

        ProjectWrapper project = client.getProject(PROJECT1.id);

        project.addTag(client, name, description);
        List<TagAnnotationWrapper> tags = client.getTags(name);
        client.delete(tags.get(0));
        List<TagAnnotationWrapper> endTags = client.getTags(name);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    public void testAddTagIdToProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.addTag(client, tag.getId());
        List<TagAnnotationWrapper> tags = project.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> endTags = project.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    public void testAddTagsToProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Project tag 1", "tag attached to a project");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Project tag 2", "tag attached to a project");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Project tag 3", "tag attached to a project");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Project tag 4", "tag attached to a project");

        project.addTags(client, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());
        List<TagAnnotationWrapper> tags = project.getTags(client);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);
        List<TagAnnotationWrapper> endTags = project.getTags(client);

        assertEquals(4, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    public void testAddTagsToProject2() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.addTags(client, tag1, tag2, tag3, tag4);
        List<TagAnnotationWrapper> tags = project.getTags(client);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);
        List<TagAnnotationWrapper> endTags = project.getTags(client);

        assertEquals(4, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    public void testAddAndRemoveTagFromProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

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
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImages(client);

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesByNameInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImages(client, "image1.fake");

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesLikeInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImagesLike(client, ".fake");

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesTaggedInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImagesTagged(client, TAG1.id);

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesTaggedInProject2() throws Exception {
        TagAnnotationWrapper tag     = client.getTag(TAG2.id);
        ProjectWrapper       project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImagesTagged(client, tag);

        assertEquals(1, images.size());
    }


    @Test
    public void testGetImagesKeyInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImagesKey(client, "testKey1");

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesPairKeyValueInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImagesPairKeyValue(client, "testKey1", "testValue1");

        assertEquals(2, images.size());
    }


    @Test
    public void testSetName() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String name  = project.getName();
        String name2 = "NewName";
        project.setName(name2);
        project.saveAndUpdate(client);
        assertEquals(name2, client.getProject(PROJECT1.id).getName());

        project.setName(name);
        project.saveAndUpdate(client);
        assertEquals(name, client.getProject(PROJECT1.id).getName());
    }


    @Test
    public void testSetDescription() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String description = project.getDescription();

        String description2 = "NewName";
        project.setDescription(description2);
        project.saveAndUpdate(client);
        assertEquals(description2, client.getProject(PROJECT1.id).getDescription());

        project.setDescription(description);
        project.saveAndUpdate(client);
        assertEquals(description, client.getProject(PROJECT1.id).getDescription());
    }

}
