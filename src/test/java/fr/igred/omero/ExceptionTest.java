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
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class ExceptionTest extends BasicTest {


    @Test
    public void testConnectionErrorUsername() throws Exception {
        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect("omero", 4064, "badUser", "omero", 3L);
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
            root.connect("omero", 4064, "root", "badPassword", 3L);
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
            root.connect("127.0.0.1", 4064, "root", "omero", 3L);
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
            root.connect("omero", 5000, "root", "omero", 3L);
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testConnectionErrorGroupNotExist() throws Exception {
        Client clientNoSuchGroup = new Client();
        clientNoSuchGroup.connect("omero", 4064, "testUser", "password", 200L);
        assertEquals(2L, clientNoSuchGroup.getId().longValue());
        assertEquals(3L, clientNoSuchGroup.getGroupId().longValue());
    }


    @Test
    public void testConnectionErrorNotInGroup() throws Exception {
        Client clientWrongGroup = new Client();
        clientWrongGroup.connect("omero", 4064, "testUser", "password", 0L);
        assertEquals(2L, clientWrongGroup.getId().longValue());
        assertEquals(3L, clientWrongGroup.getGroupId().longValue());
    }


    @Test
    public void testGetSingleProjectError() throws Exception {
        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect("omero", 4064, "testUser", "password");
            client.getProject(333L);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testGetImageError() throws Exception {
        boolean exception = false;
        Client  client    = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        try {
            client.getImage(200L);
        } catch (NoSuchElementException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testAddTagToImageWrongUser() throws Exception {
        boolean exception = false;
        Client  root      = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);
        assertEquals(0L, root.getId().longValue());

        ImageContainer image = root.getImage(3L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "image tag", "tag attached to an image");

        try {
            image.addTag(root, tag);
        } catch (AccessException e) {
            exception = true;
        }

        root.deleteTag(tag);
        root.disconnect();
        assertTrue(exception);
    }

}
