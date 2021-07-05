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

package fr.igred.omero.annotations;


import fr.igred.omero.UserTest;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TagTest extends UserTest {


    @Test
    public void testGetTagInfo() throws Exception {
        TagAnnotationWrapper tag = client.getTag(1L);
        assertEquals(1L, tag.getId());
        assertEquals("tag1", tag.getName());
        assertEquals("description", tag.getDescription());
    }


    @Test
    public void testGetTags() throws Exception {
        List<TagAnnotationWrapper> tags = client.getTags();
        assertEquals(3, tags.size());
    }


    @Test
    public void testGetTagsSorted() throws Exception {
        List<TagAnnotationWrapper> tags = client.getTags();
        for (int i = 1; i < tags.size(); i++) {
            assertTrue(tags.get(i - 1).getId() <= tags.get(i).getId());
        }
    }


    @Test
    public void testGetProjects() throws Exception {
        TagAnnotationWrapper tag = client.getTag(1L);
        List<ProjectWrapper> projects = tag.getProjects(client);
        assertEquals(1, projects.size());
        assertEquals(2L, projects.get(0).getId());
    }


    @Test
    public void testGetDatasets() throws Exception {
        TagAnnotationWrapper tag = client.getTag(1L);
        List<DatasetWrapper> datasets = tag.getDatasets(client);
        assertEquals(1, datasets.size());
        assertEquals(3L, datasets.get(0).getId());
    }


    @Test
    public void testGetImages() throws Exception {
        TagAnnotationWrapper tag      = client.getTag(1L);
        List<ImageWrapper>   images = tag.getImages(client);
        assertEquals(3, images.size());
        assertEquals(1L, images.get(0).getId());
        assertEquals(2L, images.get(1).getId());
        assertEquals(4L, images.get(2).getId());
    }


    @Test
    public void testSetName() throws Exception {
        TagAnnotationWrapper tag = client.getTag(1L);

        String name  = tag.getName();
        String name2 = "NewName";
        tag.setName(name2);
        tag.saveAndUpdate(client);
        assertEquals(name2, client.getTag(1L).getName());

        tag.setName(name);
        tag.saveAndUpdate(client);
        assertEquals(name, client.getTag(1L).getName());
    }


    @Test
    public void testSetDescription() throws Exception {
        TagAnnotationWrapper tag = client.getTag(1L);

        String description  = tag.getDescription();

        String description2 = "NewName";
        tag.setDescription(description2);
        tag.saveAndUpdate(client);
        assertEquals(description2, client.getTag(1L).getDescription());

        tag.setDescription(description);
        tag.saveAndUpdate(client);
        assertEquals(description, client.getTag(1L).getDescription());
    }

}