/*
 *  Copyright (C) 2020 GReD
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


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.util.logging.Level;

import static org.junit.Assert.assertEquals;


@Ignore
public abstract class UserTest extends BasicTest {

    protected Client client;


    @Before
    public void setUp() {
        boolean failed = false;
        client = new Client();
        try {
            client.connect("omero", 4064, "testUser", "password".toCharArray(), 3L);
            assertEquals("Wrong user", 2L, client.getId());
            assertEquals("Wrong group", 3L, client.getCurrentGroupId());
        } catch (Exception e) {
            failed = true;
            logger.log(Level.SEVERE, ANSI_RED + "Connection failed." + ANSI_RESET, e);
        }
        org.junit.Assume.assumeFalse(failed);
    }


    @After
    public void cleanUp() {
        try {
            client.disconnect();
        } catch (Exception e) {
            logger.log(Level.WARNING, ANSI_YELLOW + "Disconnection failed." + ANSI_RESET, e);
        }
    }

}
