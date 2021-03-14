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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import loci.common.DebugTools;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;


public class DatasetTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName Name of the test case.
     */
    public DatasetTest(String testName) {
        super(testName);
    }


    /**
     * @return the suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(DatasetTest.class);
    }


    public void testCreateDatasetAndDeleteIt1() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        String name = "To delete";

        Long id = project.addDataset(root, name, "Dataset which will ne deleted").getId();

        DatasetContainer dataset = root.getDataset(id);

        assertEquals(dataset.getName(), name);

        root.deleteDataset(dataset);

        try {
            root.getDataset(id);
            assert (false);
        } catch (NoSuchElementException e) {
            assert (true);
        }
    }


    public void testCreateDatasetAndDeleteIt2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        String description = "Dataset which will ne deleted";

        DatasetContainer dataset = new DatasetContainer("To delete", description);

        Long id = project.addDataset(root, dataset).getId();

        dataset = root.getDataset(id);

        assertEquals(dataset.getDescription(), description);

        root.deleteDataset(dataset);

        try {
            root.getDataset(id);
            assert (false);
        } catch (NoSuchElementException e) {
            assert (true);
        }
    }


    public void testCopyDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        ProjectContainer project = root.getProject(3L);

        String name = "Will be deleted";

        Long id = project.addDataset(root, name, "Dataset which will be deleted").getId();

        DatasetContainer newDataset = root.getDataset(id);

        newDataset.addImages(root, images);

        assert (newDataset.getImages(root).size() == images.size());

        root.deleteDataset(newDataset);

        List<ImageContainer> newImages = dataset.getImages(root);

        assert (newImages.size() == images.size());
    }


    public void testDatasetBasic() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        assertEquals("TestDataset", dataset.getName());
        assertEquals("description", dataset.getDescription());
        assert (dataset.getId() == 1L);
    }


    public void testAddTagToDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");

        dataset.addTag(root, tag);

        List<TagAnnotationContainer> tags = dataset.getTags(root);

        assert (tags.size() == 1);

        root.deleteTag(tag);

        tags = dataset.getTags(root);

        assert (tags.size() == 0);
    }


    public void testAddTagToDataset2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        dataset.addTag(root, "Dataset tag", "tag attached to a dataset");

        List<TagAnnotationContainer> tags = root.getTags("Dataset tag");
        assert (tags.size() == 1);

        root.deleteTag(tags.get(0).getId());

        tags = root.getTags("Dataset tag");
        assert (tags.size() == 0);
    }


    public void testAddTagIdToDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");

        dataset.addTag(root, tag.getId());

        List<TagAnnotationContainer> tags = dataset.getTags(root);

        assert (tags.size() == 1);

        root.deleteTag(tag);

        tags = dataset.getTags(root);

        assert (tags.size() == 0);
    }


    public void testAddTagsToDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");

        dataset.addTags(root, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationContainer> tags = dataset.getTags(root);

        assert (tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = dataset.getTags(root);

        assert (tags.size() == 0);
    }


    public void testAddTagsToDataset2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");

        dataset.addTags(root, tag1, tag2, tag3, tag4);

        List<TagAnnotationContainer> tags = dataset.getTags(root);

        assert (tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = dataset.getTags(root);

        assert (tags.size() == 0);
    }


    public void testGetImagesInDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        assert (images.size() == 3);
    }


    public void testGetImagesByNameInDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root, "image1.fake");

        assert (images.size() == 2);
    }


    public void testGetImagesLikeInDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesLike(root, ".fake");

        assert (images.size() == 3);
    }


    public void testGetImagesTaggedInDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesTagged(root, 1L);

        assert (images.size() == 2);
    }


    public void testGetImagesTaggedInDataset2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        TagAnnotationContainer tag     = root.getTag(2L);
        DatasetContainer       dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesTagged(root, tag);

        assert (images.size() == 1);
    }


    public void testGetImagesKeyInDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesKey(root, "testKey1");

        assert (images.size() == 3);
    }


    public void testGetImagesPairKeyValueInDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesPairKeyValue(root, "testKey1", "testValue1");

        assert (images.size() == 2);
    }


    public void testGetImagesFromDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        assertEquals(3, images.size());
    }


    public void testAddFileDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        File file = new File("./test.txt");
        file.createNewFile();

        Long id = dataset.addFile(root, file).getId().getValue();
        file.delete();

        root.deleteFile(id);
    }

}
