/*
 *  Copyright (C) 2020-2022 GReD
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


public class ClientTest extends UserTest {


    @Test
    public void testProjectBasic() throws Exception {
        ProjectWrapper project = client.getProject(PROJECT1.id);
        assertEquals(PROJECT1.id, project.getId());
        assertEquals(PROJECT1.name, project.getName());
        assertEquals(PROJECT1.description, project.getDescription());
        assertEquals(USER1.id, project.getOwner().getId());
        assertEquals(GROUP1.id, project.getGroupId().longValue());
    }


    @Test
    public void testGetSingleProject() throws Exception {
        String name = client.getProject(PROJECT1.id).getName();
        assertEquals(PROJECT1.name, name);
    }


    @Test
    public void testGetAllProjects() throws Exception {
        Collection<ProjectWrapper> projects = client.getProjects();
        assertEquals(2, projects.size());
    }


    @Test
    public void testGetProjectByName() throws Exception {
        Collection<ProjectWrapper> projects = client.getProjects(PROJECT1.name);

        int differences = 0;
        for (ProjectWrapper project : projects) {
            if (!PROJECT1.name.equals(project.getName()))
                differences++;
        }

        assertEquals(1, projects.size());
        assertEquals(0, differences);
    }


    @Test
    public void testCreateAndDeleteProject() throws Exception {
        boolean exception = false;

        String name = "Foo project";

        ProjectWrapper project = new ProjectWrapper(client, name, "");

        long newId = project.getId();
        assertEquals(name, client.getProject(newId).getName());

        client.delete(client.getProject(newId));
        try {
            client.getProject(newId);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testGetSingleDataset() throws Exception {
        assertEquals(DATASET1.name, client.getDataset(DATASET1.id).getName());
    }


    @Test
    public void testGetAllDatasets() throws Exception {
        Collection<DatasetWrapper> datasets = client.getDatasets();
        assertEquals(3, datasets.size());
    }


    @Test
    public void testGetDatasetByName() throws Exception {
        Collection<DatasetWrapper> datasets = client.getDatasets(DATASET1.name);

        int differences = 0;
        for (DatasetWrapper dataset : datasets) {
            if (!DATASET1.name.equals(dataset.getName()))
                differences++;
        }
        assertEquals(2, datasets.size());
        assertEquals(0, differences);
    }


    @Test
    public void testGetImages() throws Exception {
        final int nImages = 56;

        List<ImageWrapper> images = client.getImages();
        assertEquals(nImages, images.size());
    }


    @Test
    public void testGetImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        assertEquals(IMAGE1.name, image.getName());
    }


    @Test
    public void testGetImagesName() throws Exception {
        List<ImageWrapper> images = client.getImages(IMAGE1.name);
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesLike() throws Exception {
        List<ImageWrapper> images = client.getImagesLike("image1");
        assertEquals(3, images.size());
    }


    @Test
    public void testGetImagesTagged() throws Exception {
        List<ImageWrapper> images = client.getImagesTagged(TAG1.id);
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
    public void testGetImagesFromNames() throws Exception {
        List<ImageWrapper> images = client.getImages(PROJECT1.name, DATASET1.name, IMAGE1.name);
        assertEquals(2, images.size());
    }


    @Test
    public void testGetImagesCond() throws Exception {
        String key = "testKey2";

        /* Load the image with the key */
        List<ImageWrapper> images = client.getImagesKey(key);

        Collection<ImageWrapper> imagesCond = new ArrayList<>(1);

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
        final long newGroupId = 4L;
        client.switchGroup(newGroupId);
        long actualGroupId = client.getCurrentGroupId();
        assertEquals(newGroupId, actualGroupId);
    }


    @Test
    public void testSwitchGroupAndImport() throws Exception {
        final long newGroupId = 4L;

        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=256&sizeY=256.fake";

        File f = createFile(filename);

        client.switchGroup(newGroupId);

        DatasetWrapper dataset = new DatasetWrapper("test", "");
        dataset.saveAndUpdate(client);

        List<Long> ids = dataset.importImage(client, f.getAbsolutePath());

        removeFile(f);

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
        String name = client.getScreen(SCREEN1.id).getName();
        assertEquals(SCREEN1.name, name);
    }


    @Test
    public void testGetAllPlates() throws Exception {
        Collection<PlateWrapper> screens = client.getPlates();
        assertEquals(3, screens.size());
    }


    @Test
    public void testGetSinglePlate() throws Exception {
        String name = client.getPlate(PLATE1.id).getName();
        assertEquals(PLATE1.name, name);
    }


    @Test
    public void testGetAllWells() throws Exception {
        final int nWells = 17;

        Collection<WellWrapper> screens = client.getWells();
        assertEquals(nWells, screens.size());
    }


    @Test
    public void testGetSingleWell() throws Exception {
        String plateName = client.getWell(1L).getPlate().getName();
        assertEquals(PLATE1.name, plateName);
    }

}
