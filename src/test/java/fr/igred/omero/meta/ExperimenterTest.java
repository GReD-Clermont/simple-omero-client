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

package fr.igred.omero.meta;


import fr.igred.omero.RootTest;
import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ExperimenterTest extends RootTest {

    @Test(expected = NoSuchElementException.class)
    public void testGetWrongUser() throws Exception {
        client.getUser("nonexistent");
    }


    @Test(expected = NoSuchElementException.class)
    public void testSudoWrongUser() throws Exception {
        client.sudoGetUser("nonexistent");
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
        assertEquals(3, groups.size());
        assertEquals("testGroup", user.getDefaultGroup().getName());
    }

}
