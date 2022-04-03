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

package fr.igred.omero;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.util.logging.Level;

import static org.junit.Assert.assertEquals;


@Ignore("Abstract class")
public abstract class UserTest extends BasicTest {

    protected final Client client = new Client();


    @Before
    public void setUp() {
        boolean failed = false;
        try {
            client.connect(HOST, PORT, USER1.name, "password".toCharArray(), GROUP1.id);
            assertEquals("Wrong user", USER1.id, client.getId());
            assertEquals("Wrong group", GROUP1.id, client.getCurrentGroupId());
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
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, ANSI_YELLOW + "Disconnection failed." + ANSI_RESET, e);
        }
    }

}
