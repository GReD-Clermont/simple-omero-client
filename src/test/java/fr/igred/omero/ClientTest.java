package fr.igred.omero;


import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ProjectContainer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class ClientTest extends UserTest {


    @Test
    public void testProjectBasic() throws Exception {
        ProjectContainer project = client.getProject(2L);

        long id = project.getId();

        assertEquals(2L, id);
        assertEquals("TestProject", project.getName());
        assertEquals("description", project.getDescription());
    }


    @Test
    public void testGetSingleProject() throws Exception {
        String name = client.getProject(2L).getName();

        assertEquals("TestProject", name);
    }


    @Test
    public void testGetAllProjects() throws Exception {
        Collection<ProjectContainer> projects = client.getProjects();

        assertEquals(2, projects.size());
    }


    @Test
    public void testGetProjectByName() throws Exception {
        Collection<ProjectContainer> projects = client.getProjects("TestProject");

        int differences = 0;
        for (ProjectContainer project : projects) {
            if (!project.getName().equals("TestProject"))
                differences++;
        }

        assertEquals(2, projects.size());
        assertEquals(0, differences);
    }


    @Test
    public void testDeleteProject() throws Exception {
        client.deleteProject(client.getProject(1L));
        try {
            client.getProject(1L);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void testGetSingleDataset() throws Exception {
        assertEquals("TestDataset", client.getDataset(1L).getName());
    }


    @Test
    public void testGetAllDatasets() throws Exception {
        Collection<DatasetContainer> datasets = client.getDatasets();
        assertEquals(3, datasets.size());
    }


    @Test
    public void testGetDatasetByName() throws Exception {
        Collection<DatasetContainer> datasets = client.getDatasets("TestDataset");

        int differences = 0;
        for (DatasetContainer dataset : datasets) {
            if (!dataset.getName().equals("TestDataset"))
                differences++;
        }
        assertEquals(2, datasets.size());
        assertEquals(0, differences);
    }


    @Test
    public void testGetImages() throws Exception {
        List<ImageContainer> images = client.getImages();
        assertEquals(4, images.size());
    }


    @Test
    public void testGetImage() throws Exception {
        ImageContainer image = client.getImage(1L);
        assertEquals("image1.fake", image.getName());
    }


    @Test
    public void testGetImagesName() throws Exception {
        List<ImageContainer> images = client.getImages("image1.fake");
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesLike() throws Exception {
        List<ImageContainer> images = client.getImagesLike("image1");
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesTagged() throws Exception {
        List<ImageContainer> images = client.getImagesTagged(1L);
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesKey() throws Exception {
        List<ImageContainer> images = client.getImagesKey("testKey1");
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesKeyValue() throws Exception {
        List<ImageContainer> images = client.getImagesPairKeyValue("testKey1", "testValue1");
        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesCond() throws Exception {
        String key = "testKey2";

        /* Load the image with the key */
        List<ImageContainer> images = client.getImagesKey(key);

        List<ImageContainer> imagesCond = new ArrayList<>();

        for (ImageContainer image : images) {
            /* Get the value for the key */
            String value = image.getValue(client, key);

            /* Condition */
            if (value.compareTo("25") > 0) {
                imagesCond.add(image);
            }
        }

        assertEquals(1, imagesCond.size());
    }

}
