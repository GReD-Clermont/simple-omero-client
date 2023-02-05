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

package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class PlateAcquisitionTest extends UserTest {


    @Test
    void testAddTagToPlateAcquisition() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        PlateAcquisition acq = plate.getPlateAcquisitions().get(0);

        TagAnnotation tag = new TagAnnotationWrapper(client, "Plate acq. tag", "tag attached to a plate acq.");
        acq.link(client, tag);
        List<TagAnnotation> tags = acq.getTags(client);
        client.delete(tag);
        List<TagAnnotation> checkTags = acq.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
    }


    @Test
    void testSetName() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        PlateAcquisition acq = plate.getPlateAcquisitions().get(0);

        String name  = acq.getName();
        String name2 = "New name";
        acq.setName(name2);
        acq.saveAndUpdate(client);
        assertEquals(name2, client.getPlate(PLATE1.id).getPlateAcquisitions().get(0).getName());

        acq.setName(name);
        acq.saveAndUpdate(client);
        assertEquals(name, client.getPlate(PLATE1.id).getPlateAcquisitions().get(0).getName());
    }


    @Test
    void testSetDescription() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        PlateAcquisition acq = plate.getPlateAcquisitions().get(0);

        String name  = acq.getDescription();
        String name2 = "New description";
        acq.setDescription(name2);
        acq.saveAndUpdate(client);
        assertEquals(name2, client.getPlate(PLATE1.id).getPlateAcquisitions().get(0).getDescription());

        acq.setDescription(name);
        acq.saveAndUpdate(client);
        assertEquals(name, client.getPlate(PLATE1.id).getPlateAcquisitions().get(0).getDescription());
    }


    @Test
    void testGetLabel() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        PlateAcquisition acq = plate.getPlateAcquisitions().get(0);
        assertEquals(acq.getName(), acq.getLabel());
    }


    @Test
    void testGetRefPlateId() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        PlateAcquisition acq = plate.getPlateAcquisitions().get(0);
        assertEquals(-1, acq.getRefPlateId());
        acq.setRefPlateId(PLATE1.id);
        // Saving does not work: acq.saveAndUpdate(client);
        assertEquals(PLATE1.id, acq.getRefPlateId());
    }


    @Test
    void testGetStartTime() throws Exception {
        final long time = 1146766431000L;

        Plate plate = client.getPlate(PLATE1.id);

        PlateAcquisition acq = plate.getPlateAcquisitions().get(0);
        assertEquals(time, acq.getStartTime().getTime());
    }


    @Test
    void testGetEndTime() throws Exception {
        final long time = 1146766431000L;

        Plate plate = client.getPlate(PLATE1.id);

        PlateAcquisition acq = plate.getPlateAcquisitions().get(0);
        assertEquals(time, acq.getEndTime().getTime());
    }


    @Test
    void testGetMaximumFieldCount() throws Exception {
        Plate plate = client.getPlate(PLATE1.id);

        PlateAcquisition acq = plate.getPlateAcquisitions().get(0);
        assertEquals(-1, acq.getMaximumFieldCount());
    }

}