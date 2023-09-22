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

package fr.igred.omero.meta;


import fr.igred.omero.RootTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ExperimenterTest extends RootTest {


    @Test
    void testGetWrongUser() {
        assertThrows(NoSuchElementException.class, () -> client.getUser("nonexistent"));
    }


    @Test
    void testGetWrongUserId() {
        assertThrows(NoSuchElementException.class, () -> client.getUser(859L));
    }


    @Test
    void testGetUserById() throws Exception {
        assertEquals(USER1.name, client.getUser(USER1.id).getUserName());
    }


    @Test
    void testSudoWrongUser() {
        assertThrows(NoSuchElementException.class, () -> client.sudoGetUser("nonexistent"));
    }


    @Test
    void testGetUsername() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        assertEquals(USER1.name, experimenter.getUserName());
    }


    @Test
    void testSetName() throws Exception {
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
    void testSetEmail() throws Exception {
        final String email = "test.user1.name@example.org";

        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        experimenter.setEmail(email);
        experimenter.saveAndUpdate(client);
        assertEquals(email, client.getUser(USER1.name).getEmail());
    }


    @Test
    void testSetInstitution() throws Exception {
        final String institution = "Example";

        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        experimenter.setInstitution(institution);
        experimenter.saveAndUpdate(client);
        assertEquals(institution, client.getUser(USER1.name).getInstitution());
    }


    @Test
    void testIsActive() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        assertTrue(experimenter.isActive());
    }


    @Test
    void testIsLDAP() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        assertFalse(experimenter.isLDAP());
    }


    @Test
    void testIsMemberOfGroup() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        assertFalse(experimenter.isMemberOfGroup(0L));
        assertTrue(experimenter.isMemberOfGroup(GROUP1.id));
    }


    @Test
    void testGetGroups() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        List<GroupWrapper>  groups       = experimenter.getGroups();
        assertEquals(3, groups.size());
        assertEquals(GROUP1.name, experimenter.getDefaultGroup().getName());
    }


    @Test
    void testIsNotGroupLeader() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        GroupWrapper        group        = client.getGroup(GROUP1.name);
        assertFalse(experimenter.isLeader(group));
    }


    @Test
    void testIsGroupLeader() throws Exception {
        ExperimenterWrapper experimenter = client.getUser("testUser4");
        GroupWrapper        group        = client.getGroup("testGroup3");
        assertTrue(experimenter.isLeader(group));
    }


    @Test
    void testIsNotAdmin() throws Exception {
        ExperimenterWrapper experimenter = client.getUser(USER1.name);
        assertFalse(experimenter.isAdmin(client));
    }


    @Test
    void testIsAdmin() throws Exception {
        ExperimenterWrapper experimenter = client.getUser();
        assertTrue(experimenter.isAdmin(client));
    }

}
