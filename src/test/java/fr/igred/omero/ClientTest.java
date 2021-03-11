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


import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ProjectContainer;
import loci.common.DebugTools;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;


public class ClientTest extends BasicTest {


    @Test
    public void testConnection() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        assertEquals(0L, root.getId().longValue());

        root.disconnect();
    }


    @Test
    public void testConnection2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "testUser", "password");
        assertEquals(3L, root.getGroupId().longValue());
    }


    @Test
    public void testConnectionErrorUsername() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("omero", 4064, "badUser", "omero", 3L);
            fail();
        } catch (ServiceException e) {
            assertTrue(true);
        }
    }


    @Test
    public void testConnectionErrorPassword() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("omero", 4064, "root", "badPassword", 3L);
            fail();
        } catch (ServiceException e) {
            assertTrue(true);
        }
    }


    @Test
    public void testConnectionErrorHost() {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("127.0.0.1", 4064, "root", "omero", 3L);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void testConnectionErrorPort() {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("omero", 5000, "root", "omero", 3L);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }


    @Test
    public void testConnectionErrorGroupNotExist() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 200L);

        assertEquals(3L, root.getGroupId().longValue());
    }


    @Test
    public void testConnectionErrorNotInGroup() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "testUser", "password", 54L);
        assertEquals(3L, root.getGroupId().longValue());
    }


    @Test
    public void testProjectBasic() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        assertEquals(2L, project.getId().longValue());
        assertEquals("TestProject", project.getName());
        assertEquals("description", project.getDescription());
    }


    @Test
    public void testGetSingleProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        assertEquals("TestProject", root.getProject(2L).getName());
    }


    @Test
    public void testGetSingleProjectError() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("omero", 4064, "root", "omero");
            root.getProject(333L);
            fail();
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }
    }


    @Test
    public void testGetAllProjects() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<ProjectContainer> projects = root.getProjects();

        assertEquals(2, projects.size());
    }


    @Test
    public void testGetProjectByName() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<ProjectContainer> projects = root.getProjects("TestProject");

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
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 0L);

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
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        assertEquals("TestDataset", root.getDataset(1L).getName());
    }


    @Test
    public void testGetAllDatasets() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<DatasetContainer> datasets = root.getDatasets();

        assertEquals(3, datasets.size());
    }


    @Test
    public void testGetDatasetByName() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<DatasetContainer> datasets = root.getDatasets("TestDataset");

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
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImages();

        assertEquals(4, images.size());
    }


    @Test
    public void testGetImage() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        assertEquals("image1.fake", image.getName());
    }


    @Test
    public void testGetImageError() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        try {
            root.getImage(200L);
            fail();
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }
    }


    @Test
    public void testGetImagesName() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImages("image1.fake");

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesLike() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesLike(".fake");

        assertEquals(4, images.size());
    }


    @Test
    public void testGetImagesTagged() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesTagged(1L);

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesKey() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesKey("testKey1");

        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesKeyValue() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesPairKeyValue("testKey1", "testValue1");

        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesCond() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        String key = "testKey2";

        /* Load the image with the key */
        List<ImageContainer> images = root.getImagesKey(key);

        List<ImageContainer> imagesCond = new ArrayList<>();

        for (ImageContainer image : images) {
            /* Get the value for the key */
            String value = image.getValue(root, key);

            /* Condition */
            if (value.compareTo("25") > 0) {
                imagesCond.add(image);
            }
        }

        assertEquals(1, imagesCond.size());
    }


    @Test
    public void testSudoTag() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Client test = root.sudoGetUser("testUser");

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

        assertEquals(images.size(), tagged.size());
        assertEquals(0, differences);
    }

}
