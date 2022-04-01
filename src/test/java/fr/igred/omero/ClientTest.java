/*
 *  Copyright (C) 2020-2021 GReD
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
import fr.igred.omero.repository.PlateWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.omero.repository.ScreenWrapper;
import fr.igred.omero.repository.WellWrapper;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class ClientTest extends UserTest {


    @Test
    public void testProjectBasic() throws Exception {
        ProjectWrapper project = client.getProject(1L);
        assertEquals(1L, project.getId());
        assertEquals("TestProject", project.getName());
        assertEquals("description", project.getDescription());
        assertEquals(2L, project.getOwner().getId());
        assertEquals(3L, project.getGroupId().longValue());
    }


    @Test
    public void testGetSingleProject() throws Exception {
        String name = client.getProject(1L).getName();
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
    public void testCreateAndDeleteProject() throws Exception {
        ProjectWrapper project = new ProjectWrapper(client, "Foo project", "");

        long newId = project.getId();
        assertEquals("Foo project", client.getProject(newId).getName());

        client.delete(client.getProject(newId));
        try {
            client.getProject(newId);
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
        assertEquals(56, images.size());
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


    @Test
    public void testSwitchGroupAndImport() throws Exception {
        String path = "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=256&sizeY=256.fake";

        File f = new File(path);
        if (!f.createNewFile())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be created.");

        client.switchGroup(4L);

        DatasetWrapper dataset = new DatasetWrapper("test", "");
        dataset.saveAndUpdate(client);

        List<Long> ids = dataset.importImage(client, f.getAbsolutePath());

        if (!f.delete())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be deleted.");

        assertEquals(1, ids.size());

        client.delete(client.getImage(ids.get(0)));
        client.delete(dataset);
    }


    @Test
    public void testGetAllScreens() throws Exception {
        Collection<ScreenWrapper> screens = client.getScreens();
        assertEquals(2, screens.size());
    }


    @Test
    public void testGetSingleScreen() throws Exception {
        String name = client.getScreen(1L).getName();
        assertEquals("TestScreen", name);
    }


    @Test
    public void testGetAllPlates() throws Exception {
        Collection<PlateWrapper> screens = client.getPlates();
        assertEquals(3, screens.size());
    }


    @Test
    public void testGetSinglePlate() throws Exception {
        String name = client.getPlate(1L).getName();
        assertEquals("Plate Name 0", name);
    }


    @Test
    public void testGetAllWells() throws Exception {
        Collection<WellWrapper> screens = client.getWells();
        assertEquals(17, screens.size());
    }


    @Test
    public void testGetSingleWell() throws Exception {
        String plateName = client.getWell(1L).getPlate().getName();
        assertEquals("Plate Name 0", plateName);
    }

}
