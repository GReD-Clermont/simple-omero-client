package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class WellTest extends UserTest {


    @Test
    public void testAddTagToWell() throws Exception {
        WellWrapper well = client.getWells(2L).get(0);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Well tag", "tag attached to a well");
        well.addTag(client, tag);
        List<TagAnnotationWrapper> tags = well.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> checkTags = well.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
    }


    @Test
    public void testGetWellSamples() throws Exception {
        WellWrapper             well    = client.getWells(1L).get(0);
        List<WellSampleWrapper> samples = well.getWellSamples();
        assertEquals(4, samples.size());
    }


    @Test
    public void testTestGetName() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);
        WellWrapper  well  = plate.getWells(client).get(0);

        String name = "Well A-1";
        assertEquals(name, well.getName());
    }


    @Test
    public void testGetDescription() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);
        WellWrapper  well  = plate.getWells(client).get(0);

        String description = "External Description";
        assertEquals(description, well.getDescription());
    }


    @Test
    public void testGetColumn() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);
        WellWrapper  well  = plate.getWells(client).get(1);
        assertEquals(1, well.getColumn().intValue());
    }


    @Test
    public void testGetRow() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);
        WellWrapper  well  = plate.getWells(client).get(6);
        assertEquals(2, well.getRow().intValue());
    }


    @Test
    public void testSetStatus() throws Exception {
        WellWrapper well = client.getWells(1L).get(0);

        String status  = well.getStatus();
        String status2 = "New status";

        well.setStatus(status2);
        well.saveAndUpdate(client);
        assertEquals(status2, client.getWells(1L).get(0).getStatus());

        well.setStatus(status);
        well.saveAndUpdate(client);
        assertEquals(status, client.getWells(1L).get(0).getStatus());
    }


    @Test
    public void testSetWellType() throws Exception {
        WellWrapper well = client.getWells(1L).get(0);

        String type  = well.getWellType();
        String type2 = "New type";

        well.setWellType(type2);
        well.saveAndUpdate(client);
        assertEquals(type2, client.getWells(1L).get(0).getWellType());

        well.setWellType(type);
        well.saveAndUpdate(client);
        assertEquals(type, client.getWells(1L).get(0).getWellType());
    }


    @Test
    public void testSetRed() throws Exception {
        WellWrapper well = client.getWells(1L).get(0);

        int red  = well.getRed();
        int red2 = 2;

        well.setRed(red2);
        well.saveAndUpdate(client);
        assertEquals(red2, client.getWells(1L).get(0).getRed());

        well.setRed(red);
        well.saveAndUpdate(client);
        assertEquals(red, client.getWells(1L).get(0).getRed());
    }


    @Test
    public void testSetGreen() throws Exception {
        WellWrapper well = client.getWells(1L).get(0);

        int green  = well.getGreen();
        int green2 = 3;

        well.setGreen(green2);
        well.saveAndUpdate(client);
        assertEquals(green2, client.getWells(1L).get(0).getGreen());

        well.setGreen(green);
        well.saveAndUpdate(client);
        assertEquals(green, client.getWells(1L).get(0).getGreen());
    }


    @Test
    public void testSetBlue() throws Exception {
        WellWrapper well = client.getWells(1L).get(0);

        int blue  = well.getBlue();
        int blue2 = 4;

        well.setBlue(blue2);
        well.saveAndUpdate(client);
        assertEquals(blue2, client.getWells(1L).get(0).getBlue());

        well.setBlue(blue);
        well.saveAndUpdate(client);
        assertEquals(blue, client.getWells(1L).get(0).getBlue());
    }


    @Test
    public void testSetAlpha() throws Exception {
        WellWrapper well = client.getWells(1L).get(0);

        int alpha  = well.getAlpha();
        int alpha2 = 5;

        well.setAlpha(alpha2);
        well.saveAndUpdate(client);
        assertEquals(alpha2, client.getWells(1L).get(0).getAlpha());

        well.setAlpha(alpha);
        well.saveAndUpdate(client);
        assertEquals(alpha, client.getWells(1L).get(0).getAlpha());
    }

}