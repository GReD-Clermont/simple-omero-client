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


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.OMEROServerError;
import fr.igred.omero.exception.ServiceException;
import omero.ServerError;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;


public class ExceptionTest extends BasicTest {

    @Test
    public void testConnectionErrorUsername() throws Exception {
        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect("omero", 4064, "badUser", "omero".toCharArray(), 3L);
        } catch (ServiceException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testConnectionErrorPassword() throws Exception {
        boolean exception = false;
        Client  root      = new Client();
        try {
            root.connect("omero", 4064, "root", "badPassword".toCharArray(), 3L);
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
            root.connect("127.0.0.1", 4064, "root", "omero".toCharArray(), 3L);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testConnectionErrorPort() {
        boolean exception = false;
        Client  root      = new Client();
        try {
            root.connect("omero", 5000, "root", "omero".toCharArray(), 3L);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testConnectionErrorGroupNotExist() throws Exception {
        Client clientNoSuchGroup = new Client();
        clientNoSuchGroup.connect("omero", 4064, "testUser", "password".toCharArray(), 200L);
        assertEquals(2L, clientNoSuchGroup.getId());
        assertEquals(3L, clientNoSuchGroup.getCurrentGroupId());
    }


    @Test
    public void testConnectionErrorNotInGroup() throws Exception {
        Client clientWrongGroup = new Client();
        clientWrongGroup.connect("omero", 4064, "testUser", "password".toCharArray(), 0L);
        assertEquals(2L, clientWrongGroup.getId());
        assertEquals(3L, clientWrongGroup.getCurrentGroupId());
    }


    @Test
    public void testGetSingleProjectError() throws Exception {
        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect("omero", 4064, "testUser", "password".toCharArray());
            client.getProject(333L);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test
    public void testGetImageError() throws Exception {
        boolean exception = false;
        Client  client    = new Client();
        client.connect("omero", 4064, "testUser", "password".toCharArray(), 3L);
        assertEquals(2L, client.getId());

        try {
            client.getImage(200L);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test
    public void testGetImageError2() throws Exception {
        boolean exception = false;
        Client  client    = new Client();
        client.connect("omero", 4064, "testUser", "password".toCharArray(), 3L);
        assertEquals(2L, client.getId());

        try {
            client.getImage(-5L);
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
    public void testExceptionHandler5() throws Exception {
        Throwable t = new Exception();
        ExceptionHandler.handleException(t, "Great");
        assertTrue(true);
    }


    @Test
    public void testExceptionHandler6() throws Exception {
        Throwable t = new ServerError(null);
        ExceptionHandler.handleServiceOrAccess(t, "Great");
        assertTrue(true);
    }


    @Test
    public void testExceptionHandler7() throws Exception {
        Throwable t = new DSAccessException("Test", null);
        ExceptionHandler.handleServiceOrServer(t, "Great");
        assertTrue(true);
    }

}
