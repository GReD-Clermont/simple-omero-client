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
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TagTest extends UserTest {


    @Test
    public void testGetTagInfo() throws Exception {
        TagAnnotationContainer tag = client.getTag(1L);
        assertEquals(1L, tag.getId().longValue());
        assertEquals("tag1", tag.getName());
        assertEquals("description", tag.getDescription());
    }


    @Test
    public void testGetTags() throws Exception {
        List<TagAnnotationContainer> tags = client.getTags();
        assertEquals(3, tags.size());
    }


    @Test
    public void testGetTagsSorted() throws Exception {
        List<TagAnnotationContainer> tags = client.getTags();
        for (int i = 1; i < tags.size(); i++) {
            assertTrue(tags.get(i - 1).getId() <= tags.get(i).getId());
        }
    }

}