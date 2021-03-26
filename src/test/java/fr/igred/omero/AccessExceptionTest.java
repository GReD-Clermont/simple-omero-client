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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;


public class AccessExceptionTest extends BasicTest {

    private final PrintStream error = System.err;
    protected     Client      client;


    void hideErrors() {
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
                //DO NOTHING
            }
        }));
    }


    void showErrors() {
        System.setErr(error);
    }


    @Before
    public void setUp() {
        boolean failed = false;
        client = new Client();
        try {
            client.connect("omero", 4064, "testUser2", "password2", 3L);
            assertEquals("Wrong user", 3L, client.getId().longValue());
            assertEquals("Wrong group", 3L, client.getGroupId().longValue());
        } catch (Exception e) {
            failed = true;
            logger.severe(ANSI_RED + "Connection failed." + ANSI_RESET);
        }
        org.junit.Assume.assumeFalse(failed);
        hideErrors();
    }


    @After
    public void cleanUp() {
        showErrors();
        try {
            client.disconnect();
        } catch (Exception e) {
            logger.warning(ANSI_YELLOW + "Disconnection failed." + ANSI_RESET);
        }
    }


    @Test
    public void testAddTagToImageWrongUser() throws Exception {
        boolean exception = false;
        client.disconnect();
        client.connect("omero", 4064, "root", "omero", 3L);
        assertEquals(0L, client.getId().longValue());

        ImageContainer image = client.getImage(3L);

        TagAnnotationContainer tag = new TagAnnotationContainer(client, "image tag", "tag attached to an image");

        try {
            image.addTag(client, tag);
        } catch (AccessException e) {
            exception = true;
        }

        client.deleteTag(tag);
        assertTrue(exception);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetProjects() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getProjects();
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetSingleProject() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getProject(2L);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetProjectByName() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getProjects("TestProject");
    }


    @Test(expected = AccessException.class)
    public void testSudoFailDeleteProject() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.deleteProject(2L);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetDatasets() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getDatasets();
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetSingleDataset() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getDataset(1L);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetDatasetByName() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getDatasets("TestDataset");
    }


    @Test(expected = AccessException.class)
    public void testGetImages() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getImages();
    }


    @Test(expected = AccessException.class)
    public void testGetImage() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getImage(1L);
    }


    @Test(expected = AccessException.class)
    public void testGetImagesName() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getImages("image1.fake");
    }


    @Test(expected = AccessException.class)
    public void testGetImagesLike() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getImagesLike("image1");
    }


    @Test(expected = ServiceException.class)
    public void testGetAllTags() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getTags();
    }


    @Test(expected = ServiceException.class)
    public void testGetTag() throws Exception {
        Client sudo = new Client();
        try {
            sudo = client.sudoGetUser("testUser");
        } catch (AccessException | ExecutionException | ServiceException e) {
            fail();
        }
        sudo.getTag(1L);
    }

}
