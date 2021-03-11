package fr.igred.omero;


import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ProjectContainer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import loci.common.DebugTools;

import java.util.List;


public class ProjectTest extends TestCase {

    /**
     * Create the test case for Client
     *
     * @param testName Name of the test case.
     */
    public ProjectTest(String testName) {
        super(testName);
    }


    /**
     * @return the suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(ProjectTest.class);
    }


    public void testGetDatasetFromProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<DatasetContainer> datasets = project.getDatasets();

        assertEquals(2, datasets.size());
    }


    public void testGetDatasetFromProject2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<DatasetContainer> datasets = project.getDatasets("TestDataset");

        assertEquals(1, datasets.size());
    }


    public void testAddTagToProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");

        project.addTag(root, tag);

        List<TagAnnotationContainer> tags = project.getTags(root);

        assertEquals(1, tags.size());

        root.deleteTag(tag);

        tags = project.getTags(root);

        assertEquals(0, tags.size());
    }


    public void testAddTagToProject2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        project.addTag(root, "test", "test");

        List<TagAnnotationContainer> tags = root.getTags("test");
        assertEquals(1, tags.size());

        root.deleteTag(tags.get(0).getId());

        tags = root.getTags("test");
        assertEquals(0, tags.size());
    }


    public void testAddTagIdToProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");

        project.addTag(root, tag.getId());

        List<TagAnnotationContainer> tags = project.getTags(root);

        assertEquals(1, tags.size());

        root.deleteTag(tag);

        tags = project.getTags(root);

        assertEquals(0, tags.size());
    }


    public void testAddTagsToProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");

        project.addTags(root, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationContainer> tags = project.getTags(root);

        assertEquals(4, tags.size());

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = project.getTags(root);

        assertEquals(0, tags.size());
    }


    public void testAddTagsToProject2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");

        project.addTags(root, tag1, tag2, tag3, tag4);

        List<TagAnnotationContainer> tags = project.getTags(root);

        assertEquals(4, tags.size());

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = project.getTags(root);

        assertEquals(0, tags.size());
    }


    public void testGetImagesInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImages(root);

        assertEquals(3, images.size());
    }


    public void testGetImagesByNameInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImages(root, "image1.fake");

        assertEquals(2, images.size());
    }


    public void testGetImagesLikeInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesLike(root, ".fake");

        assertEquals(3, images.size());
    }


    public void testGetImagesTaggedInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesTagged(root, 1L);

        assertEquals(2, images.size());
    }


    public void testGetImagesTaggedInProject2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        TagAnnotationContainer tag     = root.getTag(2L);
        ProjectContainer       project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesTagged(root, tag);

        assertEquals(1, images.size());
    }


    public void testGetImagesKeyInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesKey(root, "testKey1");

        assertEquals(3, images.size());
    }


    public void testGetImagesPairKeyValueInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesPairKeyValue(root, "testKey1", "testValue1");

        assertEquals(2, images.size());
    }

}