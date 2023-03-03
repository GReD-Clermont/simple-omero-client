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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class GroupTest extends RootTest {

    @Test
    void testGetWrongGroup() {
        assertThrows(NoSuchElementException.class, () -> client.getGroup("nonexistent"));
    }


    @Test
    void testSetGroupName() throws Exception {
        GroupWrapper group = client.getGroup("testGroup2");
        assertEquals("testGroup2", group.getName());
        group.setName("Empty");
        group.saveAndUpdate(client);
        assertEquals("Empty", group.getName());
        group.setName("testGroup2");
        group.saveAndUpdate(client);
        assertEquals("testGroup2", group.getName());
    }


    @Test
    void testSetDescription() throws Exception {
        GroupWrapper group = client.getGroup("testGroup1");
        assertEquals(Group.PERMISSIONS_GROUP_READ, group.getPermissionsLevel());
        group.setDescription("Test");
        group.saveAndUpdate(client);
        assertEquals("Test", client.getGroup("testGroup1").getDescription());
    }


    @Test
    void testGetExperimenters() throws Exception {
        GroupWrapper group = client.getGroup("testGroup3");

        List<ExperimenterWrapper> experimenters = group.getExperimenters();

        List<String> usernames = new ArrayList<>(2);
        for (ExperimenterWrapper experimenter : experimenters) {
            usernames.add(experimenter.getUserName());
        }
        usernames.sort(String.CASE_INSENSITIVE_ORDER);

        assertEquals(2, experimenters.size());
        assertEquals("testUser3", usernames.get(0));
        assertEquals("testUser4", usernames.get(1));
    }


    @Test
    void testGetMembersOnly() throws Exception {
        GroupWrapper group = client.getGroup("testGroup3");

        List<ExperimenterWrapper> members = group.getMembersOnly();
        assertEquals(1, members.size());
        assertEquals("testUser3", members.get(0).getUserName());
    }


    @Test
    void testGetLeaders() throws Exception {
        GroupWrapper group = client.getGroup("testGroup3");

        List<ExperimenterWrapper> leaders = group.getLeaders();
        assertEquals(1, leaders.size());
        assertEquals("testUser4", leaders.get(0).getUserName());
    }

}
