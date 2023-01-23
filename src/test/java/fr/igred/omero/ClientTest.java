/*
 *  Copyright (C) 2020-2023 GReD
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


import fr.igred.omero.repository.Dataset;
import fr.igred.omero.repository.Image;
import fr.igred.omero.repository.Plate;
import fr.igred.omero.repository.Project;
import fr.igred.omero.repository.Screen;
import fr.igred.omero.repository.Well;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ClientTest extends UserTest {


    @Test
    void testProjectBasic() throws Exception {
        Project project = client.getProject(PROJECT1.id);
        assertEquals(PROJECT1.id, project.getId());
        assertEquals(PROJECT1.name, project.getName());
        assertEquals(PROJECT1.description, project.getDescription());
        assertEquals(USER1.id, project.getOwner().getId());
        assertEquals(GROUP1.id, project.getGroupId().longValue());
    }


    @Test
    void testGetSingleProject() throws Exception {
        String name = client.getProject(PROJECT1.id).getName();
        assertEquals(PROJECT1.name, name);
    }


    @Test
    void testGetAllProjects() throws Exception {
        Collection<Project> projects = client.getProjects();
        assertEquals(2, projects.size());
    }


    @Test
    void testGetProjectByName() throws Exception {
        Collection<Project> projects = client.getProjects(PROJECT1.name);

        int differences = 0;
        for (Project project : projects) {
            if (!PROJECT1.name.equals(project.getName()))
                differences++;
        }

        assertEquals(1, projects.size());
        assertEquals(0, differences);
    }


    @Test
    void testCreateAndDeleteProject() throws Exception {
        String name = "Foo project";

        Project project = new Project(client, name, "");

        long newId = project.getId();
        assertEquals(name, client.getProject(newId).getName());
        client.delete(client.getProject(newId));
        assertThrows(NoSuchElementException.class, () -> client.getProject(newId));
    }


    @Test
    void testGetSingleDataset() throws Exception {
        assertEquals(DATASET1.name, client.getDataset(DATASET1.id).getName());
    }


    @Test
    void testGetAllDatasets() throws Exception {
        Collection<Dataset> datasets = client.getDatasets();
        assertEquals(3, datasets.size());
    }


    @Test
    void testGetDatasetByName() throws Exception {
        Collection<Dataset> datasets = client.getDatasets(DATASET1.name);

        int differences = 0;
        for (Dataset dataset : datasets) {
            if (!DATASET1.name.equals(dataset.getName()))
                differences++;
        }
        assertEquals(2, datasets.size());
        assertEquals(0, differences);
    }


    @Test
    void testGetImages() throws Exception {
        final int nImages = 56;

        List<Image> images = client.getImages();
        assertEquals(nImages, images.size());
    }


    @Test
    void testGetImage() throws Exception {
        Image image = client.getImage(IMAGE1.id);
        assertEquals(IMAGE1.name, image.getName());
    }


    @Test
    void testGetImagesName() throws Exception {
        List<Image> images = client.getImages(IMAGE1.name);
        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesLike() throws Exception {
        List<Image> images = client.getImagesLike("image1");
        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesTagged() throws Exception {
        List<Image> images = client.getImagesTagged(TAG1.id);
        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesKey() throws Exception {
        List<Image> images = client.getImagesKey("testKey1");
        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesKeyValue() throws Exception {
        List<Image> images = client.getImagesPairKeyValue("testKey1", "testValue1");
        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesFromNames() throws Exception {
        List<Image> images = client.getImages(PROJECT1.name, DATASET1.name, IMAGE1.name);
        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesCond() throws Exception {
        String key = "testKey2";

        /* Load the image with the key */
        List<Image> images = client.getImagesKey(key);

        Collection<Image> imagesCond = new ArrayList<>(1);

        for (Image image : images) {
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
    void testSwitchGroup() {
        final long newGroupId = 4L;
        client.switchGroup(newGroupId);
        long actualGroupId = client.getCurrentGroupId();
        assertEquals(newGroupId, actualGroupId);
    }


    @Test
    void testSwitchGroupAndImport() throws Exception {
        final long newGroupId = 4L;

        String filename = "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=256&sizeY=256.fake";

        File file = createFile(filename);

        client.switchGroup(newGroupId);

        Dataset dataset = new Dataset("test", "");
        dataset.saveAndUpdate(client);

        List<Long> ids = dataset.importImage(client, file.getAbsolutePath());

        removeFile(file);

        assertEquals(1, ids.size());

        client.delete(client.getImage(ids.get(0)));
        client.delete(dataset);
    }


    @Test
    void testGetAllScreens() throws Exception {
        Collection<Screen> screens = client.getScreens();
        assertEquals(2, screens.size());
    }


    @Test
    void testGetSingleScreen() throws Exception {
        String name = client.getScreen(SCREEN1.id).getName();
        assertEquals(SCREEN1.name, name);
    }


    @Test
    void testGetAllPlates() throws Exception {
        Collection<Plate> screens = client.getPlates();
        assertEquals(3, screens.size());
    }


    @Test
    void testGetSinglePlate() throws Exception {
        String name = client.getPlate(PLATE1.id).getName();
        assertEquals(PLATE1.name, name);
    }


    @Test
    void testGetAllWells() throws Exception {
        final int nWells = 17;

        Collection<Well> screens = client.getWells();
        assertEquals(nWells, screens.size());
    }


    @Test
    void testGetSingleWell() throws Exception {
        String plateName = client.getWell(1L).getPlate().getName();
        assertEquals(PLATE1.name, plateName);
    }

}
