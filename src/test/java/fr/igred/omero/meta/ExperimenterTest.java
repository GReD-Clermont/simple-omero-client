package fr.igred.omero.meta;


import fr.igred.omero.RootTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class ExperimenterTest extends RootTest {

    @Test
    public void testGetWrongUser() throws Exception {
        ExperimenterWrapper user = client.getUser("nonexistent");
        assertNull(user);
    }


    @Test
    public void testGetUsername() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");
        assertEquals("testUser", user.getUserName());
    }


    @Test
    public void testSetName() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleName("O.");
        user.saveAndUpdate(client);
        assertEquals("Test", client.getUser("testUser").getFirstName());
        assertEquals("User", client.getUser("testUser").getLastName());
        assertEquals("O.", client.getUser("testUser").getMiddleName());
    }


    @Test
    public void testSetEmail() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");
        user.setEmail("test.user@example.org");
        user.saveAndUpdate(client);
        assertEquals("test.user@example.org", client.getUser("testUser").getEmail());
    }


    @Test
    public void testSetInstitution() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");
        user.setInstitution("Example");
        user.saveAndUpdate(client);
        assertEquals("Example", client.getUser("testUser").getInstitution());
    }


    @Test
    public void testIsActive() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");
        assertTrue(user.isActive());
    }


    @Test
    public void testIsLDAP() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");
        assertFalse(user.isLDAP());
    }


    @Test
    public void testIsMemberOfGroup() throws Exception {
        ExperimenterWrapper user = client.getUser("testUser");
        assertFalse(user.isMemberOfGroup(0L));
        assertTrue(user.isMemberOfGroup(3L));
    }


    @Test
    public void testGetGroups() throws Exception {
        ExperimenterWrapper user   = client.getUser("testUser");
        List<GroupWrapper>  groups = user.getGroups();
        assertEquals(2, groups.size());
        assertEquals("testGroup", user.getDefaultGroup().getName());
    }

}
