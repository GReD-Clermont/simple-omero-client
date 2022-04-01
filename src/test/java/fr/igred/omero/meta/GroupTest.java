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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;


public class GroupTest extends RootTest {

    @Test(expected = NoSuchElementException.class)
    public void testGetWrongGroup() throws Exception {
        client.getGroup("nonexistent");
    }


    @Test
    public void testSetGroupName() throws Exception {
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
    public void testSetDescription() throws Exception {
        GroupWrapper group = client.getGroup("testGroup1");
        assertEquals(GroupWrapper.PERMISSIONS_GROUP_READ, group.getPermissionsLevel());
        group.setDescription("Test");
        group.saveAndUpdate(client);
        assertEquals("Test", client.getGroup("testGroup1").getDescription());
    }


    @Test
    public void testGetExperimenters() throws Exception {
        GroupWrapper group = client.getGroup("testGroup3");

        List<ExperimenterWrapper> users = group.getExperimenters();

        List<String> usernames = new ArrayList<>(2);
        for (ExperimenterWrapper user : users) {
            usernames.add(user.getUserName());
        }
        usernames.sort(String.CASE_INSENSITIVE_ORDER);

        assertEquals(2, users.size());
        assertEquals("testUser3", usernames.get(0));
        assertEquals("testUser4", usernames.get(1));
    }


    @Test
    public void testGetMembersOnly() throws Exception {
        GroupWrapper group = client.getGroup("testGroup3");

        List<ExperimenterWrapper> users = group.getMembersOnly();
        assertEquals(1, users.size());
        assertEquals("testUser3", users.get(0).getUserName());
    }


    @Test
    public void testGetLeaders() throws Exception {
        GroupWrapper group = client.getGroup("testGroup3");

        List<ExperimenterWrapper> users = group.getLeaders();
        assertEquals(1, users.size());
        assertEquals("testUser4", users.get(0).getUserName());
    }

}
