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

package fr.igred.omero;


import fr.igred.omero.client.Client;
import fr.igred.omero.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static java.lang.String.format;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;


public abstract class UserTest extends BasicTest {

    protected final Client client = new Client();


    @BeforeEach
    public void setUp() {
        boolean failed = false;
        try {
            char[] password = "password".toCharArray();
            client.connect(HOST, PORT, USER1.name, password, GROUP1.id);
            assertEquals(USER1.id, client.getId(), "Wrong user");
            assertEquals(GROUP1.id, client.getCurrentGroupId(), "Wrong group");
        } catch (ServiceException e) {
            failed = true;
            String template = "%sConnection failed.%s";
            logger.log(SEVERE, format(template, ANSI_RED, ANSI_RESET), e);
        }
        assumeFalse(failed, "Connection failed.");
    }


    @AfterEach
    public void cleanUp() {
        try {
            client.disconnect();
        } catch (RuntimeException e) {
            String template = "%sDisconnection failed.%s";
            logger.log(WARNING, format(template, ANSI_YELLOW, ANSI_RESET), e);
        }
    }

}
