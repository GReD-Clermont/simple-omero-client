/*
 *  Copyright (C) 2020-2023 GReD
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
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.ServerError;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceAndAccess;
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
        Client client = new Client();
        assertThrows(ServiceException.class,
                     () -> client.connect(HOST, PORT, "badUser", "omero".toCharArray(), GROUP1.id));
    }


    @Test
    void testConnectionErrorPassword() {
        Client root = new Client();
        assertThrows(ServiceException.class,
                     () -> root.connect(HOST, PORT, "root", "badPassword".toCharArray(), GROUP1.id));
    }


    @Test
    void testConnectionErrorHost() {
        Client root = new Client();
        assertThrows(ServiceException.class,
                     () -> root.connect("127.0.0.1", PORT, "root", "omero".toCharArray(), GROUP1.id));
    }


    @Test
    void testConnectionErrorPort() {
        final int badPort = 5000;
        Client    root    = new Client();
        assertThrows(ServiceException.class,
                     () -> root.connect(HOST, badPort, "root", "omero".toCharArray(), GROUP1.id));
    }


    @Test
    void testConnectionErrorGroupNotExist() throws ServiceException {
        final long badGroup = 200L;

        Client clientNoSuchGroup = new Client();
        clientNoSuchGroup.connect(HOST, PORT, USER1.name, "password".toCharArray(), badGroup);
        assertEquals(USER1.id, clientNoSuchGroup.getId());
        assertEquals(GROUP1.id, clientNoSuchGroup.getCurrentGroupId());
    }


    @Test
    void testConnectionErrorNotInGroup() throws ServiceException {
        Client clientWrongGroup = new Client();
        clientWrongGroup.connect(HOST, PORT, USER1.name, "password".toCharArray(), 0L);
        assertEquals(USER1.id, clientWrongGroup.getId());
        assertEquals(GROUP1.id, clientWrongGroup.getCurrentGroupId());
    }


    @Test
    void testGetSingleProjectError() throws Exception {
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
    void testGetImageError() throws Exception {
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
    void testGetImageError2() throws Exception {
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
    void testGetSingleScreenError() throws Exception {
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
    void testGetSinglePlateError() throws Exception {
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
    void testGetSingleWellError() throws Exception {
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


    @Test
    void testExceptionHandler1() {
        Exception           e  = new DSAccessException("Test", null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower, "Test");
        assertThrows(AccessException.class, () -> eh.rethrow(DSAccessException.class, AccessException::new));
    }


    @Test
    void testExceptionHandler2() {
        Exception           e  = new ServerError(null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower, "Great");
        assertThrows(ServerException.class, () -> eh.rethrow(ServerError.class, ServerException::new));
    }


    @Test
    void testExceptionHandler3() {
        Exception e = new DSOutOfServiceException(null);
        assertThrows(ServiceException.class, () -> handleServiceAndAccess(e, ExceptionTest::thrower, "Great"));
    }


    @Test
    void testExceptionHandler4() {
        Exception e = new ServerError(null);
        assertThrows(ServerError.class, () -> handleServiceAndAccess(e, ExceptionTest::thrower, "Great"));
    }


    @Test
    void testExceptionHandler5() {
        Exception           e  = new ServerException(null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower, "Great");
        assertThrows(ServerException.class, () -> eh.rethrow(ServerException.class));
    }


    @Test
    void testExceptionHandler6() {
        Exception           e  = new AccessException(null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower, "Great");
        assertThrows(AccessException.class, () -> eh.rethrow(AccessException.class));
    }


    @Test
    void testExceptionHandler7() {
        Exception           e  = new DSAccessException("Test", null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower, "Great");
        assertDoesNotThrow(() -> eh.rethrow(ServerError.class, ServerException::new));
    }


    @Test
    void testExceptionHandlerToString() {
        Exception           e  = new DSAccessException("Test", null);
        ExceptionHandler<?> eh = ExceptionHandler.of(e, ExceptionTest::thrower, "Great");

        String expected = "ExceptionHandler{" +
                          "exception=" + e +
                          ", value=" + null +
                          ", error='Great'" +
                          "}";
        assertEquals(expected, eh.toString());
    }

}
