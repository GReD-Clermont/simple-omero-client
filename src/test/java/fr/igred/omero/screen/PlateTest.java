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
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.core.Image;
import omero.gateway.model.PlateData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static java.lang.Double.MIN_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class PlateTest extends UserTest {


    @Test
    void testGetScreens() throws Exception {
        Plate        plate   = client.getPlate(PLATE1.id);
        List<Screen> screens = plate.getScreens(client);
        assertEquals(1, screens.size());
    }


    @Test
    void testGetWells() throws Exception {
        Plate      plate = client.getPlate(PLATE1.id);
        List<Well> wells = plate.getWells(client);
        assertEquals(9, wells.size());
    }


    @Test
    void testGetWells2() throws Exception {
        Plate      plate = client.getPlate(PLATE2.id);
        List<Well> wells = plate.getWells(client);
        assertEquals(4, wells.size());
    }


    @Test
    void testGetImages() throws Exception {
        Plate       plate  = client.getPlate(PLATE1.id);
        List<Image> images = plate.getImages(client);
        assertEquals(36, images.size());
    }


    @Test
    void testGetPlateAcquisitionsFromPlate() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        List<PlateAcquisition> acquisitions = plate.getPlateAcquisitions();
        assertEquals(2, acquisitions.size());
        assertEquals(1L, acquisitions.get(0).getId());
    }


    @Test
    void testAddTagToPlate() throws Exception {
        Plate plate = client.getPlate(PLATE2.id);

        String name = "Plate tag";
        String desc = "tag attached to a plate";

        TagAnnotation tag = new TagAnnotationWrapper(client, name, desc);
        plate.link(client, tag);
        List<TagAnnotation> tags = plate.getTags(client);
        client.delete(tag);
        List<TagAnnotation> checkTags = plate.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
    }


    @Test
    void testAddAndRemoveTagFromPlate() throws Exception {
        Plate plate = client.getPlate(PLATE2.id);

        String name = "Plate tag";
        String desc = "tag attached to a plate";

        TagAnnotation tag = new TagAnnotationWrapper(client, name, desc);
        plate.link(client, tag);
        List<TagAnnotation> tags = plate.getTags(client);
        plate.unlink(client, tag);
        List<TagAnnotation> removedTags = plate.getTags(client);
        client.delete(tag);

        assertEquals(1, tags.size());
        assertEquals(0, removedTags.size());
    }


    @Test
    void testSetName() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        String name  = plate.getName();
        String name2 = "New name";
        plate.setName(name2);
        plate.saveAndUpdate(client);
        assertEquals(name2, client.getPlate(PLATE1.id).getName());

        plate.setName(name);
        plate.saveAndUpdate(client);
        assertEquals(name, client.getPlate(PLATE1.id).getName());
    }


    @Test
    void testSetDescription() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        String description = plate.getDescription();

        String description2 = "New description";
        plate.setDescription(description2);
        plate.saveAndUpdate(client);
        assertEquals(description2, client.getPlate(PLATE1.id).getDescription());

        plate.setDescription(description);
        plate.saveAndUpdate(client);
        assertEquals(description, client.getPlate(PLATE1.id).getDescription());
    }


    @Test
    void testSetStatus() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        String status = plate.getStatus();

        String status2 = "New status";
        plate.setStatus(status2);
        plate.saveAndUpdate(client);
        plate.reload(client);
        assertEquals(status2, client.getPlate(PLATE1.id).getStatus());

        plate.setStatus(status);
        plate.saveAndUpdate(client);
        assertEquals(status, client.getPlate(PLATE1.id).getStatus());
    }


    @Test
    void testGetDefaultSample() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        int sample = plate.getDefaultSample();

        int sample2 = 1;
        plate.setDefaultSample(sample2);
        plate.saveAndUpdate(client);
        assertEquals(sample2, client.getPlate(PLATE1.id).getDefaultSample());

        plate.setDefaultSample(sample);
        plate.saveAndUpdate(client);
        assertEquals(sample, client.getPlate(PLATE1.id).getDefaultSample());
    }


    @Test
    void testSetExternalIdentifier() throws Exception {
        final String identifier = "External Identifier Test";

        Plate plate = client.getPlate(PLATE1.id);
        plate.setExternalIdentifier(identifier);
        plate.saveAndUpdate(client);
        assertEquals(identifier,
                     client.getPlate(PLATE1.id).getExternalIdentifier());
    }


    @Test
    void testGetPlateType() throws Exception {
        final String type  = "9-Well Plate";
        Plate        plate = client.getPlate(PLATE1.id);
        assertEquals(type, plate.getPlateType());
    }


    @Test
    void testGetColumnSequenceIndex() throws Exception {
        Plate plate  = client.getPlate(PLATE1.id);
        int   column = 0;
        assertEquals(column, plate.getColumnSequenceIndex());
    }


    @Test
    void testGetRowSequenceIndex() throws Exception {
        final int column = 1;
        Plate     plate  = client.getPlate(PLATE1.id);
        assertEquals(column, plate.getRowSequenceIndex());
    }


    @Test
    void testGetWellOriginX() throws Exception {
        final double origin = 0.0d;
        Plate        plate  = client.getPlate(PLATE1.id);
        assertEquals(origin, plate.getWellOriginX(null).getValue(), MIN_VALUE);
    }


    @Test
    void testGetWellOriginY() throws Exception {
        final double origin = 1.0d;
        Plate        plate  = client.getPlate(PLATE1.id);
        assertEquals(origin, plate.getWellOriginY(null).getValue(), MIN_VALUE);
    }


    @Test
    void testCreateOrphanedPlateAndDeleteIt() throws Exception {
        String name = "To delete";

        Plate plate = new PlateWrapper(new PlateData());
        plate.setName(name);
        plate.saveAndUpdate(client);
        long id = plate.getId();

        List<Plate> orphaned = client.getOrphanedPlates();
        client.delete(orphaned);

        assertEquals(1, orphaned.size());
        assertEquals(name, orphaned.get(0).getName());

        assertThrows(NoSuchElementException.class, () -> client.getPlate(id));
    }

}
