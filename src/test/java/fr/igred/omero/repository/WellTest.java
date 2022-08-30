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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class WellTest extends UserTest {


    @Test
    void testAddTagToWell() throws Exception {
        WellWrapper well = client.getWell(2L);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Well tag", "tag attached to a well");
        well.addTag(client, tag);
        List<TagAnnotationWrapper> tags = well.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> checkTags = well.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
    }


    @Test
    void testGetWellSamples() throws Exception {
        WellWrapper             well    = client.getWell(1L);
        List<WellSampleWrapper> samples = well.getWellSamples();
        assertEquals(4, samples.size());
    }


    @Test
    void testTestGetName() throws Exception {
        final String name = "Well A-1";

        PlateWrapper plate = client.getPlate(PLATE1.id);
        WellWrapper  well  = plate.getWells(client).get(0);

        assertEquals(name, well.getName());
    }


    @Test
    void testGetDescription() throws Exception {
        final String description = "External Description";

        PlateWrapper plate = client.getPlate(PLATE1.id);
        WellWrapper  well  = plate.getWells(client).get(0);

        assertEquals(description, well.getDescription());
    }


    @Test
    void testGetColumn() throws Exception {
        PlateWrapper plate = client.getPlate(PLATE1.id);
        WellWrapper  well  = plate.getWells(client).get(1);
        assertEquals(1, well.getColumn().intValue());
    }


    @Test
    void testGetRow() throws Exception {
        PlateWrapper plate = client.getPlate(PLATE1.id);
        WellWrapper  well  = plate.getWells(client).get(6);
        assertEquals(2, well.getRow().intValue());
    }


    @Test
    void testSetStatus() throws Exception {
        WellWrapper well = client.getWell(1L);

        String status  = well.getStatus();
        String status2 = "New status";

        well.setStatus(status2);
        well.saveAndUpdate(client);
        assertEquals(status2, client.getWell(1L).getStatus());

        well.setStatus(status);
        well.saveAndUpdate(client);
        assertEquals(status, client.getWell(1L).getStatus());
    }


    @Test
    void testSetWellType() throws Exception {
        WellWrapper well = client.getWell(1L);

        String type  = well.getWellType();
        String type2 = "New type";

        well.setWellType(type2);
        well.saveAndUpdate(client);
        assertEquals(type2, client.getWell(1L).getWellType());

        well.setWellType(type);
        well.saveAndUpdate(client);
        assertEquals(type, client.getWell(1L).getWellType());
    }


    @Test
    void testSetRed() throws Exception {
        WellWrapper well = client.getWell(1L);

        int red  = well.getRed();
        int red2 = 2;

        well.setRed(red2);
        well.saveAndUpdate(client);
        assertEquals(red2, client.getWell(1L).getRed());

        well.setRed(red);
        well.saveAndUpdate(client);
        assertEquals(red, client.getWell(1L).getRed());
    }


    @Test
    void testSetGreen() throws Exception {
        WellWrapper well = client.getWell(1L);

        int green  = well.getGreen();
        int green2 = 3;

        well.setGreen(green2);
        well.saveAndUpdate(client);
        assertEquals(green2, client.getWell(1L).getGreen());

        well.setGreen(green);
        well.saveAndUpdate(client);
        assertEquals(green, client.getWell(1L).getGreen());
    }


    @Test
    void testSetBlue() throws Exception {
        WellWrapper well = client.getWell(1L);

        int blue  = well.getBlue();
        int blue2 = 4;

        well.setBlue(blue2);
        well.saveAndUpdate(client);
        assertEquals(blue2, client.getWell(1L).getBlue());

        well.setBlue(blue);
        well.saveAndUpdate(client);
        assertEquals(blue, client.getWell(1L).getBlue());
    }


    @Test
    void testSetAlpha() throws Exception {
        WellWrapper well = client.getWell(1L);

        int alpha  = well.getAlpha();
        int alpha2 = 5;

        well.setAlpha(alpha2);
        well.saveAndUpdate(client);
        assertEquals(alpha2, client.getWell(1L).getAlpha());

        well.setAlpha(alpha);
        well.saveAndUpdate(client);
        assertEquals(alpha, client.getWell(1L).getAlpha());
    }

}