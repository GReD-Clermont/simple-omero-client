package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class PlateAcquisitionTest extends UserTest {


    @Test
    public void testAddTagToPlateAcquisition() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "Plate acq. tag", "tag attached to a plate acq.");
        acq.addTag(client, tag);
        List<TagAnnotationWrapper> tags = acq.getTags(client);
        client.delete(tag);
        List<TagAnnotationWrapper> checkTags = acq.getTags(client);

        assertEquals(1, tags.size());
        assertEquals(0, checkTags.size());
    }


    @Test
    public void testSetName() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);

        String name  = acq.getName();
        String name2 = "New name";
        acq.setName(name2);
        acq.saveAndUpdate(client);
        assertEquals(name2, client.getPlates(1L).get(0).getPlateAcquisitions().get(0).getName());

        acq.setName(name);
        acq.saveAndUpdate(client);
        assertEquals(name, client.getPlates(1L).get(0).getPlateAcquisitions().get(0).getName());
    }


    @Test
    public void testSetDescription() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);

        String name  = acq.getDescription();
        String name2 = "New description";
        acq.setDescription(name2);
        acq.saveAndUpdate(client);
        assertEquals(name2, client.getPlates(1L).get(0).getPlateAcquisitions().get(0).getDescription());

        acq.setDescription(name);
        acq.saveAndUpdate(client);
        assertEquals(name, client.getPlates(1L).get(0).getPlateAcquisitions().get(0).getDescription());
    }


    @Test
    public void testGetLabel() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(acq.getName(), acq.getLabel());
    }


    @Test
    public void testGetRefPlateId() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(-1, acq.getRefPlateId());
    }


    @Test
    public void testGetStartTime() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(1146766431000L, acq.getStartTime().getTime());
    }


    @Test
    public void testGetEndTime() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(1146766431000L, acq.getEndTime().getTime());
    }


    @Test
    public void testGetMaximumFieldCount() throws Exception {
        PlateWrapper plate = client.getPlates(1L).get(0);

        PlateAcquisitionWrapper acq = plate.getPlateAcquisitions().get(0);
        assertEquals(-1, acq.getMaximumFieldCount());
    }

}