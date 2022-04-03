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

package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class ScreenTest extends UserTest {


    @Test
    public void testGetPlatesFromScreen() throws Exception {
        ScreenWrapper      screen = client.getScreen(SCREEN2.id);
        List<PlateWrapper> plates = screen.getPlates();
        assertEquals(2, plates.size());
    }


    @Test
    public void testGetPlatesFromScreen2() throws Exception {
        ScreenWrapper      screen = client.getScreen(SCREEN2.id);
        List<PlateWrapper> plates = screen.getPlates("Plate Name 1");
        assertEquals(1, plates.size());
    }


    @Test
    public void testAddTagToScreen() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN2.id);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Screen tag", "tag attached to a screen");
        screen.addTag(client, tag);
        List<TagAnnotationWrapper> tags = screen.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> checkTags = screen.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
    }


    @Test
    public void testSetName() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

        String name  = screen.getName();
        String name2 = "New name";
        screen.setName(name2);
        screen.saveAndUpdate(client);
        assertEquals(name2, client.getScreen(SCREEN1.id).getName());

        screen.setName(name);
        screen.saveAndUpdate(client);
        assertEquals(name, client.getScreen(SCREEN1.id).getName());
    }


    @Test
    public void testSetDescription() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

        String description = screen.getDescription();

        String description2 = "New description";
        screen.setDescription(description2);
        screen.saveAndUpdate(client);
        assertEquals(description2, client.getScreen(SCREEN1.id).getDescription());

        screen.setDescription(description);
        screen.saveAndUpdate(client);
        assertEquals(description, client.getScreen(SCREEN1.id).getDescription());
    }


    @Test
    public void testSetProtocolDescription() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

        String description = "Protocol Description Test";
        screen.setProtocolDescription(description);
        screen.saveAndUpdate(client);
        assertEquals(description, client.getScreen(SCREEN1.id).getProtocolDescription());
    }


    @Test
    public void testSetProtocolIdentifier() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

        String identifier = "Protocol Identifier Test";
        screen.setProtocolIdentifier(identifier);
        screen.saveAndUpdate(client);
        assertEquals(identifier, client.getScreen(SCREEN1.id).getProtocolIdentifier());
    }


    @Test
    public void testSetReagentSetDescription() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

        String description = "Reagent Description Test";
        screen.setReagentSetDescription(description);
        screen.saveAndUpdate(client);
        assertEquals(description, client.getScreen(SCREEN1.id).getReagentSetDescription());
    }


    @Test
    public void testSetReagentSetIdentifier() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

        String identifier = "Reagent Identifier Test";
        screen.setReagentSetIdentifier(identifier);
        screen.saveAndUpdate(client);
        assertEquals(identifier, client.getScreen(SCREEN1.id).getReagentSetIdentifier());
    }

}
