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


import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;


public class ClientTest extends UserTest {


    @Test
    public void testProjectBasic() throws Exception {
        ProjectWrapper project = client.getProject(2L);
        assertEquals(2L, project.getId());
        assertEquals("TestProject", project.getName());
        assertEquals("description", project.getDescription());
        assertEquals(2L, project.getOwner().getId());
        assertEquals(3L, project.getGroupId().longValue());
    }


    @Test
    public void testGetSingleProject() throws Exception {
        String name = client.getProject(2L).getName();
        assertEquals("TestProject", name);
    }


    @Test
    public void testGetAllProjects() throws Exception {
        Collection<ProjectWrapper> projects = client.getProjects();
        assertEquals(2, projects.size());
    }


    @Test
    public void testGetProjectByName() throws Exception {
        Collection<ProjectWrapper> projects = client.getProjects("TestProject");

        int differences = 0;
        for (ProjectWrapper project : projects) {
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
        Collection<DatasetWrapper> datasets = client.getDatasets();
        assertEquals(3, datasets.size());
    }


    @Test
    public void testGetDatasetByName() throws Exception {
        Collection<DatasetWrapper> datasets = client.getDatasets("TestDataset");

        int differences = 0;
        for (DatasetWrapper dataset : datasets) {
            if (!dataset.getName().equals("TestDataset"))
                differences++;
        }
        assertEquals(2, datasets.size());
        assertEquals(0, differences);
    }


    @Test
    public void testGetImages() throws Exception {
        List<ImageWrapper> images = client.getImages();
        assertEquals(4, images.size());
    }


    @Test
    public void testGetImage() throws Exception {
        ImageWrapper image = client.getImage(1L);
        assertEquals("image1.fake", image.getName());
    }


    @Test
    public void testGetImagesName() throws Exception {
        List<ImageWrapper> images = client.getImages("image1.fake");
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesLike() throws Exception {
        List<ImageWrapper> images = client.getImagesLike("image1");
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesTagged() throws Exception {
        List<ImageWrapper> images = client.getImagesTagged(1L);
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesKey() throws Exception {
        List<ImageWrapper> images = client.getImagesKey("testKey1");
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesKeyValue() throws Exception {
        List<ImageWrapper> images = client.getImagesPairKeyValue("testKey1", "testValue1");
        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesCond() throws Exception {
        String key = "testKey2";

        /* Load the image with the key */
        List<ImageWrapper> images = client.getImagesKey(key);

        List<ImageWrapper> imagesCond = new ArrayList<>();

        for (ImageWrapper image : images) {
            /* Get the value for the key */
            String value = image.getValue(client, key);

            /* Condition */
            if (value.compareTo("25") > 0) {
                imagesCond.add(image);
            }
        }

        assertEquals(1, imagesCond.size());
    }


    @Test
    public void testSwitchGroup() {
        client.switchGroup(4L);
        long groupId = client.getCurrentGroupId();
        assertEquals(4L, groupId);
    }

}
