package fr.igred.omero;


import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ProjectContainer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class ProjectTest extends BasicTest {


    @Test
    public void testGetDatasetFromProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        List<DatasetContainer> datasets = project.getDatasets();

        assertEquals(2, datasets.size());
    }


    @Test
    public void testGetDatasetFromProject2() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        List<DatasetContainer> datasets = project.getDatasets("TestDataset");

        assertEquals(1, datasets.size());
    }


    @Test
    public void testAddTagToProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        TagAnnotationContainer tag = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");

        project.addTag(client, tag);

        List<TagAnnotationContainer> tags = project.getTags(client);

        assertEquals(1, tags.size());

        client.deleteTag(tag);

        tags = project.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagToProject2() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        project.addTag(client, "test", "test");

        List<TagAnnotationContainer> tags = client.getTags("test");
        assertEquals(1, tags.size());

        client.deleteTag(tags.get(0).getId());

        tags = client.getTags("test");
        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagIdToProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        TagAnnotationContainer tag = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");

        project.addTag(client, tag.getId());

        List<TagAnnotationContainer> tags = project.getTags(client);

        assertEquals(1, tags.size());

        client.deleteTag(tag);

        tags = project.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagsToProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");

        project.addTags(client, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationContainer> tags = project.getTags(client);

        assertEquals(4, tags.size());

        client.deleteTag(tag1);
        client.deleteTag(tag2);
        client.deleteTag(tag3);
        client.deleteTag(tag4);

        tags = project.getTags(client);

        assertEquals(0, tags.size());
    }


    @Test
    public void testAddTagsToProject2() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(client, "Project tag", "tag attached to a project");

        project.addTags(client, tag1, tag2, tag3, tag4);

        List<TagAnnotationContainer> tags = project.getTags(client);

        assertEquals(4, tags.size());

        client.deleteTag(tag1);
        client.deleteTag(tag2);
        client.deleteTag(tag3);
        client.deleteTag(tag4);

        tags = project.getTags(client);
        client.disconnect();

        assertEquals(0, tags.size());
    }


    @Test
    public void testGetImagesInProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        List<ImageContainer> images = project.getImages(client);
        client.disconnect();

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesByNameInProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        List<ImageContainer> images = project.getImages(client, "image1.fake");
        client.disconnect();

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesLikeInProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        List<ImageContainer> images = project.getImagesLike(client, ".fake");
        client.disconnect();

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesTaggedInProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        List<ImageContainer> images = project.getImagesTagged(client, 1L);
        client.disconnect();

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesTaggedInProject2() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        TagAnnotationContainer tag     = client.getTag(2L);
        ProjectContainer       project = client.getProject(2L);

        List<ImageContainer> images = project.getImagesTagged(client, tag);
        client.disconnect();

        assertEquals(1, images.size());
    }


    @Test
    public void testGetImagesKeyInProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        List<ImageContainer> images = project.getImagesKey(client, "testKey1");
        client.disconnect();

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesPairKeyValueInProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);

        ProjectContainer project = client.getProject(2L);

        List<ImageContainer> images = project.getImagesPairKeyValue(client, "testKey1", "testValue1");
        client.disconnect();

        assertEquals(2, images.size());
    }

}