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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TagSetTest extends UserTest {


    @Test
    void testCreateTagSet() throws Exception {
        TagAnnotationWrapper tagSet = new TagSetWrapper(client, "tagset", "tagset description");
        client.delete(tagSet);
        assertTrue(tagSet.isTagSet());
    }


    @Test
    void testLinkTagSet() throws Exception {
        TagSetWrapper tagSet = new TagSetWrapper(client, "tagset", "LinkTagSet");
        tagSet.link(client, client.getTag(1L), client.getTag(2L));
        int nTags = tagSet.getTags(client).size();
        client.delete(tagSet);
        assertEquals(2, nTags);
    }

}
