/*
 *  Copyright (C) 2020-2024 GReD
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

package fr.igred.omero.client;


import fr.igred.omero.BasicTest;
import fr.igred.omero.exception.ServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class ConnectionTest extends BasicTest {


    @Test
    void testDisconnect() {
        String     expected = "ClientImpl{host=null, groupID=-1, userID=-1, connected=false}";
        ClientImpl testRoot = new ClientImpl();
        testRoot.disconnect();
        assertFalse(testRoot.isConnected());
        assertEquals(expected, testRoot.toString());
    }


    @Test
    void testSessionConnect() throws ServiceException {
        ClientImpl client1 = new ClientImpl();
        client1.connect(HOST, PORT, USER1.name, "password".toCharArray());
        String     sessionId = client1.getSessionId();
        ClientImpl client2   = new ClientImpl();
        client2.connect(HOST, PORT, sessionId);
        assertEquals(client1.getUser().getId(), client2.getUser().getId());
        client1.disconnect();
        client2.disconnect();
    }


    @Test
    void testRootConnection() throws ServiceException {
        char[]     password = "omero".toCharArray();
        ClientImpl testRoot = new ClientImpl();
        testRoot.connect(HOST, PORT, ROOT.name, password, GROUP1.id);
        long id      = testRoot.getId();
        long groupId = testRoot.getCurrentGroupId();
        try {
            testRoot.disconnect();
        } catch (RuntimeException ignored) {
            // IGNORED
        }
        assertEquals(0L, id);
        assertEquals(GROUP1.id, groupId);
    }


    @Test
    void testUserConnection() throws ServiceException {
        String template = "ClientImpl{host=%s, groupID=%d, userID=%s, connected=true}";
        String toString = String.format(template, HOST, GROUP1.id, USER1.id);

        ClientImpl testUser = new ClientImpl();
        assertFalse(testUser.isConnected());
        testUser.connect(HOST, PORT, USER1.name, "password".toCharArray());
        assertEquals(toString, testUser.toString());
        long id      = testUser.getId();
        long groupId = testUser.getCurrentGroupId();
        try {
            testUser.disconnect();
        } catch (RuntimeException ignored) {
            // IGNORED
        }
        assertEquals(USER1.id, id);
        assertEquals(GROUP1.id, groupId);
    }

}
