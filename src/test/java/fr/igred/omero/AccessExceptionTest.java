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


import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;
import omero.model.NamedValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.junit.Assert.*;


public class AccessExceptionTest extends BasicTest {

    private final PrintStream error = System.err;
    protected     Client      client;
    protected     Client      sudo;


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
            client.connect("omero", 4064, "testUser", "password", 3L);
            assertEquals("Wrong user", 2L, client.getId());
            assertEquals("Wrong group", 3L, client.getCurrentGroupId());
            sudo = client.sudoGetUser("testUser2");
        } catch (Exception e) {
            failed = true;
            logger.log(Level.SEVERE, ANSI_RED + "Connection failed." + ANSI_RESET, e);
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
            logger.log(Level.WARNING, ANSI_YELLOW + "Disconnection failed." + ANSI_RESET, e);
        }
    }


    @Test
    public void testAddTagToImageWrongUser() throws Exception {
        boolean exception = false;
        client.disconnect();
        client.connect("omero", 4064, "root", "omero", 3L);
        assertEquals(0L, client.getId());

        ImageWrapper image = client.getImage(3L);

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "image tag", "tag attached to an image");

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
        sudo.getProjects();
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetSingleProject() throws Exception {
        sudo.getProject(1L);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetProjectByName() throws Exception {
        sudo.getProjects("TestProject");
    }


    @Test(expected = AccessException.class)
    public void testSudoFailDeleteProject() throws Exception {
        sudo.deleteProject(1L);
    }


    // This test returns a ServiceException for a "security violation".
    @Test(expected = ServiceException.class)
    public void testSudoFailGetDatasets() throws Exception {
        sudo.getDatasets();
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetSingleDataset() throws Exception {
        sudo.getDataset(1L);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetDatasetByName() throws Exception {
        sudo.getDatasets("TestDataset");
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImages() throws Exception {
        sudo.getImages();
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImage() throws Exception {
        sudo.getImage(1L);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImagesName() throws Exception {
        sudo.getImages("image1.fake");
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImagesLike() throws Exception {
        sudo.getImagesLike("image1");
    }


    @Test(expected = ServiceException.class)
    public void testSudoFailGetAllTags() throws Exception {
        sudo.getTags();
    }


    @Test(expected = ServiceException.class)
    public void testSudoFailGetTag() throws Exception {
        sudo.getTag(1L);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImageTag() throws Exception {
        ImageWrapper image = client.getImage(1L);
        image.getTags(sudo);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetKVPairs() throws Exception {
        ImageWrapper image = client.getImage(1L);
        image.getKeyValuePairs(sudo);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailAddKVPair() throws Exception {
        ImageWrapper image = client.getImage(1L);

        List<NamedValue> result1 = new ArrayList<>();
        result1.add(new NamedValue("Test result1", "Value Test"));
        result1.add(new NamedValue("Test2 result1", "Value Test2"));

        MapAnnotationWrapper mapAnnotation1 = new MapAnnotationWrapper(result1);
        image.addMapAnnotation(sudo, mapAnnotation1);
    }


    @Test(expected = AccessException.class)
    public void testSudoFail() throws Exception {
        sudo.sudoGetUser("root");
    }


}
