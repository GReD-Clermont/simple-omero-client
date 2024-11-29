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

package fr.igred.omero.exception;


import fr.igred.omero.BasicTest;
import fr.igred.omero.client.ClientImpl;
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
        if (t != null) {
            throw t;
        }
        return new Exception("Exception");
    }


    @Test
    void testConnectionErrorUsername() {
        String username = "badUser";
        char[]     pw     = "badPassword".toCharArray();
        ClientImpl client = new ClientImpl();
        assertThrows(ServiceException.class,
                     () -> client.connect(HOST, PORT, username, pw, GROUP1.id));
    }


    @Test
    void testConnectionErrorPassword() {
        char[]     pw   = "badPassword".toCharArray();
        ClientImpl root = new ClientImpl();
        assertThrows(ServiceException.class,
                     () -> root.connect(HOST, PORT, ROOT.name, pw, GROUP1.id));
    }


    @Test
    void testConnectionErrorHost() {
        String host = "127.0.0.1";
        char[]     pw   = "omero".toCharArray();
        ClientImpl root = new ClientImpl();
        assertThrows(ServiceException.class,
                     () -> root.connect(host, PORT, ROOT.name, pw, GROUP1.id));
    }


    @Test
    void testConnectionErrorPort() {
        final int port = 5000;
        char[]     pw   = "omero".toCharArray();
        ClientImpl root = new ClientImpl();
        assertThrows(ServiceException.class,
                     () -> root.connect(HOST, port, ROOT.name, pw, GROUP1.id));
    }


    @Test
    void testConnectionErrorGroupNotExist() throws ServiceException {
        final long badGroup = 200L;
        char[]     pw       = "password".toCharArray();

        ClientImpl clientNoSuchGroup = new ClientImpl();
        clientNoSuchGroup.connect(HOST, PORT, USER1.name, pw, badGroup);
        assertEquals(USER1.id, clientNoSuchGroup.getId());
        assertEquals(GROUP1.id, clientNoSuchGroup.getCurrentGroupId());
    }


    @Test
    void testConnectionErrorNotInGroup() throws ServiceException {
        char[] pw = "password".toCharArray();

        ClientImpl clientWrongGroup = new ClientImpl();
        clientWrongGroup.connect(HOST, PORT, USER1.name, pw, 0L);
        assertEquals(USER1.id, clientWrongGroup.getId());
        assertEquals(GROUP1.id, clientWrongGroup.getCurrentGroupId());
    }


    @Test
    void testGetSingleProjectError() throws Exception {
        final long badProject = 333L;

        boolean    exception = false;
        ClientImpl client    = new ClientImpl();
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
        char[]     pw       = "password".toCharArray();

        boolean    exception = false;
        ClientImpl client    = new ClientImpl();
        client.connect(HOST, PORT, USER1.name, pw, GROUP1.id);
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
        char[]     pw       = "password".toCharArray();

        boolean    exception = false;
        ClientImpl client    = new ClientImpl();
        client.connect(HOST, PORT, USER1.name, pw, GROUP1.id);
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
        char[]     pw        = "password".toCharArray();

        boolean    exception = false;
        ClientImpl client    = new ClientImpl();
        try {
            client.connect(HOST, PORT, USER1.name, pw);
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
        char[]     pw       = "password".toCharArray();

        boolean    exception = false;
        ClientImpl client    = new ClientImpl();
        try {
            client.connect(HOST, PORT, USER1.name, pw);
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
        char[]     pw      = "password".toCharArray();

        boolean    exception = false;
        ClientImpl client    = new ClientImpl();
        try {
            client.connect(HOST, PORT, USER1.name, pw);
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
        assertThrows(AccessException.class,
                     () -> eh.rethrow(DSAccessException.class,
                                      AccessException::new,
                                      "Access"));
    }


    @Test
    void testExceptionHandlerDefaultServer() {
        Exception           e  = new ServerError(null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(AccessException.class,
                     () -> eh.handleOMEROException("Server to Access 1"));
    }


    @Test
    void testExceptionHandlerDSOutOfService() {
        String    msg   = "Unknown Error";
        Exception cause = null;
        Exception e     = new DSOutOfServiceException(msg, cause);

        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(ServiceException.class,
                     () -> eh.handleOMEROException("Service"));
    }


    @Test
    void testExceptionHandlerSecurityViolation1() {
        String    msg   = "Security Violation";
        Exception cause = new AccessException(new SecurityViolation(null));
        Exception e     = new DSOutOfServiceException(msg, cause);

        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(AccessException.class,
                     () -> eh.handleOMEROException("Server to Access 2"));
    }


    @Test
    void testExceptionHandlerSecurityViolation2() {
        String    msg   = "Security Violation";
        Exception cause = new SecurityViolation(null);
        Exception e     = new DSOutOfServiceException(msg, cause);

        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(AccessException.class,
                     () -> eh.handleOMEROException("Service to Access"));
    }


    @Test
    void testExceptionHandlerSessionException() {
        Exception cause = new SessionException(null);
        Exception e     = new DSOutOfServiceException("Error", cause);

        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(ServiceException.class,
                     () -> eh.handleOMEROException("Service to Service"));
    }


    @Test
    void testExceptionHandlerAuthenticationException() {
        Exception cause = new omero.AuthenticationException("Test");
        Exception e     = new DSOutOfServiceException("Error", cause);

        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(ServiceException.class,
                     () -> eh.handleOMEROException("Service to Service"));
    }


    @Test
    void testExceptionHandlerResourceError() {
        String    msg   = "Resource Error";
        Exception cause = new ResourceError(null);
        Exception e     = new DSOutOfServiceException(msg, cause);

        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(ServiceException.class,
                     () -> eh.handleOMEROException("Service to Service"));
    }


    @Test
    void testExceptionHandlerRethrow() {
        Exception           e  = new AccessException(null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertThrows(AccessException.class,
                     () -> eh.rethrow(AccessException.class));
    }


    @Test
    void testExceptionHandlerRethrowNot() {
        Exception           e  = new DSAccessException("Test", null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower);
        assertDoesNotThrow(() -> eh.rethrow(DSOutOfServiceException.class,
                                            ServiceException::new,
                                            "Not service"));
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
