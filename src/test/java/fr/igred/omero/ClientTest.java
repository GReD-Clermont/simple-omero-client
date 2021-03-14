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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;


public class ClientTest extends BasicTest {


    @Test
    public void testRootConnection() throws Exception {
        Client testRoot = new Client();
        testRoot.connect("omero", 4064, "root", "omero", 3L);
        long id      = testRoot.getId();
        long groupId = testRoot.getGroupId();
        assertEquals(0L, id);
        assertEquals(3L, groupId);
    }


    @Test
    public void testUserConnection() throws Exception {
        Client testUser = new Client();
        testUser.connect("omero", 4064, "testUser", "password");
        long id      = testUser.getId();
        long groupId = testUser.getGroupId();
        assertEquals(2L, id);
        assertEquals(3L, groupId);
    }


    @Test
    public void testProjectBasic() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        ProjectContainer project = client.getProject(2L);

        long id = project.getId();
        client.disconnect();

        assertEquals(2L, id);
        assertEquals("TestProject", project.getName());
        assertEquals("description", project.getDescription());
    }


    @Test
    public void testGetSingleProject() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        String name = client.getProject(2L).getName();
        client.disconnect();

        assertEquals("TestProject", name);
    }


    @Test
    public void testGetAllProjects() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        Collection<ProjectContainer> projects = client.getProjects();
        client.disconnect();

        assertEquals(2, projects.size());
    }


    @Test
    public void testGetProjectByName() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        Collection<ProjectContainer> projects = client.getProjects("TestProject");

        int differences = 0;
        for (ProjectContainer project : projects) {
            if (!project.getName().equals("TestProject"))
                differences++;
        }
        client.disconnect();

        assertEquals(2, projects.size());
        assertEquals(0, differences);
    }


    @Test
    public void testDeleteProject() throws Exception {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 0L);
        assertEquals(0L, root.getId().longValue());

        root.deleteProject(root.getProject(1L));

        try {
            root.getProject(1L);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void testGetSingleDataset() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());
        client.disconnect();

        assertEquals("TestDataset", client.getDataset(1L).getName());
    }


    @Test
    public void testGetAllDatasets() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        Collection<DatasetContainer> datasets = client.getDatasets();
        client.disconnect();

        assertEquals(3, datasets.size());
    }


    @Test
    public void testGetDatasetByName() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        Collection<DatasetContainer> datasets = client.getDatasets("TestDataset");

        int differences = 0;
        for (DatasetContainer dataset : datasets) {
            if (!dataset.getName().equals("TestDataset"))
                differences++;
        }
        client.disconnect();

        assertEquals(2, datasets.size());
        assertEquals(0, differences);
    }


    @Test
    public void testGetImages() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        List<ImageContainer> images = client.getImages();
        client.disconnect();

        assertEquals(4, images.size());
    }


    @Test
    public void testGetImage() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        ImageContainer image = client.getImage(1L);
        client.disconnect();

        assertEquals("image1.fake", image.getName());
    }


    @Test
    public void testGetImagesName() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        List<ImageContainer> images = client.getImages("image1.fake");
        client.disconnect();

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesLike() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        List<ImageContainer> images = client.getImagesLike("image1");
        client.disconnect();

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesTagged() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        List<ImageContainer> images = client.getImagesTagged(1L);
        client.disconnect();

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesKey() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        List<ImageContainer> images = client.getImagesKey("testKey1");
        client.disconnect();

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesKeyValue() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        List<ImageContainer> images = client.getImagesPairKeyValue("testKey1", "testValue1");
        client.disconnect();

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesCond() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

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
        client.disconnect();

        assertEquals(1, imagesCond.size());
    }


    @Test
    public void testSudoTag() throws Exception {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);
        assertEquals(0L, root.getId().longValue());

        Client test = root.sudoGetUser("testUser");
        assertEquals(2L, test.getId().longValue());

        TagAnnotationContainer tag = new TagAnnotationContainer(test, "Tag", "This is a tag");

        List<ImageContainer> images = test.getImages();

        for (ImageContainer image : images) {
            image.addTag(test, tag);
        }

        List<ImageContainer> tagged = test.getImagesTagged(tag);

        int differences = 0;
        for (int i = 0; i < images.size(); i++) {
            if (!images.get(i).getId().equals(tagged.get(i).getId()))
                differences++;
        }

        root.deleteTag(tag);

        assertNotEquals(0, images.size());
        assertEquals(images.size(), tagged.size());
        assertEquals(0, differences);
    }

}
