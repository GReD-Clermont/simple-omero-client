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
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ScreenTest extends UserTest {


    @Test
    void testGetPlatesFromScreen() throws Exception {
        ScreenWrapper      screen = client.getScreen(SCREEN2.id);
        List<PlateWrapper> plates = screen.getPlates();
        assertEquals(2, plates.size());
    }


    @Test
    void testGetPlatesFromScreen2() throws Exception {
        ScreenWrapper      screen = client.getScreen(SCREEN2.id);
        List<PlateWrapper> plates = screen.getPlates("Plate Name 1");
        assertEquals(1, plates.size());
    }


    @Test
    void testAddTagToScreen() throws Exception {
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
    void testSetName() throws Exception {
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
    void testSetDescription() throws Exception {
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
    void testSetProtocolDescription() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

        String description = "Protocol Description Test";
        screen.setProtocolDescription(description);
        screen.saveAndUpdate(client);
        assertEquals(description, client.getScreen(SCREEN1.id).getProtocolDescription());
    }


    @Test
    void testSetProtocolIdentifier() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

        String identifier = "Protocol Identifier Test";
        screen.setProtocolIdentifier(identifier);
        screen.saveAndUpdate(client);
        assertEquals(identifier, client.getScreen(SCREEN1.id).getProtocolIdentifier());
    }


    @Test
    void testSetReagentSetDescription() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

        String description = "Reagent Description Test";
        screen.setReagentSetDescription(description);
        screen.saveAndUpdate(client);
        assertEquals(description, client.getScreen(SCREEN1.id).getReagentSetDescription());
    }


    @Test
    void testSetReagentSetIdentifier() throws Exception {
        ScreenWrapper screen = client.getScreen(SCREEN1.id);

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

        ScreenWrapper screen = new ScreenWrapper(client, "Import", "test-import");

        boolean imported = screen.importImages(client, f1.getAbsolutePath(), f2.getAbsolutePath());

        removeFile(f1);
        removeFile(f2);

        List<PlateWrapper> plates = screen.getPlates();
        assertEquals(2, plates.size());
        List<WellWrapper> wells = plates.get(0).getWells(client);
        wells.addAll(plates.get(1).getWells(client));
        assertEquals(13, wells.size());
        List<WellSampleWrapper> samples = wells.stream()
                                               .map(WellWrapper::getWellSamples)
                                               .flatMap(List::stream)
                                               .collect(Collectors.toList());
        assertEquals(44, samples.size());
        List<ImageWrapper> images = samples.stream()
                                           .map(WellSampleWrapper::getImage)
                                           .collect(Collectors.toList());

        client.delete(images);
        client.delete(samples);
        client.delete(wells);
        client.delete(plates);

        screen.refresh(client);
        assertTrue(screen.getPlates().isEmpty());

        client.delete(screen);
        assertTrue(imported);
    }


    @Test
    void testImportImage() throws Exception {

        String filename = "default-screen&screens=1&plates=1&plateAcqs=1&plateRows=2&plateCols=2&fields=2.fake";

        File f = createFile(filename);

        ScreenWrapper screen = new ScreenWrapper(client, "Import", "test-import");

        List<Long> ids = screen.importImage(client, f.getAbsolutePath());

        removeFile(f);

        List<PlateWrapper> plates = screen.getPlates();
        assertEquals(1, plates.size());
        List<WellWrapper> wells = plates.get(0).getWells(client);
        assertEquals(4, wells.size());
        List<WellSampleWrapper> samples = wells.stream()
                                               .map(WellWrapper::getWellSamples)
                                               .flatMap(List::stream)
                                               .collect(Collectors.toList());
        assertEquals(8, samples.size());
        List<ImageWrapper> images = samples.stream()
                                           .map(WellSampleWrapper::getImage)
                                           .collect(Collectors.toList());

        assertEquals(images.size(), ids.size());
        client.delete(images);
        client.delete(samples);
        client.delete(wells);
        client.delete(plates);

        screen.refresh(client);
        List<PlateWrapper> endPlates = screen.getPlates();

        client.delete(screen);

        assertTrue(endPlates.isEmpty());
    }

}
