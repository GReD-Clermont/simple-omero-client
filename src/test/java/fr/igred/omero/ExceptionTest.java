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


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import omero.ServerError;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ExceptionTest extends BasicTest {


    @Test
    public void testConnectionErrorUsername() {
        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect(HOST, PORT, "badUser", "omero".toCharArray(), GROUP1.id);
        } catch (ServiceException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testConnectionErrorPassword() {
        boolean exception = false;
        Client  root      = new Client();
        try {
            root.connect(HOST, PORT, "root", "badPassword".toCharArray(), GROUP1.id);
        } catch (ServiceException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testConnectionErrorHost() {
        boolean exception = false;
        Client  root      = new Client();
        try {
            root.connect("127.0.0.1", PORT, "root", "omero".toCharArray(), GROUP1.id);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testConnectionErrorPort() {
        final int badPort = 5000;

        boolean exception = false;
        Client  root      = new Client();
        try {
            root.connect(HOST, badPort, "root", "omero".toCharArray(), GROUP1.id);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testConnectionErrorGroupNotExist() throws Exception {
        final long badGroup = 200L;

        Client clientNoSuchGroup = new Client();
        clientNoSuchGroup.connect(HOST, PORT, USER1.name, "password".toCharArray(), badGroup);
        assertEquals(USER1.id, clientNoSuchGroup.getId());
        assertEquals(GROUP1.id, clientNoSuchGroup.getCurrentGroupId());
    }


    @Test
    public void testConnectionErrorNotInGroup() throws Exception {
        Client clientWrongGroup = new Client();
        clientWrongGroup.connect(HOST, PORT, USER1.name, "password".toCharArray(), 0L);
        assertEquals(USER1.id, clientWrongGroup.getId());
        assertEquals(GROUP1.id, clientWrongGroup.getCurrentGroupId());
    }


    @Test
    public void testGetSingleProjectError() throws Exception {
        final long badProject = 333L;

        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect(HOST, PORT, USER1.name, "password".toCharArray());
            client.getProject(badProject);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test
    public void testGetImageError() throws Exception {
        final long badImage = 200L;

        boolean exception = false;
        Client  client    = new Client();
        client.connect(HOST, PORT, USER1.name, "password".toCharArray(), GROUP1.id);
        assertEquals(USER1.id, client.getId());

        try {
            client.getImage(badImage);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test
    public void testGetImageError2() throws Exception {
        final long badImage = -5L;

        boolean exception = false;
        Client  client    = new Client();
        client.connect(HOST, PORT, USER1.name, "password".toCharArray(), GROUP1.id);
        assertEquals(USER1.id, client.getId());

        try {
            client.getImage(badImage);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test
    public void testGetSingleScreenError() throws Exception {
        final long badScreen = 333L;

        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect(HOST, PORT, USER1.name, "password".toCharArray());
            client.getScreen(badScreen);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test
    public void testGetSinglePlateError() throws Exception {
        final long badPlate = 333L;

        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect(HOST, PORT, USER1.name, "password".toCharArray());
            client.getPlate(badPlate);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test
    public void testGetSingleWellError() throws Exception {
        final long badWell = 333L;

        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect(HOST, PORT, USER1.name, "password".toCharArray());
            client.getWell(badWell);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test(expected = AccessException.class)
    public void testExceptionHandler1() throws Exception {
        Throwable t = new DSAccessException("Test", null);
        ExceptionHandler.handleException(t, "Great");
    }


    @Test(expected = OMEROServerError.class)
    public void testExceptionHandler2() throws Exception {
        Throwable t = new ServerError(null);
        ExceptionHandler.handleException(t, "Great");
    }


    @Test(expected = OMEROServerError.class)
    public void testExceptionHandler3() throws Exception {
        Throwable t = new ServerError(null);
        ExceptionHandler.handleServiceOrServer(t, "Great");
    }


    @Test(expected = ServiceException.class)
    public void testExceptionHandler4() throws Exception {
        Throwable t = new DSOutOfServiceException(null);
        ExceptionHandler.handleException(t, "Great");
    }


    @Test
    public void testExceptionHandler5() {
        boolean exception = false;

        Throwable t = new Exception("Nothing");
        try {
            ExceptionHandler.handleException(t, "Great");
        } catch (Throwable t2) {
            exception = true;
        }
        assertFalse(exception);
    }


    @Test
    public void testExceptionHandler6() {
        boolean exception = false;

        Throwable t = new ServerError(null);
        try {
            ExceptionHandler.handleServiceOrAccess(t, "Great");
        } catch (Throwable t2) {
            exception = true;
        }
        assertFalse(exception);
    }


    @Test
    public void testExceptionHandler7() {
        boolean exception = false;

        Throwable t = new DSAccessException("Test", null);
        try {
            ExceptionHandler.handleServiceOrServer(t, "Great");
        } catch (Throwable t2) {
            exception = true;
        }
        assertFalse(exception);
    }

}
