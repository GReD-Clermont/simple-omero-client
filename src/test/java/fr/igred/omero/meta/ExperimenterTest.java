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
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        assertEquals(USER1.name, experimenter.getUserName());
    }


    @Test
    public void testSetName() throws Exception {
        final String first  = "Test";
        final String last   = "User";
        final String middle = "O.";

        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        experimenter.setFirstName(first);
        experimenter.setLastName(last);
        experimenter.setMiddleName(middle);
        experimenter.saveAndUpdate(client);
        assertEquals(first, client.getUser(USER1.name).getFirstName());
        assertEquals(last, client.getUser(USER1.name).getLastName());
        assertEquals(middle, client.getUser(USER1.name).getMiddleName());
    }


    @Test
    public void testSetEmail() throws Exception {
        final String email = "test.user1.name@example.org";

        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        experimenter.setEmail(email);
        experimenter.saveAndUpdate(client);
        assertEquals(email, client.getUser(USER1.name).getEmail());
    }


    @Test
    public void testSetInstitution() throws Exception {
        final String institution = "Example";

        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        experimenter.setInstitution(institution);
        experimenter.saveAndUpdate(client);
        assertEquals(institution, client.getUser(USER1.name).getInstitution());
    }


    @Test
    public void testIsActive() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        assertTrue(experimenter.isActive());
    }


    @Test
    public void testIsLDAP() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        assertFalse(experimenter.isLDAP());
    }


    @Test
    public void testIsMemberOfGroup() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        assertFalse(experimenter.isMemberOfGroup(0L));
        assertTrue(experimenter.isMemberOfGroup(GROUP1.id));
    }


    @Test
    public void testGetGroups() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        List<GroupWrapper>  groups       = experimenter.getGroups();
        assertEquals(3, groups.size());
        assertEquals(GROUP1.name, experimenter.getDefaultGroup().getName());
    }

}
