package fr.igred.omero.meta;


import fr.igred.omero.RootTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class GroupTest extends RootTest {

    @Test
    public void testGetWrongGroup() throws Exception {
        GroupWrapper group = client.getGroup("nonexistent");
        assertNull(group);
    }

    @Test
    public void testSetGroupName() throws Exception {
        GroupWrapper group = client.getGroup("testGroup2");
        assertEquals("testGroup2", group.getName());
        group.setName("Empty");
        group.saveAndUpdate(client);
        assertEquals("Empty", group.getName());
    }

    @Test
    public void testSetDescription() throws Exception {
        GroupWrapper group = client.getGroup("testGroup1");
        group.setDescription("Test");
        group.saveAndUpdate(client);
        assertEquals("Test", client.getGroup("testGroup1").getDescription());
    }

}
