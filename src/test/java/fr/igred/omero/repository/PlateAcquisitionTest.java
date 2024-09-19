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

package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class PlateAcquisitionTest extends UserTest {


    @Test
    void testGetScreens() throws Exception {
        PlateWrapper            plate   = client.getPlate(PLATE1.id);
        PlateAcquisitionWrapper acq     = plate.getPlateAcquisitions().get(0);
        List<ScreenWrapper>     screens = acq.getScreens(client);
        assertEquals(1, screens.size());
    }


    @Test
    void testGetPlates() throws Exception {
        PlateWrapper            plate = client.getPlate(PLATE1.id);
        PlateAcquisitionWrapper acq   = plate.getPlateAcquisitions().get(0);
        assertEquals(PLATE1.id, acq.getPlates(client).get(0).getId());
    }


    @Test
    void testGetWells() throws Exception {
        PlateWrapper            plate = client.getPlate(PLATE1.id);
        PlateAcquisitionWrapper acq   = plate.getPlateAcquisitions().get(0);
        List<WellWrapper>       wells = acq.getWells(client);
        assertEquals(9, wells.size());
    }


    @Test
    void testGetImages1() throws Exception {
        PlateWrapper            plate  = client.getPlate(PLATE1.id);
        PlateAcquisitionWrapper acq    = plate.getPlateAcquisitions().get(0);
        List<ImageWrapper>      images = acq.getImages(client);
        assertEquals(18, images.size());
    }


    @Test
    void testGetImages2() throws Exception {
        PlateWrapper            plate = client.getPlate(PLATE1.id);
        PlateAcquisitionWrapper acq   = plate.getPlateAcquisitions().get(0);
        acq.reload(client);
        List<ImageWrapper> images = acq.getImages();
        assertEquals(18, images.size());
    }


    @Test
    void testAddTagToPlateAcquisition() throws Exception {
        PlateWrapper plate = client.getPlate(PLATE1.id);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);

        String name = "Plate acq. tag";
        String desc = "tag attached to a plate acq.";

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, name, desc);
        acq.link(client, tag);

        List<PlateAcquisitionWrapper> taggedAcqs = tag.getPlateAcquisitions(client);
        List<TagAnnotationWrapper>    tags       = acq.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> checkTags = acq.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
        assertEquals(1, taggedAcqs.size());
        assertEquals(acq.getId(), taggedAcqs.get(0).getId());
    }


    @Test
    void testAddAndRemoveTagFromPlateAcquisition() throws Exception {
        PlateWrapper plate = client.getPlate(PLATE1.id);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);

        String name = "Plate acq. tag";
        String desc = "tag attached to a plate acq.";

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, name, desc);
        acq.link(client, tag);
        List<TagAnnotationWrapper> tags = acq.getTags(client);
        acq.unlink(client, tag);
        List<TagAnnotationWrapper> removedTags = acq.getTags(client);
        client.delete(tag);

        assertEquals(1, tags.size());
        assertEquals(0, removedTags.size());
    }


    @Test
    void testSetName() throws Exception {
        PlateWrapper plate = client.getPlate(PLATE1.id);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);

        String name  = acq.getName();
        String name2 = "New name";
        acq.setName(name2);
        acq.saveAndUpdate(client);
        assertEquals(name2, client.getPlate(PLATE1.id)
                                  .getPlateAcquisitions()
                                  .get(0)
                                  .getName());

        acq.setName(name);
        acq.saveAndUpdate(client);
        assertEquals(name, client.getPlate(PLATE1.id)
                                 .getPlateAcquisitions()
                                 .get(0)
                                 .getName());
    }


    @Test
    void testSetDescription() throws Exception {
        PlateWrapper plate = client.getPlate(PLATE1.id);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);

        String name  = acq.getDescription();
        String name2 = "New description";
        acq.setDescription(name2);
        acq.saveAndUpdate(client);
        assertEquals(name2, client.getPlate(PLATE1.id)
                                  .getPlateAcquisitions()
                                  .get(0)
                                  .getDescription());

        acq.setDescription(name);
        acq.saveAndUpdate(client);
        assertEquals(name, client.getPlate(PLATE1.id)
                                 .getPlateAcquisitions()
                                 .get(0)
                                 .getDescription());
    }


    @Test
    void testGetLabel() throws Exception {
        PlateWrapper plate = client.getPlate(PLATE1.id);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(acq.getName(), acq.getLabel());
    }


    @Test
    void testGetRefPlateId() throws Exception {
        PlateWrapper plate = client.getPlate(PLATE1.id);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(1, acq.getRefPlateId());
        acq.setRefPlateId(-1L);
        // Saving does not work: "acq.saveAndUpdate(client);" does nothing
        assertEquals(-1L, acq.getRefPlateId());
    }


    @Test
    void testGetStartTime() throws Exception {
        final long time = 1146766431000L;

        PlateWrapper plate = client.getPlate(PLATE1.id);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(time, acq.getStartTime().getTime());
    }


    @Test
    void testGetEndTime() throws Exception {
        final long time = 1146766431000L;

        PlateWrapper plate = client.getPlate(PLATE1.id);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(time, acq.getEndTime().getTime());
    }


    @Test
    void testGetMaximumFieldCount() throws Exception {
        PlateWrapper plate = client.getPlate(PLATE1.id);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(-1, acq.getMaximumFieldCount());
    }

}