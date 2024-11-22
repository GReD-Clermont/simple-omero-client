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
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.core.ImageWrapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static fr.igred.omero.util.ReplacePolicy.DELETE;
import static fr.igred.omero.util.ReplacePolicy.DELETE_ORPHANED;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ProjectTest extends UserTest {


    @Test
    void testGetDatasetFromProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<DatasetWrapper> datasets = project.getDatasets();

        assertEquals(3, datasets.size());
    }


    @Test
    void testGetDatasetFromProject2() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<DatasetWrapper> datasets = project.getDatasets(DATASET1.name);

        assertEquals(1, datasets.size());
    }


    @Test
    void testAddAndRemoveDataset() throws Exception {
        ProjectWrapper project = new ProjectWrapper(client, "To delete", "");
        DatasetWrapper dataset = client.getDataset(DATASET2.id);

        int initialSize = project.getDatasets().size();
        project.addDataset(client, dataset);
        project.reload(client);
        int size = project.getDatasets().size();

        project.removeDataset(client, dataset);
        List<DatasetWrapper> datasets = project.getDatasets();
        datasets.removeIf(ds -> ds.getId() != dataset.getId());
        int newSize = datasets.size();

        client.delete(project);

        assertEquals(initialSize + 1, size);
        assertEquals(0, newSize);
    }


    @Test
    void testAddTagToProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String name = "Project tag";
        String desc = "tag attached to a project";

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, name, desc);

        project.link(client, tag);
        List<TagAnnotationWrapper> tags = project.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> endTags = project.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagToProject2() throws Exception {
        String name = "Project tag";
        String desc = "tag attached to a project";

        ProjectWrapper project = client.getProject(PROJECT1.id);

        project.addTag(client, name, desc);
        List<TagAnnotationWrapper> tags = client.getTags(name);
        client.delete(tags.get(0));
        List<TagAnnotationWrapper> endTags = client.getTags(name);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagIdToProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String name = "Project tag";
        String desc = "tag attached to a project";

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, name, desc);

        project.addTag(client, tag.getId());
        List<TagAnnotationWrapper> tags = project.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> endTags = project.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagsToProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String   name  = "Project tag";
        String[] names = {name + " 1", name + " 2", name + " 3", name + " 4"};
        String   desc  = "tag attached to a project";

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, names[0], desc);
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, names[1], desc);
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, names[2], desc);
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, names[3], desc);

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
    void testAddTagsToProject2() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String   name  = "Project tag";
        String[] names = {name + " 1", name + " 2", name + " 3", name + " 4"};
        String   desc  = "tag attached to a project";

        TagAnnotationWrapper tag1 = new TagAnnotationWrapper(client, names[0], desc);
        TagAnnotationWrapper tag2 = new TagAnnotationWrapper(client, names[1], desc);
        TagAnnotationWrapper tag3 = new TagAnnotationWrapper(client, names[2], desc);
        TagAnnotationWrapper tag4 = new TagAnnotationWrapper(client, names[3], desc);

        project.link(client, tag1, tag2, tag3, tag4);
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
    void testAddAndRemoveTagFromProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String name = "Project tag";
        String desc = "tag attached to a project";

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, name, desc);
        project.link(client, tag);
        List<TagAnnotationWrapper> tags = project.getTags(client);
        project.unlink(client, tag);
        List<TagAnnotationWrapper> removedTags = project.getTags(client);
        client.delete(tag);

        assertEquals(1, tags.size());
        assertEquals(0, removedTags.size());
    }


    @Test
    void testGetImagesInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImages(client);

        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesByNameInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImages(client, "image1.fake");

        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesLikeInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImagesLike(client, ".fake");

        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesTaggedInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImagesTagged(client, TAG1.id);

        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesTaggedInProject2() throws Exception {
        TagAnnotationWrapper tag     = client.getTag(TAG2.id);
        ProjectWrapper       project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImagesTagged(client, tag);

        assertEquals(1, images.size());
    }


    @Test
    void testGetImagesWithKeyInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        List<ImageWrapper> images = project.getImagesWithKey(client, "testKey1");

        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesWithKeyValuePairInProject() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String key = "testKey1";
        String val = "testValue1";

        List<ImageWrapper> images = project.getImagesWithKeyValuePair(client, key, val);
        assertEquals(2, images.size());
    }


    @Test
    void testSetName() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String name  = project.getName();
        String name2 = "NewName";
        project.setName(name2);
        project.saveAndUpdate(client);
        String checkName2 = client.getProject(PROJECT1.id).getName();

        project.setName(name);
        project.saveAndUpdate(client);

        long updated = project.getUpdated().getTime();
        long now     = System.currentTimeMillis();

        String msg = "Updated at: %d. Current time was: %d";
        assertTrue(now - updated < 5000, format(msg, updated, now));
        assertEquals(name2, checkName2);
        assertEquals(name, client.getProject(PROJECT1.id).getName());
    }


    @Test
    void testSetDescription() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);

        String description = project.getDescription();

        String description2 = "NewName";
        project.setDescription(description2);
        project.saveAndUpdate(client);
        assertEquals(description2,
                     client.getProject(PROJECT1.id).getDescription());

        project.setDescription(description);
        project.saveAndUpdate(client);
        assertEquals(description,
                     client.getProject(PROJECT1.id).getDescription());
    }


    @Test
    void testCopyAnnotations() throws Exception {
        String p2name = "CopyTest";
        String p2desc = "Copy annotations";

        ProjectWrapper project1 = client.getProject(PROJECT1.id);
        ProjectWrapper project2 = new ProjectWrapper(client, p2name, p2desc);

        File file = createRandomFile("test_project.txt");

        long fileId = project1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        String name = "CopyTestTag";
        String desc = "Copy annotations";

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, name, desc);
        project1.link(client, tag);
        project1.addKeyValuePair(client, "CopyTest", "Annotation");

        TableWrapper table = new TableWrapper(1, "CopyTest");
        table.setColumn(0, "Name", String.class);
        table.setRowCount(1);
        table.addRow("Annotation");
        project1.addTable(client, table);

        project2.copyAnnotationLinks(client, project1);

        assertEquals(project1.getTags(client).size(),
                     project2.getTags(client).size());
        assertEquals(project1.getTables(client).size(),
                     project2.getTables(client).size());
        assertEquals(project1.getFileAnnotations(client).size(),
                     project2.getFileAnnotations(client).size());
        assertEquals(project1.getMapAnnotations(client).size(),
                     project2.getMapAnnotations(client).size());

        client.deleteFile(fileId);
        client.delete(tag);
        client.deleteTable(table);
        List<MapAnnotationWrapper> maps = project1.getMapAnnotations(client);
        if (!maps.isEmpty()) {
            for (MapAnnotationWrapper map : maps) {
                client.delete(map);
            }
        }

        assertEquals(0, project2.getTags(client).size());
        assertEquals(0, project2.getTables(client).size());
        assertEquals(0, project2.getFileAnnotations(client).size());
        assertEquals(0, project2.getMapAnnotations(client).size());

        client.delete(project2);
    }


    @Test
    void testCopyFileAnnotation() throws Exception {
        String p2name = "CopyTest";
        String p2desc = "Copy annotations";

        ProjectWrapper project1 = client.getProject(PROJECT1.id);
        ProjectWrapper project2 = new ProjectWrapper(client, p2name, p2desc);

        File file = createRandomFile("test_project.txt");

        long fileId = project1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        List<FileAnnotationWrapper> files = project1.getFileAnnotations(client);
        assertEquals(1, files.size());

        if (!files.isEmpty()) {
            project2.link(client, files.get(0));
        }
        assertEquals(1, project2.getFileAnnotations(client).size());

        client.deleteFile(fileId);
        client.delete(project2);

        assertNotEquals(0L, fileId);
    }


    @Test
    void testReplaceAndUnlinkFile() throws Exception {
        String name = "ReplaceTest";
        String desc = "Replace file annotation";

        ProjectWrapper project1 = new ProjectWrapper(client, name + "1", desc);
        ProjectWrapper project2 = new ProjectWrapper(client, name + "2", desc);

        File file = createRandomFile("test_project.txt");

        long fileId1 = project1.addFile(client, file);
        assertEquals(1, project1.getFileAnnotations(client).size());
        project2.link(client, project1.getFileAnnotations(client).get(0));
        long fileId2 = project1.addAndReplaceFile(client, file);
        assertEquals(1, project1.getFileAnnotations(client).size());
        assertEquals(1, project2.getFileAnnotations(client).size());
        assertNotEquals(fileId1, fileId2);

        removeFile(file);
        client.delete(project1);
        client.delete(project2);
        client.deleteFile(fileId1);
        client.deleteFile(fileId2);
    }


    @Test
    void testReplaceAndDeleteFile() throws Exception {
        String name = "ReplaceTest";
        String desc = "Replace file annotation";

        ProjectWrapper project1 = new ProjectWrapper(client, name + "1", desc);
        ProjectWrapper project2 = new ProjectWrapper(client, name + "2", desc);

        File file = createRandomFile("test_project.txt");

        long fileId1 = project1.addFile(client, file);
        assertEquals(1, project1.getFileAnnotations(client).size());
        project2.link(client, project1.getFileAnnotations(client).get(0));
        long fileId2 = project1.addAndReplaceFile(client, file, DELETE);
        assertEquals(1, project1.getFileAnnotations(client).size());
        assertEquals(0, project2.getFileAnnotations(client).size());
        assertNotEquals(fileId1, fileId2);

        removeFile(file);
        client.delete(project1);
        client.delete(project2);
        client.deleteFile(fileId2);
    }


    @Test
    void testReplaceAndDeleteOrphanedFile1() throws Exception {
        String name = "ReplaceTest";
        String desc = "Replace file annotation";

        ProjectWrapper project1 = new ProjectWrapper(client, name + "1", desc);
        ProjectWrapper project2 = new ProjectWrapper(client, name + "2", desc);

        File file = createRandomFile("test_project.txt");

        long fileId1 = project1.addFile(client, file);
        assertEquals(1, project1.getFileAnnotations(client).size());
        project2.link(client, project1.getFileAnnotations(client).get(0));
        long fileId2 = project1.addAndReplaceFile(client, file, DELETE_ORPHANED);
        assertEquals(1, project1.getFileAnnotations(client).size());
        assertEquals(1, project2.getFileAnnotations(client).size());
        assertNotEquals(fileId1, fileId2);

        removeFile(file);
        client.delete(project1);
        client.delete(project2);
        client.deleteFile(fileId1);
        client.deleteFile(fileId2);
    }


    @Test
    void testReplaceAndDeleteOrphanedFile2() throws Exception {
        String name = "ReplaceTest";
        String desc = "Replace file annotation";

        ProjectWrapper project = new ProjectWrapper(client, name, desc);

        File file = createRandomFile("test_project.txt");

        long fileId1 = project.addFile(client, file);
        assertEquals(1, project.getFileAnnotations(client).size());
        long fileId2 = project.addAndReplaceFile(client, file, DELETE_ORPHANED);
        assertEquals(1, project.getFileAnnotations(client).size());
        assertNotEquals(fileId1, fileId2);

        removeFile(file);
        client.delete(project);
        client.deleteFile(fileId2);
    }

}
