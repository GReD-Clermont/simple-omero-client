/*
 *  Copyright (C) 2020-2023 GReD
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

package fr.igred.omero.annotations;


import fr.igred.omero.UserTest;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.containers.Project;
import fr.igred.omero.core.Image;
import fr.igred.omero.screen.Plate;
import fr.igred.omero.screen.Screen;
import fr.igred.omero.screen.Well;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TagTest extends UserTest {


    @Test
    void testGetTagInfo() throws Exception {
        TagAnnotation tag = client.getTag(TAG1.id);
        assertEquals(TAG1.id, tag.getId());
        assertEquals(TAG1.name, tag.getName());
        assertEquals(TAG1.description, tag.getDescription());
    }


    @Test
    void testGetTags() throws Exception {
        List<TagAnnotation> tags = client.getTags();
        assertEquals(3, tags.size());
    }


    @Test
    void testGetTagsSorted() throws Exception {
        List<TagAnnotation> tags = client.getTags();
        for (int i = 1; i < tags.size(); i++) {
            assertTrue(tags.get(i - 1).getId() <= tags.get(i).getId());
        }
    }


    @Test
    void testGetProjects() throws Exception {
        TagAnnotation tag      = client.getTag(TAG1.id);
        List<Project> projects = tag.getProjects(client);
        assertEquals(1, projects.size());
        assertEquals(2L, projects.get(0).getId());
    }


    @Test
    void testGetDatasets() throws Exception {
        TagAnnotation tag      = client.getTag(TAG1.id);
        List<Dataset> datasets = tag.getDatasets(client);
        assertEquals(1, datasets.size());
        assertEquals(3L, datasets.get(0).getId());
    }


    @Test
    void testGetImages() throws Exception {
        TagAnnotation tag    = client.getTag(TAG1.id);
        List<Image>   images = tag.getImages(client);
        assertEquals(3, images.size());
        assertEquals(1L, images.get(0).getId());
        assertEquals(2L, images.get(1).getId());
        assertEquals(4L, images.get(2).getId());
    }


    @Test
    void testGetScreens() throws Exception {
        TagAnnotation tag     = client.getTag(TAG1.id);
        List<Screen>  screens = tag.getScreens(client);
        assertEquals(1, screens.size());
        assertEquals(1L, screens.get(0).getId());
    }


    @Test
    void testGetPlates() throws Exception {
        TagAnnotation tag    = client.getTag(TAG1.id);
        List<Plate>   plates = tag.getPlates(client);
        assertEquals(1, plates.size());
        assertEquals(1L, plates.get(0).getId());
    }


    @Test
    void testGetWells() throws Exception {
        TagAnnotation tag   = client.getTag(TAG1.id);
        List<Well>    wells = tag.getWells(client);
        assertEquals(1, wells.size());
        assertEquals(1L, wells.get(0).getId());
    }


    @Test
    void testSetName() throws Exception {
        TagAnnotation tag = client.getTag(TAG1.id);

        String name  = tag.getName();
        String name2 = "NewName";
        tag.setName(name2);
        tag.saveAndUpdate(client);
        assertEquals(name2, client.getTag(TAG1.id).getName());

        tag.setName(name);
        tag.saveAndUpdate(client);
        assertEquals(name, client.getTag(TAG1.id).getName());
    }


    @Test
    void testSetDescription() throws Exception {
        TagAnnotation tag = client.getTag(TAG1.id);

        String description = tag.getDescription();

        String description2 = "NewName";
        tag.setDescription(description2);
        tag.saveAndUpdate(client);
        assertEquals(description2, client.getTag(TAG1.id).getDescription());

        tag.setDescription(description);
        tag.saveAndUpdate(client);
        assertEquals(description, client.getTag(TAG1.id).getDescription());
    }

}