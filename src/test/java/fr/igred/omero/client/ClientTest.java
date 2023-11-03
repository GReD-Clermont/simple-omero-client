/*
 *  Copyright (C) 2020-2024 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero.client;


import fr.igred.omero.UserTest;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import fr.igred.omero.containers.DatasetWrapper;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.screen.PlateWrapper;
import fr.igred.omero.containers.ProjectWrapper;
import fr.igred.omero.screen.ScreenWrapper;
import fr.igred.omero.screen.WellWrapper;
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
        ProjectWrapper project = client.getProject(PROJECT1.id);
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
        Collection<ProjectWrapper> projects = client.getProjects();
        assertEquals(2, projects.size());
    }


    @Test
    void testGetProjectByName() throws Exception {
        Collection<ProjectWrapper> projects = client.getProjects(PROJECT1.name);

        int differences = 0;
        for (ProjectWrapper project : projects) {
            if (!PROJECT1.name.equals(project.getName())) {
                differences++;
            }
        }

        assertEquals(1, projects.size());
        assertEquals(0, differences);
    }


    @Test
    void testCreateAndDeleteProject() throws Exception {
        String name = "Foo project";

        ProjectWrapper project = new ProjectWrapper(client, name, "");

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
        Collection<DatasetWrapper> datasets = client.getDatasets();
        assertEquals(4, datasets.size());
    }


    @Test
    void testGetDatasetByName() throws Exception {
        Collection<DatasetWrapper> datasets = client.getDatasets(DATASET1.name);

        int differences = 0;
        for (DatasetWrapper dataset : datasets) {
            if (!DATASET1.name.equals(dataset.getName())) {
                differences++;
            }
        }
        assertEquals(2, datasets.size());
        assertEquals(0, differences);
    }


    @Test
    void testGetImages() throws Exception {
        final int nImages = 56;

        List<ImageWrapper> images = client.getImages();
        assertEquals(nImages, images.size());
    }


    @Test
    void testGetImage() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        assertEquals(IMAGE1.name, image.getName());
    }


    @Test
    void testGetImagesName() throws Exception {
        List<ImageWrapper> images = client.getImages(IMAGE1.name);
        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesLike() throws Exception {
        List<ImageWrapper> images = client.getImagesLike("image1");
        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesWithKey() throws Exception {
        List<ImageWrapper> images = client.getImagesWithKey("testKey1");
        assertEquals(3, images.size());
    }


    @Test
    void testGetImagesWithKeyValuePair() throws Exception {
        List<ImageWrapper> images = client.getImagesWithKeyValuePair("testKey1", "testValue1");
        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesFromNames() throws Exception {
        List<ImageWrapper> images = client.getImages(PROJECT1.name, DATASET1.name, IMAGE1.name);
        assertEquals(2, images.size());
    }


    @Test
    void testGetImagesCond() throws Exception {
        String key = "testKey2";

        /* Load the image with the key */
        List<ImageWrapper> images = client.getImagesWithKey(key);

        Collection<ImageWrapper> imagesCond = new ArrayList<>(1);

        for (ImageWrapper image : images) {
            /* Get the value for the key */
            List<String> values = image.getValues(client, key);

            /* Condition */
            if (values.get(0).compareTo("25") > 0) {
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

        DatasetWrapper dataset = new DatasetWrapper("test", "");
        dataset.saveAndUpdate(client);

        List<Long> ids = dataset.importImage(client, file.getAbsolutePath());

        removeFile(file);

        assertEquals(1, ids.size());

        client.delete(client.getImage(ids.get(0)));
        client.delete(dataset);
    }


    @Test
    void testGetAllScreens() throws Exception {
        Collection<ScreenWrapper> screens = client.getScreens();
        assertEquals(2, screens.size());
    }


    @Test
    void testGetSingleScreen() throws Exception {
        String name = client.getScreen(SCREEN1.id).getName();
        assertEquals(SCREEN1.name, name);
    }


    @Test
    void testGetAllPlates() throws Exception {
        Collection<PlateWrapper> screens = client.getPlates();
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

        Collection<WellWrapper> screens = client.getWells();
        assertEquals(nWells, screens.size());
    }


    @Test
    void testGetSingleWell() throws Exception {
        String plateName = client.getWell(1L).getPlate().getName();
        assertEquals(PLATE1.name, plateName);
    }


    @Test
    void testGetAllProjectsForUser1() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");

        Collection<ProjectWrapper> projects = client.getProjects(user);
        assertEquals(2, projects.size());
    }


    @Test
    void testGetAllProjectsForUser2() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser2");

        Collection<ProjectWrapper> projects = client.getProjects(user);
        assertEquals(0, projects.size());
    }


    @Test
    void testGetAllDatasetsForUser1() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");

        Collection<DatasetWrapper> datasets = client.getDatasets(user);
        assertEquals(4, datasets.size());
    }


    @Test
    void testGetAllDatasetsForUser2() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser2");

        Collection<DatasetWrapper> datasets = client.getDatasets(user);
        assertEquals(0, datasets.size());
    }


    @Test
    void testGetAllScreensForUser1() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");

        Collection<ScreenWrapper> screens = client.getScreens(user);
        assertEquals(2, screens.size());
    }


    @Test
    void testGetAllScreensForUser2() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser2");

        Collection<ScreenWrapper> screens = client.getScreens(user);
        assertEquals(0, screens.size());
    }


    @Test
    void testGetAllPlatesForUser1() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");

        Collection<PlateWrapper> plates = client.getPlates(user);
        assertEquals(3, plates.size());
    }


    @Test
    void testGetAllPlatesForUser2() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser2");

        Collection<PlateWrapper> plates = client.getPlates(user);
        assertEquals(0, plates.size());
    }


    @Test
    void testGetAllWellsForUser1() throws Exception {
        ExperimenterWrapper user   = client.getUser("testUser");
        final int           nWells = 17;

        Collection<WellWrapper> wells = client.getWells(user);
        assertEquals(nWells, wells.size());
    }


    @Test
    void testGetAllWellsForUser2() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser2");

        Collection<WellWrapper> wells = client.getWells(user);
        assertEquals(0, wells.size());
    }


    @Test
    void testGetAllGroups() throws Exception {
        List<GroupWrapper> groups = client.getGroups();
        assertEquals(7, groups.size());
    }

}
