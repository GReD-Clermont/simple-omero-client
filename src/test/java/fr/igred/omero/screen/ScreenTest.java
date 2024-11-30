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

package fr.igred.omero.screen;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.core.Image;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ScreenTest extends UserTest {


    @Test
    void testGetPlateAcquisitions() throws Exception {
        Screen screen = client.getScreen(SCREEN2.id);
        assertEquals(2, screen.getPlateAcquisitions(client).size());
    }


    @Test
    void testGetWells() throws Exception {
        Screen     screen = client.getScreen(SCREEN2.id);
        List<Well> wells  = screen.getWells(client);
        assertEquals(8, wells.size());
    }


    @Test
    void testGetImages() throws Exception {
        Screen      screen = client.getScreen(SCREEN2.id);
        List<Image> images = screen.getImages(client);
        assertEquals(16, images.size());
    }


    @Test
    void testGetPlatesFromScreen() throws Exception {
        Screen      screen = client.getScreen(SCREEN2.id);
        List<Plate> plates = screen.getPlates();
        assertEquals(2, plates.size());
    }


    @Test
    void testGetPlatesFromScreen2() throws Exception {
        Screen      screen = client.getScreen(SCREEN2.id);
        List<Plate> plates = screen.getPlates("Plate Name 1");
        assertEquals(1, plates.size());
    }


    @Test
    void testAddTagToScreen() throws Exception {
        Screen screen = client.getScreen(SCREEN2.id);

        String name = "Screen tag";
        String desc = "tag attached to a screen";

        TagAnnotation tag = new TagAnnotation(client, name, desc);
        screen.link(client, tag);
        List<TagAnnotation> tags = screen.getTags(client);
        client.delete(tag);
        List<TagAnnotation> checkTags = screen.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
    }


    @Test
    void testAddAndRemoveTagFromScreen() throws Exception {
        Screen screen = client.getScreen(SCREEN2.id);

        String name = "Screen tag";
        String desc = "tag attached to a screen";

        TagAnnotation tag = new TagAnnotation(client, name, desc);
        screen.link(client, tag);
        List<TagAnnotation> tags = screen.getTags(client);
        screen.unlink(client, tag);
        List<TagAnnotation> removedTags = screen.getTags(client);
        client.delete(tag);

        assertEquals(1, tags.size());
        assertEquals(0, removedTags.size());
    }


    @Test
    void testSetName() throws Exception {
        Screen screen = client.getScreen(SCREEN1.id);

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
    void testSetDescription() throws Exception {
        Screen screen = client.getScreen(SCREEN1.id);

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
    void testSetProtocolDescription() throws Exception {
        Screen screen = client.getScreen(SCREEN1.id);

        String description = "Protocol Description Test";
        screen.setProtocolDescription(description);
        screen.saveAndUpdate(client);
        assertEquals(description, client.getScreen(SCREEN1.id).getProtocolDescription());
    }


    @Test
    void testSetProtocolIdentifier() throws Exception {
        Screen screen = client.getScreen(SCREEN1.id);

        String identifier = "Protocol Identifier Test";
        screen.setProtocolIdentifier(identifier);
        screen.saveAndUpdate(client);
        assertEquals(identifier, client.getScreen(SCREEN1.id).getProtocolIdentifier());
    }


    @Test
    void testSetReagentSetDescription() throws Exception {
        Screen screen = client.getScreen(SCREEN1.id);

        String description = "Reagent Description Test";
        screen.setReagentSetDescription(description);
        screen.saveAndUpdate(client);
        assertEquals(description, client.getScreen(SCREEN1.id).getReagentSetDescription());
    }


    @Test
    void testSetReagentSetIdentifier() throws Exception {
        Screen screen = client.getScreen(SCREEN1.id);

        String identifier = "Reagent Identifier Test";
        screen.setReagentSetIdentifier(identifier);
        screen.saveAndUpdate(client);
        assertEquals(identifier, client.getScreen(SCREEN1.id).getReagentSetIdentifier());
    }


    @Test
    void testImportImages() throws Exception {
        String filename1 = "default-screen&screens=1&plates=1&plateAcqs=1&plateRows=3&plateCols=3&fields=4.fake";
        String filename2 = "default-screen&screens=1&plates=1&plateAcqs=1&plateRows=2&plateCols=2&fields=2.fake";

        File f1 = createFile(filename1);
        File f2 = createFile(filename2);

        String name = "Import";
        String desc = "test-import";

        Screen screen = new Screen(client, name, desc);

        boolean imported = screen.importImages(client, f1.getAbsolutePath(), f2.getAbsolutePath());
        screen.reload(client);

        removeFile(f1);
        removeFile(f2);

        List<Plate> plates = screen.getPlates();
        assertEquals(2, plates.size());
        List<Well> wells = plates.get(0).getWells(client);
        wells.addAll(plates.get(1).getWells(client));
        assertEquals(13, wells.size());
        List<WellSample> samples = wells.stream()
                                        .map(Well::getWellSamples)
                                        .flatMap(List::stream)
                                        .collect(Collectors.toList());
        assertEquals(44, samples.size());
        List<Image> images = samples.stream()
                                    .map(WellSample::getImage)
                                    .collect(Collectors.toList());

        client.delete(images);
        client.delete(samples);
        client.delete(wells);
        client.delete(plates);

        screen.reload(client);
        assertTrue(screen.getPlates().isEmpty());

        client.delete(screen);
        assertTrue(imported);
    }


    @Test
    void testImportImage() throws Exception {
        String filename = "default-screen&screens=1&plates=1&plateAcqs=1&plateRows=2&plateCols=2&fields=2.fake";

        File file = createFile(filename);

        String name = "Import";
        String desc = "test-import";

        Screen screen = new Screen(client, name, desc);

        List<Long> ids = screen.importImage(client, file.getAbsolutePath());
        screen.reload(client);

        removeFile(file);

        List<Plate> plates = screen.getPlates();
        assertEquals(1, plates.size());
        List<Well> wells = plates.get(0).getWells(client);
        assertEquals(4, wells.size());
        List<WellSample> samples = wells.stream()
                                        .map(Well::getWellSamples)
                                        .flatMap(List::stream)
                                        .collect(Collectors.toList());
        assertEquals(8, samples.size());
        List<Image> images = samples.stream()
                                    .map(WellSample::getImage)
                                    .collect(Collectors.toList());

        assertEquals(images.size(), ids.size());
        client.delete(images);
        client.delete(samples);
        client.delete(wells);
        client.delete(plates);

        screen.reload(client);
        List<Plate> endPlates = screen.getPlates();

        client.delete(screen);

        assertTrue(endPlates.isEmpty());
    }

}
