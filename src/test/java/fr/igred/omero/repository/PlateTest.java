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


public class PlateTest extends UserTest {


    @Test
    public void testGetWellsFromPlate() throws Exception {
        PlateWrapper      plate = client.getPlates(1L).get(0);
        List<WellWrapper> wells = plate.getWells(client);
        assertEquals(9, wells.size());
    }


    @Test
    public void testGetWellsFromPlate2() throws Exception {
        PlateWrapper      plate = client.getPlates(2L).get(0);
        List<WellWrapper> wells = plate.getWells(client);
        assertEquals(4, wells.size());
    }


    @Test
    public void testGetPlateAcquisitionsFromPlate() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        List<PlateAcquisitionWrapper> acquisitions = plate.getPlateAcquisitions();
        assertEquals(1, acquisitions.size());
        String name = "PlateAcquisition Name 0";
        assertEquals(name, acquisitions.get(0).getName());
    }


    @Test
    public void testAddTagToPlate() throws Exception {
        PlateWrapper plate = client.getPlates(2L).get(0);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Plate tag", "tag attached to a plate");
        plate.addTag(client, tag);
        List<TagAnnotationWrapper> tags = plate.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> checkTags = plate.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
    }


    @Test
    public void testSetName() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        String name  = plate.getName();
        String name2 = "New name";
        plate.setName(name2);
        plate.saveAndUpdate(client);
        assertEquals(name2, client.getPlates(1L).get(0).getName());

        plate.setName(name);
        plate.saveAndUpdate(client);
        assertEquals(name, client.getPlates(1L).get(0).getName());
    }


    @Test
    public void testSetDescription() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        String description = plate.getDescription();

        String description2 = "New description";
        plate.setDescription(description2);
        plate.saveAndUpdate(client);
        assertEquals(description2, client.getPlates(1L).get(0).getDescription());

        plate.setDescription(description);
        plate.saveAndUpdate(client);
        assertEquals(description, client.getPlates(1L).get(0).getDescription());
    }


    @Test
    public void testSetStatus() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        String status = plate.getStatus();

        String status2 = "New status";
        plate.setStatus(status2);
        plate.saveAndUpdate(client);
        assertEquals(status2, client.getPlates(1L).get(0).getStatus());

        plate.setStatus(status);
        plate.saveAndUpdate(client);
        assertEquals(status, client.getPlates(1L).get(0).getStatus());
    }


    @Test
    public void testGetDefaultSample() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        int sample = plate.getDefaultSample();

        int sample2 = 1;
        plate.setDefaultSample(sample2);
        plate.saveAndUpdate(client);
        assertEquals(sample2, client.getPlates(1L).get(0).getDefaultSample());

        plate.setDefaultSample(sample);
        plate.saveAndUpdate(client);
        assertEquals(sample, client.getPlates(1L).get(0).getDefaultSample());
    }


    @Test
    public void testSetExternalIdentifier() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        String identifier = "External Identifier Test";
        plate.setExternalIdentifier(identifier);
        plate.saveAndUpdate(client);
        assertEquals(identifier, client.getPlates(1L).get(0).getExternalIdentifier());
    }


    @Test
    public void testGetPlateType() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);
        String       type  = "9-Well Plate";
        assertEquals(type, plate.getPlateType());
    }


    @Test
    public void testGetColumnSequenceIndex() throws Exception {
        PlateWrapper plate  = client.getPlates(1L).get(0);
        int          column = 0;
        assertEquals(column, plate.getColumnSequenceIndex());
    }


    @Test
    public void testGetRowSequenceIndex() throws Exception {
        PlateWrapper plate  = client.getPlates(1L).get(0);
        int          column = 1;
        assertEquals(column, plate.getRowSequenceIndex());
    }


    @Test
    public void testGetWellOriginX() throws Exception {
        PlateWrapper plate  = client.getPlates(1L).get(0);
        double       origin = 0.0d;
        assertEquals(origin, plate.getWellOriginX(null).getValue(), Double.MIN_VALUE);
    }


    @Test
    public void testGetWellOriginY() throws Exception {
        PlateWrapper plate  = client.getPlates(1L).get(0);
        double       origin = 1.0d;
        assertEquals(origin, plate.getWellOriginY(null).getValue(), Double.MIN_VALUE);
    }

}
