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

package fr.igred.omero.screen;


import fr.igred.omero.UserTest;
import fr.igred.omero.core.Image;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class WellSampleTest extends UserTest {


    @Test
    void testGetScreens() throws Exception {
        Well         well    = client.getWell(1L);
        WellSample   sample  = well.getWellSamples().get(0);
        List<Screen> screens = sample.getScreens(client);
        assertEquals(1, screens.size());
        assertEquals(SCREEN1.id, screens.get(0).getId());
    }


    @Test
    void testGetPlates() throws Exception {
        Well        well   = client.getWell(1L);
        WellSample  sample = well.getWellSamples().get(0);
        List<Plate> plates = sample.getPlates(client);
        assertEquals(1, plates.size());
        assertEquals(well.getPlate().getId(), plates.get(0).getId());
    }


    @Test
    void testGetPlateAcquisitions() throws Exception {
        Well                   well   = client.getWell(1L);
        WellSample             sample = well.getWellSamples().get(0);
        List<PlateAcquisition> acqs   = sample.getPlateAcquisitions(client);
        assertEquals(1, acqs.size());
    }


    @Test
    void testGetWells() throws Exception {
        Well       well   = client.getWell(1L);
        WellSample sample = well.getWellSamples().get(0);
        List<Well> wells  = sample.getWells(client);
        assertEquals(1, wells.size());
        assertEquals(well.getId(), wells.get(0).getId());
    }


    @Test
    void testGetImages() throws Exception {
        Well        well   = client.getWell(1L);
        WellSample  sample = well.getWellSamples().get(0);
        List<Image> images = sample.getImages(client);
        assertEquals(1, images.size());
        assertEquals(sample.getImage().getId(), images.get(0).getId());
    }


    @Test
    void testGetImage() throws Exception {
        final String name = "screen1.fake [screen1 2]";

        Plate plate = client.getPlate(PLATE1.id);
        Well  well  = plate.getWells(client).get(0);

        WellSample sample = well.getWellSamples().get(1);

        Image image = sample.getImage();

        assertFalse(image.isOrphaned(client));
        assertEquals(name, image.getName());
    }


    @Test
    void testGetWell() throws Exception {
        final long wellId = 1L;
        Well       well   = client.getWell(wellId);
        WellSample sample = well.getWellSamples().get(0);

        assertEquals(wellId, sample.getWell(client).getId());
    }


    @Test
    void testGetPositionX() throws Exception {
        Well well = client.getWells(1L).get(0);

        WellSample sample = well.getWellSamples().get(0);
        assertEquals(0.0, sample.getPositionX(null).getValue(), Double.MIN_VALUE);
    }


    @Test
    void testGetPositionY() throws Exception {
        Well well = client.getWells(1L).get(0);

        WellSample sample = well.getWellSamples().get(0);
        assertEquals(1.0, sample.getPositionY(null).getValue(), Double.MIN_VALUE);
    }


    @Test
    void testGetStartTime() throws Exception {
        final long time = 1146766431000L;

        Well well = client.getWells(1L).get(0);

        WellSample sample = well.getWellSamples().get(0);
        assertEquals(time, sample.getStartTime());
    }

}