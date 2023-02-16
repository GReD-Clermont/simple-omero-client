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

package fr.igred.omero.containers;


import fr.igred.omero.Annotatable;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.FileAnnotation;
import fr.igred.omero.annotations.MapAnnotation;
import fr.igred.omero.annotations.Table;
import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.core.Image;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static fr.igred.omero.util.ReplacePolicy.DELETE;
import static fr.igred.omero.util.ReplacePolicy.DELETE_ORPHANED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class ProjectTest extends UserTest {


    @Test
    void testGetProjects() throws Exception {
        Project project = client.getProject(PROJECT1.id);
        assertEquals(project, project.getProjects(client).get(0));
    }


    @Test
    void testGetDatasets() throws Exception {
        Project project = client.getProject(PROJECT1.id);
        assertEquals(2, project.getDatasets(client).size());
    }


    @Test
    void testGetDatasetFromProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        List<Dataset> datasets = project.getDatasets();

        assertEquals(2, datasets.size());
    }


    @Test
    void testGetDatasetFromProject2() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        List<Dataset> datasets = project.getDatasets(DATASET1.name);

        assertEquals(1, datasets.size());
    }


    @Test
    void testAddAndRemoveDataset() throws Exception {
        Project project = new ProjectWrapper(client, "To delete", "");
        Dataset dataset = client.getDataset(DATASET2.id);

        int initialSize = project.getDatasets().size();
        project.addDataset(client, dataset);
        int size = project.getDatasets().size();

        project.removeDataset(client, dataset);
        List<Dataset> datasets = project.getDatasets();
        datasets.removeIf(ds -> ds.getId() != dataset.getId());
        int newSize = datasets.size();

        client.delete(project);

        assertEquals(initialSize + 1, size);
        assertEquals(0, newSize);
    }


    @Test
    void testAddTagToProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        TagAnnotation tag = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.link(client, tag);
        List<TagAnnotation> tags = project.getTags(client);
        client.delete(tag);
        List<TagAnnotation> endTags = project.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagToProject2() throws Exception {
        final String name        = "test";
        final String description = "test";

        Project project = client.getProject(PROJECT1.id);

        project.addTag(client, name, description);
        List<TagAnnotation> tags = client.getTags(name);
        client.delete(tags.get(0));
        List<TagAnnotation> endTags = client.getTags(name);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagIdToProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        RemoteObject<?> tag = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.addTag(client, tag.getId());
        List<TagAnnotation> tags = project.getTags(client);
        client.delete(tag);
        List<TagAnnotation> endTags = project.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagsToProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        RemoteObject<?> tag1 = new TagAnnotationWrapper(client, "Project tag 1", "tag attached to a project");
        RemoteObject<?> tag2 = new TagAnnotationWrapper(client, "Project tag 2", "tag attached to a project");
        RemoteObject<?> tag3 = new TagAnnotationWrapper(client, "Project tag 3", "tag attached to a project");
        RemoteObject<?> tag4 = new TagAnnotationWrapper(client, "Project tag 4", "tag attached to a project");

        project.addTags(client, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());
        List<TagAnnotation> tags = project.getTags(client);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);
        List<TagAnnotation> endTags = project.getTags(client);

        assertEquals(4, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddTagsToProject2() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        TagAnnotation tag1 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotation tag2 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotation tag3 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        TagAnnotation tag4 = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");

        project.link(client, tag1, tag2, tag3, tag4);
        List<TagAnnotation> tags = project.getTags(client);
        client.delete(tag1);
        client.delete(tag2);
        client.delete(tag3);
        client.delete(tag4);
        List<TagAnnotation> endTags = project.getTags(client);

        assertEquals(4, tags.size());
        assertEquals(0, endTags.size());
    }


    @Test
    void testAddAndRemoveTagFromProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        TagAnnotation tag = new TagAnnotationWrapper(client, "Project tag", "tag attached to a project");
        project.link(client, tag);
        List<TagAnnotation> tags = project.getTags(client);
        project.unlink(client, tag);
        List<TagAnnotation> removedTags = project.getTags(client);
        client.delete(tag);

        assertEquals(1, tags.size());
        assertEquals(0, removedTags.size());
    }


    @Test
    void testGetImagesInProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        List<Image> images = project.getImages(client);

        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesByNameInProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        List<Image> images = project.getImages(client, "image1.fake");

        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesLikeInProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        List<Image> images = project.getImagesLike(client, ".fake");

        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesTaggedInProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        List<Image> images = project.getImagesTagged(client, TAG1.id);

        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesTaggedInProject2() throws Exception {
        TagAnnotation tag     = client.getTag(TAG2.id);
        Project       project = client.getProject(PROJECT1.id);

        List<Image> images = project.getImagesTagged(client, tag);

        assertEquals(1, images.size());
    }


    @Test
    void testGetImagesKeyInProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        List<Image> images = project.getImagesWithKey(client, "testKey1");

        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesKeyValuePairInProject() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        List<Image> images = project.getImagesWithKeyValuePair(client, "testKey1", "testValue1");

        assertEquals(2, images.size());
    }


    @Test
    void testSetName() throws Exception {
        Project project = client.getProject(PROJECT1.id);

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
    void testSetDescription() throws Exception {
        Project project = client.getProject(PROJECT1.id);

        String description = project.getDescription();

        String description2 = "NewName";
        project.setDescription(description2);
        project.saveAndUpdate(client);
        assertEquals(description2, client.getProject(PROJECT1.id).getDescription());

        project.setDescription(description);
        project.saveAndUpdate(client);
        assertEquals(description, client.getProject(PROJECT1.id).getDescription());
    }


    @Test
    void testCopyAnnotations() throws Exception {
        Project        project1 = client.getProject(PROJECT1.id);
        Annotatable<?> project2 = new ProjectWrapper(client, "CopyTest", "Copy annotations");

        File file = createRandomFile("test_project.txt");

        long fileId = project1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        TagAnnotation tag = new TagAnnotationWrapper(client, "CopyTestTag", "Copy annotations");
        project1.link(client, tag);
        project1.addKeyValuePair(client, "CopyTest", "Annotation");

        Table table = new TableWrapper(1, "CopyTest");
        table.setColumn(0, "Name", String.class);
        table.setRowCount(1);
        table.addRow("Annotation");
        project1.addTable(client, table);

        project2.copyAnnotationLinks(client, project1);

        assertEquals(project1.getTags(client).size(), project2.getTags(client).size());
        assertEquals(project1.getTables(client).size(), project2.getTables(client).size());
        assertEquals(project1.getFileAnnotations(client).size(), project2.getFileAnnotations(client).size());
        assertEquals(project1.getMapAnnotations(client).size(), project2.getMapAnnotations(client).size());

        client.deleteFile(fileId);
        client.delete(tag);
        client.delete(table);
        List<MapAnnotation> maps = project1.getMapAnnotations(client);
        if (!maps.isEmpty())
            for (MapAnnotation map : maps)
                client.delete(map);

        assertEquals(0, project2.getTags(client).size());
        assertEquals(0, project2.getTables(client).size());
        assertEquals(0, project2.getFileAnnotations(client).size());
        assertEquals(0, project2.getMapAnnotations(client).size());

        client.delete(project2);
    }


    @Test
    void testCopyFileAnnotation() throws Exception {
        Project        project1 = client.getProject(PROJECT1.id);
        Annotatable<?> project2 = new ProjectWrapper(client, "CopyTest", "Copy file annotation");

        File file = createRandomFile("test_project.txt");

        long fileId = project1.addFile(client, file);
        removeFile(file);
        assertNotEquals(0L, fileId);

        List<FileAnnotation> files = project1.getFileAnnotations(client);
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
        Annotatable<?> project1 = new ProjectWrapper(client, "ReplaceTest1", "Replace file annotation");
        Annotatable<?> project2 = new ProjectWrapper(client, "ReplaceTest2", "Replace file annotation");

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
        Annotatable<?> project1 = new ProjectWrapper(client, "ReplaceTest1", "Replace file annotation");
        Annotatable<?> project2 = new ProjectWrapper(client, "ReplaceTest2", "Replace file annotation");

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
        Annotatable<?> project1 = new ProjectWrapper(client, "ReplaceTest1", "Replace file annotation");
        Annotatable<?> project2 = new ProjectWrapper(client, "ReplaceTest2", "Replace file annotation");

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
        Annotatable<?> project1 = new ProjectWrapper(client, "ReplaceTest1", "Replace file annotation");

        File file = createRandomFile("test_project.txt");

        long fileId1 = project1.addFile(client, file);
        assertEquals(1, project1.getFileAnnotations(client).size());
        long fileId2 = project1.addAndReplaceFile(client, file, DELETE_ORPHANED);
        assertEquals(1, project1.getFileAnnotations(client).size());
        assertNotEquals(fileId1, fileId2);

        removeFile(file);
        client.delete(project1);
        client.deleteFile(fileId2);
    }

}
