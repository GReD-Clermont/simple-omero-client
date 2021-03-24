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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;


public class AccessExceptionTest extends BasicTest {

    private PrintStream error;


    void hideErrors() {
        error = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
                //DO NOTHING
            }
        }));
    }


    void showErrors() {
        System.setErr(error);
    }


    @Test
    public void testAddTagToImageWrongUser() throws Exception {
        hideErrors();
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
        showErrors();
        assertTrue(exception);
    }


    @Test
    public void testSudoFailGetProjects() {
        hideErrors();
        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect("omero", 4064, "testUser2", "password2", 3L);
            client.sudoGetUser("testUser").getProjects();
        } catch (AccessException | ExecutionException | ServiceException e) {
            if (e instanceof AccessException) {
                exception = true;
            }
        }
        client.disconnect();
        showErrors();
        assertTrue(exception);
    }


    @Test
    public void testSudoFailGetSingleProject() {
        hideErrors();
        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect("omero", 4064, "testUser2", "password2", 3L);
            client.sudoGetUser("testUser").getProject(2L);
        } catch (AccessException | ExecutionException | ServiceException e) {
            if (e instanceof AccessException) {
                exception = true;
            }
        }
        client.disconnect();
        showErrors();
        assertTrue(exception);
    }


    @Test
    public void testSudoFailGetDatasets() {
        hideErrors();
        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect("omero", 4064, "testUser", "password", 3L);
            client.sudoGetUser("testUser").getDatasets();
        } catch (AccessException | ExecutionException | ServiceException e) {
            if (e instanceof AccessException) {
                exception = true;
            }
        }
        client.disconnect();
        showErrors();
        assertTrue(exception);
    }


    @Test
    public void testSudoFailGetSingleDataset() {
        hideErrors();
        boolean exception = false;
        Client  client    = new Client();
        try {
            client.connect("omero", 4064, "testUser2", "password2", 3L);
            client.sudoGetUser("testUser").getDataset(1L);
        } catch (AccessException | ExecutionException | ServiceException e) {
            if (e instanceof AccessException) {
                exception = true;
            }
        }
        client.disconnect();
        showErrors();
        assertTrue(exception);
    }

}
