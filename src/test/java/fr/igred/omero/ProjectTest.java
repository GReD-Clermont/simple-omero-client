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

        assert (datasets.size() == 2);
    }


    public void testGetDatasetFromProject2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<DatasetContainer> datasets = project.getDatasets("TestDataset");

        assert (datasets.size() == 1);
    }


    public void testAddTagToProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");

        project.addTag(root, tag);

        List<TagAnnotationContainer> tags = project.getTags(root);

        assert (tags.size() == 1);

        root.deleteTag(tag);

        tags = project.getTags(root);

        assert (tags.size() == 0);
    }


    public void testAddTagToProject2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        project.addTag(root, "test", "test");

        List<TagAnnotationContainer> tags = root.getTags("test");
        assert (tags.size() == 1);

        root.deleteTag(tags.get(0).getId());

        tags = root.getTags("test");
        assert (tags.size() == 0);
    }


    public void testAddTagIdToProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");

        project.addTag(root, tag.getId());

        List<TagAnnotationContainer> tags = project.getTags(root);

        assert (tags.size() == 1);

        root.deleteTag(tag);

        tags = project.getTags(root);

        assert (tags.size() == 0);
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

        assert (tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = project.getTags(root);

        assert (tags.size() == 0);
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

        assert (tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = project.getTags(root);

        assert (tags.size() == 0);
    }


    public void testGetImagesInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImages(root);

        assert (images.size() == 3);
    }


    public void testGetImagesByNameInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImages(root, "image1.fake");

        assert (images.size() == 2);
    }


    public void testGetImagesLikeInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesLike(root, ".fake");

        assert (images.size() == 3);
    }


    public void testGetImagesTaggedInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesTagged(root, 1L);

        assert (images.size() == 2);
    }


    public void testGetImagesTaggedInProject2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        TagAnnotationContainer tag     = root.getTag(2L);
        ProjectContainer       project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesTagged(root, tag);

        assert (images.size() == 1);
    }


    public void testGetImagesKeyInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesKey(root, "testKey1");

        assert (images.size() == 3);
    }


    public void testGetImagesPairKeyValueInProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesPairKeyValue(root, "testKey1", "testValue1");

        assert (images.size() == 2);
    }

}