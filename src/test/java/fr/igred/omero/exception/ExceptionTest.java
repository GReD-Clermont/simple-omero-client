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

package fr.igred.omero.exception;


import fr.igred.omero.BasicTest;
import fr.igred.omero.client.Client;
import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.client.GatewayWrapper;
import omero.ResourceError;
import omero.SecurityViolation;
import omero.ServerError;
import omero.SessionException;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ExceptionTest extends BasicTest {

    private static <E extends Exception> Exception thrower(E t) throws E {
        if (t != null) throw t;
        return new Exception("Exception");
    }


    @Test
    void testConnectionErrorUsername() {
        ConnectionHandler client = new GatewayWrapper();
        assertThrows(ServiceException.class,
                     () -> client.connect(HOST, PORT, "badUser", "omero".toCharArray(), GROUP1.id));
    }


    @Test
    void testConnectionErrorPassword() {
        ConnectionHandler root = new GatewayWrapper();
        assertThrows(ServiceException.class,
                     () -> root.connect(HOST, PORT, ROOT.name, "badPassword".toCharArray(), GROUP1.id));
    }


    @Test
    void testConnectionErrorHost() {
        ConnectionHandler root = new GatewayWrapper();
        assertThrows(ServiceException.class,
                     () -> root.connect("127.0.0.1", PORT, ROOT.name, "omero".toCharArray(), GROUP1.id));
    }


    @Test
    void testConnectionErrorPort() {
        final int badPort = 5000;

        ConnectionHandler root = new GatewayWrapper();
        assertThrows(ServiceException.class,
                     () -> root.connect(HOST, badPort, ROOT.name, "omero".toCharArray(), GROUP1.id));
    }


    @Test
    void testConnectionErrorGroupNotExist() throws ServiceException {
        final long badGroup = 200L;

        ConnectionHandler clientNoSuchGroup = new GatewayWrapper();
        clientNoSuchGroup.connect(HOST, PORT, USER1.name, "password".toCharArray(), badGroup);
        assertEquals(USER1.id, clientNoSuchGroup.getId());
        assertEquals(GROUP1.id, clientNoSuchGroup.getCurrentGroupId());
    }


    @Test
    void testConnectionErrorNotInGroup() throws ServiceException {
        ConnectionHandler clientWrongGroup = new GatewayWrapper();
        clientWrongGroup.connect(HOST, PORT, USER1.name, "password".toCharArray(), 0L);
        assertEquals(USER1.id, clientWrongGroup.getId());
        assertEquals(GROUP1.id, clientWrongGroup.getCurrentGroupId());
    }


    @Test
    void testGetSingleProjectError() throws Exception {
        final long badProject = 333L;

        boolean exception = false;
        Client  client    = new GatewayWrapper();
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
    void testGetImageError() throws Exception {
        final long badImage = 200L;

        boolean exception = false;
        Client  client    = new GatewayWrapper();
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
    void testGetImageError2() throws Exception {
        final long badImage = -5L;

        boolean exception = false;
        Client  client    = new GatewayWrapper();
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
    void testGetSingleScreenError() throws Exception {
        final long badScreen = 333L;

        boolean exception = false;
        Client  client    = new GatewayWrapper();
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
    void testGetSinglePlateError() throws Exception {
        final long badPlate = 333L;

        boolean exception = false;
        Client  client    = new GatewayWrapper();
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
    void testGetSingleWellError() throws Exception {
        final long badWell = 333L;

        boolean exception = false;
        Client  client    = new GatewayWrapper();
        try {
            client.connect(HOST, PORT, USER1.name, "password".toCharArray());
            client.getWell(badWell);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        client.disconnect();
        assertTrue(exception);
    }


    @Test
    void testExceptionHandlerDSAccess() {
        Exception           e  = new DSAccessException("Test", null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(AccessException.class, () -> eh.rethrow(DSAccessException.class, AccessException::new, "Access"));
    }


    @Test
    void testExceptionHandlerDefaultServer() {
        Exception           e  = new ServerError(null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(AccessException.class, () -> eh.handleOMEROException("Server to Access 1"));
    }


    @Test
    void testExceptionHandlerDSOutOfService() {
        Exception           e  = new DSOutOfServiceException(null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(ServiceException.class, () -> eh.handleOMEROException("Service"));
    }


    @Test
    void testExceptionHandlerSecurityViolation() {
        Exception           e  = new ServerError(new SecurityViolation(null));
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(AccessException.class, () -> eh.handleOMEROException("Server to Access 2"));
    }


    @Test
    void testExceptionHandlerSessionException() {
        Exception           e  = new ServerError(new SessionException(null));
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(ServiceException.class, () -> eh.handleOMEROException("Server to Service 1"));
    }


    @Test
    void testExceptionHandlerAuthenticationException() {
        Exception           e  = new ServerError(new omero.AuthenticationException("Test"));
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(ServiceException.class, () -> eh.handleOMEROException("Server to Service 1"));
    }


    @Test
    void testExceptionHandlerResourceError() {
        Exception           e  = new ServerError(new ResourceError(null));
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(ServiceException.class, () -> eh.handleOMEROException("Server to Service 2"));
    }


    @Test
    void testExceptionHandlerRethrow() {
        Exception           e  = new AccessException(null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(AccessException.class, () -> eh.rethrow(AccessException.class));
    }


    @Test
    void testExceptionHandlerRethrowNot() {
        Exception           e  = new DSAccessException("Test", null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertDoesNotThrow(() -> eh.rethrow(DSOutOfServiceException.class, ServiceException::new, "Not service"));
    }


    @Test
    void testExceptionHandlerToString() {
        Exception           e  = new DSAccessException("Test", null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);

        String expected = "ExceptionHandler{" +
                          "exception=" + e +
                          ", value=" + null +
                          "}";
        assertEquals(expected, eh.toString());
    }

}
