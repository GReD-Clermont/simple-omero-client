/*
 *  Copyright (C) 2020-2021 GReD
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

package fr.igred.omero;


import org.junit.Test;

import static org.junit.Assert.*;


public class ConnectionTest extends BasicTest {


    @Test
    public void testDisconnect() {
        Client testRoot = new Client();
        testRoot.disconnect();
        assertTrue(true);
    }


    @Test
    public void testSessionConnect() throws Exception {
        Client client1 = new Client();
        client1.connect("omero", 4064, "testUser", "password".toCharArray());
        String sessionId = client1.getSessionId();
        Client client2   = new Client();
        client2.connect("omero", 4064, sessionId);
        assertEquals(client1.getUser().getId(), client2.getUser().getId());
        client1.disconnect();
        client2.disconnect();
    }


    @Test
    public void testRootConnection() throws Exception {
        Client testRoot = new Client();
        testRoot.connect("omero", 4064, "root", "omero".toCharArray(), 3L);
        long id      = testRoot.getId();
        long groupId = testRoot.getCurrentGroupId();
        try {
            testRoot.disconnect();
        } catch (Exception ignored) {
        }
        assertEquals(0L, id);
        assertEquals(3L, groupId);
    }


    @Test
    public void testUserConnection() throws Exception {
        Client testUser = new Client();
        assertFalse(testUser.isConnected());
        testUser.connect("omero", 4064, "testUser", "password".toCharArray());
        long id      = testUser.getId();
        long groupId = testUser.getCurrentGroupId();
        try {
            testUser.disconnect();
        } catch (Exception ignored) {
        }
        assertEquals(2L, id);
        assertEquals(3L, groupId);
    }

}
