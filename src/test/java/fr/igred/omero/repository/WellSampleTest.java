package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class WellSampleTest extends UserTest {


    @Test
    public void testGetImage() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);
        WellWrapper  well  = plate.getWells(client).get(0);

        WellSampleWrapper sample = well.getWellSamples().get(1);

        ImageWrapper image = sample.getImage();

        String name = "screen1.fake [screen1 2]";
        assertEquals(name, image.getName());
    }


    @Test
    public void testGetPositionX() throws Exception {
        WellWrapper well = client.getWells(1L).get(0);

        WellSampleWrapper sample = well.getWellSamples().get(0);
        assertEquals(0.0, sample.getPositionX(null).getValue(), Double.MIN_VALUE);
    }


    @Test
    public void testGetPositionY() throws Exception {
        WellWrapper well = client.getWells(1L).get(0);

        WellSampleWrapper sample = well.getWellSamples().get(0);
        assertEquals(1.0, sample.getPositionY(null).getValue(), Double.MIN_VALUE);
    }


    @Test
    public void testGetStartTime() throws Exception {
        WellWrapper well = client.getWells(1L).get(0);

        WellSampleWrapper sample = well.getWellSamples().get(0);
        assertEquals(1146766431000L, sample.getStartTime());
    }

}